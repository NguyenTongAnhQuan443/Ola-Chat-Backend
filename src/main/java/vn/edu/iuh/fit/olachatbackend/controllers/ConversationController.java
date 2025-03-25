/*
 * @ (#) ConversationController.java       1.0     14/02/2025
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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.dtos.ConversationDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.MessageDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Conversation;
import vn.edu.iuh.fit.olachatbackend.entities.Message;
import vn.edu.iuh.fit.olachatbackend.services.ConversationService;
import vn.edu.iuh.fit.olachatbackend.services.MessageService;
import vn.edu.iuh.fit.olachatbackend.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final UserService userService;

    public ConversationController(ConversationService conversationService, MessageService messageService, UserService userService) {
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ConversationDTO> createConversation(@RequestBody ConversationDTO conversationDTO) {
        return ResponseEntity.ok(conversationService.createConversation(conversationDTO));
    }

    @GetMapping
    public ResponseEntity<List<ConversationDTO>> getConversationsByUserId(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(conversationService.getAllConversationsByUserId(userId));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageDTO>> getMessagesByConversationId(@PathVariable String id) {
        List<MessageDTO> messages = messageService.getMessagesByConversationId(id);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{conversationId}/users")
    public ResponseEntity<List<UserResponse>> getUsersByConversation(@PathVariable String conversationId) {
        List<UserResponse> users = userService.getUsersByConversationId(conversationId);
        return ResponseEntity.ok(users);
    }
}
