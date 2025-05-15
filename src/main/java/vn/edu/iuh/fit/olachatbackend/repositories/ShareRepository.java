/*
 * @ (#) ShareRepository.java    1.0    02/05/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package vn.edu.iuh.fit.olachatbackend.repositories;/*
 * @description:
 * @author: Bao Thong
 * @date: 02/05/2025
 * @version: 1.0
 */

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.olachatbackend.entities.Post;
import vn.edu.iuh.fit.olachatbackend.entities.Share;

import java.util.List;

public interface ShareRepository extends JpaRepository<Share, Long> {
    void deleteBySharedPost(Post sharedPost);
    List<Share> findByPost(Post post);
}
