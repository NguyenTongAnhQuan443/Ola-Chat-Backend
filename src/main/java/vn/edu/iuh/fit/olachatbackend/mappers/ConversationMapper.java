/*
 * @ (#) ConversationMapper.java       1.0     28/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.mappers;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 28/02/2025
 * @version:    1.0
 */

import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import vn.edu.iuh.fit.olachatbackend.dtos.ConversationDTO;
import vn.edu.iuh.fit.olachatbackend.entities.Conversation;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    ConversationMapper INSTANCE = Mappers.getMapper(ConversationMapper.class);

    @Mapping(source = "id", target = "id", qualifiedByName = "objectIdToString")
    ConversationDTO toDTO(Conversation conversation);

    @Mapping(source = "id", target = "id", qualifiedByName = "stringToObjectId")
    Conversation toEntity(ConversationDTO conversationDTO);

    @Named("objectIdToString")
    default String objectIdToString(ObjectId value) {
        return value != null ? value.toHexString() : null;
    }

    @Named("stringToObjectId")
    default ObjectId stringToObjectId(String value) {
        return value != null ? new ObjectId(value) : null;
    }
}
