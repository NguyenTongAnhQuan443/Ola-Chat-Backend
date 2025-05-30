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
import vn.edu.iuh.fit.olachatbackend.dtos.CallSession;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.CallNotificationRequest;
import vn.edu.iuh.fit.olachatbackend.entities.Conversation;
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
        redisService.createCallSession(request.getConversationId(), currentUser.getId(), request.getCallType(), Duration.ofDays(1));

        // Notify for conversation
        notificationService.notifyConversation(conversation.getId().toString(), currentUser.getId(), "Cuộc gọi đến",
                "Bạn có cuộc gọi từ " + currentUser.getDisplayName(), NotificationType.CALL_OFFER);

        // Schedule
        callTimeoutScheduler.schedule(() -> {
            if (redisService.getCallSession(conversation.getId().toString()) != null) {
                handleCallTimeout(conversation.getId().toString());
            }
        }, 45, TimeUnit.SECONDS);

    }

    private void handleCallTimeout(String conversationId) {
        System.out.println("Missed call");
        redisService.deleteCallSession(conversationId);
    }

    @Override
    public void acceptCall(CallNotificationRequest request) {
        User currentUser = getCurrentUser();

        // Check conversation
        Conversation conversation = conversationRepository.findById(new ObjectId(request.getConversationId()))
                .orElseThrow(() -> new NotFoundException("Nhóm không tồn tại"));

        // Check if user exists in conversation
        findParticipantInGroup(conversation.getId(), currentUser.getId());

        CallSession session = redisService.getCallSession(conversation.getId().toString());

        // Check if call exists
        if (session == null) {
            throw new NotFoundException("Không tìm thấy cuộc gọi");
        }

        // Check if call type valid
        if (!session.getType().equals(request.getCallType().toString())) {
            throw new NotFoundException("Không tìm thấy cuộc gọi " + request.getCallType().toString());
        }

        System.out.println("Sender: " + session.getCallerId());
        System.out.println("Current: "+ currentUser.getId());

        // Check if user is sender
        if (session.getCallerId().equals(currentUser.getId())) {
            throw new BadRequestException("Người gọi không thể chấp nhận cuộc gọi của mình");
        }

        // Notify for conversation
        notificationService.notifyConversation(conversation.getId().toString(), currentUser.getId(), "Cuộc gọi được chấp nhận",
                currentUser.getDisplayName() + " đã trả lời cuộc gọi", NotificationType.CALL_ACCEPTED);

        // Remove to redis
        redisService.deleteCallSession(request.getConversationId());
    }

    @Override
    public void rejectCall(CallNotificationRequest request) {
        User currentUser = getCurrentUser();

        // Check conversation
        Conversation conversation = conversationRepository.findById(new ObjectId(request.getConversationId()))
                .orElseThrow(() -> new NotFoundException("Nhóm không tồn tại"));

        // Check if user exists in conversation
        findParticipantInGroup(conversation.getId(), currentUser.getId());

        CallSession session = redisService.getCallSession(conversation.getId().toString());

        // Check if call exists
        if (session == null) {
            throw new NotFoundException("Không tìm thấy cuộc gọi");
        }

        // Check if call type valid
        if (!session.getType().equals(request.getCallType().toString())) {
            throw new NotFoundException("Không tìm thấy cuộc gọi " + request.getCallType().toString());
        }

        // Check if user is sender
        if (session.getCallerId().equals(currentUser.getId())) {
            throw new BadRequestException("Người gọi không thể từ chối cuộc gọi của mình");
        }

        // Notify for conversation
        notificationService.notifyConversation(conversation.getId().toString(), currentUser.getId(), "Cuộc gọi bị từ chối",
                currentUser.getDisplayName() + " đã từ chối cuộc gọi", NotificationType.CALL_REJECTED);

        // Remove to redis
        redisService.deleteCallSession(request.getConversationId());
    }

    @Override
    public void cancelCall(CallNotificationRequest request) {
        User currentUser = getCurrentUser();

        // Check conversation
        Conversation conversation = conversationRepository.findById(new ObjectId(request.getConversationId()))
                .orElseThrow(() -> new NotFoundException("Nhóm không tồn tại"));

        // Check if user exists in conversation
        findParticipantInGroup(conversation.getId(), currentUser.getId());

        CallSession session = redisService.getCallSession(conversation.getId().toString());

        // Check if call exists
        if (session == null) {
            throw new NotFoundException("Không tìm thấy cuộc gọi");
        }

        // Check if call type valid
        if (!session.getType().equals(request.getCallType().toString())) {
            throw new NotFoundException("Không tìm thấy cuộc gọi " + request.getCallType().toString());
        }

        // Check if user is sender
        if (!session.getCallerId().equals(currentUser.getId())) {
            throw new BadRequestException("Chỉ người gọi mới được hủy bỏ cuộc gọi");
        }

        // Notify for conversation
        notificationService.notifyConversation(conversation.getId().toString(), currentUser.getId(), "Cuộc gọi nhỡ",
                "Bạn đã bỏ lỡ cuộc gọi từ " + currentUser.getDisplayName() , NotificationType.CALL_MISSED);

        // Remove to redis
        redisService.deleteCallSession(request.getConversationId());
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
     *
     * @param groupId ID of the group
     * @param userId  ID of the user
     * @throws BadRequestException if user is not in the group
     */
    private void findParticipantInGroup(ObjectId groupId, String userId) {
        participantRepository.findByConversationIdAndUserId(groupId, userId)
                .orElseThrow(() -> new BadRequestException("Người dùng không tồn tại trong nhóm"));
    }
}
