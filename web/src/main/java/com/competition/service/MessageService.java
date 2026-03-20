package com.competition.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.competition.common.Result;
import com.competition.entity.Message;

public interface MessageService extends IService<Message> {
    Result getMyMessages(Integer userId, Integer page, Integer size);
    Result markAsRead(Integer messageId, Integer userId);
    Result sendMessage(Message message);
    Result getAllMessages(Integer page, Integer size);
    Result updateMessage(Message message);
    Result deleteMessage(Integer id);
}

