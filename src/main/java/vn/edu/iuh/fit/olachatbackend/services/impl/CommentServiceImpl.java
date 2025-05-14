/*
 * @ (#) CommentServiceImpl.java    1.0    02/05/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package vn.edu.iuh.fit.olachatbackend.services.impl;/*
 * @description:
 * @author: Bao Thong
 * @date: 02/05/2025
 * @version: 1.0
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.CommentHierarchyResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PostUserResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Comment;
import vn.edu.iuh.fit.olachatbackend.entities.Post;
import vn.edu.iuh.fit.olachatbackend.repositories.CommentRepository;
import vn.edu.iuh.fit.olachatbackend.services.CommentService;

import java.util.*;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentRepository commentRepository;

    // Phương thức xây dựng cấu trúc phân cấp
    @Override
    public List<CommentHierarchyResponse> buildCommentHierarchy(List<Comment> comments) {
        // Map để lưu trữ các bình luận theo ID
        Map<Long, CommentHierarchyResponse> commentMap = new HashMap<>();

        // Chuyển đổi tất cả bình luận sang CommentHierarchyResponse và lưu vào Map
        for (Comment comment : comments) {
            commentMap.put(comment.getCommentId(), mapToHierarchyResponse(comment));
        }

        // Danh sách bình luận gốc (parent_comment_id = null)
        List<CommentHierarchyResponse> rootComments = new ArrayList<>();

        // Xây dựng cấu trúc phân cấp
        for (Comment comment : comments) {
            if (comment.getParentComment() == null) {
                // Nếu là bình luận gốc
                rootComments.add(commentMap.get(comment.getCommentId()));
            } else {
                // Nếu là bình luận con, thêm vào replies của bình luận cha
                CommentHierarchyResponse parent = commentMap.get(comment.getParentComment().getCommentId());
                parent.getReplies().add(commentMap.get(comment.getCommentId()));
            }
        }

        return rootComments;
    }

    // Phương thức chuyển đổi từ Comment sang CommentHierarchyResponse
    private CommentHierarchyResponse mapToHierarchyResponse(Comment comment) {
        return CommentHierarchyResponse.builder()
                .commentId(comment.getCommentId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .commentedBy(PostUserResponse.builder()
                        .userId(comment.getCommentedBy().getId())
                        .username(comment.getCommentedBy().getUsername())
                        .displayName(comment.getCommentedBy().getDisplayName())
                        .avatar(comment.getCommentedBy().getAvatar())
                        .build())
                .replies(new ArrayList<>()) // Khởi tạo danh sách rỗng cho replies
                .build();
    }

    @Override
    public List<Comment> findAllByPost(Post post) {
        return commentRepository.findAllByPost(post);
    }

    @Override
    public List<Comment> findAllByParentComment(Comment parentComment) {
        return commentRepository.findAllByParentComment(parentComment);
    }

    @Override
    public Optional<Comment> findById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    @Override
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public void delete(Comment comment) {
        commentRepository.delete(comment);
    }
}
