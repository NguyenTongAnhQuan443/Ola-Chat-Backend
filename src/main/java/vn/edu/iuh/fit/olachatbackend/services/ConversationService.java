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

import vn.edu.iuh.fit.olachatbackend.dtos.ConversationDTO;
import vn.edu.iuh.fit.olachatbackend.entities.Conversation;

import java.util.List;

public interface ConversationService {
    Conversation createConversation(ConversationDTO conversationDTO);
    List<Conversation> getAllConversationsByUserId(Long userId);
}
