/*
 * @ (#) QRLoginServiceImpl.java       1.0     28/05/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services.impl;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 28/05/2025
 * @version:    1.0
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.dtos.QrLoginSession;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.QrSessionRequest;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.repositories.UserRepository;
import vn.edu.iuh.fit.olachatbackend.services.QRLoginService;
import vn.edu.iuh.fit.olachatbackend.services.RedisService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QRLoginServiceImpl implements QRLoginService {

    private final RedisService redisService;
    private final Duration TTL = Duration.ofMinutes(3);
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @Override
    public String createQrToken(QrSessionRequest request, HttpServletRequest httpRequest) {
        String sessionId = UUID.randomUUID().toString();
        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        QrLoginSession session = QrLoginSession.builder()
                .sessionId(sessionId)
                .ipAddress(ip)
                .userAgent(userAgent)
                .deviceName(request.getDeviceName())
                .deviceId(request.getDeviceId())
                .locationHint(request.getLocationHint())
                .createdAt(LocalDateTime.now())
                .isConfirmed(false)
                .build();

        try {
            redisService.saveQRCodeToken(session, TTL);
        } catch (JsonProcessingException ex) {
            System.err.println("Lỗi khi tạo token: "+ ex.getMessage());
            return null;
        }
        return "https://ola-chat-backend-latest-ir9h.onrender.com/ola-chat/auth/qr-login/scan?sessionId="+sessionId;
    }

    @Override
    public QrLoginSession scanQrAndGetInfo(String sessionId) {
        User currentUser = getCurrentUser();
        QrLoginSession session = redisService.getQRCodeToken(sessionId);

        // Assign userId
        session.setConfirmedUserId(currentUser.getId());

        // Send to web
        String webSocketTopic = "/topic/qr/" + sessionId;
        messagingTemplate.convertAndSend(webSocketTopic, Map.of(
                "type", "USER_INFO_PREVIEW",
                "user", Map.of("name", currentUser.getDisplayName(),
                                    "avatar", currentUser.getAvatar())
        ));

        return session;
    }

    @Override
    public void confirm(String qrToken) {
//        if (!redisService.isValid(qrToken)) {
//            throw new BadRequestException("QR invalid!");
//        }
//
//        if (redisService.isAlreadyUsed(qrToken)) {
//            throw new BadRequestException("QR already used!");
//        }
//
//        User currentUser = getCurrentUser();
//
//        redisService.markAsUsed(qrToken, currentUser.getId());
//
//        // Gửi thông báo qua WebSocket tới browser
//        messagingTemplate.convertAndSend("/topic/qr/" + qrToken, Map.of(
//                "status", "CONFIRMED",
//                "userId", userId,
//                "accessToken", jwt
//        ));
//
//        return ResponseEntity.ok("Confirmed");
    }

    private User getCurrentUser() {
        // Check user
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return userRepository.findByUsername(name)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
    }
}
