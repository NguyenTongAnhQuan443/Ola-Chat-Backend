package vn.edu.iuh.fit.olachatbackend.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.entities.File;

import java.io.IOException;
import java.util.Map;

@Service
public interface CloudinaryService {
    File uploadFileAndSaveToDB(MultipartFile file, Long associatedIDMessageId) throws IOException;
    //delete file and delete from database
    void deleteFile(String publicId) throws IOException;
    public String uploadImage(MultipartFile file) throws IOException;
    Map<String, Object> downloadFile(String publicId, String savePath) throws IOException;
}
