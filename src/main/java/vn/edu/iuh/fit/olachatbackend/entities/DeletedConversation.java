package vn.edu.iuh.fit.olachatbackend.entities;

import jakarta.persistence.Id;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeletedConversation {
    @Id
    private ObjectId id;
    private String userId;
    private String conversationId;
    private LocalDateTime deletedAt;
}
