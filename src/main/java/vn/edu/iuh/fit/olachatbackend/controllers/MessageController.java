/*
 * @ (#) MessageController.java       1.0     14/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.controllers;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/02/2025
 * @version:    1.0
 */

import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.dtos.MessageDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MessageResponse;
import vn.edu.iuh.fit.olachatbackend.services.MessageService;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public MessageDTO createMessage(@RequestBody MessageDTO messageDTO) {
        return messageService.save(messageDTO);
    }

    @PutMapping("/{messageId}/received")
    public MessageResponse<String> markAsReceived(
            @PathVariable String messageId,
            @RequestParam String userId
    ) {
        messageService.markMessageAsReceived(messageId, userId);
        return MessageResponse.<String>builder()
                .message("Đã đánh dấu tin nhắn là đã nhận.")
                .data(messageId)
                .build();
    }


    @PutMapping("/{messageId}/read")
    public MessageResponse<String> markAsRead(
            @PathVariable String messageId,
            @RequestParam String userId
    ) {
        messageService.markMessageAsRead(messageId, userId);
        return MessageResponse.<String>builder()
                .message("Đánh dấu đã đọc thành công.")
                .data("OK")
                .build();
    }




}
