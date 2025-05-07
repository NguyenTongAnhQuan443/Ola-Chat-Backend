package vn.edu.iuh.fit.olachatbackend.dtos.responses;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import org.bson.types.ObjectId;
import vn.edu.iuh.fit.olachatbackend.enums.ParticipantRole;

import java.time.LocalDateTime;

@Data
public class ParticipantResponse {
    private String userId;
    private String displayName;
    private String avatar;
    private ParticipantRole role;
    private LocalDateTime joinedAt;
    private boolean muted = false;
}
