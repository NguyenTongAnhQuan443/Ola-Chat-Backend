/*
 * @ (#) UserSearchResponse.java       1.0     08/05/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.dtos.responses;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 08/05/2025
 * @version:    1.0
 */

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSearchResponse {
    private String userId;
    private String email;
    private String displayName;
    private String nickname;
    private String avatar;
    private String bio;
    private LocalDateTime dob;
    private String friendAction; // "Kết bạn", "Hủy kết bạn", "Đồng ý", "Nhắn tin"
    private boolean isFriend;
}

