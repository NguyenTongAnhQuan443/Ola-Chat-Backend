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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.dtos.MessageDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MediaMessageResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.SenderInfoResponse;
import vn.edu.iuh.fit.olachatbackend.entities.*;
import vn.edu.iuh.fit.olachatbackend.enums.ConversationType;
import vn.edu.iuh.fit.olachatbackend.enums.MessageStatus;
import vn.edu.iuh.fit.olachatbackend.enums.MessageType;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.repositories.ConversationRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.MessageRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.ParticipantRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.UserRepository;
import vn.edu.iuh.fit.olachatbackend.services.ConversationService;
import vn.edu.iuh.fit.olachatbackend.services.MessageService;


import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final MongoTemplate mongoTemplate;
    private final ParticipantRepository participantRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ConversationService conversationService;

    @Override
    public MessageDTO save(MessageDTO messageDTO) {
        Message message = Message.builder()
                .senderId(messageDTO.getSenderId())
                .conversationId(new ObjectId(messageDTO.getConversationId()))
                .content(messageDTO.getContent())
                .type(messageDTO.getType())
                .mediaUrls(messageDTO.getMediaUrls())
                .status(MessageStatus.SENT)
                .deliveryStatus(messageDTO.getDeliveryStatus() == null ? new ArrayList<>() : messageDTO.getDeliveryStatus())
                .readStatus(messageDTO.getReadStatus() == null ? new ArrayList<>() : messageDTO.getReadStatus())
                .replyStatus(messageDTO.getReplyStatus() == null ? new ArrayList<>() : messageDTO.getReplyStatus())
                .deletedStatus(messageDTO.getDeletedStatus() == null ? new ArrayList<>() : messageDTO.getDeletedStatus())
                .createdAt(LocalDateTime.now())
                .recalled(messageDTO.isRecalled())
                .build();
        Message savedMessage = messageRepository.save(message);

        conversationService.updateLastMessage(message.getConversationId(), savedMessage);
        return messageDTO;
    }

//    private void updateLastMessage(Message message) {
//        LastMessage lastMessage = LastMessage.builder()
//                .messageId(message.getId())
//                .content(message.getContent())
//                .createdAt(message.getCreatedAt())
//                .senderId(message.getSenderId())
//                .build();
//
//        Query query = new Query(Criteria.where("_id").is(message.getConversationId()));
//        Update update = new Update().set("lastMessage", lastMessage);
//        mongoTemplate.updateFirst(query, update, Conversation.class);
//    }

    public List<MessageDTO> getMessagesByConversationId(String conversationId) {
        List<Message> messages = messageRepository.findByConversationId(new ObjectId(conversationId));

        return messages.stream()
                .map(message -> {
                    return MessageDTO.builder()
                            .id(message.getId().toHexString())
                            .senderId(message.getSenderId())
                            .conversationId(message.getConversationId().toHexString())
                            .content(message.getContent())
                            .type(message.getType())
                            .mediaUrls(message.getMediaUrls())
                            .status(message.getStatus())
                            .deliveryStatus(message.getDeliveryStatus() == null ? new ArrayList<>() : message.getDeliveryStatus())
                            .readStatus(message.getReadStatus() == null ? new ArrayList<>() : message.getReadStatus())
                            .replyStatus(message.getReplyStatus() == null ? new ArrayList<>() : message.getReplyStatus())
                            .deletedStatus(message.getDeletedStatus() == null ? new ArrayList<>() : message.getDeletedStatus())
                            .createdAt(message.getCreatedAt())
                            .recalled(message.isRecalled())
                            .build();
                })
                .toList();
    }

    private SenderInfoResponse toSenderInfo(String senderId) {
        User user = userRepository.findById(senderId).orElse(null);
        if (user == null) return null;
        return new SenderInfoResponse(user.getId(), user.getDisplayName(), user.getAvatar());
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

        // Update last message
        conversationService.updateLastMessage(message.getConversationId(), message);

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
    public void markMessageAsReceived(String messageId, String userId) {
        Message message = messageRepository.findById(new ObjectId(messageId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tin nhắn"));
        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy cuộc trò chuyện"));

        if (message.getDeliveryStatus() == null) {
            message.setDeliveryStatus(new ArrayList<>());
        }

        // Thêm người nhận vào deliveryStatus nếu chưa có
        if (message.getDeliveryStatus().stream()
                .noneMatch(ds -> ds.getUserId().equals(userId))) {
            message.getDeliveryStatus().add(DeliveryStatus.builder()
                    .userId(userId)
                    .deliveredAt(LocalDateTime.now())
                    .build());
        }

        // Nếu là chat đơn sẽ thay đổi trạng thái thành "RECEIVED"
        if (conversation.getType() == ConversationType.PRIVATE) {
            message.setStatus(MessageStatus.RECEIVED);
        }

        messageRepository.save(message);
    }

    @Override
    public void hiddenForUser(String messageId) {
        // Check message exists
        Message message = messageRepository.findById(new ObjectId(messageId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tin nhắn"));

        // Check if message in conversation
        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy cuộc trò chuyện"));

        User currentUser = getCurrentUser();

        // Check if user exist in conversation
        checkUserExistsInConversation(conversation.getId(), currentUser.getId());

        // hidden message
        Message.DeletedStatus deletedStatus = Message.DeletedStatus.builder()
                .userId(currentUser.getId())
                .deletedAt(LocalDateTime.now())
                .build();

        if (message.getDeletedStatus() == null) {
            message.setDeletedStatus(new ArrayList<>());
        }

        message.getDeletedStatus().add(deletedStatus);

        // save to database
        messageRepository.save(message);
    }

    @Override
    public void markMessageAsRead(String messageId, String userId) {
        Message message = messageRepository.findById(new ObjectId(messageId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy message"));

        if (message.getReadStatus() == null) {
            message.setReadStatus(new ArrayList<>());
        }

        if (message.getReadStatus().stream().anyMatch(rs -> rs.getUserId().equals(userId))) {
            return;
        }

        message.getReadStatus().add(new ReadStatus(userId, LocalDateTime.now()));

        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy cuộc trò chuyện"));

        if (conversation.getType() == ConversationType.PRIVATE) {
            message.setStatus(MessageStatus.READ);
        } else {
            // Đối với chat nhóm, kiểm tra xem tất cả người tham gia đã đọc chưa
            long participantCount = participantRepository.countByConversationId(message.getConversationId());
            boolean isAllRead = message.getReadStatus().size() == participantCount;

            // Nếu tất cả người tham gia đã đọc thì đánh dấu tin nhắn là đã đọc
            if (isAllRead) {
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

    private User getCurrentUser() {
        // Check user
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return userRepository.findByUsername(name)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
    }

    /**
     * Check if user exists in conversation or throw exception if not found
     * @param conversationId ID of the group
     * @param userId ID of the user
     * @throws BadRequestException if user is not in the group
     */
    private void checkUserExistsInConversation(ObjectId conversationId, String userId) {
        if (!participantRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new BadRequestException("Bạn không thuộc nhóm này.");
        }
    }

}
