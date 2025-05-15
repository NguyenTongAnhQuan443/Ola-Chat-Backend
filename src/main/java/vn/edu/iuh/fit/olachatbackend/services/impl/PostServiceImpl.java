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
    private PostMapper postMapper;

    @Autowired
    private FavoriteRepository favoriteRepository;

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

        if (originalPost != null) {
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
                post.setOriginalPost(null);
            }
        }

        List<Comment> allComments = commentService.findAllByPost(post);
        List<CommentHierarchyResponse> commentHierarchy = commentService.buildCommentHierarchy(allComments);

        PostResponse postResponse = postMapper.toPostResponse(post);
        postResponse.setComments(commentHierarchy);

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

            if (originalPost != null) {
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
                    post.setOriginalPost(null);
                }
            }

            List<Comment> allComments = commentService.findAllByPost(post);
            List<CommentHierarchyResponse> commentHierarchy = commentService.buildCommentHierarchy(allComments);

            UserPostOnlyResponse postResponse = postMapper.toUserPostOnlyResponse(post);
            postResponse.setComments(commentHierarchy);

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

    @Transactional
    public void deletePostById(Long postId) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to delete this post");
        }

        // Ngắt liên kết các bài share đã tham chiếu bài này
        List<Post> sharedPosts = postRepository.findByOriginalPost(post);
        for (Post sharedPost : sharedPosts) {
            sharedPost.setOriginalPost(null);
            postRepository.save(sharedPost);
        }

        // Xóa các bản ghi share, like, comment liên quan (nếu có)
        shareRepository.deleteBySharedPost(post);
        List<Share> sharesToDelete = shareRepository.findByPost(post);
        shareRepository.deleteAll(sharesToDelete);

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
        boolean hideOriginalPost = false;

        if (originalPost != null) {
            // Là bài share => chỉ được sửa content
            if (content != null && !content.isEmpty()) {
                post.setContent(content);
            } else {
                throw new BadRequestException("Shared posts can only update content.");
            }

            // Kiểm tra quyền xem bài gốc
            if (originalPost.getPrivacy() == Privacy.PRIVATE && !originalPost.getCreatedBy().equals(currentUser)) {
                hideOriginalPost = true;
            } else if (originalPost.getPrivacy() == Privacy.FRIENDS &&
                    !isFriend(originalPost.getCreatedBy(), currentUser) &&
                    !originalPost.getCreatedBy().equals(currentUser)) {
                hideOriginalPost = true;
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

        // Nếu không có quyền xem bài gốc, thì ẩn nội dung bài gốc trong phản hồi
        if (hideOriginalPost) {
            postResponse.setOriginalPost(null);
        }

        return postResponse;
    }

    @Override
    public PostResponse sharePost(Long postId, String content) {
        // Lấy bài viết cần chia sẻ
        Post postToShare = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        // Lấy người dùng hiện tại
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Xác định bài gốc thực sự (nếu là bài share thì lấy bài gốc của bài đó)
        Post originalPost = postToShare.getOriginalPost() != null ? postToShare.getOriginalPost() : postToShare;

        // Kiểm tra quyền chia sẻ
        if (!canSharePost(originalPost, currentUser)) {
            throw new BadRequestException("You do not have permission to share this post.");
        }

        // Tạo bài share mới
        Post sharedPost = Post.builder()
                .content(content)
                .attachments(null) // Không đính kèm file trong bài share
                .privacy(Privacy.PUBLIC) // Bài share mặc định là công khai
                .createdBy(currentUser)
                .createdAt(LocalDateTime.now())
                .originalPost(originalPost)
                .originalPostId(originalPost.getPostId())
                .build();

        // Lưu bài share
        Post savedPost = postRepository.save(sharedPost);

        // Lưu thông tin vào bảng share (với bài gốc thực sự)
        Share share = Share.builder()
                .post(originalPost)
                .sharedPost(savedPost)
                .sharedBy(currentUser)
                .sharedAt(LocalDateTime.now())
                .build();
        shareRepository.save(share);

        return postMapper.toPostResponse(savedPost);
    }

    // Helper method to check if two users are friends
    private boolean isFriend(User user1, User user2) {
        return friendRepository.findByUserIdAndFriendId(user1.getId(), user2.getId())
                .or(() -> friendRepository.findByUserIdAndFriendId(user2.getId(), user1.getId()))
                .isPresent();
    }

    private boolean canSharePost(Post post, User currentUser) {
        // Người tạo được quyền chia sẻ bài của chính họ
        if (post.getCreatedBy().equals(currentUser)) {
            return true;
        }

        return switch (post.getPrivacy()) {
            case PUBLIC -> true;
            case FRIENDS -> isFriend(post.getCreatedBy(), currentUser);
            case PRIVATE -> false;
        };
    }

    @Override
    public void likePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (post.getPrivacy() == Privacy.PRIVATE && !post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to like this post");
        } else if (post.getPrivacy() == Privacy.FRIENDS && !isFriend(post.getCreatedBy(), currentUser)) {
            throw new BadRequestException("You do not have permission to like this post");
        }

        boolean alreadyLiked = likeRepository.existsByPostAndLikedBy(post, currentUser);
        if (alreadyLiked) {
            throw new BadRequestException("You have already liked this post");
        }

        Like like = Like.builder()
                .post(post)
                .likedBy(currentUser)
                .build();
        likeRepository.save(like);
    }

    @Override
    public List<PostUserResponse> getPostLikes(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Kiểm tra quyền truy cập bài đăng
        if (post.getPrivacy() == Privacy.PRIVATE && !post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to view likes for this post");
        } else if (post.getPrivacy() == Privacy.FRIENDS &&
                !isFriend(post.getCreatedBy(), currentUser) &&
                !post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to view likes for this post");
        }

        // Lấy danh sách người thích bài đăng
        return likeRepository.findAllByPost(post).stream()
                .map(like -> postMapper.userToPostUserResponse(like.getLikedBy()))
                .toList();
    }

    @Override
    public boolean toggleLikePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (post.getPrivacy() == Privacy.PRIVATE && !post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to like/unlike this post");
        } else if (post.getPrivacy() == Privacy.FRIENDS && !isFriend(post.getCreatedBy(), currentUser)) {
            throw new BadRequestException("You do not have permission to like/unlike this post");
        }

        boolean alreadyLiked = likeRepository.existsByPostAndLikedBy(post, currentUser);

        if (alreadyLiked) {
            Like like = likeRepository.findAllByPost(post).stream()
                    .filter(l -> l.getLikedBy().equals(currentUser))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Like not found"));
            likeRepository.delete(like);
            return true; // Unliked
        } else {
            Like like = Like.builder()
                    .post(post)
                    .likedBy(currentUser)
                    .build();
            likeRepository.save(like);
            return false; // Liked
        }
    }

    @Override
    public List<CommentHierarchyResponse> addCommentToPost(Long postId, String content) {
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
        return commentService.buildCommentHierarchy(allComments);
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
        return posts.stream().map(post -> {
            Post originalPost = post.getOriginalPost();

            if (originalPost != null) {

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
                    post.setOriginalPost(null);
                }
            }

            PostResponse postResponse = postMapper.toPostResponse(post);

            return postResponse;
        }).toList();
    }

    @Override
    public UserPostsResponse getUserProfilePosts(String userId, int page, int size) {
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
            posts = postRepository.findByCreatedBy(profileUser, pageRequest);
        } else if (isFriend) {
            posts = postRepository.findByCreatedByAndPrivacyIn(profileUser, List.of(Privacy.PUBLIC, Privacy.FRIENDS), pageRequest);
        } else {
            posts = postRepository.findByCreatedByAndPrivacy(profileUser, Privacy.PUBLIC, pageRequest);
        }

        // Map danh sách bài đăng sang UserPostOnlyResponse
        List<UserPostOnlyResponse> postResponses = posts.stream().map(post -> {
            Post originalPost = post.getOriginalPost();

            if (originalPost != null) {

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
                    post.setOriginalPost(null); // Ẩn chi tiết bài gốc
                }
            }

            UserPostOnlyResponse postResponse = postMapper.toUserPostOnlyResponse(post);

            return postResponse;
        }).toList();

        // Tạo thông tin người dùng
        PostUserResponse createdBy = postMapper.userToPostUserResponse(profileUser);

        // Trả về UserPostsResponse
        return new UserPostsResponse(
                createdBy,
                postResponses,
                posts.getTotalPages(),
                posts.getNumber() + 1,
                posts.getSize()
        );
    }

    @Override
    public List<PostResponse> searchPosts(String keyword, int page, int size) {
        // Lấy tên người dùng hiện tại từ SecurityContext
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Tạo đối tượng phân trang
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Tìm các bài viết có nội dung chứa từ khóa (không phân biệt hoa thường)
        Page<Post> posts = postRepository.findByContentContainingIgnoreCase(keyword, pageable);

        // Lọc bài viết dựa trên quyền riêng tư và mối quan hệ bạn bè
        return posts.stream()
                .filter(post -> {
                    if (post.getCreatedBy().equals(currentUser)) {
                        return true; // Hiển thị tất cả bài viết của chính người dùng
                    } else if (isFriend(post.getCreatedBy(), currentUser)) {
                        // Hiển thị bài viết công khai hoặc chỉ bạn bè
                        return post.getPrivacy() == Privacy.PUBLIC || post.getPrivacy() == Privacy.FRIENDS;
                    } else {
                        // Chỉ hiển thị bài viết công khai
                        return post.getPrivacy() == Privacy.PUBLIC;
                    }
                })
                .map(post -> {
                    // Kiểm tra quyền xem bài gốc nếu bài viết là bài chia sẻ
                    Post originalPost = post.getOriginalPost();
                    if (originalPost != null) {
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
                            post.setOriginalPost(null); // Ẩn bài gốc nếu không có quyền xem
                        }
                    }

                    // Chuyển đổi bài viết sang PostResponse
                    return postMapper.toPostResponse(post);
                })
                .toList();
    }

    @Override
    public PostResponse updatePostPrivacy(Long postId, String privacy) {
        // Lấy bài viết từ DB
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        // Lấy người dùng hiện tại
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Kiểm tra quyền cập nhật
        if (!post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to update this post");
        }

        // Cập nhật Privacy
        post.setPrivacy(Privacy.valueOf(privacy.toUpperCase()));
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);

        // Trả về PostResponse
        return postMapper.toPostResponse(post);
    }

    @Override
    public void addPostToFavorites(Long postId) {
        // Lấy bài viết từ DB
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        // Lấy người dùng hiện tại
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Kiểm tra quyền truy cập bài đăng
        if (post.getPrivacy() == Privacy.PRIVATE && !post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to favorite this post");
        } else if (post.getPrivacy() == Privacy.FRIENDS &&
                !isFriend(post.getCreatedBy(), currentUser) &&
                !post.getCreatedBy().equals(currentUser)) {
            throw new BadRequestException("You do not have permission to favorite this post");
        }

        // Kiểm tra nếu bài viết đã được thêm vào danh sách yêu thích
        boolean alreadyFavorited = favoriteRepository.existsByPostAndUser(post, currentUser);
        if (alreadyFavorited) {
            throw new BadRequestException("Post is already in your favorites");
        }

        // Lưu bài viết vào danh sách yêu thích
        Favorite favorite = Favorite.builder()
                .post(post)
                .user(currentUser)
                .createdAt(LocalDateTime.now())
                .build();
        favoriteRepository.save(favorite);
    }
}