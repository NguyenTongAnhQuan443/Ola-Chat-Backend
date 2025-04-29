package vn.edu.iuh.fit.olachatbackend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PostResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Like;
import vn.edu.iuh.fit.olachatbackend.entities.Media;
import vn.edu.iuh.fit.olachatbackend.entities.Post;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.mappers.PostMapper;
import vn.edu.iuh.fit.olachatbackend.repositories.LikeRepository;
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
    private final PostMapper postMapper;
    private final LikeRepository likeRepository;

    public PostController(PostService postService, MediaService mediaService, PostMapper postMapper, LikeRepository likeRepository) {
        this.postService = postService;
        this.mediaService = mediaService;
        this.postMapper = postMapper;
        this.likeRepository = likeRepository;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Post> createPost(
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

        Post createdPost = postService.createPost(content, privacy, mediaList);
        return ResponseEntity.ok(createdPost);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long postId) {
        Post post = postService.getPostById(postId);

        // Fetch all users who liked the post
        List<User> likedUsers = likeRepository.findAllByPost(post).stream()
                .map(Like::getLikedBy)
                .toList();

        // Map the post to PostResponse
        PostResponse postResponse = postMapper.toPostResponse(post);
        postResponse.setLikedUsers(likedUsers);

        return ResponseEntity.ok(postResponse);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        var posts = postService.getAllPosts();
        var postResponses = postMapper.toPostResponseList(posts);
        return ResponseEntity.ok(postResponses);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<List<PostResponse>> deletePostAndReturnRemaining(@PathVariable Long postId) throws IOException {
        List<Post> remainingPosts = postService.deletePostByIdAndReturnRemaining(postId);
        List<PostResponse> postResponses = postMapper.toPostResponseList(remainingPosts);
        return ResponseEntity.ok(postResponses);
    }

    @PutMapping(value = "/{postId}", consumes = "multipart/form-data")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "filesToDelete", required = false) List<String> filesToDelete,
            @RequestParam(value = "newFiles", required = false) List<MultipartFile> newFiles) throws IOException {

        Post updatedPost = postService.updatePost(postId, content, filesToDelete, newFiles);
        PostResponse postResponse = postMapper.toPostResponse(updatedPost);
        return ResponseEntity.ok(postResponse);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<PostResponse> likePost(@PathVariable Long postId) {
        PostResponse postResponse = postService.likePost(postId);
        return ResponseEntity.ok(postResponse);
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<PostResponse> toggleLikePost(@PathVariable Long postId) {
        PostResponse postResponse = postService.toggleLikePost(postId);
        return ResponseEntity.ok(postResponse);
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<PostResponse> addCommentToPost(
            @PathVariable Long postId,
            @RequestParam("content") String content) {
        PostResponse postResponse = postService.addCommentToPost(postId, content);
        return ResponseEntity.ok(postResponse);
    }
}