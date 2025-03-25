/*
 * @ (#) ConversationRepository.java       1.0     14/02/2025
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
import vn.edu.iuh.fit.olachatbackend.entities.Conversation;

import java.util.List;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, ObjectId> {
//    List<Conversation> findByParticipantsContaining(Long userId);
    List<Conversation> findByIdIn(List<ObjectId> ids);
}