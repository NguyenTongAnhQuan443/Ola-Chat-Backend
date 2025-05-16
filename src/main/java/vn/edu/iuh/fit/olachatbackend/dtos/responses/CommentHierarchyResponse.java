package vn.edu.iuh.fit.olachatbackend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentHierarchyResponse {
    private Long commentId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; // New field for last update time
    private PostUserResponse commentedBy; // Sender's information
    private List<CommentHierarchyResponse> replies;
}