/*
 * @ (#) ConversationDTO.java       1.0     15/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.dtos;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 15/02/2025
 * @version:    1.0
 */

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.edu.iuh.fit.olachatbackend.entities.LastMessage;
import vn.edu.iuh.fit.olachatbackend.enums.ConversationType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ConversationDTO {
    private String id;
    private String name;
    private String avatar;
    private ConversationType type;
    private LastMessage lastMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> userIds;
    private List<String> moderatorIds;
    private String adminId;
    private String backgroundUrl;
}
