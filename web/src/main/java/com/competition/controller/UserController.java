package com.competition.controller;

import com.competition.common.Result;
import com.competition.entity.User;
import com.competition.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        return userService.login(username, password);
    }

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        System.out.println("收到注册请求，用户名: " + user.getUsername());
        System.out.println("注册数据: " + user);
        try {
            Result result = userService.register(user);
            System.out.println("注册结果: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("注册异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result getUserInfo(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return userService.getUserInfo(userId);
    }

    /**
     * 按用户ID获取用户信息（用于报名详情展示等）
     */
    @GetMapping("/{id}")
    public Result getUserById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @PutMapping("/info")
    public Result updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        user.setId(userId);
        return userService.updateUserInfo(user);
    }

    @GetMapping("/list")
    public Result getUserList(@RequestParam(required = false) String role,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size) {
        return userService.getUserList(role, keyword, page, size);
    }

    @PostMapping("/create")
    public Result createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/status/{userId}")
    public Result updateUserStatus(@PathVariable Integer userId, @RequestParam Integer status) {
        return userService.updateUserStatus(userId, status);
    }

    @PutMapping("/update")
    public Result updateUserByAdmin(@RequestBody User user) {
        return userService.updateUserByAdmin(user);
    }

    @PutMapping("/password")
    public Result changePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        return userService.changePassword(userId, oldPassword, newPassword);
    }

    @PostMapping("/balance/recharge")
    public Result rechargeBalance(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"student".equals(role)) {
            return Result.error(403, "只有学生可以充值");
        }
        java.math.BigDecimal amount = new java.math.BigDecimal(params.get("amount").toString());
        return userService.rechargeBalance(userId, amount);
    }

    @GetMapping("/balance")
    public Result getBalance(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return userService.getBalance(userId);
    }
}

