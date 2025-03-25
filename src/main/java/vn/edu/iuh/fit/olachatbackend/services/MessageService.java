/*
 * @ (#) MessageService.java       1.0     14/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/02/2025
 * @version:    1.0
 */

import vn.edu.iuh.fit.olachatbackend.dtos.MessageDTO;
import vn.edu.iuh.fit.olachatbackend.entities.Message;

import java.util.List;

public interface MessageService {
    List<MessageDTO> getMessagesByConversationId(String conversationId);
    MessageDTO save(MessageDTO messageDTO);
}
