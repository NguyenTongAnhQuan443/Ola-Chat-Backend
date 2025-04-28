package vn.edu.iuh.fit.olachatbackend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PostResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Media;
import vn.edu.iuh.fit.olachatbackend.entities.Post;
import vn.edu.iuh.fit.olachatbackend.mappers.PostMapper;
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

    public PostController(PostService postService, MediaService mediaService, PostMapper postMapper) {
        this.postService = postService;
        this.mediaService = mediaService;
        this.postMapper = postMapper;
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
        var post = postService.getPostById(postId);
        var postResponse = postMapper.toPostResponse(post);
        return ResponseEntity.ok(postResponse);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        var posts = postService.getAllPosts();
        var postResponses = postMapper.toPostResponseList(posts);
        return ResponseEntity.ok(postResponses);
    }
}