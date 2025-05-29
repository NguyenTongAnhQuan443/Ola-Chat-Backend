/*
 * @ (#) QrSessionRequest.java       1.0     28/05/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.dtos.requests;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 28/05/2025
 * @version:    1.0
 */

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrSessionRequest {
    private String deviceName;
    private String deviceId;
    private String locationHint;
}
