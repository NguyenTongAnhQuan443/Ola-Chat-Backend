/*
 * @ (#) MessageMapper.java       1.0     20/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.mappers;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 20/04/2025
 * @version:    1.0
 */

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import vn.edu.iuh.fit.olachatbackend.dtos.MessageResponseDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.MessageRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.SenderInfoResponse;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.repositories.UserRepository;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class MessageMapper {

    @Autowired
    private UserRepository userRepository;

    @Mapping(target = "sender", expression = "java(toSenderInfo(request.getSenderId()))")
    public abstract MessageResponseDTO toResponseDTO(MessageRequest request);

    protected SenderInfoResponse toSenderInfo(String senderId) {
        if (senderId == null) return null;
        User user = userRepository.findById(senderId).orElse(null);
        if (user == null) return null;
        return new SenderInfoResponse(user.getId(), user.getDisplayName(), user.getAvatar());
    }
}
