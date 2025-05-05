/*
 * @ (#) MessageService.java       1.0     14/02/2025
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

import vn.edu.iuh.fit.olachatbackend.dtos.MessageResponseDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.MessageRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MediaMessageResponse;

import java.util.List;

public interface MessageService {
    List<MessageResponseDTO> getMessagesByConversationId(String conversationId);
    MessageRequest save(MessageRequest messageDTO);
    MessageRequest recallMessage(String messageId, String senderId);
    List<MediaMessageResponse> getMediaMessages(String conversationId, String senderId);
    List<MediaMessageResponse> getFileMessages(String conversationId, String senderId);

    void markMessageAsRead(String messageId, String userId);
    void markMessageAsReceived(String messageId, String userId);
}
