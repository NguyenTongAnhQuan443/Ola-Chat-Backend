/*
 * @ (#) MessageRepository.java       1.0     14/02/2025
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.olachatbackend.entities.Message;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, ObjectId> {
    List<Message> findByConversationId(ObjectId conversationId, Pageable pageable);
    Message findTopByConversationIdOrderByCreatedAtDesc(ObjectId conversationId);
}
