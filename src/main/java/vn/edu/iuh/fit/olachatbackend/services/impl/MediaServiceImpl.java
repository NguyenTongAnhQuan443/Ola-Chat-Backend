package vn.edu.iuh.fit.olachatbackend.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.entities.Media;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.repositories.MediaRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.UserRepository;
import vn.edu.iuh.fit.olachatbackend.services.MediaService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class MediaServiceImpl implements MediaService {
    private final Cloudinary cloudinary;
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;

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
}