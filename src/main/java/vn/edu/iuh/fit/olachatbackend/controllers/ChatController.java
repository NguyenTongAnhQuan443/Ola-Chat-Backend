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

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import vn.edu.iuh.fit.olachatbackend.dtos.MessageDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Mention;
import vn.edu.iuh.fit.olachatbackend.enums.NotificationType;
import vn.edu.iuh.fit.olachatbackend.services.MessageService;
import vn.edu.iuh.fit.olachatbackend.services.NotificationService;
import vn.edu.iuh.fit.olachatbackend.services.impl.UserServiceImpl;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate template;
    private final MessageService messageService;
    private final NotificationService notificationService;
    private final UserServiceImpl userServiceImpl;

    /**
     * Handles private messages between two users
     *
     * @param messageDTO Message information from client
     * @return MessageResponseDTO Processed message information
     */
    @MessageMapping("/private-message")
    public MessageDTO receivePrivateMessage(@Payload MessageDTO messageDTO) {
        System.out.println("Private message from client: " + messageDTO);

        // Save message to database
        messageService.save(messageDTO);

        // Send message to specific recipient
        template.convertAndSend("/user/" + messageDTO.getConversationId() + "/private", messageDTO);

        // Send notification to recipient
        UserResponse sender = userServiceImpl.getUserById(messageDTO.getSenderId());
        notificationService.notifyConversation(
                messageDTO.getConversationId(),
                messageDTO.getSenderId(),
                "Tin nhắn mới",
                "Bạn có tin nhắn từ " + sender.getDisplayName(),
                NotificationType.MESSAGE
        );

        // Process @mentions
        processMentions(messageDTO, sender);

        return messageDTO;
    }

    @MessageMapping("/recall-message")
    public void recallMessage(@Payload MessageDTO messageDTO) {
        System.out.println("Message Recall from client: "+ messageDTO);
        MessageDTO recalled = messageService.recallMessage(messageDTO.getId(), messageDTO.getSenderId());
        template.convertAndSend("/user/" + recalled.getConversationId() + "/private", recalled);
    }

    /**
     * Processes @mentions in messages
     *
     * @param messageDTO Message information
     * @param sender Message sender
     */
    private void processMentions(MessageDTO messageDTO, UserResponse sender) {
        if (messageDTO.getMentions() == null || messageDTO.getMentions().isEmpty()) {
            return;
        }

        boolean mentionAll = checkMentionAll(messageDTO.getMentions());

        if (mentionAll) {
            // If @All, send notification to the entire group
            notificationService.notifyConversation(
                    messageDTO.getConversationId(),
                    messageDTO.getSenderId(),
                    "Có tin nhắn mới",
                    sender.getDisplayName() + " đã nhắc tất cả trong nhóm",
                    NotificationType.MESSAGE
            );
        } else {
            // If individual mentions, send notifications to each mentioned user
            for (Mention mention : messageDTO.getMentions()) {
                if (isValidUserId(mention.getUserId())) {
                    notificationService.notifyUserMentioned(
                            messageDTO.getSenderId(),
                            mention.getUserId(),
                            messageDTO.getConversationId(),
                            "Bạn được nhắc tên",
                            sender.getDisplayName() + " đã nhắc bạn trong cuộc trò chuyện",
                            NotificationType.MENTION
                    );
                }
            }
        }
    }

    /**
     * Checks if there's a mention for the entire group
     *
     * @param mentions List of mentions
     * @return true if @All is mentioned, false otherwise
     */
    private boolean checkMentionAll(List<Mention> mentions) {
        for (Mention mention : mentions) {
            if ("All".equalsIgnoreCase(mention.getDisplayName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates if a user ID is valid
     *
     * @param userId User ID
     * @return true if ID is valid, false otherwise
     */
    private boolean isValidUserId(String userId) {
        return userId != null && !userId.equals("0");
    }


}
