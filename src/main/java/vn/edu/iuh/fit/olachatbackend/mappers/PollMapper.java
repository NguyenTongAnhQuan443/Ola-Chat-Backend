/*
 * @ (#) PollMapper.java       1.0     27/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.mappers;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 27/04/2025
 * @version:    1.0
 */

import org.mapstruct.Mapper;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.AddOptionRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.CreatePollRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PollOptionResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.PollResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Poll;
import vn.edu.iuh.fit.olachatbackend.entities.PollOption;

@Mapper(componentModel = "spring")
public interface PollMapper {
    Poll toPoll(CreatePollRequest request);
    PollResponse toPollResponse(Poll poll);
    PollOption toPollOption(AddOptionRequest request, String pollId);
    PollOptionResponse toPollOptionResponse(PollOption option);
}
