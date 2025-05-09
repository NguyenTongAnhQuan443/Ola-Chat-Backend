/*
 * @ (#) UploadFilesResponse.java    1.0    09/05/2025
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
import vn.edu.iuh.fit.olachatbackend.entities.File;
import vn.edu.iuh.fit.olachatbackend.entities.User;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadFilesResponse {
    private User uploadedBy;
    private List<File> files;
}
