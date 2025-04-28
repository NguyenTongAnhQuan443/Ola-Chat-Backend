/*
 * @ (#) MessageResponse.java       1.0     20/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.dtos;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 20/04/2025
 * @version:    1.0
 */

import lombok.*;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.SenderInfoResponse;
import vn.edu.iuh.fit.olachatbackend.entities.DeliveryStatus;
import vn.edu.iuh.fit.olachatbackend.entities.ReadStatus;
import vn.edu.iuh.fit.olachatbackend.enums.MessageStatus;
import vn.edu.iuh.fit.olachatbackend.enums.MessageType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseDTO {
    private String id;
    private SenderInfoResponse sender;
    private String conversationId;
    private String content;
    private MessageType type;
    private List<String> mediaUrls;
    private MessageStatus status;
    private List<DeliveryStatus> deliveryStatus;
    private List<ReadStatus> readStatus;
    private LocalDateTime createdAt;
    private boolean recalled;
}
