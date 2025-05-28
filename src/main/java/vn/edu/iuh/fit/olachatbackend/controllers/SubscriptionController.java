package vn.edu.iuh.fit.olachatbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MessageResponse;
import vn.edu.iuh.fit.olachatbackend.models.Subscriber;
import vn.edu.iuh.fit.olachatbackend.services.EmailService_Weather;

@RestController
@RequestMapping("/api/subscribe")
@RequiredArgsConstructor
public class SubscriptionController {

    private final EmailService_Weather emailService_Weather;

    @PostMapping
    public ResponseEntity<MessageResponse<Void>> subscribe(@RequestBody Subscriber s) {
        emailService_Weather.subscribe(
                s.getEmail(),
                "Xác nhận đăng ký nhận thông báo thời tiết",
                s
        );
        return ResponseEntity.ok(
                new MessageResponse<>(200, "Đăng ký thành công", true, null)
        );
    }

    @DeleteMapping
    public ResponseEntity<MessageResponse<Void>> unsubscribe(@RequestParam String email) {
        emailService_Weather.unsubscribe(email);
        return ResponseEntity.ok(
                new MessageResponse<>(200, "Hủy đăng ký thành công", true, null)
        );
    }
}
