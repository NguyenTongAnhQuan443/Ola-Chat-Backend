package vn.edu.iuh.fit.olachatbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FriendStatusUpdate {
    private String type;        // "accepted", "rejected", etc.
    private String senderId;    // Người gửi lời mời (sẽ nhận socket)
    private String receiverId;  // Người chấp nhận lời mời
    private String displayName; // Tên người chấp nhận
}
