/*
 * @ (#) RegisterDeviceRequest.java       1.0     27/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.dtos.requests;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 27/04/2025
 * @version:    1.0
 */

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDeviceRequest {
    private String userId;
    private String token;
    private String deviceId;
}
