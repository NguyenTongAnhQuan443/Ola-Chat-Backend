package vn.edu.iuh.fit.olachatbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.CallNotificationRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MessageResponse;
import vn.edu.iuh.fit.olachatbackend.services.NotificationService;
import vn.edu.iuh.fit.olachatbackend.services.RedisService;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallNotificationController {

    private final NotificationService notificationService;
    private final RedisService redisService;

    @PostMapping("/invite")
    public ResponseEntity<MessageResponse<Void>> inviteCall(@RequestBody CallNotificationRequest req) {
        redisService.setCallPending(req.getChannelId(), req.getSenderId(), req.getReceiverId(), 45); // Timeout 45s
        notificationService.sendCallNotification(req); // action mặc định OFFER
        return ResponseEntity.ok(
                new MessageResponse<>(200, "Gửi lời mời gọi điện thành công", true, null)
        );
    }

    @PostMapping("/accept")
    public ResponseEntity<MessageResponse<Void>> acceptCall(@RequestBody CallNotificationRequest req) {
        redisService.setCallAccepted(req.getChannelId());
        notificationService.sendCallAcceptedFCM(req);
        return ResponseEntity.ok(
                new MessageResponse<>(200, "Chấp nhận gọi điện thành công", true, null)
        );
    }

    @PostMapping("/reject")
    public ResponseEntity<MessageResponse<Void>> rejectCall(@RequestBody CallNotificationRequest req) {
        redisService.setCallRejected(req.getChannelId());
        notificationService.sendCallRejectedFCM(req);
        return ResponseEntity.ok(
                new MessageResponse<>(200, "Từ chối gọi điện thành công", true, null)
        );
    }

    @PostMapping("/cancel")
    public ResponseEntity<MessageResponse<Void>> cancelCall(@RequestBody CallNotificationRequest req) {
        redisService.setCallCanceled(req.getChannelId());
        notificationService.sendCallCanceledFCM(req);
        return ResponseEntity.ok(
                new MessageResponse<>(200, "Hủy bỏ gọi điện thành công", true, null)
        );
    }
}
