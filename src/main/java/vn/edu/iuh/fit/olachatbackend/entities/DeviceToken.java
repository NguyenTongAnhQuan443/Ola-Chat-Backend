/*
 * @ (#) DeviceToken.java       1.0     06/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.entities;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 06/04/2025
 * @version:    1.0
 */

import jakarta.persistence.*;

@Entity
public class DeviceToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String userId;
    private String token;
}
