package vn.edu.iuh.fit.olachatbackend.services;

import vn.edu.iuh.fit.olachatbackend.entities.Media;
import vn.edu.iuh.fit.olachatbackend.entities.Post;

import java.util.List;

public interface PostService {
    Post createPost(String content, String privacy, List<Media> mediaList);
    Post getPostById(Long postId);
    List<Post> getAllPosts();
}