/*
 * @ (#) PollController.java       1.0     27/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.controllers;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 27/04/2025
 * @version:    1.0
 */

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.AddOptionRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.CreatePollRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.VoteRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MessageResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PollOptionResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PollResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PollResultsResponse;
import vn.edu.iuh.fit.olachatbackend.services.PollService;

import java.util.Map;

@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
class PollController {
    private final PollService pollService;

    @PostMapping
    public ResponseEntity<MessageResponse<PollResponse>> createPoll(@RequestBody CreatePollRequest request) {
        PollResponse createdPoll = pollService.createPoll(request);
        MessageResponse<PollResponse> response = new MessageResponse<>(201, "Tạo bình chọn thành công", true, createdPoll);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{pollId}/options")
    public ResponseEntity<MessageResponse<PollOptionResponse>> addOption(@PathVariable String pollId,
                                                                         @RequestBody AddOptionRequest request) {
        PollOptionResponse addedOption = pollService.addOption(pollId, request);
        MessageResponse<PollOptionResponse> response = new MessageResponse<>(201, "Thêm lựa chọn thành công", true, addedOption);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{pollId}/vote")
    public ResponseEntity<MessageResponse<String>> vote(@PathVariable String pollId, @RequestBody VoteRequest request) {
        pollService.vote(pollId, request);
        MessageResponse<String> response = new MessageResponse<>(200, "Vote đã được ghi nhận thành công", true, null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pollId}/results")
    public ResponseEntity<MessageResponse<PollResultsResponse>> getPollResults(@PathVariable String pollId) {
        PollResultsResponse results = pollService.getPollResults(pollId);
        MessageResponse<PollResultsResponse> response = new MessageResponse<>(200, "Lấy kết quả bình chọn thành công", true, results);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pollId}/pin")
    public ResponseEntity<MessageResponse<PollResponse>> pinPoll(@PathVariable String pollId) {
        PollResponse pinnedPoll = pollService.pinPoll(pollId);
        MessageResponse<PollResponse> response = new MessageResponse<>(200, "Đã ghim bình chọn thành công", true, pinnedPoll);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pollId}/unpin")
    public ResponseEntity<MessageResponse<PollResponse>> unpinPoll(@PathVariable String pollId) {
        PollResponse unpinnedPoll = pollService.unpinPoll(pollId);
        MessageResponse<PollResponse> response = new MessageResponse<>(200, "Đã bỏ ghim bình chọn", true, unpinnedPoll);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pollId}/lock")
    public ResponseEntity<MessageResponse<PollResponse>> lockPoll(@PathVariable String pollId) {
        PollResponse lockedPoll = pollService.lockPoll(pollId);
        MessageResponse<PollResponse> response = new MessageResponse<>(200, "Poll locked successfully", true, lockedPoll);
        return ResponseEntity.ok(response);
    }
}