package vn.edu.iuh.fit.olachatbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.olachatbackend.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
  Optional<User> findByUsername(String username);
  Boolean existsByUsername(String username);
  Optional<User> findByEmail(String email);
  List<User> findByIdIn(List<String> ids);
  boolean existsByEmail(String email);

}