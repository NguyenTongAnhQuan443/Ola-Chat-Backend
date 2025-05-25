/*
 * @ (#) MessageRepositoryCustomImpl.java       1.0     26/05/2025
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

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.olachatbackend.dtos.MessageDTO;
import vn.edu.iuh.fit.olachatbackend.entities.Message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryCustomImpl {

    private final MongoTemplate mongoTemplate;

    public Page<MessageDTO> searchMessages(
            String conversationId,
            String keyword,
            String senderId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size
    ) {
        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.add(Criteria.where("conversationId").is(new ObjectId(conversationId)));

        if (keyword != null && !keyword.isBlank()) {
            criteriaList.add(Criteria.where("content").regex(keyword, "i")); // ignore case
        }

        if (senderId != null && !senderId.isBlank()) {
            criteriaList.add(Criteria.where("senderId").is(senderId));
        }

        if (fromDate != null) {
            criteriaList.add(Criteria.where("createdAt").gte(fromDate));
        }

        if (toDate != null) {
            criteriaList.add(Criteria.where("createdAt").lte(toDate));
        }

        Query query = new Query(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));

        long total = mongoTemplate.count(query, Message.class);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        query.with(pageable);

        List<Message> messages = mongoTemplate.find(query, Message.class);
        List<MessageDTO> dtos = messages.stream().map(message -> {
            List<String> emojiTypes = message.getReactions() == null
                    ? new ArrayList<>()
                    : message.getReactions().stream()
                    .map(Message.Reaction::getEmoji)
                    .distinct()
                    .collect(Collectors.toList());

            int totalReactionCount = message.getReactions() == null ? 0 : message.getReactions().size();

            String lastUserReaction = message.getReactions() == null || message.getReactions().isEmpty()
                    ? null
                    : message.getReactions().get(message.getReactions().size() - 1).getEmoji();

            return MessageDTO.builder()
                    .id(message.getId().toHexString())
                    .senderId(message.getSenderId())
                    .conversationId(message.getConversationId().toHexString())
                    .content(message.getContent())
                    .type(message.getType())
                    .mediaUrls(message.getMediaUrls() == null ? new ArrayList<>() : message.getMediaUrls())
                    .status(message.getStatus())
                    .deletedStatus(message.getDeletedStatus() == null ? new ArrayList<>() : message.getDeletedStatus())
                    .createdAt(message.getCreatedAt())
                    .recalled(message.isRecalled())
                    .mentions(message.getMentions() == null ? new ArrayList<>() : message.getMentions())
                    .replyTo(message.getReplyTo() == null ? null : message.getReplyTo().toHexString())
                    .emojiTypes(emojiTypes)
                    .totalReactionCount(totalReactionCount)
                    .lastUserReaction(lastUserReaction)
                    .build();
        }).toList();

        return new PageImpl<>(dtos, pageable, total);
    }
}

