/*
 * @ (#) User.java       1.0     14/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.entities;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/02/2025
 * @version:    1.0
 */

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.edu.iuh.fit.olachatbackend.enums.AuthProvider;
import vn.edu.iuh.fit.olachatbackend.enums.Role;
import vn.edu.iuh.fit.olachatbackend.enums.UserStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String username;
    private String password;
    private String displayName;
    private String nickname;

    @Column(unique = true)
    private String email;
    @Column(length = 1000)
    private String avatar;

    private String bio;
    private LocalDateTime dob;
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", length = 20)
    private AuthProvider authProvider;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = UserStatus.ACTIVE;
        }
        if (this.role == null) {
            this.role = Role.USER; // mặc định role là USER
        }
        if (this.authProvider == null) {
            this.authProvider = AuthProvider.LOCAL; // mặc định authProvider là LOCAL
        }
    }


    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
