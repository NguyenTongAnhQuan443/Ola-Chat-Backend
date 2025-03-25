/*
 * @ (#) FriendRequest.java       1.0     14/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.entities;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.olachatbackend.enums.RequestStatus;

import java.time.LocalDateTime;

/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/02/2025
 * @version:    1.0
 */

@Entity
@Table(name = "friend_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime responseAt;

    @PrePersist
    public void prePersist() {
        this.sentAt = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }

}
