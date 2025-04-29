/*
 * @ (#) CommentRepository.java    1.0    30/04/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package vn.edu.iuh.fit.olachatbackend.repositories;/*
 * @description:
 * @author: Bao Thong
 * @date: 30/04/2025
 * @version: 1.0
 */

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.olachatbackend.entities.Comment;
import vn.edu.iuh.fit.olachatbackend.entities.Post;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPost(Post post);
}
