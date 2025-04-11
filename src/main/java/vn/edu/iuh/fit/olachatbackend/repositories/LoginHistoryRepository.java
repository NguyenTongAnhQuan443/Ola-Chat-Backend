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
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.olachatbackend.entities.LoginHistory;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, String> {
    Optional<LoginHistory> findFirstByUserIdOrderByLoginTimeDesc(String userId);
    List<LoginHistory> findAllByUserIdOrderByLoginTimeDesc(String userId);
}
