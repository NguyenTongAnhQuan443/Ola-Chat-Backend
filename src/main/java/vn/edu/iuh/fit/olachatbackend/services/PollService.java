/*
 * @ (#) PollService.java       1.0     27/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 27/04/2025
 * @version:    1.0
 */

import vn.edu.iuh.fit.olachatbackend.dtos.requests.AddOptionRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.CreatePollRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.VoteRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PollOptionResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PollResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PollResultsResponse;

public interface PollService {
    PollResponse createPoll(CreatePollRequest request);
    PollOptionResponse addOption(String pollId, AddOptionRequest request);
    void vote(String pollId, VoteRequest request);
    PollResultsResponse getPollResults(String pollId);
    PollResponse pinPoll(String pollId);
    PollResponse unpinPoll(String pollId);
    PollResponse lockPoll(String pollId);
}
