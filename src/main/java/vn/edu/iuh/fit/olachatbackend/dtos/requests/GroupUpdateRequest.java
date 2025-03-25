/*
 * @ (#) GroupUpdateRequest.java       1.0     28/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.dtos.requests;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 28/02/2025
 * @version:    1.0
 */

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupUpdateRequest {
    private String name;
    private String avatar;
}
