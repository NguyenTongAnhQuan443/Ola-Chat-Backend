package vn.edu.iuh.fit.olachatbackend.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShareResponse {
    private Long shareId;
    private PostUserResponse sharedBy;
    private LocalDateTime sharedAt;
}