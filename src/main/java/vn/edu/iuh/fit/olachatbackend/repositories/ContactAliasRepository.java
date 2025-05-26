/*
 * @ (#) UserNicknameRepository.java       1.0     26/05/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.repositories;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 26/05/2025
 * @version:    1.0
 */

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.olachatbackend.entities.ContactAlias;

import java.util.Optional;

public interface ContactAliasRepository extends JpaRepository<ContactAlias, String> {
    Optional<ContactAlias> findByOwnerIdAndTargetId(String ownerId, String targetId);
}

