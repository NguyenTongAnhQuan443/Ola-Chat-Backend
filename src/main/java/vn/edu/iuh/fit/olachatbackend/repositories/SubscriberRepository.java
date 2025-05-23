package vn.edu.iuh.fit.olachatbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.olachatbackend.models.Subscriber;

import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    Optional<Subscriber> findByEmail(String email);
    void deleteByEmail(String email);
}
