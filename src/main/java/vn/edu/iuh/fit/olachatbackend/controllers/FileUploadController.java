package vn.edu.iuh.fit.olachatbackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.services.CloudinaryService;

import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileUploadController {
    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/upload/image")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file, @RequestParam("folder") String folderName)
            throws IOException, IOException {
        return ResponseEntity.ok(cloudinaryService.uploadFile(file, folderName));
    }

    @PostMapping("/upload/video")
    public ResponseEntity<?> uploadVideo(
            @RequestParam("file") MultipartFile file, @RequestParam("folder") String folderName) throws IOException {
        return ResponseEntity.ok(cloudinaryService.uploadVideo(file, folderName));
    }
}
