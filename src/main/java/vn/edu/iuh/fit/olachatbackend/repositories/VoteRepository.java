/*
 * @ (#) VoteRepository.java       1.0     27/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.repositories;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 27/04/2025
 * @version:    1.0
 */

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.olachatbackend.entities.Vote;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, String> {
    List<Vote> findByPollId(String pollId);
    List<Vote> findByPollIdAndUserId(String pollId, String userId);
}
