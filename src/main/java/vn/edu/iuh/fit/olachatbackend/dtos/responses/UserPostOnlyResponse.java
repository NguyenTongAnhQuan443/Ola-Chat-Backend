/*
 * @ (#) UserPostOnlyResponse.java    1.0    14/05/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package vn.edu.iuh.fit.olachatbackend.dtos.responses;/*
 * @description:
 * @author: Bao Thong
 * @date: 14/05/2025
 * @version: 1.0
 */

import lombok.Builder;
import lombok.Data;
import vn.edu.iuh.fit.olachatbackend.enums.Privacy;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserPostOnlyResponse {
    private Long postId;
    private String content;
    private List<MediaPostResponse> attachments;
    private Privacy privacy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PostUserResponse> likedUsers;
    private List<CommentHierarchyResponse> comments;
    private Long originalPostId;
    private PostResponse originalPost;
}
