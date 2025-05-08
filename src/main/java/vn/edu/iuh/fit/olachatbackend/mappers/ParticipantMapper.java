package vn.edu.iuh.fit.olachatbackend.mappers;

import org.mapstruct.Mapper;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.ParticipantResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Participant;

@Mapper(componentModel = "spring")
public interface ParticipantMapper {
    ParticipantResponse toParticipantResponse(Participant user);
}
