/*
 * @ (#) NotificationController.java       1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.controllers;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/04/2025
 * @version:    1.0
 */

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.dtos.NotificationDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.NotificationPageDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.RegisterDeviceRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MessageResponse;
import vn.edu.iuh.fit.olachatbackend.services.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    public ResponseEntity<MessageResponse<NotificationPageDTO>> getUserNotifications(
            @PathVariable String userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        NotificationPageDTO notificationPageDTO = notificationService.getNotificationsByUser(userId, pageable);

        MessageResponse<NotificationPageDTO> response = MessageResponse.<NotificationPageDTO>builder()
                .message("Lấy danh sách thông báo thành công!")
                .data(notificationPageDTO)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<MessageResponse<String>> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);

        MessageResponse<String> response = MessageResponse.<String>builder()
                .message("Đã cập nhật thông báo.")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-device")
    public ResponseEntity<MessageResponse<Void>> registerDevice(@RequestBody RegisterDeviceRequest request) {
        notificationService.registerDevice(request);

        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .message("Đã đăng ký thiết bị thành công.")
                .build();

        return ResponseEntity.ok(response);
    }
}
