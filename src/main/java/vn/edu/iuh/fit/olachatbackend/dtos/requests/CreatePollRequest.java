/*
 * @ (#) CreatePollRequest.java       1.0     27/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.dtos.requests;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 27/04/2025
 * @version:    1.0
 */

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CreatePollRequest {
    private String question;
    private String groupId;
    private LocalDateTime deadline;
    private boolean isPinned;
    private boolean allowMultipleChoices;
    private boolean allowAddOptions;
    private boolean hideResultsUntilVoted;
    private boolean hideVoters;
    private List<String> options;
}
