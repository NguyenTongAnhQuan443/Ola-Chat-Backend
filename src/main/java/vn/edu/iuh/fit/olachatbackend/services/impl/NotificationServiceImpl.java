/*
 * @ (#) NotificationServiceImpl.java       1.0     06/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services.impl;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 06/04/2025
 * @version:    1.0
 */

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.dtos.NotificationDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.NotificationPageDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.NotificationRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.RegisterDeviceRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Conversation;
import vn.edu.iuh.fit.olachatbackend.entities.DeviceToken;
import vn.edu.iuh.fit.olachatbackend.entities.Notification;
import vn.edu.iuh.fit.olachatbackend.entities.Participant;
import vn.edu.iuh.fit.olachatbackend.enums.NotificationType;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.exceptions.InternalServerErrorException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.mappers.NotificationMapper;
import vn.edu.iuh.fit.olachatbackend.repositories.ConversationRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.DeviceTokenRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.NotificationRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.ParticipantRepository;
import vn.edu.iuh.fit.olachatbackend.services.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final FirebaseMessaging firebaseMessaging;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final DeviceTokenRepository deviceTokenRepository;
    private final ConversationRepository conversationRepository;
    private final UserServiceImpl userServiceImpl;
    private final ParticipantRepository participantRepository;

    @Override
    public void registerDevice(RegisterDeviceRequest request) {
        validateDeviceRequest(request);

        DeviceToken deviceToken = deviceTokenRepository.findByDeviceId(request.getDeviceId());

        if (deviceToken == null) {
            // Create new device token
            deviceToken = new DeviceToken();
            deviceToken.setToken(request.getToken());
            logger.info("Creating new device token for user {}", request.getUserId());
        } else {
            deviceToken.setToken(request.getToken());
            logger.info("Updating existing device token for user {}", request.getUserId());
        }

        // Set or update common fields
        deviceToken.setUserId(request.getUserId());
        deviceToken.setDeviceId(request.getDeviceId());

        deviceTokenRepository.save(deviceToken);
    }

    private void validateDeviceRequest(RegisterDeviceRequest request) {
        if (request.getDeviceId() == null || request.getDeviceId().isEmpty()) {
            throw new BadRequestException("Device ID cannot be empty");
        }

        if (request.getToken() == null || request.getToken().isEmpty()) {
            throw new BadRequestException("Token cannot be empty");
        }
    }

    private void sendNotification(NotificationRequest request) {
        // Save notification to database first
        Notification notification = Notification.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .read(false)
                .type(request.getType())
                .createdAt(LocalDateTime.now())
                .build();

        try {
            notificationRepository.save(notification);

            // Send Firebase notification to the specific device
            Message message = Message.builder()
                    .setToken(request.getToken())
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getBody())
                            .build())
                    .putData("senderId", request.getSenderId())
                    .putData("receiverId", request.getReceiverId())
                    .putData("notificationId", notification.getId())
                    .putData("type", request.getType().toString())
                    .build();

            firebaseMessaging.send(message);
            logger.info("Sent notification to device token: {}", request.getToken());
        } catch (NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (DataAccessException e) {
            throw new InternalServerErrorException("Error accessing data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to send notification: {}", e.getMessage());
            throw new InternalServerErrorException("Error sending notification: " + e.getMessage());
        }
    }

    @Override
    public NotificationPageDTO getNotificationsByUser(String userId, Pageable pageable) {
        Page<NotificationDTO> page = notificationRepository.findByReceiverId(userId, pageable)
                .map(notificationMapper::toDTO);

        return NotificationPageDTO.builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresentOrElse(
                notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                    logger.info("Marked notification {} as read", notificationId);
                },
                () -> {
                    throw new NotFoundException("Notification not found with ID: " + notificationId);
                }
        );
    }

    /**
     * Sends notification to all devices of a user
     *
     * @param receiveId The user ID to send notification to
     * @param title Notification title
     * @param body Notification body
     * @param type Notification type
     * @param senderId Sender ID
     */
    private void sendNotificationToAllDevices(String receiveId, String title, String body,
                                              NotificationType type, String senderId) {
        List<DeviceToken> userDevices = deviceTokenRepository.findAllByUserId(receiveId);

        if (userDevices.isEmpty()) {
            logger.warn("No registered devices found for user: {}", receiveId);
            return;
        }

        for (DeviceToken deviceToken : userDevices) {
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .title(title)
                    .body(body)
                    .token(deviceToken.getToken())
                    .type(type)
                    .senderId(senderId)
                    .receiverId(receiveId)
                    .build();

            sendNotification(notificationRequest);
        }
    }

    @Override
    public void notifyConversation(String conversationId, String senderId, String title, String body, NotificationType type) {
//        Conversation conversation = conversationRepository.findById(new ObjectId(conversationId))
//                .orElseThrow(() -> new NotFoundException("Conversation not found"));
//
//        UserResponse sender = userServiceImpl.getUserById(senderId);
//
//        List<Participant> participants = participantRepository.findParticipantByConversationId(new ObjectId(conversationId));
//
//        for (Participant participant : participants) {
//            String receiverId = participant.getUserId();
//
//            // Skip sender and muted participants
//            if (receiverId.equals(senderId) || participant.isMuted()) {
//                continue;
//            }
//
//            // Send to all devices of this user
//            sendNotificationToAllDevices(receiverId, title, body, type, senderId);
//        }
        try {
            Conversation conversation = conversationRepository.findById(new ObjectId(conversationId))
                    .orElseThrow(() -> new NotFoundException("Conversation not found"));

            UserResponse sender = userServiceImpl.getUserById(senderId);

            List<Participant> participants = participantRepository.findParticipantByConversationId(new ObjectId(conversationId));

            for (Participant participant : participants) {
                String receiverId = participant.getUserId();

                // Skip sender and muted participants
                if (receiverId.equals(senderId) || participant.isMuted()) {
                    continue;
                }

                try {
                    sendNotificationToAllDevices(receiverId, title, body, type, senderId);
                } catch (Exception e) {
                    // Log lỗi gửi riêng cho 1 user nhưng không làm gián đoạn vòng lặp
                    System.err.println("Error sending to user " + receiverId + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error while notifying conversation: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void notifyUserMentioned(String senderId, String receiverId, String conversationId, String title, String body, NotificationType type) {
        // Skip if sender is trying to mention themselves
        if (senderId.equals(receiverId)) {
            return;
        }

        // Send to all devices of this user
        sendNotificationToAllDevices(receiverId, title, body, type, senderId);
    }

    @Override
    public void notifyGuestUser(String deviceId, String title, String body, NotificationType type) {
        List<DeviceToken> devices = deviceTokenRepository.findAllByDeviceId(deviceId);

        if (devices.isEmpty()) {
            logger.warn("No registered devices found for device ID: {}", deviceId);
            return;
        }

        for (DeviceToken deviceToken : devices) {
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .title(title)
                    .body(body)
                    .token(deviceToken.getToken())
                    .type(type)
                    .senderId("system")
                    .receiverId(deviceToken.getUserId() != null ? deviceToken.getUserId() : "guest")
                    .build();

            sendNotification(notificationRequest);
        }
    }

    @Override
    public void notifyUser(String receiverId, String title, String body, NotificationType type, String senderId) {
        try {
            sendNotificationToAllDevices(receiverId, title, body, type, senderId);
        } catch (Exception e) {
            logger.error("Error while notifying user: {}", e.getMessage());
        }
    }


}
