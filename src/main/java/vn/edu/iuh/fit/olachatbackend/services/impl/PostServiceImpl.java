package vn.edu.iuh.fit.olachatbackend.services.impl;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.CommentHierarchyResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.CommentedByResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PostResponse;
import vn.edu.iuh.fit.olachatbackend.entities.*;
import vn.edu.iuh.fit.olachatbackend.enums.Privacy;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.mappers.PostMapper;
import vn.edu.iuh.fit.olachatbackend.repositories.*;
import vn.edu.iuh.fit.olachatbackend.services.CommentService;
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
    private final CommentService commentService;
    private final ShareRepository shareRepository;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository, MediaService mediaService, LikeRepository likeRepository, PostMapper postMapper, FriendRepository friendRepository, CommentRepository commentRepository, CommentService commentService, ShareRepository shareRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.mediaService = mediaService;
        this.likeRepository = likeRepository;
        this.postMapper = postMapper;
        this.friendRepository = friendRepository;
        this.commentRepository = commentRepository;
        this.commentService = commentService;
        this.shareRepository = shareRepository;
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
    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        // Fetch all comments and build hierarchy
        List<Comment> allComments = commentService.findAllByPost(post);
        List<CommentHierarchyResponse> commentHierarchy = commentService.buildCommentHierarchy(allComments);

        // Fetch all users who liked the post
        List<User> likedUsers = likeRepository.findAllByPost(post).stream()
                .map(Like::getLikedBy)
                .toList();

        // Map the post to PostResponse
        PostResponse postResponse = postMapper.toPostResponse(post);
        postResponse.setComments(commentHierarchy);
        postResponse.setLikedUsers(likedUsers);

        return postResponse;
    }

    @Override
    public List<PostResponse> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        List<PostResponse> postResponses = new ArrayList<>();

        for (Post post : posts) {
            // Fetch all comments and build hierarchy
            List<Comment> allComments = commentService.findAllByPost(post);
            List<CommentHierarchyResponse> commentHierarchy = commentService.buildCommentHierarchy(allComments);

            // Fetch all users who liked the post
            List<User> likedUsers = likeRepository.findAllByPost(post).stream()
                    .map(Like::getLikedBy)
                    .toList();

            // Map the post to PostResponse
            PostResponse postResponse = postMapper.toPostResponse(post);
            postResponse.setComments(commentHierarchy);
            postResponse.setLikedUsers(likedUsers);

            postResponses.add(postResponse);
        }

        return postResponses;
    }

    @Override
    @Transactional
    public List<PostResponse> deletePostByIdAndReturnRemaining(Long postId) throws IOException {
        // Fetch the post from the database
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        // Check if the post is a shared post
        if (post.getOriginalPost() != null) {
            // Retrieve the current user
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            // Delete the specific entry in the post_share table
            shareRepository.deleteByPostAndSharedBy(post.getOriginalPost(), currentUser);
        }

        // Delete associated media using MediaService
        if (post.getAttachments() != null && !post.getAttachments().isEmpty()) {
            mediaService.deleteMediaFromCloudinary(post.getAttachments());
        }

        // Delete all likes associated with the post
        likeRepository.deleteAllByPost(post);

        // Delete all comments associated with the post
        commentRepository.deleteAllByPost(post);

        // Delete the post from the database
        postRepository.delete(post);

        // Fetch all remaining posts
        List<Post> remainingPosts = postRepository.findAll();
        List<PostResponse> postResponses = new ArrayList<>();

        for (Post remainingPost : remainingPosts) {
            // Fetch all comments and build hierarchy
            List<Comment> allComments = commentService.findAllByPost(remainingPost);
            List<CommentHierarchyResponse> commentHierarchy = commentService.buildCommentHierarchy(allComments);

            // Fetch all users who liked the post
            List<User> likedUsers = likeRepository.findAllByPost(remainingPost).stream()
                    .map(Like::getLikedBy)
                    .toList();

            // Map the post to PostResponse
            PostResponse postResponse = postMapper.toPostResponse(remainingPost);
            postResponse.setComments(commentHierarchy);
            postResponse.setLikedUsers(likedUsers);

            postResponses.add(postResponse);
        }

        return postResponses;
    }

    @Override
    public PostResponse updatePost(Long postId, String content, List<String> filesToDelete, List<MultipartFile> newFiles) throws IOException {
        // Lấy bài đăng từ DB
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        // Lấy người dùng hiện tại
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Kiểm tra quyền sở hữu bài đăng
        if (!post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to update this post");
        }

        // Kiểm tra nếu bài đăng là bài share
        if (post.getOriginalPost() != null) {
            // Chỉ cho phép cập nhật content
            if (content != null && !content.isEmpty()) {
                post.setContent(content);
            } else {
                throw new BadRequestException("Shared posts can only update content.");
            }
        } else {

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
        }
        // Cập nhật updatedAt
        post.setUpdatedAt(LocalDateTime.now());

        // Lưu bài đăng
        postRepository.save(post);

        // Lấy tất cả bình luận của bài đăng và xây dựng cấu trúc phân cấp
        List<Comment> allComments = commentService.findAllByPost(post);
        List<CommentHierarchyResponse> commentHierarchy = commentService.buildCommentHierarchy(allComments);

        // Lấy tất cả người dùng đã thích bài đăng
        List<User> likedUsers = likeRepository.findAllByPost(post).stream()
                .map(Like::getLikedBy)
                .toList();

        // Map bài đăng sang PostResponse
        PostResponse postResponse = postMapper.toPostResponse(post);
        postResponse.setComments(commentHierarchy);
        postResponse.setLikedUsers(likedUsers);

        return postResponse;
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

        // Fetch all comments and build hierarchy
        List<Comment> allComments = commentService.findAllByPost(post);
        List<CommentHierarchyResponse> commentHierarchy = commentService.buildCommentHierarchy(allComments);

        // Map the post to PostResponse
        PostResponse postResponse = postMapper.toPostResponse(post);
        postResponse.setLikedUsers(likedUsers);
        postResponse.setComments(commentHierarchy);

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

        // Fetch all comments and build hierarchy
        List<Comment> allComments = commentService.findAllByPost(post);
        List<CommentHierarchyResponse> commentHierarchy = commentService.buildCommentHierarchy(allComments);

        // Map the post to PostResponse
        PostResponse postResponse = postMapper.toPostResponse(post);
        postResponse.setLikedUsers(likedUsers);
        postResponse.setComments(commentHierarchy);

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
        commentService.save(comment);

        // Lấy tất cả bình luận của bài đăng
        List<Comment> allComments = commentService.findAllByPost(post);

        // Xây dựng cấu trúc phân cấp cho bình luận
        List<CommentHierarchyResponse> commentHierarchy = commentService.buildCommentHierarchy(allComments);

        // Map bài đăng sang PostResponse
        PostResponse postResponse = postMapper.toPostResponse(post);
        postResponse.setComments(commentHierarchy);

        return postResponse;
    }

    @Override
    public List<CommentHierarchyResponse> getCommentHierarchy(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        List<Comment> allComments = commentService.findAllByPost(post);
        return commentService.buildCommentHierarchy(allComments);
    }

    @Override
    public List<CommentHierarchyResponse> deleteComment(Long commentId) {
        Comment comment = commentService.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!comment.getCommentedBy().equals(currentUser) && !comment.getPost().getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to delete this comment");
        }

        commentService.delete(comment);

        Post post = comment.getPost();
        List<Comment> updatedComments = commentService.findAllByPost(post);
        return commentService.buildCommentHierarchy(updatedComments);
    }

    @Override
    public List<CommentHierarchyResponse> addReplyToComment(Long commentId, String content) {
        // Lấy bình luận cha
        Comment parentComment = commentService.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        // Lấy bài đăng từ bình luận cha
        Post post = parentComment.getPost();

        // Lấy người dùng hiện tại
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Tạo bình luận trả lời
        Comment replyComment = Comment.builder()
                .post(post)
                .parentComment(parentComment)
                .commentedBy(currentUser)
                .content(content)
                .createdAt(LocalDateTime.now())
                .updatedAt(null) // Set updatedAt to null initially
                .build();

        // Lưu bình luận trả lời
        commentService.save(replyComment);

        // Lấy tất cả bình luận của bài đăng
        List<Comment> allComments = commentService.findAllByPost(post);

        // Xây dựng cấu trúc phân cấp
        return commentService.buildCommentHierarchy(allComments);
    }

    // Cập nhật bình luận
    @Override
    public CommentHierarchyResponse updateComment(Long commentId, String content) {
        // Retrieve the comment from the database
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        // Retrieve the current user
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if the current user is the owner of the comment
        if (!comment.getCommentedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to update this comment");
        }

        // Update the comment content and updatedAt field
        comment.setContent(content);
        comment.setUpdatedAt(LocalDateTime.now());

        // Save the updated comment
        commentRepository.save(comment);

        // Map the updated comment to CommentHierarchyResponse
        return CommentHierarchyResponse.builder()
                .commentId(comment.getCommentId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .commentedBy(CommentedByResponse.builder()
                        .username(currentUser.getUsername())
                        .displayName(currentUser.getDisplayName())
                        .avatar(currentUser.getAvatar())
                        .build())
                .replies(new ArrayList<>()) // Replies are not needed for this response
                .build();
    }

    @Override
    public PostResponse sharePost(Long postId, String content) {
        // Retrieve the original post
        Post originalPost = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        // Retrieve the current user
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check sharing permissions based on privacy
        if (originalPost.getPrivacy() == Privacy.PRIVATE && !originalPost.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to share this post");
        } else if (originalPost.getPrivacy() == Privacy.FRIENDS && !isFriend(originalPost.getCreatedBy(), currentUser)) {
            throw new BadRequestException("You do not have permission to share this post");
        }

        // Create the shared post
        Post sharedPost = Post.builder()
                .content(content) // Content of the shared post
                .attachments(null) // Shared posts do not have attachments
                .privacy(Privacy.PUBLIC) // Default privacy for shared posts
                .createdBy(currentUser)
                .createdAt(LocalDateTime.now())
                .originalPost(originalPost) // Reference to the original post
                .build();

        // Save the shared post
        Post savedPost = postRepository.save(sharedPost);

        // Save the share information in the Share table
        Share share = Share.builder()
                .post(originalPost)
                .sharedBy(currentUser)
                .sharedAt(LocalDateTime.now())
                .build();
        shareRepository.save(share);

        // Map the shared post to PostResponse
        return postMapper.toPostResponse(savedPost);
    }
}