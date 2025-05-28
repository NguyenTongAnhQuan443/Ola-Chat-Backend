/*
 * @ (#) NotificationRequest.java       1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.dtos.requests;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/04/2025
 * @version:    1.0
 */

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.edu.iuh.fit.olachatbackend.enums.NotificationType;

@Getter
@Setter
@Builder
public class NotificationRequest {
    private String title;
    private String body;
    private String senderId;
    private String receiverId;
    private String token;
    private NotificationType type;

}
