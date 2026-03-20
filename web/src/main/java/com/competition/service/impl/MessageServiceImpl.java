package com.competition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.common.Result;
import com.competition.entity.Message;
import com.competition.mapper.MessageMapper;
import com.competition.service.MessageService;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    @Override
    public Result getMyMessages(Integer userId, Integer page, Integer size) {
        Page<Message> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Message::getUserId, userId).or().isNull(Message::getUserId))
                .orderByDesc(Message::getCreateTime);
        Page<Message> result = this.page(pageParam, wrapper);
        return Result.success(result);
    }

    @Override
    public Result markAsRead(Integer messageId, Integer userId) {
        Message message = this.getById(messageId);
        if (message == null) {
            return Result.error("消息不存在");
        }
        if (message.getUserId() != null && !message.getUserId().equals(userId)) {
            return Result.error("无权限操作");
        }
        message.setIsRead(1);
        this.updateById(message);
        return Result.success("标记成功");
    }

    @Override
    public Result sendMessage(Message message) {
        this.save(message);
        return Result.success("发送成功");
    }

    @Override
    public Result getAllMessages(Integer page, Integer size) {
        Page<Message> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Message::getCreateTime);
        Page<Message> result = this.page(pageParam, wrapper);
        // 如果返回的是Page对象，直接返回；如果是List，包装成Page格式
        if (result.getRecords() != null) {
            return Result.success(result);
        } else {
            return Result.success(pageParam);
        }
    }

    @Override
    public Result updateMessage(Message message) {
        this.updateById(message);
        return Result.success("更新成功");
    }

    @Override
    public Result deleteMessage(Integer id) {
        this.removeById(id);
        return Result.success("删除成功");
    }
}

