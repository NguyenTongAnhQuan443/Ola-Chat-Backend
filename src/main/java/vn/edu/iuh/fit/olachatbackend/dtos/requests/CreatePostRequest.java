package vn.edu.iuh.fit.olachatbackend.dtos.requests;

import lombok.Data;

import java.util.List;

@Data
public class CreatePostRequest {
    private String content; // Optional
    private List<Long> mediaIds; // Optional
    private String privacy; // PUBLIC, FRIENDS, PRIVATE
}