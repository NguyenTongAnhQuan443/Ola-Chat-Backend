package vn.edu.iuh.fit.olachatbackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.CallNotificationRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.ApiResponse;
import vn.edu.iuh.fit.olachatbackend.services.NotificationService;
import vn.edu.iuh.fit.olachatbackend.services.RedisService;

@RestController
@RequestMapping("/api/calls")
public class CallNotificationController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private RedisService redisService;

    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<Void>> inviteCall(@RequestBody CallNotificationRequest req) {
        redisService.setCallPending(req.getChannelId(), req.getSenderId(), req.getReceiverId(), 45); // Timeout 45s
        notificationService.sendCallNotification(req); // action mặc định OFFER
        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Call invite sent",
                null
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<Void>> acceptCall(@RequestBody CallNotificationRequest req) {
        redisService.setCallAccepted(req.getChannelId());
        notificationService.sendCallAcceptedFCM(req);
        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Call accepted",
                null
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<Void>> rejectCall(@RequestBody CallNotificationRequest req) {
        redisService.setCallRejected(req.getChannelId());
        notificationService.sendCallRejectedFCM(req);
        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Call rejected",
                null
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelCall(@RequestBody CallNotificationRequest req) {
        redisService.setCallCanceled(req.getChannelId());
        notificationService.sendCallCanceledFCM(req);
        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Call canceled",
                null
        );
        return ResponseEntity.ok(response);
    }
}
