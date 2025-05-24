
package vn.edu.iuh.fit.olachatbackend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.bson.types.ObjectId;

import org.springframework.data.mongodb.core.mapping.Document;
import vn.edu.iuh.fit.olachatbackend.enums.MessageStatus;
import vn.edu.iuh.fit.olachatbackend.enums.MessageType;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "message")
public class Message {
    @Id
    private ObjectId id;
    private String senderId;
    private ObjectId conversationId;
    private String content;
    @Enumerated(EnumType.STRING)
    private MessageType type;
    private List<String> mediaUrls;

    private MessageStatus status;
    private List<DeliveryStatus> deliveryStatus;
    private List<ReadStatus> readStatus;
    private LocalDateTime createdAt;

    private List<DeletedStatus> deletedStatus;
    private boolean recalled = false;
    private List<Mention> mentions;
    private ObjectId replyTo;

    @Data
    @Builder
    public static class DeletedStatus {
        private String userId;
        private LocalDateTime deletedAt;
    }
}



