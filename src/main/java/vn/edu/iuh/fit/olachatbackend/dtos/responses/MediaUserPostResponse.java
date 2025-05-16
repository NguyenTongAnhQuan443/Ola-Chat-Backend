/*
 * @ (#) MediaPostResponse.java    1.0    14/05/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package vn.edu.iuh.fit.olachatbackend.dtos.responses;/*
 * @description:
 * @author: Bao Thong
 * @date: 14/05/2025
 * @version: 1.0
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MediaUserPostResponse {
    private String userId;
    private Long mediaId;
    private String fileUrl;
    private String fileType;
    private String originalFileName;
    private String publicId;
    private Long postId;
}
