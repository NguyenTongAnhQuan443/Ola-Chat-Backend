/*
 * @ (#) QrLoginSession.java       1.0     28/05/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.dtos;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 28/05/2025
 * @version:    1.0
 */

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrLoginSession implements Serializable {
    private String sessionId;
    private String ipAddress;
    private String userAgent;

    private String deviceName;
    private String deviceId;
    private String locationHint;

    private LocalDateTime createdAt;
    private boolean isConfirmed;
    private String confirmedUserId;
}
