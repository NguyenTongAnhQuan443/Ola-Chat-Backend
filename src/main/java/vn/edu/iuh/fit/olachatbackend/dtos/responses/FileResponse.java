/*
 * @ (#) FileResponse.java    1.0    09/05/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package vn.edu.iuh.fit.olachatbackend.dtos.responses;/*
 * @description:
 * @author: Bao Thong
 * @date: 09/05/2025
 * @version: 1.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {
    private Long fileId;
    private String fileUrl;
    private String fileType;
    private double fileSize;
    private String publicId;
    private String resourceType;
    private String originalFileName;
    private Long associatedIDMessageId;
}