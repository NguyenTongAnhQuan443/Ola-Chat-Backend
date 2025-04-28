package vn.edu.iuh.fit.olachatbackend.services.impl;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.entities.Media;
import vn.edu.iuh.fit.olachatbackend.entities.Post;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.enums.Privacy;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.repositories.PostRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.UserRepository;
import vn.edu.iuh.fit.olachatbackend.services.MediaService;
import vn.edu.iuh.fit.olachatbackend.services.PostService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MediaService mediaService;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository, MediaService mediaService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.mediaService = mediaService;
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
}