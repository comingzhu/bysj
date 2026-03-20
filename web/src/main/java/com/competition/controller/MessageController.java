package com.competition.controller;

import com.competition.common.Result;
import com.competition.entity.Message;
import com.competition.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @GetMapping("/my")
    public Result getMyMessages(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size,
                                HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return messageService.getMyMessages(userId, page, size);
    }

    @PutMapping("/read/{id}")
    public Result markAsRead(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return messageService.markAsRead(id, userId);
    }

    @PostMapping("/send")
    public Result sendMessage(@RequestBody Message message, HttpServletRequest request) {
        // 验证管理员或教师权限
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role) && !"teacher".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        return messageService.sendMessage(message);
    }

    @GetMapping("/all")
    public Result getAllMessages(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size,
                                 HttpServletRequest request) {
        // 验证管理员权限
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        return messageService.getAllMessages(page, size);
    }

    @PutMapping("/{id}")
    public Result updateMessage(@PathVariable Integer id, @RequestBody Message message, HttpServletRequest request) {
        // 验证管理员权限
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        message.setId(id);
        return messageService.updateMessage(message);
    }

    @DeleteMapping("/{id}")
    public Result deleteMessage(@PathVariable Integer id, HttpServletRequest request) {
        // 验证管理员权限
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        return messageService.deleteMessage(id);
    }
}

