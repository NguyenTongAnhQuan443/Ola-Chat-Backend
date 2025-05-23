package vn.edu.iuh.fit.olachatbackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.models.Subscriber;
import vn.edu.iuh.fit.olachatbackend.repositories.SubscriberRepository;
import vn.edu.iuh.fit.olachatbackend.services.EmailService_Weather;

import java.util.Optional;

@RestController
@RequestMapping("/api/subscribe")
public class SubscriptionController {

    @Autowired
    private SubscriberRepository subscriberRepo;
    @Autowired
    private EmailService_Weather emailService_Weather;

    @PostMapping
    public ResponseEntity<?> subscribe(@RequestBody Subscriber s) {
        if (subscriberRepo.findByEmail(s.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered!");
        }
        subscriberRepo.save(s);
        String content = "Chúc mừng bạn đã đăng ký nhận thông báo thời tiết thành công!\n"
                + "Bạn sẽ nhận bản tin thời tiết vào 7h sáng hàng ngày.\n"
                + "Nếu muốn huỷ, hãy sử dụng chức năng hủy đăng ký trên ứng dụng/web.";

        emailService_Weather.sendWeatherEmail(
                s.getEmail(),
                "Xác nhận đăng ký nhận thông báo thời tiết",
                content
        );
        return ResponseEntity.ok("Registered successfully!");
    }

    @DeleteMapping
    public ResponseEntity<?> unsubscribe(@RequestParam String email) {
        Optional<Subscriber> s = subscriberRepo.findByEmail(email);
        if (s.isEmpty()) {
            return ResponseEntity.badRequest().body("Email not found!");
        }
        subscriberRepo.delete(s.get());
        return ResponseEntity.ok("Unsubscribed successfully!");
    }
}
