/*
 * @ (#) LikeRepository.java    1.0    29/04/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package vn.edu.iuh.fit.olachatbackend.repositories;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/04/2025
 * @version: 1.0
 */

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.olachatbackend.entities.Like;
import vn.edu.iuh.fit.olachatbackend.entities.Post;
import vn.edu.iuh.fit.olachatbackend.entities.User;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByPostAndLikedBy(Post post, User likedBy);

    List<Like> findAllByPost(Post post);
}
