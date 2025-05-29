package vn.edu.iuh.fit.olachatbackend.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.FileResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UploadFilesResponse;
import vn.edu.iuh.fit.olachatbackend.entities.File;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public interface CloudinaryService {
    //delete file and delete from database
    void deleteFile(String publicId) throws IOException;
    public String uploadImage(MultipartFile file) throws IOException;
    Map<String, Object> downloadFile(String publicId, String savePath) throws IOException;
    UploadFilesResponse uploadFileAndSaveToDB_v2(List<MultipartFile> files, Long associatedIDMessageId) throws IOException;
    File uploadFileAndSaveToDB_v3(MultipartFile file, Long associatedIDMessageId) throws IOException;
    FileResponse uploadAudioFile(MultipartFile audioFile) throws IOException;
}
