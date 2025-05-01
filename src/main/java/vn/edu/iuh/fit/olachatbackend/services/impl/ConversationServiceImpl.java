/*
 * @ (#) ConversationServiceImpl.java       1.0     14/02/2025
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
import vn.edu.iuh.fit.olachatbackend.dtos.ConversationDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.ConversationResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Conversation;
import vn.edu.iuh.fit.olachatbackend.entities.LastMessage;
import vn.edu.iuh.fit.olachatbackend.entities.Message;
import vn.edu.iuh.fit.olachatbackend.entities.Participant;
import vn.edu.iuh.fit.olachatbackend.enums.MessageType;
import vn.edu.iuh.fit.olachatbackend.enums.ParticipantRole;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.mappers.ConversationMapperImpl;
import vn.edu.iuh.fit.olachatbackend.mappers.UserMapper;
import vn.edu.iuh.fit.olachatbackend.repositories.ConversationRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.MessageRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.ParticipantRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.UserRepository;
import vn.edu.iuh.fit.olachatbackend.services.ConversationService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ConversationMapperImpl conversationMapperImpl;

    public ConversationDTO createConversation(ConversationDTO conversationDTO) {
        Conversation conversation = Conversation.builder()
                .name(conversationDTO.getName())
                .avatar(conversationDTO.getAvatar())
                .type(conversationDTO.getType())
                .lastMessage(conversationDTO.getLastMessage())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Conversation savedConversation = conversationRepository.save(conversation);

        List<Participant> participants = conversationDTO.getUserIds().stream()
                .map(userId -> Participant.builder()
                        .conversationId(savedConversation.getId())
                        .userId(userId)
                        .role(ParticipantRole.MEMBER)
                        .joinedAt(LocalDateTime.now())
                        .build())
                .toList();
        participantRepository.saveAll(participants);

        return conversationMapperImpl.toDTO(savedConversation);
    }

    @Override
    public List<ConversationResponse> getAllConversationsByUserId(String userId) {
        // Lấy tất cả participants liên quan đến userId đã cho
        List<Participant> participants = participantRepository.findByUserId(userId);

        // Lấy danh sách conversationId từ participants
        List<ObjectId> conversationIds = participants.stream()
                .map(Participant::getConversationId)
                .distinct() // Đảm bảo loại bỏ trùng lặp nếu có
                .toList();

        // Lấy tất cả các cuộc trò chuyện từ danh sách conversationIds
        List<Conversation> conversations = conversationRepository.findByIdIn(conversationIds);

        // Tiến hành xây dựng danh sách ConversationDTO
        return conversations.stream().map(conversation -> {
            // Lấy tất cả participants của mỗi conversation
            List<Participant> participantsInConversation = participantRepository.findByConversationId(conversation.getId());

            // Lấy danh sách userIds từ participants trong cuộc trò chuyện này
            List<String> userIds = participantsInConversation.stream()
                    .map(Participant::getUserId)
                    .distinct() // Loại bỏ trùng lặp nếu có
                    .toList();

            List<UserResponse> userResponses = userMapper.toUserResponseList(userRepository.findByIdIn(userIds));

            return ConversationResponse.builder()
                    .id(conversation.getId() != null ? conversation.getId().toHexString() : null)
                    .name(conversation.getName())
                    .avatar(conversation.getAvatar())
                    .type(conversation.getType())
                    .lastMessage(conversation.getLastMessage())
                    .createdAt(conversation.getCreatedAt())
                    .updatedAt(conversation.getUpdatedAt())
                    .users(userResponses)
                    .build();
        }).toList();
    }

    @Override
    public void sendSystemMessageAndUpdateLast(String conversationId, String content) {
        // Create system message
        Message systemMessage = Message.builder()
                .conversationId(new ObjectId(conversationId))
                .senderId(null)
                .content(content)
                .createdAt(LocalDateTime.now())
                .type(MessageType.SYSTEM)
                .build();

        // Save message
        messageRepository.save(systemMessage);

        // Update lastMessage for conversation
        Conversation conversation = conversationRepository.findById(new ObjectId(conversationId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy conversation!"));

        // Set lastMessage
        conversation.setLastMessage(LastMessage.builder()
                        .messageId(systemMessage.getId())
                        .content(content)
                        .createdAt(LocalDateTime.now())
                        .senderId(null)
                .build());

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    @Override
    public void updateLastMessage(ObjectId conversationId, Message newLastMessage) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy conversation!"));

        conversation.setLastMessage(LastMessage.builder()
                .messageId(newLastMessage.getId())
                .content(getLastMessagePreview(newLastMessage))
                .createdAt(newLastMessage.getCreatedAt())
                .senderId(newLastMessage.getSenderId())
                .build());

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    private String getLastMessagePreview(Message message) {
        if (message.isRecalled()) {
            return "[Tin nhắn đã thu hồi]";
        }

        return switch (message.getType()) {
            case TEXT, SYSTEM -> message.getContent();
            case MEDIA -> "[Đã gửi ảnh]";
            case FILE -> "[Đã gửi tệp tin]";
            case STICKER -> "[Sticker]";
            case EMOJI -> message.getContent(); // emoji unicode
            case VOICE -> "[Tin nhắn thoại]";
            default -> "[Tin nhắn]";
        };
    }

}
