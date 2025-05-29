/*
 * @ (#) MessageDetailDTO.java       1.0     25/05/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.dtos;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 25/05/2025
 * @version:    1.0
 */

import lombok.*;
import vn.edu.iuh.fit.olachatbackend.entities.DeliveryStatus;
import vn.edu.iuh.fit.olachatbackend.entities.Mention;
import vn.edu.iuh.fit.olachatbackend.entities.Message;
import vn.edu.iuh.fit.olachatbackend.entities.ReadStatus;
import vn.edu.iuh.fit.olachatbackend.enums.MessageStatus;
import vn.edu.iuh.fit.olachatbackend.enums.MessageType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDetailDTO {
    private String id;
    private String senderId;
    private String conversationId;
    private String content;
    private MessageType type;
    private List<String> mediaUrls;
    private MessageStatus status;
    private List<DeliveryStatus> deliveryStatus;
    private List<ReadStatus> readStatus;
    private List<Message.DeletedStatus> deletedStatus;
    private LocalDateTime createdAt;
    private boolean recalled;
    private List<Mention> mentions;
    private String replyTo;
    // emoji
    private List<String> emojiTypes;
    private int totalReactionCount;
    private String lastUserReaction;
}

