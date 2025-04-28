/*
 * @ (#) Conversation.java       1.0     14/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.edu.iuh.fit.olachatbackend.enums.ConversationType;

import java.time.LocalDateTime;

/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/02/2025
 * @version:    1.0
 */

@Document
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {
    @Id
    private ObjectId id;
    private String name;
    private String avatar;
    @Enumerated(EnumType.STRING)
    private ConversationType type;
    private LastMessage lastMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String backgroundUrl;
}
