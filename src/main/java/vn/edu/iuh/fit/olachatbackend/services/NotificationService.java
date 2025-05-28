/*
 * @ (#) NotificationService.java       1.0     06/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 06/04/2025
 * @version:    1.0
 */

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.edu.iuh.fit.olachatbackend.dtos.NotificationDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.NotificationPageDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.CallNotificationRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.NotificationRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.RegisterDeviceRequest;
import vn.edu.iuh.fit.olachatbackend.enums.NotificationType;

public interface NotificationService {
    void registerDevice(RegisterDeviceRequest request);
    void removeDevice(String userId, String deviceId);
    NotificationPageDTO getNotificationsByUser(String userId, Pageable pageable);
    void markAsRead(String notificationId);
    void notifyConversation(String conversationId, String senderId, String title, String body, NotificationType type);
    void notifyUserMentioned(String senderId, String receiverId, String conversationId, String title, String body, NotificationType notificationType);
    void notifyGuestUser(String deviceId, String title, String body, NotificationType type);
    void notifyUser(String receiverId, String title, String body, NotificationType type, String senderId);
    void sendCallNotification(CallNotificationRequest request);
    public void sendCallCanceledFCM(CallNotificationRequest req);
    public void sendCallAcceptedFCM(CallNotificationRequest req);
    public void sendCallRejectedFCM(CallNotificationRequest req);
}
