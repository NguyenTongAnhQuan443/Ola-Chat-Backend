package vn.edu.iuh.fit.olachatbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.olachatbackend.entities.FriendRequest;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, String> {
}