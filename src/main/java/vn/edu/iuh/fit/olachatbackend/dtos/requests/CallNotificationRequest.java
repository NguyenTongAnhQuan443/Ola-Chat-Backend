package vn.edu.iuh.fit.olachatbackend.dtos.requests;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallNotificationRequest {
    private String title;
    private String body;
    private String senderId;
    private String receiverId;
    private String channelId;
    private String agoraToken;
    private String callType; // "VIDEO" | "VOICE"
    private String token; // FCM token của người nhận
    private String action; // "OFFER", "ACCEPT", "REJECT", "CANCEL"
    private Map<String, String> extra; // Thêm data nếu muốn
}
