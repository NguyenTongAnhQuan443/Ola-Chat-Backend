/*
 * @ (#) SenderInfoResponse.java       1.0     20/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.dtos.responses;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 20/04/2025
 * @version:    1.0
 */

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SenderInfoResponse {
    private String id;
    private String name;
    private String avatar;
}
