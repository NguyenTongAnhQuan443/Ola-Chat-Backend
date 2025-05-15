package vn.edu.iuh.fit.olachatbackend.repositories;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.iuh.fit.olachatbackend.entities.Post;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.enums.Privacy;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.createdBy.id = :userId OR p.createdBy.id IN :friendIds ORDER BY p.createdAt DESC")
    List<Post> findFeedPosts(@Param("userId") String userId, @Param("friendIds") List<String> friendIds, Pageable pageable);

    Page<Post> findByCreatedBy(User user, Pageable pageable);

    Page<Post> findByCreatedByAndPrivacy(User user, Privacy privacy, Pageable pageable);

    Page<Post> findByCreatedByAndPrivacyIn(User user, List<Privacy> privacies, Pageable pageable);

    List<Post> findByOriginalPost(Post originalPost);

    Page<Post> findByContentContainingIgnoreCase(String keyword, Pageable pageable);
}