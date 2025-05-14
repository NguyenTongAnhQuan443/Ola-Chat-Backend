/*
 * @ (#) CloudinaryServiceImpl.java    1.0    03/04/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package vn.edu.iuh.fit.olachatbackend.services.impl;/*
 * @description:
 * @author: Bao Thong
 * @date: 03/04/2025
 * @version: 1.0
 */

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.FileResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UploadFilesResponse;
import vn.edu.iuh.fit.olachatbackend.entities.File;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.repositories.FileRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.UserRepository;
import vn.edu.iuh.fit.olachatbackend.services.CloudinaryService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public CloudinaryServiceImpl(Cloudinary cloudinary, FileRepository fileRepository, UserRepository userRepository) {
        this.cloudinary = cloudinary;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    @Override
    public File uploadFileAndSaveToDB_v3(MultipartFile file, Long associatedIDMessageId) throws IOException {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));

        String resourceType = "image"; // default
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";

        if (contentType.contains("pdf") || contentType.contains("doc") ||
                contentType.contains("xls") || contentType.contains("ppt") ||
                contentType.contains("text") || contentType.contains("csv")) {
            resourceType = "raw";
        } else if (contentType.contains("video")) {
            resourceType = "video";
        }

        // Lấy đuôi file (extension) từ tên gốc
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        String baseFilename = "unknown";

        if (originalFilename != null && originalFilename.contains(".")) {
            int lastDotIndex = originalFilename.lastIndexOf(".");
            baseFilename = originalFilename.substring(0, lastDotIndex);
            extension = originalFilename.substring(lastDotIndex); // giữ cả dấu chấm
        } else if (originalFilename != null) {
            baseFilename = originalFilename;
        }

        // Chuẩn hóa baseFilename (không bao gồm đuôi)
        String sanitizedBaseFilename = baseFilename.replaceAll("[^a-zA-Z0-9.\\-_]", "_");

        // Tạo public_id có định dạng: sanitizedBaseFilename--UUID + extension
        String publicId;
        if ("raw".equals(resourceType)) {
            publicId = sanitizedBaseFilename + "--" + UUID.randomUUID() + extension;
        } else {
            publicId = sanitizedBaseFilename + "--" + UUID.randomUUID(); // Không có đuôi
        }

        // Upload lên Cloudinary, gắn public_id rõ ràng
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", resourceType,
                        "public_id", publicId
                )
        );

        String url = uploadResult.get("secure_url").toString();
        String returnedPublicId = uploadResult.get("public_id").toString();

        File fileUpload = File.builder()
                .fileUrl(url)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .uploadedBy(user)
                .associatedIDMessageId(associatedIDMessageId)
                .publicId(returnedPublicId)
                .resourceType(resourceType)
                .originalFileName(originalFilename)
                .build();

        fileRepository.save(fileUpload);
        return fileUpload;
    }

    @Override
    public UploadFilesResponse uploadFileAndSaveToDB_v2(List<MultipartFile> files, Long associatedIDMessageId) throws IOException {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));

        List<FileResponse> uploadedFileResponses = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            // Determine the resource type based on file content type
            String resourceType = "image"; // default
            String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";

            if (contentType.contains("pdf") || contentType.contains("doc") ||
                    contentType.contains("xls") || contentType.contains("ppt") ||
                    contentType.contains("text") || contentType.contains("csv")) {
                resourceType = "raw";
            } else if (contentType.contains("video")) {
                resourceType = "video";
            }

            // Lấy đuôi file (extension) từ tên gốc
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            String baseFilename = "unknown";

            if (originalFilename != null && originalFilename.contains(".")) {
                int lastDotIndex = originalFilename.lastIndexOf(".");
                baseFilename = originalFilename.substring(0, lastDotIndex);
                extension = originalFilename.substring(lastDotIndex); // giữ cả dấu chấm
            } else if (originalFilename != null) {
                baseFilename = originalFilename;
            }

            // Chuẩn hóa baseFilename (không bao gồm đuôi)
            String sanitizedBaseFilename = baseFilename.replaceAll("[^a-zA-Z0-9.\\-_]", "_");

            // Tạo public_id có định dạng: sanitizedBaseFilename--UUID + extension
            String publicId;
            if ("raw".equals(resourceType)) {
                publicId = sanitizedBaseFilename + "--" + UUID.randomUUID() + extension;
            } else {
                publicId = sanitizedBaseFilename + "--" + UUID.randomUUID(); // Không có đuôi
            }

            // Upload lên Cloudinary, gắn public_id rõ ràng
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", resourceType,
                            "public_id", publicId
                    )
            );

            String url = uploadResult.get("secure_url").toString();
            String returnedPublicId = uploadResult.get("public_id").toString();

            // Lưu vào DB
            File fileUpload = File.builder()
                    .fileUrl(url)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .uploadedAt(LocalDateTime.now())
                    .uploadedBy(user)
                    .associatedIDMessageId(associatedIDMessageId)
                    .publicId(returnedPublicId)
                    .resourceType(resourceType)
                    .originalFileName(originalFilename)
                    .build();

            fileRepository.save(fileUpload);

            FileResponse fileResponse = new FileResponse(
                    fileUpload.getFileId(),
                    fileUpload.getFileUrl(),
                    fileUpload.getFileType(),
                    fileUpload.getFileSize(),
                    fileUpload.getPublicId(),
                    fileUpload.getResourceType(),
                    fileUpload.getOriginalFileName(),
                    fileUpload.getAssociatedIDMessageId()
            );


            uploadedFileResponses.add(fileResponse);
        }

        return new UploadFilesResponse(user, uploadedFileResponses);
    }


    @Override
    public Map<String, Object> downloadFile(String publicId, String savePath) throws IOException {
        // Lấy thông tin file từ DB
        File fileEntity = fileRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException("File not found with public ID: " + publicId));

        String fileUrl = fileEntity.getFileUrl();
        String originalFileName = fileEntity.getOriginalFileName();

        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new NotFoundException("Không tìm thấy URL cho file có publicId: " + publicId);
        }

        // Tải file bằng HttpURLConnection
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + responseCode);
        }

        byte[] fileData;
        try (InputStream inputStream = connection.getInputStream()) {
            fileData = inputStream.readAllBytes();

            // Lưu file vào thư mục người dùng yêu cầu
            java.io.File saveDir = new java.io.File(savePath);
            if (!saveDir.exists() && !saveDir.mkdirs()) {
                throw new IOException("Failed to create directories at: " + savePath);
            }

            try (FileOutputStream fos = new FileOutputStream(savePath + java.io.File.separator + originalFileName)) {
                fos.write(fileData);
            }
        }

        return Map.of(
                "fileName", originalFileName,
                "location", savePath + java.io.File.separator + originalFileName,
                "message", "Tải xuống thành công"
        );
    }

    @Override
    public void deleteFile(String publicId) throws IOException {
        // Lấy thông tin file từ cơ sở dữ liệu
        File file = fileRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException("File not found"));

        // Lấy resourceType từ file
        String resourceType = file.getResourceType();

        // Xóa file trên Cloudinary với resourceType chính xác
        Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));

        // Kiểm tra kết quả trả về từ Cloudinary
        if (!"ok".equals(result.get("result"))) {
            throw new IOException("Failed to delete file from Cloudinary: " + result.get("result"));
        }

        // Xóa file khỏi cơ sở dữ liệu
        fileRepository.delete(file);
    }

    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", "travel"
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        return uploadResult.get("url").toString();
    }
}

