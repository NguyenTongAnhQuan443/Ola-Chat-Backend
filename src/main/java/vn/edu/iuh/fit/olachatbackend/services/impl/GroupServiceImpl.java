/*
 * @ (#) GroupServiceImpl.java       1.0     28/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services.impl;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 28/02/2025
 * @version:    1.0
 */

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.dtos.ConversationDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.AddMemberRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.GroupUpdateRequest;
import vn.edu.iuh.fit.olachatbackend.entities.Conversation;
import vn.edu.iuh.fit.olachatbackend.entities.LastMessage;
import vn.edu.iuh.fit.olachatbackend.entities.Message;
import vn.edu.iuh.fit.olachatbackend.entities.Participant;
import vn.edu.iuh.fit.olachatbackend.enums.ConversationType;
import vn.edu.iuh.fit.olachatbackend.enums.ParticipantRole;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.mappers.ConversationMapper;
import vn.edu.iuh.fit.olachatbackend.repositories.ConversationRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.MessageRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.ParticipantRepository;
import vn.edu.iuh.fit.olachatbackend.services.GroupService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;
    private final ConversationMapper conversationMapper;
    private final MessageRepository messageRepository;

    @Override
    public ConversationDTO createGroup(String creatorId, String name, String avatar, List<String> userIds) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhóm không được để trống");
        }
        if (userIds == null || userIds.size() < 2) {
            throw new IllegalArgumentException("Nhóm phải có ít nhất 3 thành viên");
        }

        // Create group
        Conversation group = Conversation.builder()
                .name(name)
                .avatar(avatar) // Avatar can allow null
                .type(ConversationType.GROUP)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Conversation savedGroup = conversationRepository.save(group);

        List<Participant> participants = new ArrayList<>();

        // Assign moderator to creator
        participants.add(Participant.builder()
                .conversationId(savedGroup.getId())
                .userId(creatorId)
                .role(ParticipantRole.MODERATOR)
                .joinedAt(LocalDateTime.now())
                .build());

        // Add members into group
        for (String userId : userIds) {
            participants.add(Participant.builder()
                    .conversationId(savedGroup.getId())
                    .userId(userId)
                    .role(ParticipantRole.MEMBER)
                    .joinedAt(LocalDateTime.now())
                    .build());
        }

        participantRepository.saveAll(participants);

        return conversationMapper.toDTO(savedGroup);
    }

    @Override
    public ConversationDTO getGroupById(ObjectId id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nhóm không tồn tại"));

        // Get list userId from Participant
        List<String> userIds = participantRepository.findParticipantByConversationId(id)
                .stream().map(Participant::getUserId).collect(Collectors.toList());

        // Get last message
        if (conversation.getLastMessage() == null) {
            Message lastMessage = messageRepository.findTopByConversationIdOrderByCreatedAtDesc(id);
            if (lastMessage != null) {
                conversation.setLastMessage(
                        LastMessage.builder()
                                .messageId(lastMessage.getId())
                                .content(lastMessage.getContent())
                                .createdAt(lastMessage.getCreatedAt())
                                .build()
                );
                conversationRepository.save(conversation);
            }
        }

        // Convert to DTO
        ConversationDTO conversationDTO = conversationMapper.toDTO(conversation);
        conversationDTO.setUserIds(userIds);

        return conversationDTO;
    }

    @Override
    public void updateGroup(ObjectId groupId, String userId, GroupUpdateRequest request) {
        Conversation group = conversationMapper.toEntity(getGroupById(groupId));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            group.setName(request.getName());
        }
        if (request.getAvatar() != null) {
            group.setAvatar(request.getAvatar());
        }
        group.setUpdatedAt(LocalDateTime.now());

        conversationMapper.toDTO(conversationRepository.save(group));
    }

    @Override
    public void deleteGroup(ObjectId groupId, String userId) {
        validateOwner(groupId, userId);
        conversationRepository.deleteById(groupId);
        participantRepository.deleteByConversationId(groupId);
    }

    @Override
    public void joinGroup(ObjectId groupId, String userId) {
        if (participantRepository.existsByConversationIdAndUserId(groupId, userId)) {
            throw new IllegalStateException("Bạn đã tham gia nhóm này rồi.");
        }

        participantRepository.save(new Participant(
                new ObjectId(), groupId, userId, ParticipantRole.MEMBER, LocalDateTime.now()
        ));
    }

    @Override
    public void leaveGroup(ObjectId groupId, String userId) {
        Participant participant = participantRepository.findByConversationIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalStateException("Bạn không ở trong nhóm này."));

        if (participant.getRole() == ParticipantRole.MODERATOR) {
            throw new IllegalStateException("Nhóm trưởng không thể rời nhóm.");
        }

        participantRepository.delete(participant);
    }

    @Override
    public void addMembers(ObjectId groupId, String ownerId, AddMemberRequest request) {
        Conversation group = conversationMapper.toEntity(getGroupById(groupId));

        List<Participant> existingMembers = participantRepository.findByConversationId(groupId);
        Set<String> existingUserIds = existingMembers.stream().map(Participant::getUserId).collect(Collectors.toSet());

        List<Participant> newMembers = request.getUserIds().stream()
                .filter(userId -> !existingUserIds.contains(userId)) // Remove user existed in group
                .map(userId -> Participant.builder()
                        .conversationId(groupId)
                        .userId(userId)
                        .role(ParticipantRole.MEMBER)
                        .joinedAt(LocalDateTime.now())
                        .build()
                ).collect(Collectors.toList());

        if (newMembers.isEmpty()) {
            throw new BadRequestException("Thành viên đã có trong nhóm");
        }

        participantRepository.saveAll(newMembers);
        group.setUpdatedAt(LocalDateTime.now());
        conversationMapper.toDTO(conversationRepository.save(group));
    }

    @Override
    public void removeUserFromGroup(ObjectId groupId, String userId, String requesterId) {
        // search
        Conversation conversation = conversationRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Nhóm không tồn tại"));

        // Check if user exist in group
        Participant participant = participantRepository.findByConversationIdAndUserId(groupId, userId)
                .orElseThrow(() -> new NotFoundException("Thành viên không tồn tại trong nhóm"));

        validateOwner(groupId, requesterId);

        // remove
        participantRepository.delete(participant);
    }

    private void validateOwner(ObjectId groupId, String userId) {
        Participant participant = participantRepository.findByConversationIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalStateException("Bạn không thuộc nhóm này."));
        if (participant.getRole() != ParticipantRole.MODERATOR) {
            throw new AccessDeniedException("Chỉ nhóm trưởng mới có quyền thực hiện hành động này.");
        }
    }
}
