package vn.edu.iuh.fit.olachatbackend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentedByResponse {
    private String username;
    private String displayName;
    private String avatar;
}