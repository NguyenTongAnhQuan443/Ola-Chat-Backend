package vn.edu.iuh.fit.olachatbackend.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.*;
import vn.edu.iuh.fit.olachatbackend.entities.*;
import vn.edu.iuh.fit.olachatbackend.enums.Privacy;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.mappers.MediaMapper;
import vn.edu.iuh.fit.olachatbackend.mappers.PostMapper;
import vn.edu.iuh.fit.olachatbackend.repositories.*;
import vn.edu.iuh.fit.olachatbackend.services.CommentService;
import vn.edu.iuh.fit.olachatbackend.services.MediaService;
import vn.edu.iuh.fit.olachatbackend.services.PostService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MediaService mediaService;
    private final LikeRepository likeRepository;
    private final FriendRepository friendRepository;
    private final CommentRepository commentRepository;
    private final CommentService commentService;
    private final ShareRepository shareRepository;

    @Autowired
    private MediaMapper mediaMapper;
    @Autowired
    private PostMapper postMapper;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository, MediaService mediaService, LikeRepository likeRepository, FriendRepository friendRepository, CommentRepository commentRepository, CommentService commentService, ShareRepository shareRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.mediaService = mediaService;
        this.likeRepository = likeRepository;
        this.friendRepository = friendRepository;
        this.commentRepository = commentRepository;
        this.commentService = commentService;
        this.shareRepository = shareRepository;
    }

    @Override
    public PostResponse createPost(String content, String privacy, List<Media> mediaList) {
        if ((content == null || content.isEmpty()) && (mediaList == null || mediaList.isEmpty())) {
            throw new BadRequestException("Post must have either content or media.");
        }

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Post post = Post.builder()
                .content(content)
                .attachments(mediaList)
                .privacy(Privacy.valueOf(privacy))
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .build();

        if (mediaList != null) {
            for (Media media : mediaList) {
                media.setPost(post);
            }
        }

        Post savedPost = postRepository.save(post);

        return postMapper.toPostResponse(savedPost);
    }

    @Override
    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (post.getPrivacy() == Privacy.PRIVATE && !post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to access this post");
        } else if (post.getPrivacy() == Privacy.FRIENDS &&
                !isFriend(post.getCreatedBy(), currentUser) &&
                !post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to access this post");
        }

        Post originalPost = post.getOriginalPost();
        Long originalPostId = null;

        if (originalPost != null) {
            originalPostId = originalPost.getPostId(); // lưu lại ID

            boolean canViewOriginal = true;

            if (originalPost.getPrivacy() == Privacy.PRIVATE &&
                    !originalPost.getCreatedBy().equals(currentUser)) {
                canViewOriginal = false;
            } else if (originalPost.getPrivacy() == Privacy.FRIENDS &&
                    !isFriend(originalPost.getCreatedBy(), currentUser) &&
                    !originalPost.getCreatedBy().equals(currentUser)) {
                canViewOriginal = false;
            }

            if (!canViewOriginal) {
                post.setOriginalPost(null); // chặn xem chi tiết
            }
        }

        List<Comment> allComments = commentService.findAllByPost(post);
        List<CommentHierarchyResponse> commentHierarchy = commentService.buildCommentHierarchy(allComments);

        PostResponse postResponse = postMapper.toPostResponse(post);
        postResponse.setComments(commentHierarchy);

        // Gán originalPostId dù originalPost đã bị null
        postResponse.setOriginalPostId(originalPostId);

        return postResponse;
    }

    @Override
    public UserPostsResponse getUserPosts(int page, int size) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = postRepository.findByCreatedBy(currentUser, pageable);

        List<UserPostOnlyResponse> postResponses = postPage.stream().map(post -> {
            Post originalPost = post.getOriginalPost();
            Long originalPostId = null;

            if (originalPost != null) {
                originalPostId = originalPost.getPostId();

                boolean canViewOriginal = true;

                if (originalPost.getPrivacy() == Privacy.PRIVATE &&
                        !originalPost.getCreatedBy().equals(currentUser)) {
                    canViewOriginal = false;
                } else if (originalPost.getPrivacy() == Privacy.FRIENDS &&
                        !isFriend(originalPost.getCreatedBy(), currentUser) &&
                        !originalPost.getCreatedBy().equals(currentUser)) {
                    canViewOriginal = false;
                }

                if (!canViewOriginal) {
                    post.setOriginalPost(null); // Xoá chi tiết bài gốc
                }
            }

            List<Comment> allComments = commentService.findAllByPost(post);
            List<CommentHierarchyResponse> commentHierarchy = commentService.buildCommentHierarchy(allComments);

            UserPostOnlyResponse postResponse = postMapper.toUserPostOnlyResponse(post);
            postResponse.setComments(commentHierarchy);

            // Gán originalPostId dù originalPost bị ẩn
            postResponse.setOriginalPostId(originalPostId);

            return postResponse;
        }).collect(Collectors.toList());

        PostUserResponse createdBy = postMapper.userToPostUserResponse(currentUser);

        return new UserPostsResponse(
                createdBy,
                postResponses,
                postPage.getTotalPages(),
                postPage.getNumber() + 1,
                postPage.getSize()
        );
    }

    @Override
    @Transactional
    public void deletePostById(Long postId) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        if (post.getOriginalPost() != null) {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            shareRepository.deleteByPostAndSharedBy(post.getOriginalPost(), currentUser);
        }

        if (post.getAttachments() != null && !post.getAttachments().isEmpty()) {
            mediaService.deleteMediaFromCloudinary(post.getAttachments());
        }

        likeRepository.deleteAllByPost(post);
        commentRepository.deleteAllByPost(post);
        postRepository.delete(post);
    }

    @Override
    public PostResponse updatePost(Long postId, String content, List<String> filesToDelete, List<MultipartFile> newFiles) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to update this post");
        }

        Post originalPost = post.getOriginalPost();
        Long originalPostId = null;

        if (originalPost != null) {
            // Là bài share => chỉ được sửa content
            if (content != null && !content.isEmpty()) {
                post.setContent(content);
            } else {
                throw new BadRequestException("Shared posts can only update content.");
            }

            originalPostId = originalPost.getPostId();

            boolean canViewOriginal = true;
            if (originalPost.getPrivacy() == Privacy.PRIVATE && !originalPost.getCreatedBy().equals(currentUser)) {
                canViewOriginal = false;
            } else if (originalPost.getPrivacy() == Privacy.FRIENDS &&
                    !isFriend(originalPost.getCreatedBy(), currentUser) &&
                    !originalPost.getCreatedBy().equals(currentUser)) {
                canViewOriginal = false;
            }

            if (!canViewOriginal) {
                post.setOriginalPost(null); // Ẩn nội dung bài gốc
            }

        } else {
            // Là bài viết bình thường
            if (content != null && !content.isEmpty()) {
                post.setContent(content);
            }

            if (filesToDelete != null && !filesToDelete.isEmpty()) {
                List<Media> mediaToDelete = post.getAttachments().stream()
                        .filter(media -> filesToDelete.contains(media.getPublicId()))
                        .toList();

                mediaService.deleteMediaFromCloudinary(mediaToDelete);
                post.getAttachments().removeAll(mediaToDelete);
            }

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

        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);

        List<Comment> allComments = commentService.findAllByPost(post);
        List<CommentHierarchyResponse> commentHierarchy = commentService.buildCommentHierarchy(allComments);

        PostResponse postResponse = postMapper.toPostResponse(post);
        postResponse.setComments(commentHierarchy);

        // Gán lại originalPostId nếu cần (dù originalPost = null)
        postResponse.setOriginalPostId(originalPostId);

        return postResponse;
    }

    @Override
    public PostResponse sharePost(Long postId, String content) {
        // Retrieve the post to be shared
        Post postToShare = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        // Retrieve the current user
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // If the post has an original post (it's a shared post), check the permissions for sharing the original post
        Post originalPost = postToShare.getOriginalPost(); // Directly get the original post

        if (originalPost != null) {
            // Check if the user has permission to share the original post
            checkSharePermissions(originalPost, currentUser);

            // Create a shared post with a reference to the original post
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

        // If no original post, share the post itself (not a shared post)
        Post sharedPost = Post.builder()
                .content(content)
                .attachments(null) // Shared posts do not have attachments
                .privacy(Privacy.PUBLIC)
                .createdBy(currentUser)
                .createdAt(LocalDateTime.now())
                .originalPost(postToShare) // Reference to the original post is the post itself
                .build();

        // Save the shared post
        Post savedPost = postRepository.save(sharedPost);

        // Save the share information in the Share table
        Share share = Share.builder()
                .post(postToShare)
                .sharedBy(currentUser)
                .sharedAt(LocalDateTime.now())
                .build();
        shareRepository.save(share);

        // Map the shared post to PostResponse
        return postMapper.toPostResponse(savedPost);
    }

    private void checkSharePermissions(Post originalPost, User currentUser) {
        if (originalPost.getPrivacy() == Privacy.PRIVATE && !originalPost.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to share this post");
        } else if (originalPost.getPrivacy() == Privacy.FRIENDS && !isFriend(originalPost.getCreatedBy(), currentUser)) {
            throw new BadRequestException("You do not have permission to share this post");
        }
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

        // Fetch all comments and build hierarchy
        List<Comment> allComments = commentService.findAllByPost(post);
        List<CommentHierarchyResponse> commentHierarchy = commentService.buildCommentHierarchy(allComments);

        // Map the post to PostResponse
        PostResponse postResponse = postMapper.toPostResponse(post);
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

        // Fetch all comments and build hierarchy
        List<Comment> allComments = commentService.findAllByPost(post);
        List<CommentHierarchyResponse> commentHierarchy = commentService.buildCommentHierarchy(allComments);

        // Map the post to PostResponse
        PostResponse postResponse = postMapper.toPostResponse(post);
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
                .commentedBy(PostUserResponse.builder()
                        .userId(currentUser.getId())
                        .username(currentUser.getUsername())
                        .displayName(currentUser.getDisplayName())
                        .avatar(currentUser.getAvatar())
                        .build())
                .replies(new ArrayList<>()) // Replies are not needed for this response
                .build();
    }

    @Override
    public List<PostResponse> getFeed(int page, int size) {
        // Lấy người dùng hiện tại
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Lấy danh sách bạn bè
        List<String> friendIds = friendRepository.findByUser_IdOrFriend_Id(currentUser.getId(), currentUser.getId())
                .stream()
                .map(friend -> friend.getUser().getId().equals(currentUser.getId()) ? friend.getFriend().getId() : friend.getUser().getId())
                .toList();

        // Lấy bài viết của người dùng và bạn bè với điều kiện Privacy
        List<Post> posts = postRepository.findFeedPosts(currentUser.getId(), friendIds, PageRequest.of(page, size))
                .stream()
                .filter(post -> {
                    if (post.getCreatedBy().equals(currentUser)) {
                        return true; // Hiển thị tất cả bài đăng của chính người dùng
                    } else if (friendIds.contains(post.getCreatedBy().getId())) {
                        return post.getPrivacy() == Privacy.PUBLIC || post.getPrivacy() == Privacy.FRIENDS;
                    } else {
                        return post.getPrivacy() == Privacy.PUBLIC;
                    }
                })
                .toList();

        // Map sang PostResponse
        return posts.stream()
                .map(postMapper::toPostResponse)
                .toList();
    }

    @Override
    public List<PostResponse> getUserProfilePosts(String userId, int page, int size) {
        // Lấy người dùng hiện tại
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Lấy người dùng được truy cập
        User profileUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Kiểm tra mối quan hệ bạn bè
        boolean isFriend = friendRepository.findByUserIdAndFriendId(currentUser.getId(), profileUser.getId())
                .or(() -> friendRepository.findByUserIdAndFriendId(profileUser.getId(), currentUser.getId()))
                .isPresent();

        // Lấy danh sách bài đăng dựa trên quyền riêng tư
        Page<Post> posts;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (currentUser.equals(profileUser)) {
            // Chính chủ: Hiển thị tất cả bài đăng
            posts = postRepository.findByCreatedBy(profileUser, pageRequest);
        } else if (isFriend) {
            // Bạn bè: Hiển thị bài đăng PUBLIC và FRIENDS
            posts = postRepository.findByCreatedByAndPrivacyIn(profileUser, List.of(Privacy.PUBLIC, Privacy.FRIENDS), pageRequest);
        } else {
            // Không phải bạn bè: Chỉ hiển thị bài đăng PUBLIC
            posts = postRepository.findByCreatedByAndPrivacy(profileUser, Privacy.PUBLIC, pageRequest);
        }

        // Map sang PostResponse
        return posts.stream()
                .map(postMapper::toPostResponse)
                .toList();
    }
}