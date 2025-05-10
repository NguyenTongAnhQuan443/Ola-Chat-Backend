/*
 * @ (#) ConversationService.java       1.0     14/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/02/2025
 * @version:    1.0
 */

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.olachatbackend.dtos.ConversationDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.ConversationResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Conversation;
import vn.edu.iuh.fit.olachatbackend.entities.Message;

import java.util.List;

public interface ConversationService {
    ConversationDTO createConversation(ConversationDTO conversationDTO);
    List<ConversationResponse> getAllConversationsByUser();
    void sendSystemMessageAndUpdateLast(String conversationId, String content);
    void updateLastMessage(ObjectId conversationId, Message newLastMessage);
    void softDeleteConversation(String userId, String conversationId);

}
