package vn.edu.iuh.fit.olachatbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestDTO {
    private String senderId;
    private String receiverId;
}
