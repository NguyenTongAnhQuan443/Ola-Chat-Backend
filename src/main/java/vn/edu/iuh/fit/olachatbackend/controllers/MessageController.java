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
            @PathVariable String messageId
    ) {
        messageService.markMessageAsReceived(messageId);
        return MessageResponse.<String>builder()
                .message("Đã đánh dấu tin nhắn là đã nhận.")
                .data(messageId)
                .build();
    }


    @PutMapping("/{messageId}/read")
    public MessageResponse<String> markAsRead(
            @PathVariable String messageId
    ) {
        messageService.markMessageAsRead(messageId);
        return MessageResponse.<String>builder()
                .message("Đánh dấu đã đọc thành công.")
                .data("OK")
                .build();
    }

    @PostMapping("/{messageId}/hiddenForUser")
    public MessageResponse<String> hiddenForUser(@PathVariable String messageId) {
        messageService.hiddenForUser(messageId);
        return MessageResponse.<String>builder()
                .message("Xóa tin nhắn ở phía bạn thành công.")
                .data(null)
                .build();
    }

    @PostMapping("/{messageId}/replies")
    public MessageResponse<String> addReplyToMessage(@PathVariable String messageId,
                                                     @RequestBody MessageDTO messageDTO) {
        messageService.addReplyToMessage(messageId, messageDTO);
        return MessageResponse.<String>builder()
                .message("Trả lời tin nhắn thành công.")
                .data(null)
                .build();
    }


}
