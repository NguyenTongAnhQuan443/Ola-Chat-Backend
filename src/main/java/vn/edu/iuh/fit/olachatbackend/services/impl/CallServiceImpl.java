/*
 * @ (#) CallServiceImpl.java       1.0     30/05/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services.impl;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 30/05/2025
 * @version:    1.0
 */

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.CallNotificationRequest;
import vn.edu.iuh.fit.olachatbackend.entities.Conversation;
import vn.edu.iuh.fit.olachatbackend.entities.Participant;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.enums.NotificationType;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.repositories.ConversationRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.ParticipantRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.UserRepository;
import vn.edu.iuh.fit.olachatbackend.services.CallService;
import vn.edu.iuh.fit.olachatbackend.services.NotificationService;
import vn.edu.iuh.fit.olachatbackend.services.RedisService;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CallServiceImpl implements CallService {

    private final NotificationService notificationService;
    private final ScheduledExecutorService callTimeoutScheduler;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;

    @Override
    public void inviteCall(CallNotificationRequest request) {
        User currentUser = getCurrentUser();

        // Check conversation
        Conversation conversation = conversationRepository.findById(new ObjectId(request.getConversationId()))
                .orElseThrow(() -> new NotFoundException("Nhóm không tồn tại"));

        // Check if user exists in conversation
        findParticipantInGroup(conversation.getId(), currentUser.getId());

        // Save to redis
        redisService.createCallSession(request.getConversationId(), Duration.ofSeconds(45));

        // Notify for conversation
        notificationService.notifyConversation(conversation.getId().toString(), currentUser.getId(), "Cuộc gọi đến",
                "Bạn có cuộc gọi từ " + currentUser.getDisplayName(), NotificationType.CALL_OFFER);

        // Schedule
        callTimeoutScheduler.schedule(() -> {
            if (redisService.isCallSession(conversation.getId().toString())) {
                handleCallTimeout(conversation.getId().toString());
            }
        }, 45, TimeUnit.SECONDS);

    }

    private void handleCallTimeout(String string) {

    }

    @Override
    public void acceptCall(CallNotificationRequest request) {

    }

    @Override
    public void rejectCall(CallNotificationRequest request) {

    }

    @Override
    public void cancelCall(CallNotificationRequest request) {

    }

    private User getCurrentUser() {
        // Check user
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return userRepository.findByUsername(name)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
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
}
