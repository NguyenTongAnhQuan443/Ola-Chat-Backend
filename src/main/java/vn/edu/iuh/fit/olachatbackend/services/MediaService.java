package vn.edu.iuh.fit.olachatbackend.services;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.entities.Media;

import java.io.IOException;

public interface MediaService {
    Media uploadMedia(MultipartFile file) throws IOException;
}