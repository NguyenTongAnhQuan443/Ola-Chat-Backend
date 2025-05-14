package vn.edu.iuh.fit.olachatbackend.services;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.CommentHierarchyResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PostResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserPostsResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Media;
import vn.edu.iuh.fit.olachatbackend.entities.Post;

import java.io.IOException;
import java.util.List;

public interface PostService {
    PostResponse createPost(String content, String privacy, List<Media> mediaList);
    PostResponse getPostById(Long postId);
    UserPostsResponse getUserPosts(int page, int size);
    void deletePostById(Long postId) throws IOException;
    PostResponse updatePost(Long postId, String content, List<String> filesToDelete, List<MultipartFile> newFiles) throws IOException;
    PostResponse likePost(Long postId);
    PostResponse toggleLikePost(Long postId);
    PostResponse addCommentToPost(Long postId, String content);
    List<CommentHierarchyResponse> getCommentHierarchy(Long postId);
    List<CommentHierarchyResponse> deleteComment(Long commentId);
    List<CommentHierarchyResponse> addReplyToComment(Long commentId, String content);
    CommentHierarchyResponse updateComment(Long commentId, String content);
    PostResponse sharePost(Long postId, String content);
    List<PostResponse> getFeed(int page, int size);
    List<PostResponse> getUserProfilePosts(String userId, int page, int size);
}