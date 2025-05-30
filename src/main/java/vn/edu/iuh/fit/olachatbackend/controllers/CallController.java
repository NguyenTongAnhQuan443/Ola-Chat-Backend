package vn.edu.iuh.fit.olachatbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.CallNotificationRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MessageResponse;
import vn.edu.iuh.fit.olachatbackend.services.CallService;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallController {

    private final CallService callService;

    @PostMapping("/invite")
    public ResponseEntity<MessageResponse<Void>> inviteCall(@RequestBody CallNotificationRequest req) {
        callService.inviteCall(req);
        return ResponseEntity.ok(
                new MessageResponse<>(200, "Gửi lời mời gọi điện thành công", true, null)
        );
    }

    @PostMapping("/accept")
    public ResponseEntity<MessageResponse<Void>> acceptCall(@RequestBody CallNotificationRequest req) {
        callService.acceptCall(req);
        return ResponseEntity.ok(
                new MessageResponse<>(200, "Chấp nhận gọi điện thành công", true, null)
        );
    }


    @PostMapping("/reject")
    public ResponseEntity<MessageResponse<Void>> rejectCall(@RequestBody CallNotificationRequest req) {
        callService.rejectCall(req);
        return ResponseEntity.ok(
                new MessageResponse<>(200, "Từ chối gọi điện thành công", true, null)
        );
    }

    @PostMapping("/cancel")
    public ResponseEntity<MessageResponse<Void>> cancelCall(@RequestBody CallNotificationRequest req) {
        callService.cancelCall(req);
        return ResponseEntity.ok(
                new MessageResponse<>(200, "Hủy bỏ gọi điện thành công", true, null)
        );
    }
}
