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
import vn.edu.iuh.fit.olachatbackend.entities.Conversation;
import vn.edu.iuh.fit.olachatbackend.entities.Participant;
import vn.edu.iuh.fit.olachatbackend.enums.ParticipantRole;
import vn.edu.iuh.fit.olachatbackend.repositories.ConversationRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.ParticipantRepository;
import vn.edu.iuh.fit.olachatbackend.services.ConversationService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;

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
    public List<ConversationDTO> getAllConversationsByUserId(String userId) {
        List<Participant> participants = participantRepository.findByUserId(userId);

        List<ObjectId> conversationIds = participants.stream()
                .map(Participant::getConversationId)
                .toList();

        List<Conversation> conversations = conversationRepository.findByIdIn(conversationIds);

        return conversations.stream().map(conversation -> ConversationDTO.builder()
                        .id(conversation.getId() != null ? conversation.getId().toHexString() : null)
                        .name(conversation.getName())
                        .avatar(conversation.getAvatar())
                        .type(conversation.getType())
                        .lastMessage(conversation.getLastMessage())
                        .createdAt(conversation.getCreatedAt())
                        .updatedAt(conversation.getUpdatedAt())
                        .userIds(participants.stream()
                                .map(Participant::getUserId)
                                .distinct()
                                .toList())
                        .build())
                .toList();
    }
}
