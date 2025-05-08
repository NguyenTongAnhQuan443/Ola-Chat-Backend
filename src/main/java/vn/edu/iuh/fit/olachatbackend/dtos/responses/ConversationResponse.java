package vn.edu.iuh.fit.olachatbackend.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.edu.iuh.fit.olachatbackend.entities.LastMessage;
import vn.edu.iuh.fit.olachatbackend.entities.Participant;
import vn.edu.iuh.fit.olachatbackend.enums.ConversationType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ConversationResponse {
    private String id;
    private String name;
    private String avatar;
    private ConversationType type;
    private LastMessage lastMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
//    private List<ParticipantResponse> participants;
}