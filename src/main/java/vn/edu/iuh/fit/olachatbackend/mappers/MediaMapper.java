/*
 * @ (#) MediaMapper.java    1.0    14/05/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package vn.edu.iuh.fit.olachatbackend.mappers;/*
 * @description:
 * @author: Bao Thong
 * @date: 14/05/2025
 * @version: 1.0
 */

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MediaPostResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Media;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MediaMapper {

    MediaMapper INSTANCE = Mappers.getMapper(MediaMapper.class);

    MediaPostResponse toResponse(Media media);

    List<MediaPostResponse> toResponseList(List<Media> mediaList);
}
