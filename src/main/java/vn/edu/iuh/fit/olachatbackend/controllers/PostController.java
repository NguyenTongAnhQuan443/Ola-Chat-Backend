package vn.edu.iuh.fit.olachatbackend.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.*;
import vn.edu.iuh.fit.olachatbackend.entities.Media;

import vn.edu.iuh.fit.olachatbackend.services.MediaService;
import vn.edu.iuh.fit.olachatbackend.services.PostService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final MediaService mediaService;

    public PostController(PostService postService, MediaService mediaService) {
        this.postService = postService;
        this.mediaService = mediaService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<MessageResponse<PostResponse>> createPost(
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "privacy") String privacy,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) throws IOException {

        List<Media> mediaList = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                Media media = mediaService.uploadMedia(file);
                mediaList.add(media);
            }
        }

        PostResponse createdPost = postService.createPost(content, privacy, mediaList);
        MessageResponse<PostResponse> response = MessageResponse.<PostResponse>builder()
                .message("Post created successfully")
                .data(createdPost)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<MessageResponse<PostResponse>> getPostById(@PathVariable Long postId) {
        PostResponse postResponse = postService.getPostById(postId);
        MessageResponse<PostResponse> response = MessageResponse.<PostResponse>builder()
                .message("Post retrieved successfully")
                .data(postResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<MessageResponse<UserPostsResponse>> getUserPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UserPostsResponse postResponses = postService.getUserPosts(page, size);
        return ResponseEntity.ok(
                MessageResponse.<UserPostsResponse>builder()
                        .message("User posts retrieved successfully")
                        .data(postResponses)
                        .build()
        );
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<MessageResponse<String>> deletePost(@PathVariable Long postId) throws IOException {
        postService.deletePostById(postId);
        return ResponseEntity.ok(
                MessageResponse.<String>builder()
                        .message("Post deleted successfully")
                        .data(null)
                        .build()
        );
    }

    @PutMapping(value = "/{postId}", consumes = "multipart/form-data")
    public ResponseEntity<MessageResponse<PostResponse>> updatePost(
            @PathVariable Long postId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "filesToDelete", required = false) List<String> filesToDelete,
            @RequestParam(value = "newFiles", required = false) List<MultipartFile> newFiles) throws IOException {

        PostResponse postResponse = postService.updatePost(postId, content, filesToDelete, newFiles);
        return ResponseEntity.ok(
                MessageResponse.<PostResponse>builder()
                        .message("Post updated successfully")
                        .data(postResponse)
                        .build()
        );
    }

    @PostMapping("/{postId}/share")
    public ResponseEntity<MessageResponse<PostResponse>> sharePost(
            @PathVariable Long postId,
            @RequestParam(value = "content", required = false) String content) {
        PostResponse postResponse = postService.sharePost(postId, content);
        return ResponseEntity.ok(
                MessageResponse.<PostResponse>builder()
                        .message("Post shared successfully")
                        .data(postResponse)
                        .build()
        );
    }

    @GetMapping("/{postId}/shares")
    public ResponseEntity<MessageResponse<List<ShareResponse>>> getPostShares(@PathVariable Long postId) {
        List<ShareResponse> shares = postService.getPostShares(postId);
        return ResponseEntity.ok(
                MessageResponse.<List<ShareResponse>>builder()
                        .message("Shares retrieved successfully")
                        .data(shares)
                        .build()
        );
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<MessageResponse<String>> likePost(@PathVariable Long postId) {
        postService.likePost(postId);
        return ResponseEntity.ok(
                MessageResponse.<String>builder()
                        .message("Post liked successfully")
                        .build()
        );
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<MessageResponse<?>> toggleLikePost(@PathVariable Long postId) {
        boolean isUnliked = postService.toggleLikePost(postId);

        String message = isUnliked ? "Post unliked successfully" : "Post liked successfully";

        return ResponseEntity.ok(
                MessageResponse.builder()
                        .message(message)
                        .build()
        );
    }

    @GetMapping("/{postId}/likes")
    public ResponseEntity<MessageResponse<List<PostUserResponse>>> getPostLikes(@PathVariable Long postId) {
        List<PostUserResponse> likes = postService.getPostLikes(postId);
        return ResponseEntity.ok(
                MessageResponse.<List<PostUserResponse>>builder()
                        .message("Likes retrieved successfully")
                        .data(likes)
                        .build()
        );
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<MessageResponse<List<CommentHierarchyResponse>>> addCommentToPost(
            @PathVariable Long postId,
            @RequestParam("content") String content) {
        List<CommentHierarchyResponse> commentHierarchy = postService.addCommentToPost(postId, content);
        return ResponseEntity.ok(
                MessageResponse.<List<CommentHierarchyResponse>>builder()
                        .message("Comment added to post successfully")
                        .data(commentHierarchy)
                        .build()
        );
    }

    //Lấy danh sách bình luận theo cấu trúc phân cấp
    @GetMapping("/{postId}/comments/hierarchy")
    public ResponseEntity<MessageResponse<List<CommentHierarchyResponse>>> getCommentHierarchy(@PathVariable Long postId) {
        List<CommentHierarchyResponse> commentHierarchy = postService.getCommentHierarchy(postId);
        return ResponseEntity.ok(
                MessageResponse.<List<CommentHierarchyResponse>>builder()
                        .message("Comment hierarchy retrieved successfully")
                        .data(commentHierarchy)
                        .build()
        );
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<MessageResponse<List<CommentHierarchyResponse>>> deleteComment(@PathVariable Long commentId) {
        List<CommentHierarchyResponse> updatedComments = postService.deleteComment(commentId);
        return ResponseEntity.ok(
                MessageResponse.<List<CommentHierarchyResponse>>builder()
                        .message("Comment deleted successfully")
                        .data(updatedComments)
                        .build()
        );
    }

    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<MessageResponse<List<CommentHierarchyResponse>>> addReplyToComment(
            @PathVariable Long commentId,
            @RequestParam("content") String content) {
        List<CommentHierarchyResponse> commentHierarchy = postService.addReplyToComment(commentId, content);
        return ResponseEntity.ok(
                MessageResponse.<List<CommentHierarchyResponse>>builder()
                        .message("Reply added to comment successfully")
                        .data(commentHierarchy)
                        .build()
        );
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<MessageResponse<CommentHierarchyResponse>> updateComment(
            @PathVariable Long commentId,
            @RequestParam("content") String content) {
        CommentHierarchyResponse updatedComment = postService.updateComment(commentId, content);
        return ResponseEntity.ok(
                MessageResponse.<CommentHierarchyResponse>builder()
                        .message("Comment updated successfully")
                        .data(updatedComment)
                        .build()
        );
    }

    @GetMapping("/feed")
    public ResponseEntity<MessageResponse<List<PostResponse>>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<PostResponse> feed = postService.getFeed(page, size);
        return ResponseEntity.ok(
                MessageResponse.<List<PostResponse>>builder()
                        .message("Feed retrieved successfully")
                        .data(feed)
                        .build()
        );
    }

    @GetMapping("/user/{userId}/posts")
    public ResponseEntity<MessageResponse<UserPostsResponse>> getUserProfilePosts(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserPostsResponse posts = postService.getUserProfilePosts(userId, page, size);
        return ResponseEntity.ok(
                MessageResponse.<UserPostsResponse>builder()
                        .message("User profile posts retrieved successfully")
                        .data(posts)
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<MessageResponse<List<PostResponse>>> searchPosts(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<PostResponse> posts = postService.searchPosts(keyword, page, size);
        return ResponseEntity.ok(
                MessageResponse.<List<PostResponse>>builder()
                        .message("Posts retrieved successfully")
                        .data(posts)
                        .build()
        );
    }

    @PutMapping("/{postId}/privacy")
    public ResponseEntity<MessageResponse<PostResponse>> updatePostPrivacy(
            @PathVariable Long postId,
            @RequestParam("privacy") String privacy) {
        PostResponse updatedPost = postService.updatePostPrivacy(postId, privacy);
        return ResponseEntity.ok(
                MessageResponse.<PostResponse>builder()
                        .message("Post privacy updated successfully")
                        .data(updatedPost)
                        .build()
        );
    }

    @PostMapping("/{postId}/favorite")
    public ResponseEntity<MessageResponse<String>> addPostToFavorites(@PathVariable Long postId) {
        postService.addPostToFavorites(postId);
        return ResponseEntity.ok(
                MessageResponse.<String>builder()
                        .message("Post added to favorites successfully")
                        .build()
        );
    }

    @DeleteMapping("/{postId}/favorite")
    public ResponseEntity<MessageResponse<String>> removePostFromFavorites(@PathVariable Long postId) {
        postService.removePostFromFavorites(postId);
        return ResponseEntity.ok(
                MessageResponse.<String>builder()
                        .message("Post removed from favorites successfully")
                        .build()
        );
    }

    @GetMapping("/favorites")
    public ResponseEntity<MessageResponse<List<PostResponse>>> getUserFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<PostResponse> favoritePosts = postService.getUserFavorites(page, size);
        return ResponseEntity.ok(
                MessageResponse.<List<PostResponse>>builder()
                        .message("Favorite posts retrieved successfully")
                        .data(favoritePosts)
                        .build()
        );
    }
}