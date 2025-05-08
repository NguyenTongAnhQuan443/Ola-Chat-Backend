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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.dtos.ConversationDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.AddMemberRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.ChangeBackgroundRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.GroupUpdateRequest;
import vn.edu.iuh.fit.olachatbackend.entities.*;
import vn.edu.iuh.fit.olachatbackend.enums.ConversationType;
import vn.edu.iuh.fit.olachatbackend.enums.NotificationType;
import vn.edu.iuh.fit.olachatbackend.enums.ParticipantRole;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.mappers.ConversationMapper;
import vn.edu.iuh.fit.olachatbackend.repositories.ConversationRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.MessageRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.ParticipantRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.UserRepository;
import vn.edu.iuh.fit.olachatbackend.services.ConversationService;
import vn.edu.iuh.fit.olachatbackend.services.GroupService;
import vn.edu.iuh.fit.olachatbackend.services.NotificationService;

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
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final NotificationService notificationService;

    @Override
    public ConversationDTO createGroup(String name, String avatar, List<String> userIds) {
        User user = getCurrentUser();

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
                .userId(getCurrentUser().getId())
                .role(ParticipantRole.ADMIN)
                .joinedAt(LocalDateTime.now())
                .build());

        // Add members into group
        for (String userId : userIds) {
            // Check if userId is person who create group
            if (userId.equals(getCurrentUser().getId())) {
                continue;
            }

            participants.add(Participant.builder()
                    .conversationId(savedGroup.getId())
                    .userId(userId)
                    .role(ParticipantRole.MEMBER)
                    .joinedAt(LocalDateTime.now())
                    .build());
        }

        participantRepository.saveAll(participants);

        String systemMsg = user.getDisplayName() + " đã tạo nhóm";

        // Send system message for group creation
        conversationService.sendSystemMessageAndUpdateLast(
                savedGroup.getId().toString(),
                systemMsg
        );

        // Send notification to all members
        notificationService.notifyConversation(
                savedGroup.getId().toString(),
                user.getId(),
                savedGroup.getName(),
                systemMsg,
                NotificationType.GROUP
        );

        return conversationMapper.toDTO(savedGroup);
    }

    @Override
    public ConversationDTO getGroupById(ObjectId id) {
        Conversation conversation = findGroupById(id);

        // Get list participant
        List<Participant> participants = participantRepository.findParticipantByConversationId(id);

        // userIds
        List<String> userIds = participants.stream()
                .map(Participant::getUserId)
                .collect(Collectors.toList());

        // moderatorIds
        List<String> moderatorIds = participants.stream()
                .filter(p -> p.getRole() == ParticipantRole.MODERATOR)
                .map(Participant::getUserId)
                .collect(Collectors.toList());

        String adminId = participants.stream()
                .filter(p -> p.getRole() == ParticipantRole.ADMIN)
                .map(Participant::getUserId)
                .findFirst()
                .orElse(null);

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
        conversationDTO.setModeratorIds(moderatorIds);
        conversationDTO.setAdminId(adminId);

        return conversationDTO;
    }


    @Override
    public void updateGroup(ObjectId groupId, GroupUpdateRequest request) {
        Conversation group = findGroupById(groupId);
        User currentUser = getCurrentUser();
        String systemMessage = null;

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String oldName = group.getName();
            group.setName(request.getName());
            systemMessage = currentUser.getDisplayName() + " đã đổi tên nhóm từ " + oldName + " thành " + request.getName();
        }
        if (request.getAvatar() != null) {
            group.setAvatar(request.getAvatar());
            if (systemMessage == null) {
                systemMessage = currentUser.getDisplayName() + " đã thay đổi ảnh đại diện của nhóm";
            }
        }
        group.setUpdatedAt(LocalDateTime.now());
        conversationMapper.toDTO(conversationRepository.save(group));

        if (systemMessage != null) {
            conversationService.sendSystemMessageAndUpdateLast(groupId.toString(), systemMessage);

            // Send notification
            notificationService.notifyConversation(
                    groupId.toString(),
                    currentUser.getId(),
                    group.getName(),
                    systemMessage,
                    NotificationType.GROUP
            );
        }
    }

    @Override
    public void deleteGroup(ObjectId groupId) {
        validateOwner(groupId, getCurrentUser().getId());
        conversationRepository.deleteById(groupId);
        participantRepository.deleteByConversationId(groupId);
    }

    @Override
    public void joinGroup(ObjectId groupId) {
        User user = getCurrentUser();
        if (isUserInGroup(groupId, user.getId())) {
            throw new BadRequestException("Bạn đã tham gia nhóm này rồi.");
        }

        participantRepository.save(Participant.builder()
                .id(new ObjectId())
                .conversationId(groupId)
                .userId(user.getId())
                .role(ParticipantRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build());

        String systemMsg = user.getDisplayName() + " đã tham gia nhóm";
        Conversation group = findGroupById(groupId);

        // Send system message
        conversationService.sendSystemMessageAndUpdateLast(
                groupId.toString(),
                systemMsg
        );

        // Send notification
        notificationService.notifyConversation(
                groupId.toString(),
                user.getId(),
                group.getName(),
                systemMsg,
                NotificationType.GROUP
        );
    }

    @Override
    public void leaveGroup(ObjectId groupId) {
        User user = getCurrentUser();
        Participant participant = findParticipantInGroup(groupId, user.getId());

        if (participant.getRole() == ParticipantRole.ADMIN) {
            throw new BadRequestException("Nhóm trưởng không thể rời nhóm.");
        }

        participantRepository.delete(participant);
    }

    @Override
    public void addMembers(ObjectId groupId, AddMemberRequest request) {
        Conversation group = findGroupById(groupId);

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
        conversationRepository.save(group);
    }

    @Override
    public void removeUserFromGroup(ObjectId groupId, String userId) {
        // Find the group
        findGroupById(groupId);

        // Check if user exists in group
        Participant participant = findParticipantInGroup(groupId, userId);

        // Check remove user
        if (participant.getRole() == ParticipantRole.ADMIN) {
            throw new BadRequestException("Không thể xóa trưởng nhóm!");
        }

        validateOwnerOrModerator(groupId, getCurrentUser().getId(), participant);

        // remove
        participantRepository.delete(participant);
    }

    @Override
    public void transferGroupOwner(ObjectId groupId, String newOwnerId) {
        User user = getCurrentUser();

        Participant requester = findParticipantInGroup(groupId, user.getId());
        validateOwner(groupId, user.getId());

        Participant newOwner = findParticipantInGroup(groupId, newOwnerId);

        // Update role
        requester.setRole(ParticipantRole.MODERATOR);
        newOwner.setRole(ParticipantRole.ADMIN);

        participantRepository.save(requester);
        participantRepository.save(newOwner);
    }

    @Override
    public void setModerator(ObjectId groupId, String userId) {
        User user = getCurrentUser();

        Participant requester = findParticipantInGroup(groupId, user.getId());
        validateOwner(groupId, user.getId());

        Participant member = findParticipantInGroup(groupId, userId);

        if (member.getRole() == ParticipantRole.ADMIN) {
            throw new BadRequestException("Không thể gán quyền cho nhóm trưởng.");
        }

        // Check current number of moderator
        long currentDeputyCount = participantRepository.countByConversationIdAndRole(groupId, ParticipantRole.MODERATOR);
        if (currentDeputyCount >= 2) {
            throw new BadRequestException("Nhóm chỉ cho phép tối đa 2 phó nhóm.");
        }

        member.setRole(ParticipantRole.MODERATOR);
        participantRepository.save(member);
    }

    @Override
    public void removeModerator(ObjectId groupId, String userId) {
        User user = getCurrentUser();

        Participant requester = findParticipantInGroup(groupId, user.getId());

        validateOwner(groupId, user.getId());

        // Check moderator
        Participant deputy = findParticipantInGroup(groupId, userId);

        if (deputy.getRole() != ParticipantRole.MODERATOR) {
            throw new BadRequestException("Thành viên này không phải là phó nhóm.");
        }

        // Remove moderator role: return to MEMBER
        deputy.setRole(ParticipantRole.MEMBER);
        participantRepository.save(deputy);
    }


    @Override
    public void muteConversation(ObjectId groupId) {
        User user = getCurrentUser();
        Participant participant = findParticipantInGroup(groupId, user.getId());
        participant.setMuted(true);
        participantRepository.save(participant);
    }

    @Override
    public void unmuteConversation(ObjectId groupId) {
        User user = getCurrentUser();
        Participant participant = findParticipantInGroup(groupId, user.getId());
        participant.setMuted(false);
        participantRepository.save(participant);
    }

    @Override
    public void changeBackground(ObjectId groupId, ChangeBackgroundRequest request) {
        User user = getCurrentUser();

        Conversation group = findGroupById(groupId);

        Participant requester = findParticipantInGroup(groupId, user.getId());

        // Validate backgroundUrl
        if (request.getBackgroundUrl() == null || request.getBackgroundUrl().trim().isEmpty()) {
            throw new BadRequestException("Background không được để trống");
        }

        group.setBackgroundUrl(request.getBackgroundUrl().trim());
        conversationRepository.save(group);
    }

    /**
     * Check if a group exists and return it
     * @param groupId ID of the group to find
     * @return Conversation object if found
     * @throws NotFoundException if group doesn't exist
     */
    private Conversation findGroupById(ObjectId groupId) {
        return conversationRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Nhóm không tồn tại"));
    }

    /**
     * Check if a user is a member of a group
     * @param groupId ID of the group
     * @param userId ID of the user
     * @return true if user is in the group, false otherwise
     */
    private boolean isUserInGroup(ObjectId groupId, String userId) {
        return participantRepository.existsByConversationIdAndUserId(groupId, userId);
    }

    /**
     * Find a participant in a group or throw exception if not found
     * @param groupId ID of the group
     * @param userId ID of the user
     * @return Participant object if found
     * @throws BadRequestException if user is not in the group
     */
    private Participant findParticipantInGroup(ObjectId groupId, String userId) {
        return participantRepository.findByConversationIdAndUserId(groupId, userId)
                .orElseThrow(() -> new BadRequestException("Người dùng không tồn tại trong nhóm"));
    }

    private void validateOwner(ObjectId groupId, String userId) {
        Participant participant = findParticipantInGroup(groupId, userId);
        if (participant.getRole() != ParticipantRole.ADMIN) {
            throw new BadRequestException("Chỉ nhóm trưởng mới có quyền thực hiện hành động này.");
        }
    }

    private void validateOwnerOrModerator(ObjectId groupId, String requesterId, Participant targetParticipant) {
        Participant requester = findParticipantInGroup(groupId, requesterId);

        if (requester.getRole() == ParticipantRole.ADMIN) {
            return;
        }

        if (requester.getRole() == ParticipantRole.MODERATOR) {
            if (targetParticipant.getRole() != ParticipantRole.MEMBER) {
                throw new BadRequestException("Bạn chỉ có thể xóa thành viên thường (MEMBER).");
            }
            return;
        }

        throw new BadRequestException("Bạn không có quyền xóa thành viên.");
    }

    private User getCurrentUser() {
        // Check user
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return userRepository.findByUsername(name)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
    }
}