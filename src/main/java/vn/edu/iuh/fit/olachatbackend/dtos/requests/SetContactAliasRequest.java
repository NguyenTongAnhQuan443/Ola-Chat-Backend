/*
 * @ (#) SetNickNameRequest.java       1.0     26/05/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.dtos.requests;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 26/05/2025
 * @version:    1.0
 */

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SetContactAliasRequest {
    private String userId;
    private String aliasName;
}
