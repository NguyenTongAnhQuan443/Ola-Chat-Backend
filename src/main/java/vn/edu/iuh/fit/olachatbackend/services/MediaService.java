package vn.edu.iuh.fit.olachatbackend.services;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.entities.Media;

import java.io.IOException;
import java.util.List;

public interface MediaService {
    Media uploadMedia(MultipartFile file) throws IOException;
    void deleteMediaFromCloudinary(List<Media> mediaList) throws IOException;
}