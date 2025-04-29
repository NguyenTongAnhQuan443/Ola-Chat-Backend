package vn.edu.iuh.fit.olachatbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PostResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Like;
import vn.edu.iuh.fit.olachatbackend.entities.Post;
import vn.edu.iuh.fit.olachatbackend.entities.User;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    @Mapping(target = "likedUsers", expression = "java(mapLikedUsers(post.getLikes()))")
    @Mapping(target = "comments", source = "comments")
    PostResponse toPostResponse(Post post);

    List<PostResponse> toPostResponseList(List<Post> posts);

    default List<User> mapLikedUsers(List<Like> likes) {
        return likes.stream()
                .map(Like::getLikedBy)
                .collect(Collectors.toList());
    }
}