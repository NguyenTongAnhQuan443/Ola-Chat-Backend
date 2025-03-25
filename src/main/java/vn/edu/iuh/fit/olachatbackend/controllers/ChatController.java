/*
 * @ (#) ChatController.java       1.0     24/01/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.controllers;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 24/01/2025
 * @version:    1.0
 */

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import vn.edu.iuh.fit.olachatbackend.dtos.MessageDTO;
import vn.edu.iuh.fit.olachatbackend.services.MessageService;

@Controller
public class ChatController {

    private final SimpMessagingTemplate template;
    private final MessageService messageService;

    public ChatController(SimpMessagingTemplate template, MessageService messageService) {
        this.template = template;
        this.messageService = messageService;
    }

    // Public chat
    @MessageMapping("/message")
    @SendTo("/chatroom/public")
    public MessageDTO receivePublicMessage(@Payload MessageDTO messageDTO) {
        return messageDTO;
    }

    // Private chat
    @MessageMapping("/private-message")
    public MessageDTO receivePrivateMessage(@Payload MessageDTO messageDTO) {
        messageService.save(messageDTO);
        System.out.println("Message from client: "+ messageDTO);
        template.convertAndSend("/user/" + messageDTO.getConversationId() + "/private", messageDTO);
        return messageDTO;
    }
}
