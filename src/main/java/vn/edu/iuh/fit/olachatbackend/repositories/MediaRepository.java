package vn.edu.iuh.fit.olachatbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.olachatbackend.entities.Media;
import vn.edu.iuh.fit.olachatbackend.entities.User;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByUploadedByOrderByUploadedAtDescPost_PostIdAsc(User user);
}