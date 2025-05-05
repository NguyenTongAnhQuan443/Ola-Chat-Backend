/*
 * @ (#) Poll.java       1.0     27/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.entities;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 27/04/2025
 * @version:    1.0
 */

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String question;
    private String groupId;
    private String creatorId;
    private LocalDateTime deadline;
    private boolean isPinned;
    private boolean allowMultipleChoices;
    private boolean allowAddOptions;
    private boolean hideResultsUntilVoted;
    private boolean hideVoters;
    private boolean isLocked;
}
