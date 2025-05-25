/*
 * @ (#) MessageServiceImpl.java       1.0     14/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services.impl;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/02/2025
 * @version:    1.0
 */
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.dtos.MessageDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.MessageDetailDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.ReactionInfoDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MediaMessageResponse;
import vn.edu.iuh.fit.olachatbackend.entities.*;
import vn.edu.iuh.fit.olachatbackend.enums.ConversationType;
import vn.edu.iuh.fit.olachatbackend.enums.MessageStatus;
import vn.edu.iuh.fit.olachatbackend.enums.MessageType;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.repositories.*;
import vn.edu.iuh.fit.olachatbackend.services.ConversationService;
import vn.edu.iuh.fit.olachatbackend.services.MessageService;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final MongoTemplate mongoTemplate;
    private final ParticipantRepository participantRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final MessageRepositoryCustomImpl messageRepositoryCustomImpl;

    @Override
    public MessageDTO save(MessageDTO messageDTO) {
        Message message = Message.builder()
                .senderId(messageDTO.getSenderId())
                .conversationId(new ObjectId(messageDTO.getConversationId()))
                .content(messageDTO.getContent())
                .type(messageDTO.getType())
                .mediaUrls(messageDTO.getMediaUrls() == null ? new ArrayList<>() : messageDTO.getMediaUrls())
                .status(MessageStatus.SENT)
                .deletedStatus(messageDTO.getDeletedStatus() == null ? new ArrayList<>() : messageDTO.getDeletedStatus())
                .createdAt(LocalDateTime.now())
                .recalled(messageDTO.isRecalled())
                .mentions(messageDTO.getMentions() == null ? new ArrayList<>() : messageDTO.getMentions())
                .replyTo(messageDTO.getReplyTo() == null ? null : new ObjectId(messageDTO.getReplyTo()))
                .reactions(new ArrayList<>())
                .build();
        Message savedMessage = messageRepository.save(message);

        conversationService.updateLastMessage(message.getConversationId(), savedMessage);
        return messageDTO;
    }

    public List<MessageDTO> getMessagesByConversationId(String conversationId, int page, int size, String sortDirection) {
        User currentUser = getCurrentUser();

        // Create sort direction
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        // Create pageable
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        List<Message> messages = messageRepository.findByConversationId(new ObjectId(conversationId), pageable);

        return messages.stream()
                .map(message -> {

                    // Get reactions
                    List<Message.Reaction> reactions = message.getReactions();
                    if (reactions == null) {
                        reactions = new ArrayList<>();
                        message.setReactions(reactions);
                    }

                    // Get distinct reactions
                    List<String> emojiTypes = reactions.stream()
                            .map(Message.Reaction::getEmoji)
                            .distinct()
                            .toList();

                    // Get total reaction
                    int totalReactionCount = reactions.stream()
                            .mapToInt(Message.Reaction::getCount)
                            .sum();

                    // Get last reaction of user
                    String lastUserReaction = reactions.stream()
                            .filter(r -> r.getUserId().equals(currentUser.getId()))
                            .filter(r -> r.getReactedAt() != null)
                            .max(Comparator.comparing(Message.Reaction::getReactedAt))
                            .map(Message.Reaction::getEmoji)
                            .orElse(null);

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
                            .replyTo(message.getReplyTo() == null ? null : message.getReplyTo().toString())
                            .emojiTypes(emojiTypes)
                            .totalReactionCount(totalReactionCount)
                            .lastUserReaction(lastUserReaction)
                            .build();
                })
                .toList();
    }

    public MessageDTO recallMessage(String messageId, String senderId) {
        System.out.println("Mess" + messageId);
        // Kiểm tra định dạng Message ID
        if (messageId == null || !messageId.matches("[0-9a-fA-F]{24}")) {
            throw new IllegalArgumentException("Message ID must be a valid 24-character hex string.");
        }

        // Chuyển messageId thành ObjectId
        ObjectId objectId = new ObjectId(messageId);

        // Tìm tin nhắn trong cơ sở dữ liệu
        Message message = messageRepository.findById(objectId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Kiểm tra xem người gửi có quyền thu hồi tin nhắn này
        if (!message.getSenderId().equals(senderId)) {
            throw new RuntimeException("Only sender can recall this message");
        }

        // Nếu tin nhắn chưa được thu hồi thì thực hiện thu hồi
        if (!message.isRecalled()) {
            message.setRecalled(true);
            message.setContent("Tin nhắn đã được thu hồi");
            message.setMediaUrls(null);  // Nếu là tin nhắn media thì xóa URL
            messageRepository.save(message);
        }

        // Update last message
        conversationService.updateLastMessage(message.getConversationId(), message);

        // Trả về MessageDTO với trạng thái tin nhắn đã thu hồi
        return MessageDTO.builder()
                .id(message.getId().toHexString())
                .senderId(message.getSenderId())
                .conversationId(message.getConversationId().toHexString())
                .content(message.getContent())
                .type(message.getType())
                .mediaUrls(null)
                .status(message.getStatus())
                .recalled(true)
                .createdAt(message.getCreatedAt())
                .build();
    }

    @Override
    public List<MediaMessageResponse> getMediaMessages(String conversationId, String senderId) {
        return getMessagesByTypes(conversationId, senderId, List.of(MessageType.MEDIA));
    }

    @Override
    public List<MediaMessageResponse> getFileMessages(String conversationId, String senderId) {
        return getMessagesByTypes(conversationId, senderId, List.of(MessageType.FILE));
    }

    @Override
    public void markMessageAsReceived(String messageId) {
        User currentUser = getCurrentUser();
        Message message = messageRepository.findById(new ObjectId(messageId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tin nhắn"));
        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy cuộc trò chuyện"));

        if (message.getDeliveryStatus() == null) {
            message.setDeliveryStatus(new ArrayList<>());
        }

        // Thêm người nhận vào deliveryStatus nếu chưa có
        if (message.getDeliveryStatus().stream()
                .noneMatch(ds -> ds.getUserId().equals(currentUser.getId()))) {
            message.getDeliveryStatus().add(DeliveryStatus.builder()
                    .userId(currentUser.getId())
                    .deliveredAt(LocalDateTime.now())
                    .build());
        }

        // Nếu là chat đơn sẽ thay đổi trạng thái thành "RECEIVED"
        if (conversation.getType() == ConversationType.PRIVATE) {
            message.setStatus(MessageStatus.RECEIVED);
        }

        messageRepository.save(message);
    }

    @Override
    public void hiddenForUser(String messageId) {
        // Check message exists
        Message message = messageRepository.findById(new ObjectId(messageId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tin nhắn"));

        // Check if message in conversation
        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy cuộc trò chuyện"));

        User currentUser = getCurrentUser();

        // Check if user exist in conversation
        checkUserExistsInConversation(conversation.getId(), currentUser.getId());

        // hidden message
        Message.DeletedStatus deletedStatus = Message.DeletedStatus.builder()
                .userId(currentUser.getId())
                .deletedAt(LocalDateTime.now())
                .build();

        if (message.getDeletedStatus() == null) {
            message.setDeletedStatus(new ArrayList<>());
        }

        message.getDeletedStatus().add(deletedStatus);

        // save to database
        messageRepository.save(message);
    }

    @Override
    public void markMessageAsRead(String messageId) {
        User currentUser = getCurrentUser();
        Message message = messageRepository.findById(new ObjectId(messageId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy message"));

        if (message.getReadStatus() == null) {
            message.setReadStatus(new ArrayList<>());
        }

        if (message.getReadStatus().stream().anyMatch(rs -> rs.getUserId().equals(currentUser.getId()))) {
            return;
        }

        message.getReadStatus().add(new ReadStatus(currentUser.getId(), LocalDateTime.now()));

        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy cuộc trò chuyện"));

        if (conversation.getType() == ConversationType.PRIVATE) {
            message.setStatus(MessageStatus.READ);
        } else {
            // Đối với chat nhóm, kiểm tra xem tất cả người tham gia đã đọc chưa
            long participantCount = participantRepository.countByConversationId(message.getConversationId());
            boolean isAllRead = message.getReadStatus().size() == participantCount;

            // Nếu tất cả người tham gia đã đọc thì đánh dấu tin nhắn là đã đọc
            if (isAllRead) {
                message.setStatus(MessageStatus.READ);
            }
        }

        messageRepository.save(message);
    }

    // Common method
    private List<MediaMessageResponse> getMessagesByTypes(String conversationId, String senderId, List<MessageType> types) {
        Criteria criteria = Criteria.where("conversationId").is(new ObjectId(conversationId))
                .and("type").in(types);

        if (senderId != null && !senderId.isEmpty()) {
            criteria.and("senderId").is(senderId);
        }

        Query query = new Query(criteria);
        List<Message> messages = mongoTemplate.find(query, Message.class);

        return convertToResponse(messages);
    }

    // Convert method
    private List<MediaMessageResponse> convertToResponse(List<Message> messages) {
        return messages.stream()
                .map(msg -> MediaMessageResponse.builder()
                        .id(msg.getId().toString())
                        .mediaUrls(msg.getMediaUrls())
                        .type(msg.getType().getValue())
                        .senderId(msg.getSenderId())
                        .createdAt(msg.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public void addReplyToMessage(String messageId, MessageDTO messageDTO) {
        // Check message exists
        Message parrentMessage = messageRepository.findById(new ObjectId(messageId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tin nhắn"));

        // Check if message in conversation
        Conversation conversation = conversationRepository.findById(parrentMessage.getConversationId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy cuộc trò chuyện"));

        User currentUser = getCurrentUser();

        // Check if user exist in conversation
        checkUserExistsInConversation(conversation.getId(), currentUser.getId());

        // set reply to message
        messageDTO.setReplyTo(messageId);

        // save message
        save(messageDTO);
    }

    @Override
    public void addReactionToMessage(String messageId, String emoji) {
        // Check message exists
        Message message = messageRepository.findById(new ObjectId(messageId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tin nhắn"));

        // Check if message in conversation
        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy cuộc trò chuyện"));

        User currentUser = getCurrentUser();

        // Check user exists in conversation
        checkUserExistsInConversation(conversation.getId(), currentUser.getId());

        // Check system message
        if (message.getType().equals(MessageType.SYSTEM)) {
            throw new BadRequestException("Bạn không thể react tin nhắn hệ thống!");
        }

        // Check Sticker message
        if (message.getType().equals(MessageType.STICKER)) {
            throw new BadRequestException("Bạn không thể react Sticker!");
        }

        List<Message.Reaction> reactions = message.getReactions();
        if (reactions == null) {
            reactions = new ArrayList<>();
            message.setReactions(reactions);
        }

        // Find reaction of user with emoji
        Optional<Message.Reaction> existingReactionOpt = reactions.stream()
                .filter(r -> r.getEmoji().equals(emoji) && r.getUserId().equals(currentUser.getId()))
                .findFirst();

        if (existingReactionOpt.isPresent()) {
            // Increase count reaction
            Message.Reaction existingReaction = existingReactionOpt.get();
            existingReaction.setCount(existingReaction.getCount() + 1);
        } else {
            // Add new reaction
            Message.Reaction newReaction = Message.Reaction.builder()
                    .userId(currentUser.getId())
                    .emoji(emoji)
                    .count(1)
                    .reactedAt(LocalDateTime.now())
                    .build();
            reactions.add(newReaction);
        }

        // Save message
        messageRepository.save(message);
    }

    @Override
    public void removeReactionToMessage(String messageId) {
        // Check message exists
        Message message = messageRepository.findById(new ObjectId(messageId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tin nhắn"));

        // Check if message in conversation
        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy cuộc trò chuyện"));

        User currentUser = getCurrentUser();

        // Check user exists in conversation
        checkUserExistsInConversation(conversation.getId(), currentUser.getId());

        // Check system message
        if (message.getType().equals(MessageType.SYSTEM)) {
            throw new BadRequestException("Bạn không thể xóa react tin nhắn hệ thống!");
        }

        // Check Sticker message
        if (message.getType().equals(MessageType.STICKER)) {
            throw new BadRequestException("Bạn không thể xóa react Sticker!");
        }

        // Remove the reaction of current user if exists
        List<Message.Reaction> reactions = message.getReactions();
        if (reactions != null) {
            boolean removed = reactions.removeIf(r -> r.getUserId().equals(currentUser.getId()));
            if (removed) {
                message.setReactions(reactions);
                messageRepository.save(message);
            } else {
                throw new BadRequestException("Người dùng chưa thả reaction nào vào tin nhắn này");
            }
        } else {
            throw new BadRequestException("Tin nhắn này chưa có reaction nào");
        }
    }

    @Override
    public ReactionInfoDTO getReactionInfoByMessageId(String messageId) {
        // Check message exists
        Message message = messageRepository.findById(new ObjectId(messageId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tin nhắn"));

        // Check if message in conversation
        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy cuộc trò chuyện"));

        User currentUser = getCurrentUser();

        // Check user exists in conversation
        checkUserExistsInConversation(conversation.getId(), currentUser.getId());

        // Check system message
        if (message.getType().equals(MessageType.SYSTEM)) {
            throw new BadRequestException("Bạn không thể xem react tin nhắn hệ thống!");
        }

        // Check Sticker message
        if (message.getType().equals(MessageType.STICKER)) {
            throw new BadRequestException("Bạn không thể xem react Sticker!");
        }

        // Check if message have reactions
        List<Message.Reaction> reactions = message.getReactions();
        if (reactions == null || reactions.isEmpty()) {
            return ReactionInfoDTO.builder()
                    .totalReactions(0)
                    .userReactions(List.of())
                    .emojiCounts(List.of())
                    .detailedReactions(Map.of())
                    .build();
        }

        int totalReactions = 0;
        Map<String, Integer> userTotalMap = new HashMap<>();
        Map<String, Set<String>> userEmojiMap = new HashMap<>();
        Map<String, Integer> emojiCountMap = new HashMap<>();
        Map<String, List<ReactionInfoDTO.DetailedReaction>> detailedMap = new HashMap<>();

        for (Message.Reaction r : reactions) {
            int count = r.getCount() == null ? 0 : r.getCount();
            totalReactions += count;

            userTotalMap.merge(r.getUserId(), count, Integer::sum);
            userEmojiMap.computeIfAbsent(r.getUserId(), k -> new HashSet<>()).add(r.getEmoji());

            emojiCountMap.merge(r.getEmoji(), count, Integer::sum);

            detailedMap.computeIfAbsent(r.getEmoji(), k -> new ArrayList<>())
                    .add(new ReactionInfoDTO.DetailedReaction(r.getUserId(), r.getEmoji(), count));
        }

        List<ReactionInfoDTO.UserReactionSummary> userSummary = userTotalMap.entrySet().stream()
                .map(e -> new ReactionInfoDTO.UserReactionSummary(
                        e.getKey(),
                        e.getValue(),
                        new ArrayList<>(userEmojiMap.getOrDefault(e.getKey(), Set.of()))
                )).toList();

        List<ReactionInfoDTO.EmojiReactionSummary> emojiSummary = emojiCountMap.entrySet().stream()
                .map(e -> new ReactionInfoDTO.EmojiReactionSummary(e.getKey(), e.getValue()))
                .toList();

        return ReactionInfoDTO.builder()
                .totalReactions(totalReactions)
                .userReactions(userSummary)
                .emojiCounts(emojiSummary)
                .detailedReactions(detailedMap)
                .build();
    }

    @Override
    public MessageDetailDTO getMessageDetail(String messageId) {
        // Check message exists
        Message message = messageRepository.findById(new ObjectId(messageId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tin nhắn"));

        // Check if message in conversation
        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy cuộc trò chuyện"));

        User currentUser = getCurrentUser();

        // Check user exists in conversation
        checkUserExistsInConversation(conversation.getId(), currentUser.getId());

        return MessageDetailDTO.builder()
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
                .replyTo(message.getReplyTo() == null ? null : message.getReplyTo().toString())
                .readStatus(message.getReadStatus() == null ? new ArrayList<>() : message.getReadStatus())
                .deliveryStatus(message.getDeliveryStatus() == null ? new ArrayList<>() : message.getDeliveryStatus())
                .build();
    }

    @Override
    public Page<MessageDTO> searchMessages(String conversationId, String keyword, String senderId, LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {
        User currentUser = getCurrentUser();

        // Check if message in conversation
        Conversation conversation = conversationRepository.findById(new ObjectId(conversationId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy cuộc trò chuyện"));

        // Check user exists in conversation
        checkUserExistsInConversation(conversation.getId(), currentUser.getId());

        return messageRepositoryCustomImpl.searchMessages(
                conversationId, keyword, senderId, fromDate, toDate, page, size
        );
    }

    private User getCurrentUser() {
        // Check user
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return userRepository.findByUsername(name)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
    }

    /**
     * Check if user exists in conversation or throw exception if not found
     * @param conversationId ID of the group
     * @param userId ID of the user
     * @throws BadRequestException if user is not in the group
     */
    private void checkUserExistsInConversation(ObjectId conversationId, String userId) {
        if (!participantRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new BadRequestException("Bạn không thuộc nhóm này.");
        }
    }

}
