/*
 * @ (#) ReactionInfoDTO.java       1.0     25/05/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.dtos;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 25/05/2025
 * @version:    1.0
 */

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReactionInfoDTO {
    private int totalReactions;

    private List<UserReactionSummary> userReactions;
    private List<EmojiReactionSummary> emojiCounts;
    private Map<String, List<DetailedReaction>> detailedReactions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserReactionSummary {
        private String userId;
        private int totalCount;
        private List<String> emojiTypes;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmojiReactionSummary {
        private String emoji;
        private int count;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailedReaction {
        private String userId;
        private String emoji;
        private int count;
    }
}

