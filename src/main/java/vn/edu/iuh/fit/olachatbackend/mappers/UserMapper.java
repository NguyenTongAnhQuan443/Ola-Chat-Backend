package vn.edu.iuh.fit.olachatbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import vn.edu.iuh.fit.olachatbackend.dtos.requests.UserRegisterRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserResponse;
import vn.edu.iuh.fit.olachatbackend.entities.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role", source = "role")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "userId", source = "id")
    UserResponse toUserResponse(User user);

    @Mapping(target = "role", source = "role")
    User toUser(UserRegisterRequest user);
}
