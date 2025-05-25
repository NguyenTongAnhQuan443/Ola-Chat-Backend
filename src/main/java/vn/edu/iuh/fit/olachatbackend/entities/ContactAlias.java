/*
 * @ (#) UserNickname.java       1.0     26/05/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.entities;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 26/05/2025
 * @version:    1.0
 */

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "contact_aliases")
public class ContactAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // The person who is giving the nickname (the user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Nicknamed person (e.g. person you are texting)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    // The personal nickname that the owner gives to the target
    @Column(nullable = false)
    private String aliasName;
}
