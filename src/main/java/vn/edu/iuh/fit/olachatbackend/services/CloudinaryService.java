package vn.edu.iuh.fit.olachatbackend.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    public Map uploadFile(MultipartFile file, String folderName) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", folderName));
    }

    public Map uploadVideo(MultipartFile file, String folderName) throws IOException {
        return cloudinary
                .uploader()
                .upload(file.getBytes(), ObjectUtils.asMap("resource_type", "video", "folder", folderName));
    }
}
