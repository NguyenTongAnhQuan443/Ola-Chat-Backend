/*
 * @ (#) AddOptionRequest.java       1.0     27/04/2025
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

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddOptionRequest {
    private String optionText;
}
