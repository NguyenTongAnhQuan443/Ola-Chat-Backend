package vn.edu.iuh.fit.olachatbackend.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MediaPostResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MediaUserPostResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PostUserResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserMediaResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Media;
import vn.edu.iuh.fit.olachatbackend.entities.Post;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.enums.Privacy;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.exceptions.UnauthorizedException;
import vn.edu.iuh.fit.olachatbackend.repositories.FriendRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.MediaRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.UserRepository;
import vn.edu.iuh.fit.olachatbackend.services.MediaService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MediaServiceImpl implements MediaService {
    private final Cloudinary cloudinary;
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    public MediaServiceImpl(Cloudinary cloudinary, MediaRepository mediaRepository, UserRepository userRepository) {
        this.cloudinary = cloudinary;
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Media uploadMedia(MultipartFile file) throws IOException {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Determine the resource type (image or video)
        String resourceType = file.getContentType() != null && file.getContentType().toLowerCase().contains("video")
                ? "video"
                : "image";

        // Upload to Cloudinary
        Map<?, ?> uploadResult = cloudinary.uploader()
                .upload(file.getBytes(), ObjectUtils.asMap("resource_type", resourceType));

        String url = uploadResult.get("secure_url").toString();
        String publicId = uploadResult.get("public_id").toString();
        String originalFileName = file.getOriginalFilename();

        // Save media metadata to the database
        Media media = Media.builder()
                .fileUrl(url)
                .fileType(file.getContentType())
                .uploadedAt(LocalDateTime.now())
                .uploadedBy(user)
                .originalFileName(originalFileName)
                .publicId(publicId)
                .build();

        return mediaRepository.save(media);
    }

    @Override
    public void deleteMediaFromCloudinary(List<Media> mediaList) throws IOException {
        if (mediaList != null && !mediaList.isEmpty()) {
            for (Media media : mediaList) {
                // Determine the resource type based on the file type
                String resourceType = media.getFileType() != null && media.getFileType().toLowerCase().contains("video")
                        ? "video"
                        : "image";

                // Delete the file from Cloudinary using its publicId and resourceType
                cloudinary.uploader().destroy(media.getPublicId(), ObjectUtils.asMap("resource_type", resourceType));

                // Remove the media record from the database
                mediaRepository.delete(media);
            }
        }
    }

    @Override
    public UserMediaResponse getMediaByUserId(String userId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Current user not found"));

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        List<Media> allMedia = mediaRepository.findByUploadedByOrderByUploadedAtDescPost_PostIdAsc(targetUser);

        List<MediaUserPostResponse> mediaResponses = allMedia.stream()
                .filter(media -> {
                    Post post = media.getPost();
                    if (post == null) return false;

                    if (post.getCreatedBy().equals(currentUser)) return true;
                    if (post.getPrivacy() == Privacy.PUBLIC) return true;
                    if (post.getPrivacy() == Privacy.FRIENDS && isFriend(post.getCreatedBy(), currentUser)) return true;

                    return false;
                })
                .map(media -> MediaUserPostResponse.builder()
                        .userId(media.getUploadedBy().getId())
                        .mediaId(media.getMediaId())
                        .fileUrl(media.getFileUrl())
                        .fileType(media.getFileType())
                        .originalFileName(media.getOriginalFileName())
                        .publicId(media.getPublicId())
                        .postId(media.getPost().getPostId())
                        .build())
                .collect(Collectors.toList());

        PostUserResponse uploaderInfo = PostUserResponse.builder()
                .userId(targetUser.getId())
                .username(targetUser.getUsername())
                .displayName(targetUser.getDisplayName())
                .avatar(targetUser.getAvatar())
                .build();

        return UserMediaResponse.builder()
                .uploadBy(uploaderInfo)
                .listMedia(mediaResponses)
                .build();
    }

    @Override
    public void deleteMediaById(Long mediaId) throws IOException {
        // Lấy user hiện tại
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Current user not found"));

        // Tìm media theo ID
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new NotFoundException("Media not found with ID: " + mediaId));

        // Kiểm tra quyền xóa (chỉ chủ sở hữu mới được xóa)
        if (!media.getUploadedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this media");
        }

        // Xác định loại tài nguyên để xóa trên Cloudinary
        String resourceType = media.getFileType() != null && media.getFileType().toLowerCase().contains("video")
                ? "video"
                : "image";

        // Xóa khỏi Cloudinary
        cloudinary.uploader().destroy(media.getPublicId(), ObjectUtils.asMap("resource_type", resourceType));

        // Xóa khỏi cơ sở dữ liệu
        mediaRepository.delete(media);
    }

    private boolean isFriend(User user1, User user2) {
        return friendRepository.findByUserIdAndFriendId(user1.getId(), user2.getId())
                .or(() -> friendRepository.findByUserIdAndFriendId(user2.getId(), user1.getId()))
                .isPresent();
    }
}