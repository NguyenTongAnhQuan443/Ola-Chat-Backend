package vn.edu.iuh.fit.olachatbackend.services.impl;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PostResponse;
import vn.edu.iuh.fit.olachatbackend.entities.*;
import vn.edu.iuh.fit.olachatbackend.enums.Privacy;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.mappers.PostMapper;
import vn.edu.iuh.fit.olachatbackend.repositories.*;
import vn.edu.iuh.fit.olachatbackend.services.MediaService;
import vn.edu.iuh.fit.olachatbackend.services.PostService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MediaService mediaService;
    private final LikeRepository likeRepository;
    private final PostMapper postMapper;
    private final FriendRepository friendRepository;
    private final CommentRepository commentRepository;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository, MediaService mediaService, LikeRepository likeRepository, PostMapper postMapper, FriendRepository friendRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.mediaService = mediaService;
        this.likeRepository = likeRepository;
        this.postMapper = postMapper;
        this.friendRepository = friendRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public Post createPost(String content, String privacy, List<Media> mediaList) {
        if ((content == null || content.isEmpty()) && (mediaList == null || mediaList.isEmpty())) {
            throw new BadRequestException("Post must have either content or media.");
        }

        // Retrieve the currently authenticated user
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Create the post
        Post post = Post.builder()
                .content(content)
                .attachments(mediaList)
                .privacy(Privacy.valueOf(privacy))
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .build();

        // Associate each media with the post
        if (mediaList != null) {
            for (Media media : mediaList) {
                media.setPost(post);
            }
        }

        // Save the post
        return postRepository.save(post);
    }

    @Override
    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
    }

    @Override
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Override
    public List<Post> deletePostByIdAndReturnRemaining(Long postId) throws IOException {
        // Fetch the post from the database
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        // Delete associated media using MediaService
        if (post.getAttachments() != null && !post.getAttachments().isEmpty()) {
            mediaService.deleteMediaFromCloudinary(post.getAttachments());
        }

        // Delete the post from the database
        postRepository.delete(post);

        // Fetch the remaining posts of the user
        return postRepository.findByCreatedBy(post.getCreatedBy());
    }

    @Override
    public Post updatePost(Long postId, String content, List<String> filesToDelete, List<MultipartFile> newFiles) throws IOException {
        // Lấy bài đăng từ DB
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        // Cập nhật content nếu có
        if (content != null && !content.isEmpty()) {
            post.setContent(content);
        }

        // Xóa media nếu có filesToDelete
        if (filesToDelete != null && !filesToDelete.isEmpty()) {
            List<Media> mediaToDelete = post.getAttachments().stream()
                    .filter(media -> filesToDelete.contains(media.getPublicId()))
                    .toList();

            mediaService.deleteMediaFromCloudinary(mediaToDelete);
            post.getAttachments().removeAll(mediaToDelete);
        }

        // Thêm media mới nếu có newFiles
        if (newFiles != null && !newFiles.isEmpty()) {
            List<Media> newMedia = new ArrayList<>();
            for (MultipartFile file : newFiles) {
                Media media = mediaService.uploadMedia(file);
                media.setPost(post);
                newMedia.add(media);
            }
            post.getAttachments().addAll(newMedia);
        }

        // Cập nhật updatedAt
        post.setUpdatedAt(LocalDateTime.now());

        // Lưu bài đăng
        return postRepository.save(post);
    }
    @Override
    public PostResponse likePost(Long postId) {
        // Retrieve the post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        // Retrieve the current user
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check access based on privacy
        if (post.getPrivacy() == Privacy.PRIVATE && !post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to like this post");
        } else if (post.getPrivacy() == Privacy.FRIENDS && !isFriend(post.getCreatedBy(), currentUser)) {
            throw new BadRequestException("You do not have permission to like this post");
        }

        // Check if the user already liked the post
        boolean alreadyLiked = likeRepository.existsByPostAndLikedBy(post, currentUser);
        if (alreadyLiked) {
            throw new BadRequestException("You have already liked this post");
        }

        // Add the like
        Like like = Like.builder()
                .post(post)
                .likedBy(currentUser)
                .build();
        likeRepository.save(like);

        // Fetch all users who liked the post
        List<User> likedUsers = likeRepository.findAllByPost(post).stream()
                .map(Like::getLikedBy)
                .toList();

        // Map the post to PostResponse
        PostResponse postResponse = postMapper.toPostResponse(post);
        postResponse.setLikedUsers(likedUsers);

        return postResponse;
    }

    // Helper method to check if two users are friends
    private boolean isFriend(User user1, User user2) {
        return friendRepository.findByUserIdAndFriendId(user1.getId(), user2.getId())
                .or(() -> friendRepository.findByUserIdAndFriendId(user2.getId(), user1.getId()))
                .isPresent();
    }
    @Override
    public PostResponse toggleLikePost(Long postId) {
        // Retrieve the post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        // Retrieve the current user
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check access based on privacy
        if (post.getPrivacy() == Privacy.PRIVATE && !post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to like/unlike this post");
        } else if (post.getPrivacy() == Privacy.FRIENDS && !isFriend(post.getCreatedBy(), currentUser)) {
            throw new BadRequestException("You do not have permission to like/unlike this post");
        }

        // Check if the user already liked the post
        boolean alreadyLiked = likeRepository.existsByPostAndLikedBy(post, currentUser);

        if (alreadyLiked) {
            // Unlike the post
            Like like = likeRepository.findAllByPost(post).stream()
                    .filter(l -> l.getLikedBy().equals(currentUser))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Like not found"));
            likeRepository.delete(like);
        } else {
            // Like the post
            Like like = Like.builder()
                    .post(post)
                    .likedBy(currentUser)
                    .build();
            likeRepository.save(like);
        }

        // Fetch all users who liked the post
        List<User> likedUsers = likeRepository.findAllByPost(post).stream()
                .map(Like::getLikedBy)
                .toList();

        // Map the post to PostResponse
        PostResponse postResponse = postMapper.toPostResponse(post);
        postResponse.setLikedUsers(likedUsers);

        return postResponse;
    }
    @Override
    public PostResponse addCommentToPost(Long postId, String content) {
        // Lấy bài đăng từ DB
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        // Lấy người dùng hiện tại
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Kiểm tra quyền truy cập dựa trên Privacy
        if (post.getPrivacy() == Privacy.PRIVATE && !post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to comment on this post");
        } else if (post.getPrivacy() == Privacy.FRIENDS && !isFriend(post.getCreatedBy(), currentUser) && !post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to comment on this post");
        }

        // Tạo bình luận mới
        Comment comment = Comment.builder()
                .post(post)
                .commentedBy(currentUser)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        // Lưu bình luận vào DB
        commentRepository.save(comment);

        // Lấy danh sách bình luận của bài đăng
        List<Comment> comments = commentRepository.findAllByPost(post);

        // Map bài đăng sang PostResponse
        PostResponse postResponse = postMapper.toPostResponse(post);
        postResponse.setComments(comments);

        return postResponse;
    }
}