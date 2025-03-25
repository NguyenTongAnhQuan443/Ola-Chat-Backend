/*
 * @ (#) MessageServiceImpl.java       1.0     14/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services.impl;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/02/2025
 * @version:    1.0
 */

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.dtos.MessageDTO;
import vn.edu.iuh.fit.olachatbackend.entities.Message;
import vn.edu.iuh.fit.olachatbackend.repositories.MessageRepository;
import vn.edu.iuh.fit.olachatbackend.services.MessageService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;

    @Override
    public MessageDTO save(MessageDTO messageDTO) {
        Message message = Message.builder()
                .senderId(messageDTO.getSenderId())
                .conversationId(new ObjectId(messageDTO.getConversationId()))
                .content(messageDTO.getContent())
                .type(messageDTO.getType())
                .mediaUrl(messageDTO.getMediaUrl())
                .status(messageDTO.getStatus())
                .deliveryStatus(messageDTO.getDeliveryStatus())
                .readStatus(messageDTO.getReadStatus())
                .createdAt(LocalDateTime.now())
                .build();
        messageRepository.save(message);
        return messageDTO;
    }

    public List<MessageDTO> getMessagesByConversationId(String conversationId) {
        List<Message> messages = messageRepository.findByConversationId(new ObjectId(conversationId));

        return messages.stream().map(msg -> MessageDTO.builder()
                .id(msg.getId().toHexString())
                .senderId(msg.getSenderId())
                .conversationId(msg.getConversationId().toHexString())
                .content(msg.getContent())
                .type(msg.getType())
                .mediaUrl(msg.getMediaUrl())
                .status(msg.getStatus())
                .deliveryStatus(msg.getDeliveryStatus())
                .readStatus(msg.getReadStatus())
                .createdAt(msg.getCreatedAt())
                .build()).toList();
    }
}
