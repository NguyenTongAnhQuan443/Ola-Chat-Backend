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
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MediaMessageResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Conversation;
import vn.edu.iuh.fit.olachatbackend.entities.LastMessage;
import vn.edu.iuh.fit.olachatbackend.entities.Message;
import vn.edu.iuh.fit.olachatbackend.entities.ReadStatus;
import vn.edu.iuh.fit.olachatbackend.enums.MessageStatus;
import vn.edu.iuh.fit.olachatbackend.enums.MessageType;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.repositories.MessageRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.ParticipantRepository;
import vn.edu.iuh.fit.olachatbackend.services.MessageService;


import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final MongoTemplate mongoTemplate;
    private final ParticipantRepository participantRepository;

    @Override
    public MessageDTO save(MessageDTO messageDTO) {
        Message message = Message.builder()
                .senderId(messageDTO.getSenderId())
                .conversationId(new ObjectId(messageDTO.getConversationId()))
                .content(messageDTO.getContent())
                .type(messageDTO.getType())
                .mediaUrls(messageDTO.getMediaUrls())
                .status(MessageStatus.SENT)
                .deliveryStatus(messageDTO.getDeliveryStatus())
                .readStatus(messageDTO.getReadStatus())
                .createdAt(LocalDateTime.now())
                .recalled(messageDTO.isRecalled())
                .build();
        Message savedMessage = messageRepository.save(message);

        updateLastMessage(savedMessage);
        return messageDTO;
    }

    private void updateLastMessage(Message message) {
        LastMessage lastMessage = LastMessage.builder()
                .messageId(message.getId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .senderId(message.getSenderId())
                .build();

        Query query = new Query(Criteria.where("_id").is(message.getConversationId()));
        Update update = new Update().set("lastMessage", lastMessage);
        mongoTemplate.updateFirst(query, update, Conversation.class);
    }

    public List<MessageDTO> getMessagesByConversationId(String conversationId) {
        List<Message> messages = messageRepository.findByConversationId(new ObjectId(conversationId));

        return messages.stream().map(msg -> MessageDTO.builder()
                .id(msg.getId().toHexString())
                .senderId(msg.getSenderId())
                .conversationId(msg.getConversationId().toHexString())
                .content(msg.getContent())
                .type(msg.getType())
                .mediaUrls(msg.getMediaUrls())
                .status(msg.getStatus())
                .deliveryStatus(msg.getDeliveryStatus())
                .readStatus(msg.getReadStatus())
                .createdAt(msg.getCreatedAt())
                .recalled(msg.isRecalled())
                .build()).toList();
    }

    public MessageDTO recallMessage(String messageId, String senderId) {
        System.out.println("Mess" + messageId);
//         Kiểm tra định dạng Message ID
        if (messageId == null || !messageId.matches("[0-9a-fA-F]{24}")) {
            throw new IllegalArgumentException("Message ID must be a valid 24-character hex string.");
        }

        // Chuyển messageId thành ObjectId
        ObjectId objectId = new ObjectId(messageId);

        // Tìm tin nhắn trong cơ sở dữ liệu
        Message message = messageRepository.findById(objectId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Kiểm tra xem người gửi có quyền thu hồi tin nhắn này
        if (!message.getSenderId().equals(senderId)) {
            throw new RuntimeException("Only sender can recall this message");
        }

        // Nếu tin nhắn chưa được thu hồi thì thực hiện thu hồi
        if (!message.isRecalled()) {
            message.setRecalled(true);
            message.setContent("Tin nhắn đã được thu hồi");
            message.setMediaUrls(null);  // Nếu là tin nhắn media thì xóa URL
            messageRepository.save(message);
        }

//         Trả về MessageDTO với trạng thái tin nhắn đã thu hồi
        return MessageDTO.builder()
                .id(message.getId().toHexString())
                .senderId(message.getSenderId())
                .conversationId(message.getConversationId().toHexString())
                .content(message.getContent())
                .type(message.getType())
                .mediaUrls(null)
                .status(message.getStatus())
                .deliveryStatus(message.getDeliveryStatus())
                .readStatus(message.getReadStatus())
                .recalled(true)
                .createdAt(message.getCreatedAt())
                .build();
    }

    @Override
    public List<MediaMessageResponse> getMediaMessages(String conversationId, String senderId) {
        return getMessagesByTypes(conversationId, senderId, List.of(MessageType.MEDIA));
    }

    @Override
    public List<MediaMessageResponse> getFileMessages(String conversationId, String senderId) {
        return getMessagesByTypes(conversationId, senderId, List.of(MessageType.FILE));
    }

    @Override
    public void markMessageAsRead(String messageId, String userId) {
        Message message = messageRepository.findById(new ObjectId(messageId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy message"));

        List<ReadStatus> readStatusList = message.getReadStatus();
        if (readStatusList == null) {
            readStatusList = new ArrayList<>();
            message.setReadStatus(readStatusList);
        }

        boolean alreadyRead = readStatusList.stream()
                .anyMatch(rs -> rs.getUserId().equals(userId));

        if (alreadyRead) return;

        // Thêm trạng thái đã đọc
        readStatusList.add(new ReadStatus(userId, LocalDateTime.now()));

        // Lấy tổng số người trong cuộc trò chuyện
        long participantCount = participantRepository.countByConversationId(message.getConversationId());

        // Nếu là chat đơn → set READ ngay khi có 1 người đọc
        if (participantCount == 2) {
            message.setStatus(MessageStatus.READ);
        } else {
            // Chat nhóm → kiểm tra nếu tất cả đã đọc
            Set<String> readUserIds = message.getReadStatus().stream()
                    .map(ReadStatus::getUserId)
                    .collect(Collectors.toSet());

            if (readUserIds.size() == participantCount) {
                message.setStatus(MessageStatus.READ);
            }
        }

        messageRepository.save(message);
    }


    // Common method
    private List<MediaMessageResponse> getMessagesByTypes(String conversationId, String senderId, List<MessageType> types) {
        Criteria criteria = Criteria.where("conversationId").is(new ObjectId(conversationId))
                .and("type").in(types);

        if (senderId != null && !senderId.isEmpty()) {
            criteria.and("senderId").is(senderId);
        }

        Query query = new Query(criteria);
        List<Message> messages = mongoTemplate.find(query, Message.class);

        return convertToResponse(messages);
    }

    // Convert method
    private List<MediaMessageResponse> convertToResponse(List<Message> messages) {
        return messages.stream()
                .map(msg -> MediaMessageResponse.builder()
                        .id(msg.getId().toString())
                        .mediaUrls(msg.getMediaUrls())
                        .type(msg.getType().getValue())
                        .senderId(msg.getSenderId())
                        .createdAt(msg.getCreatedAt())
                        .build())
                .toList();
    }



}
