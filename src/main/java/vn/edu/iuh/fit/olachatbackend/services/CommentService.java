/*
 * @ (#) CommentService.java    1.0    02/05/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package vn.edu.iuh.fit.olachatbackend.services;/*
 * @description:
 * @author: Bao Thong
 * @date: 02/05/2025
 * @version: 1.0
 */

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.CommentHierarchyResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Comment;
import vn.edu.iuh.fit.olachatbackend.entities.Post;

import java.util.List;
import java.util.Optional;


public interface CommentService {
    List<CommentHierarchyResponse> buildCommentHierarchy(List<Comment> comments);
    //find all by post
    List<Comment> findAllByPost(Post post);
    //find all by parent comment
    List<Comment> findAllByParentComment(Comment parentComment);
    //find by id
    Optional<Comment> findById(Long commentId);
    //save comment
    Comment save(Comment comment);
    //delete comment
    void delete(Comment comment);
}
