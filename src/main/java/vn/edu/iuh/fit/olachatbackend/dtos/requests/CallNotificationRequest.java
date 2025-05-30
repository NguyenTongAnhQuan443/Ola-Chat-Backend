package vn.edu.iuh.fit.olachatbackend.dtos.requests;

import lombok.*;
import vn.edu.iuh.fit.olachatbackend.enums.CallActionType;
import vn.edu.iuh.fit.olachatbackend.enums.CallType;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallNotificationRequest {
    private String title;
    private String body;
    private String conversationId;
    private CallType callType;
    private CallActionType callActionType;
}
