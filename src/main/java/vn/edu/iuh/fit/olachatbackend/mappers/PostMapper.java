package vn.edu.iuh.fit.olachatbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.*;
import vn.edu.iuh.fit.olachatbackend.entities.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    @Mapping(target = "likedUsers", expression = "java(mapLikedUsers(post.getLikes()))")
    @Mapping(target = "comments", expression = "java(commentListToCommentHierarchyResponseList(post.getComments()))")
    @Mapping(target = "originalPostId", source = "originalPost.postId")
    @Mapping(target = "originalPost", expression = "java(post.getOriginalPost() != null ? toPostResponse(post.getOriginalPost()) : null)")
    @Mapping(target = "attachments", expression = "java(mediaListToMediaPostResponseList(post.getAttachments()))")
    @Mapping(target = "createdBy", expression = "java(userToPostUserResponse(post.getCreatedBy()))")
    PostResponse toPostResponse(Post post);

    List<PostResponse> toPostResponseList(List<Post> posts);

    List<CommentHierarchyResponse> commentListToCommentHierarchyResponseList(List<Comment> comments);

    default List<PostUserResponse> mapLikedUsers(List<Like> likes) {
        if (likes == null) {
            return List.of();
        }
        return likes.stream()
                .map(Like::getLikedBy)
                .map(this::userToPostUserResponse)
                .collect(Collectors.toList());
    }

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "displayName", target = "displayName")
    @Mapping(source = "avatar", target = "avatar")
    PostUserResponse userToPostUserResponse(User user);

    @Mapping(source = "mediaId", target = "mediaId")
    @Mapping(source = "fileUrl", target = "fileUrl")
    @Mapping(source = "fileType", target = "fileType")
    @Mapping(source = "originalFileName", target = "originalFileName")
    @Mapping(source = "publicId", target = "publicId")
    @Mapping(source = "uploadedBy.id", target = "userId")
    MediaPostResponse mediaToMediaPostResponse(Media media);

    List<MediaPostResponse> mediaListToMediaPostResponseList(List<Media> mediaList);

    @Mapping(target = "attachments", expression = "java(mediaListToMediaPostResponseList(post.getAttachments()))")
    @Mapping(target = "likedUsers", expression = "java(mapLikedUsers(post.getLikes()))")
    @Mapping(target = "originalPostId", source = "originalPost.postId")
    @Mapping(target = "originalPost", expression = "java(post.getOriginalPost() != null ? toPostResponse(post.getOriginalPost()) : null)")
    UserPostOnlyResponse toUserPostOnlyResponse(Post post);
}