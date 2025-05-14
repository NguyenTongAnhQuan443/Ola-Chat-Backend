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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MessageResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserMediaResponse;
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
    public ResponseEntity<MessageResponse<UserMediaResponse>> getMediaByUserId(@RequestParam String userId) {
        UserMediaResponse mediaList = mediaService.getMediaByUserId(userId);
        MessageResponse<UserMediaResponse> response = MessageResponse.<UserMediaResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User media retrieved successfully")
                .success(true)
                .data(mediaList)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<MessageResponse<Object>> deleteMedia(@PathVariable Long mediaId) throws IOException {
        mediaService.deleteMediaById(mediaId);

        MessageResponse<Object> response = MessageResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Media deleted successfully")
                .success(true)
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }
}
