/*
 * @ (#) ParticipantRepository.java       1.0     14/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.repositories;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/02/2025
 * @version:    1.0
 */

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.olachatbackend.entities.Participant;
import vn.edu.iuh.fit.olachatbackend.enums.ParticipantRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends MongoRepository<Participant, ObjectId> {
    List<Participant> findByUserId(String userId);
    List<Participant> findParticipantByConversationId(ObjectId conversationId);
    Optional<Participant> findByConversationIdAndUserId(ObjectId groupId, String userId);
    void deleteByConversationId(ObjectId groupId);
    boolean existsByConversationIdAndUserId(ObjectId groupId, String userId);

    List<Participant> findByConversationId(ObjectId groupId);

    long countByConversationId(ObjectId conversationId);
    long countByConversationIdAndRole(ObjectId conversationId, ParticipantRole role);
}
