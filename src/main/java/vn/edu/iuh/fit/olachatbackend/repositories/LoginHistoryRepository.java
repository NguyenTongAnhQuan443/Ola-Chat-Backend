/*
 * @ (#) LoginHistoryRepository.java       1.0     30/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.repositories;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 30/03/2025
 * @version:    1.0
 */

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.olachatbackend.entities.LoginHistory;
import vn.edu.iuh.fit.olachatbackend.enums.LoginHistoryStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, String> {
    Optional<LoginHistory> findFirstByUserIdOrderByLoginTimeDesc(String userId);
    List<LoginHistory> findAllByUserIdOrderByLoginTimeDesc(String userId);

    @Query("SELECT DISTINCT lh.userAgent FROM LoginHistory lh WHERE lh.user.id = :userId")
    List<String> findDistinctUserAgentsByUserId(@Param("userId") String userId);

    List<LoginHistory> findByStatus(LoginHistoryStatus status);
    Optional<LoginHistory> findTopByUserIdAndStatusOrderByLoginTimeDesc(String userId, LoginHistoryStatus status);
}
