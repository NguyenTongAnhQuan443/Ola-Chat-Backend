/*
 * @ (#) MessageDTO.java       1.0     15/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.dtos.requests;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 15/02/2025
 * @version:    1.0
 */

import lombok.*;
import vn.edu.iuh.fit.olachatbackend.entities.DeliveryStatus;
import vn.edu.iuh.fit.olachatbackend.entities.Mention;
import vn.edu.iuh.fit.olachatbackend.entities.ReadStatus;
import vn.edu.iuh.fit.olachatbackend.enums.MessageStatus;
import vn.edu.iuh.fit.olachatbackend.enums.MessageType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequest {
    private String id;
    private String senderId;
    private String conversationId;
    private String content;
    private MessageType type;
    private List<String> mediaUrls;
    private MessageStatus status;
    private List<DeliveryStatus> deliveryStatus;
    private List<ReadStatus> readStatus;
    private LocalDateTime createdAt;
    private boolean recalled;
    private List<Mention> mentions;
}
