/*
 * @ (#) MessageSearchResponse.java       1.0     26/05/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.dtos.responses;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 26/05/2025
 * @version:    1.0
 */

import vn.edu.iuh.fit.olachatbackend.entities.Message;
import vn.edu.iuh.fit.olachatbackend.enums.MessageType;

import java.time.LocalDateTime;
import java.util.List;

public class MessageSearchResponse {
    private String id;
    private String senderId;
    private String conversationId;
    private String content;
    private MessageType type;
    private List<Message.DeletedStatus> deletedStatus;
    private LocalDateTime createdAt;
    private String avatar;
}
