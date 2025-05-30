/*
 * @ (#) CallNotificationRequest_v2.java    1.0    30/05/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package vn.edu.iuh.fit.olachatbackend.dtos.requests;
/*
 * @description: Request model for group call notifications
 * @author: Bao Thong
 * @date: 30/05/2025
 * @version: 1.0
 */

import lombok.*;
import vn.edu.iuh.fit.olachatbackend.enums.CallActionType;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallNotificationRequest_v2 {
    private String title;
    private String body;
    private String senderId;
    private String channelId;
    private String callType; // "VIDEO" | "VOICE"
    private List<Receiver> receivers; // List of receivers with their actions

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Receiver {
        private String userId;
        private String token; // FCM token của người nhận
        private CallActionType action; // "CALLING", "OFFER", "ACCEPT", "REJECT", "CANCEL" etc.
    }
}