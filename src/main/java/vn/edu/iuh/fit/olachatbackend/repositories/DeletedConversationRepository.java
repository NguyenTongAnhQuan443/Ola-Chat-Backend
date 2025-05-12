package vn.edu.iuh.fit.olachatbackend.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.edu.iuh.fit.olachatbackend.entities.DeletedConversation;

import java.util.List;

public interface DeletedConversationRepository extends MongoRepository<DeletedConversation, String> {
    List<DeletedConversation> findByUserId(String userId);
    boolean existsByUserIdAndConversationId(String userId, String conversationId);
}