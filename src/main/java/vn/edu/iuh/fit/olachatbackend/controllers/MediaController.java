/*
 * @ (#) MediaController.java    1.0    06/05/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package vn.edu.iuh.fit.olachatbackend.controllers;/*
 * @description:
 * @author: Bao Thong
 * @date: 06/05/2025
 * @version: 1.0
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.entities.Media;
import vn.edu.iuh.fit.olachatbackend.services.MediaService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {
    @Autowired
    private MediaService mediaService;

    @GetMapping("/user")
    public ResponseEntity<List<Media>> getMediaByUserId(@RequestParam String userId) {
        List<Media> mediaList = mediaService.getMediaByUserId(userId);
        return ResponseEntity.ok(mediaList);
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<List<Media>> deleteMedia(
            @PathVariable Long mediaId,
            @RequestParam String userId) throws IOException {
        // Xóa media và trả về danh sách media còn lại của user
        List<Media> remainingMedia = mediaService.deleteMediaByIdAndReturnRemaining(mediaId, userId);
        return ResponseEntity.ok(remainingMedia);
    }
}
