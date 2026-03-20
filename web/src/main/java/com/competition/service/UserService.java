package com.competition.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.competition.entity.User;
import com.competition.common.Result;

public interface UserService extends IService<User> {
    Result login(String username, String password);
    Result register(User user);
    Result getUserInfo(Integer userId);
    Result getUserById(Integer userId);
    Result updateUserInfo(User user);
    Result getUserList(String role, String keyword, Integer page, Integer size);
    Result createUser(User user);
    Result updateUserStatus(Integer userId, Integer status);
    Result updateUserByAdmin(User user);
    Result changePassword(Integer userId, String oldPassword, String newPassword);
    Result rechargeBalance(Integer userId, java.math.BigDecimal amount);
    Result getBalance(Integer userId);
}

