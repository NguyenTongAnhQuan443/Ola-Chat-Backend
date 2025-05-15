package vn.edu.iuh.fit.olachatbackend.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.olachatbackend.entities.Favorite;
import vn.edu.iuh.fit.olachatbackend.entities.Post;
import vn.edu.iuh.fit.olachatbackend.entities.User;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByPostAndUser(Post post, User user);
    Optional<Favorite> findByPostAndUser(Post post, User user);
    List<Favorite> findAllByUser(User user, Pageable pageable);
}