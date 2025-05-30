package vn.edu.iuh.fit.olachatbackend.services;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.*;
import vn.edu.iuh.fit.olachatbackend.entities.Media;

import java.io.IOException;
import java.util.List;

public interface PostService {
    PostResponse createPost(String content, String privacy, List<Media> mediaList);
    PostResponse getPostById(Long postId);
    UserPostsResponse getUserPosts(int page, int size);
    List<PostResponse> getUserPosts_v2(int page, int size);
    void deletePostById(Long postId) throws IOException;
    PostResponse updatePost(Long postId, String content, List<String> filesToDelete, List<MultipartFile> newFiles) throws IOException;
    void likePost(Long postId);
    boolean toggleLikePost(Long postId);
    List<CommentHierarchyResponse> addCommentToPost(Long postId, String content);
    List<CommentHierarchyResponse> getCommentHierarchy(Long postId);
    List<CommentHierarchyResponse> deleteComment(Long commentId);
    List<CommentHierarchyResponse> addReplyToComment(Long commentId, String content);
    CommentHierarchyResponse updateComment(Long commentId, String content);
    PostResponse sharePost(Long postId, String content, String privacy);
    List<ShareResponse> getPostShares(Long postId);
    List<PostResponse> getFeed(int page, int size);
    UserPostsResponse getUserProfilePosts(String userId, int page, int size);
    List<PostResponse> getUserProfilePosts_v2(String userId, int page, int size);
    List<PostUserResponse> getPostLikes(Long postId);
    List<PostResponse> searchPosts(String keyword, int page, int size);
    PostResponse updatePostPrivacy(Long postId, String privacy);
    void addPostToFavorites(Long postId);
    void removePostFromFavorites(Long postId);
    List<PostResponse> getUserFavorites(int page, int size);
}