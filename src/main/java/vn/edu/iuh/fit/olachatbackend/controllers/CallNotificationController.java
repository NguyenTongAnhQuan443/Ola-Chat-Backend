package vn.edu.iuh.fit.olachatbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.CallNotificationRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.CallNotificationRequest_v2;
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

    @PostMapping("/invite_v2")
    public ResponseEntity<MessageResponse<Void>> inviteCallV2(@RequestBody CallNotificationRequest_v2 req) {
        // Process group call invitation
        for (CallNotificationRequest_v2.Receiver receiver : req.getReceivers()) {
            // Set call pending status for each receiver in Redis
            redisService.setCallPending(req.getChannelId(), req.getSenderId(), receiver.getUserId(), 45);

            // Create notification for each receiver
            CallNotificationRequest notification = CallNotificationRequest.builder()
                    .title(req.getTitle())
                    .body(req.getBody())
                    .senderId(req.getSenderId())
                    .receiverId(receiver.getUserId())
                    .channelId(req.getChannelId())
                    .callType(req.getCallType())
                    .token(receiver.getToken())
                    .action(receiver.getAction().toString())
                    .build();

            // Send notification to each receiver
            notificationService.sendCallNotification(notification);
        }

        return ResponseEntity.ok(
                new MessageResponse<>(200, "Gửi lời mời gọi nhóm thành công", true, null)
        );
    }

    @PostMapping("/accept_v2")
    public ResponseEntity<MessageResponse<Void>> acceptCallV2(@RequestBody CallNotificationRequest_v2 req) {
        // Process group call acceptance
        for (CallNotificationRequest_v2.Receiver receiver : req.getReceivers()) {
            // Set call accepted status in Redis for each receiver
            redisService.setCallAccepted(req.getChannelId());

            // Create notification for each receiver
            CallNotificationRequest notification = CallNotificationRequest.builder()
                    .title(req.getTitle())
                    .body(req.getBody())
                    .senderId(req.getSenderId())
                    .receiverId(receiver.getUserId())
                    .channelId(req.getChannelId())
                    .callType(req.getCallType())
                    .token(receiver.getToken())
                    .action(receiver.getAction().toString()) // Should be "accepted"
                    .build();

            // Send notification to each receiver
            notificationService.sendCallNotification(notification);
        }

        return ResponseEntity.ok(
                new MessageResponse<>(200, "Chấp nhận gọi nhóm thành công", true, null)
        );
    }

    @PostMapping("/reject_v2")
    public ResponseEntity<MessageResponse<Void>> rejectCallV2(@RequestBody CallNotificationRequest_v2 req) {
        // Process group call rejection
        for (CallNotificationRequest_v2.Receiver receiver : req.getReceivers()) {
            // Set call rejected status in Redis for each receiver
            redisService.setCallRejected(req.getChannelId());

            // Create notification for each receiver
            CallNotificationRequest notification = CallNotificationRequest.builder()
                    .title(req.getTitle())
                    .body(req.getBody())
                    .senderId(req.getSenderId())
                    .receiverId(receiver.getUserId())
                    .channelId(req.getChannelId())
                    .callType(req.getCallType())
                    .token(receiver.getToken())
                    .action(receiver.getAction().toString()) // Should be "rejected"
                    .build();

            // Send notification to each receiver
            notificationService.sendCallNotification(notification);
        }

        return ResponseEntity.ok(
                new MessageResponse<>(200, "Từ chối gọi nhóm thành công", true, null)
        );
    }

    @PostMapping("/cancel_v2")
    public ResponseEntity<MessageResponse<Void>> cancelCallV2(@RequestBody CallNotificationRequest_v2 req) {
        // Process group call cancellation
        for (CallNotificationRequest_v2.Receiver receiver : req.getReceivers()) {
            // Set call canceled status in Redis for each receiver
            redisService.setCallCanceled(req.getChannelId());

            // Create notification for each receiver
            CallNotificationRequest notification = CallNotificationRequest.builder()
                    .title(req.getTitle())
                    .body(req.getBody())
                    .senderId(req.getSenderId())
                    .receiverId(receiver.getUserId())
                    .channelId(req.getChannelId())
                    .callType(req.getCallType())
                    .token(receiver.getToken())
                    .action(receiver.getAction().toString()) // Should be "noAnswer" or other appropriate status
                    .build();

            // Send notification to each receiver
            notificationService.sendCallNotification(notification);
        }

        return ResponseEntity.ok(
                new MessageResponse<>(200, "Hủy bỏ gọi nhóm thành công", true, null)
        );
    }
}
