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
import vn.edu.iuh.fit.olachatbackend.entities.Participant;
import vn.edu.iuh.fit.olachatbackend.enums.ParticipantRole;
import vn.edu.iuh.fit.olachatbackend.mappers.UserMapper;
import vn.edu.iuh.fit.olachatbackend.repositories.ConversationRepository;
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

        return conversationDTO;
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

}
