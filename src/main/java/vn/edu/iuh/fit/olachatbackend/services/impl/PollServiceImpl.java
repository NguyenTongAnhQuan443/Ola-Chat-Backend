/*
 * @ (#) PollServiceImpl.java       1.0     27/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services.impl;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 27/04/2025
 * @version:    1.0
 */

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.AddOptionRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.CreatePollRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.VoteRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.*;
import vn.edu.iuh.fit.olachatbackend.entities.*;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.mappers.PollMapper;
import vn.edu.iuh.fit.olachatbackend.repositories.*;
import vn.edu.iuh.fit.olachatbackend.services.PollService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PollServiceImpl implements PollService {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final VoteRepository voteRepository;
    private final PollMapper pollMapper;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;

    @Override
    public PollResponse createPoll(CreatePollRequest request) {
        validateCreatePollRequest(request);

        User user = getCurrentUser();
        validateGroupMembership(new ObjectId(request.getGroupId()), user.getId());

        // Save poll
        Poll poll = pollMapper.toPoll(request);
        poll.setCreatorId(user.getId());
        poll.setLocked(false);
        poll = pollRepository.save(poll);

        // Save options
        Poll finalPoll = poll;
        List<PollOption> options = request.getOptions().stream()
                .map(optionText -> {
                    PollOption option = new PollOption();
                    option.setPollId(finalPoll.getId());
                    option.setOptionText(optionText);
                    return pollOptionRepository.save(option);
                })
                .toList();

        // Create response
        PollResponse response = pollMapper.toPollResponse(poll);
        response.setOptions(options.stream().map(pollMapper::toPollOptionResponse).collect(Collectors.toList()));
        return response;
    }

    @Override
    public PollOptionResponse addOption(String pollId, AddOptionRequest request) {
        validatePollId(pollId);

        Poll poll = getPollById(pollId);
        User user = getCurrentUser();
        validateGroupMembership(new ObjectId(poll.getGroupId()), user.getId());

        // Check poll is locked
        if (poll.isLocked()) {
            throw new BadRequestException("Bình chọn đã bị khóa, không thể thêm tùy chọn");
        }

        // Check role
        if (!poll.isAllowAddOptions()) {
            throw new BadRequestException("Không được phép thêm tùy chọn cho bình chọn này");
        }

        // Check poll expiration
        validatePollNotExpired(poll);

        // Check option
        if (request.getOptionText() == null || request.getOptionText().trim().isEmpty()) {
            throw new BadRequestException("Tùy chọn không được để trống");
        }

        // Check for duplicate content
        checkDuplicateOption(pollId, request.getOptionText());

        // Save new option
        PollOption option = pollMapper.toPollOption(request, pollId);
        option = pollOptionRepository.save(option);

        return pollMapper.toPollOptionResponse(option);
    }

    @Override
    public void vote(String pollId, VoteRequest request) {
        validatePollId(pollId);

        Poll poll = getPollById(pollId);
        User user = getCurrentUser();
        validateGroupMembership(new ObjectId(poll.getGroupId()), user.getId());

        // Check poll status
        validatePollNotExpired(poll);
        validatePollNotLocked(poll);

        // Check the list option Ids are not empty
        if (request.getOptionIds() == null || request.getOptionIds().isEmpty()) {
            throw new BadRequestException("Ít nhất một tùy chọn phải được chọn");
        }

        // Check multiple options
        if (!poll.isAllowMultipleChoices() && request.getOptionIds().size() > 1) {
            throw new BadRequestException("Không được phép lựa chọn nhiều cho bình chọn này");
        }

        // Check and manage existing votes
        if (!poll.isAllowMultipleChoices()) {
            voteRepository.deleteAll(voteRepository.findByPollIdAndUserId(pollId, user.getId()));
        }

        // Validate option IDs
        validateOptionIds(pollId, request.getOptionIds());

        // Save votes
        request.getOptionIds().forEach(optionId -> {
            Vote vote = new Vote();
            vote.setPollId(pollId);
            vote.setUserId(user.getId());
            vote.setOptionId(optionId);
            voteRepository.save(vote);
        });
    }

    @Override
    public PollResultsResponse getPollResults(String pollId) {
        validatePollId(pollId);

        Poll poll = getPollById(pollId);
        User user = getCurrentUser();
        validateGroupMembership(new ObjectId(poll.getGroupId()), user.getId());

        // Check view role (if hide results until vote)
        if (poll.isHideResultsUntilVoted()) {
            List<Vote> userVotes = voteRepository.findByPollIdAndUserId(pollId, user.getId());
            if (userVotes.isEmpty()) {
                throw new BadRequestException("Kết quả sẽ được ẩn cho đến khi bạn bỏ phiếu!");
            }
        }

        // Get data to create response
        List<PollOption> options = pollOptionRepository.findByPollId(pollId);
        List<Vote> votes = voteRepository.findByPollId(pollId);
        Map<String, Integer> voteCounts = votes.stream()
                .collect(Collectors.groupingBy(Vote::getOptionId, Collectors.summingInt(v -> 1)));

        // Build response
        PollResultsResponse results = new PollResultsResponse();
        PollResponse pollResponse = pollMapper.toPollResponse(poll);
        pollResponse.setOptions(options.stream().map(pollMapper::toPollOptionResponse).collect(Collectors.toList()));

        results.setPoll(pollResponse);
        results.setOptions(options.stream()
                .map(opt -> new PollOptionResult(pollMapper.toPollOptionResponse(opt), voteCounts.getOrDefault(opt.getId(), 0)))
                .collect(Collectors.toList()));

        results.setVoters(poll.isHideVoters() ? new ArrayList<>() :
                votes.stream()
                        .map(vote -> new Voter(vote.getUserId(), vote.getOptionId()))
                        .collect(Collectors.toList()));

        return results;
    }

    @Override
    public PollResponse pinPoll(String pollId) {
        return updatePollPinStatus(pollId, true);
    }

    @Override
    public PollResponse unpinPoll(String pollId) {
        return updatePollPinStatus(pollId, false);
    }

    @Override
    public PollResponse lockPoll(String pollId) {
        validatePollId(pollId);

        Poll poll = getPollById(pollId);
        User user = getCurrentUser();
        validateGroupMembership(new ObjectId(poll.getGroupId()), user.getId());

        // Check poll locked
        if (poll.isLocked()) {
            throw new BadRequestException("Bình chọn đã bị khóa từ trước");
        }

        // Save poll
        poll.setLocked(true);
        poll = pollRepository.save(poll);

        return buildPollResponse(poll);
    }

    // Helper methods
    private User getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return userRepository.findByUsername(name)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
    }

    private void validateCreatePollRequest(CreatePollRequest request) {
        // Check question
        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            throw new BadRequestException("Câu hỏi không được để trống");
        }

        // Check number of option
        if (request.getOptions() == null || request.getOptions().size() < 2) {
            throw new BadRequestException("Phải có ít nhất 2 tùy chọn");
        }

        // Check option
        for (String option : request.getOptions()) {
            if (option == null || option.trim().isEmpty()) {
                throw new BadRequestException("Lựa chọn không được để trống");
            }
        }

        // Check groupId
        if (request.getGroupId() == null || request.getGroupId().isEmpty()) {
            throw new BadRequestException("Group id không được để trống");
        }

        // Check deadline
        if (request.getDeadline() != null && request.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Ngày hết hạn phải là thời điểm trong tương lai");
        }

        // Check group exists
        conversationRepository.findById(new ObjectId(request.getGroupId()))
                .orElseThrow(() -> new BadRequestException("Nhóm không tồn tại"));
    }

    private void validatePollId(String pollId) {
        if (pollId == null || pollId.trim().isEmpty()) {
            throw new BadRequestException("Poll ID không được để trống");
        }
    }

    private Poll getPollById(String pollId) {
        return pollRepository.findById(pollId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy bình chọn"));
    }

    private void validateGroupMembership(ObjectId groupId, String userId) {
        boolean isMember = participantRepository.existsByConversationIdAndUserId(groupId, userId);
        if (!isMember) {
            throw new BadRequestException("Bạn không phải là thành viên của nhóm này");
        }
    }

    private void validatePollNotExpired(Poll poll) {
        if (poll.getDeadline() != null && poll.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Bình chọn đã hết hạn");
        }
    }

    private void validatePollNotLocked(Poll poll) {
        if (poll.isLocked()) {
            throw new BadRequestException("Bình chọn đã bị khóa");
        }
    }

    private void checkDuplicateOption(String pollId, String optionText) {
        String newOptionNormalized = optionText.trim().toLowerCase();
        for (PollOption existingOption : pollOptionRepository.findByPollId(pollId)) {
            String existingOptionNormalized = existingOption.getOptionText().trim().toLowerCase();
            if (existingOptionNormalized.equals(newOptionNormalized)) {
                throw new BadRequestException("Tùy chọn đã tồn tại");
            }
        }
    }

    private void validateOptionIds(String pollId, List<String> optionIds) {
        List<String> validOptionIds = pollOptionRepository.findByPollId(pollId)
                .stream()
                .map(PollOption::getId)
                .toList();

        for (String optionId : optionIds) {
            if (!validOptionIds.contains(optionId)) {
                throw new BadRequestException("Invalid option ID: " + optionId);
            }
        }
    }

    private PollResponse updatePollPinStatus(String pollId, boolean pinned) {
        validatePollId(pollId);

        Poll poll = getPollById(pollId);
        User user = getCurrentUser();
        validateGroupMembership(new ObjectId(poll.getGroupId()), user.getId());

        poll.setPinned(pinned);
        poll = pollRepository.save(poll);

        return buildPollResponse(poll);
    }

    private PollResponse buildPollResponse(Poll poll) {
        PollResponse response = pollMapper.toPollResponse(poll);
        List<PollOption> options = pollOptionRepository.findByPollId(poll.getId());
        response.setOptions(options.stream().map(pollMapper::toPollOptionResponse).collect(Collectors.toList()));
        response.setPinned(poll.isPinned());
        response.setLocked(poll.isLocked());
        return response;
    }
}