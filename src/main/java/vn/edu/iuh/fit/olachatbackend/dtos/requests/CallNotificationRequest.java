package vn.edu.iuh.fit.olachatbackend.dtos.requests;

import lombok.*;
import vn.edu.iuh.fit.olachatbackend.enums.CallType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallNotificationRequest {
    private String conversationId;
    private CallType callType;
}
