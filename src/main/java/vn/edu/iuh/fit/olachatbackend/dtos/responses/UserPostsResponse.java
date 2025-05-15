/*
 * @ (#) UserPostsResponse.java    1.0    14/05/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package vn.edu.iuh.fit.olachatbackend.dtos.responses;/*
 * @description:
 * @author: Bao Thong
 * @date: 14/05/2025
 * @version: 1.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonPropertyOrder({"createdBy", "totalPages", "currentPage", "pageSize", "posts"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPostsResponse {
    @JsonProperty("createdBy")
    private PostUserResponse createdBy;

    private List<UserPostOnlyResponse> posts;

    private int totalPages;
    private int currentPage;
    private int pageSize;
}
