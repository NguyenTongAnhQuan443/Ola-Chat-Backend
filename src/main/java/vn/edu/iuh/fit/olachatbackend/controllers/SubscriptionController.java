package vn.edu.iuh.fit.olachatbackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.models.Subscriber;
import vn.edu.iuh.fit.olachatbackend.repositories.SubscriberRepository;

import java.util.Optional;

@RestController
@RequestMapping("/api/subscribe")
public class SubscriptionController {

    @Autowired
    private SubscriberRepository subscriberRepo;

    @PostMapping
    public ResponseEntity<?> subscribe(@RequestBody Subscriber s) {
        if (subscriberRepo.findByEmail(s.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered!");
        }
        subscriberRepo.save(s);
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
