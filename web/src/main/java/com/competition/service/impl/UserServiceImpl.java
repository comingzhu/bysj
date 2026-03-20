package com.competition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.common.Constants;
import com.competition.common.Result;
import com.competition.entity.User;
import com.competition.mapper.UserMapper;
import com.competition.service.UserService;
import com.competition.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
                .eq(User::getDeleted, 0);
        User user = this.getOne(wrapper);
        if (user == null) {
            return Result.error("用户名不存在");
        }
        if (!user.getPassword().equals(password)) {
            return Result.error("密码错误");
        }
        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        return Result.success(data);
    }

    @Override
    public Result register(User user) {
        System.out.println("开始处理注册，用户名: " + user.getUsername());
        
        // 验证必填字段
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
            return Result.error("真实姓名不能为空");
        }
        if (user.getStudentNo() == null || user.getStudentNo().trim().isEmpty()) {
            return Result.error("学号不能为空");
        }
        
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername())
                .eq(User::getDeleted, 0);
        if (this.count(wrapper) > 0) {
            System.out.println("用户名已存在: " + user.getUsername());
            return Result.error("用户名已存在");
        }
        
        // 设置默认值
        user.setStatus(1); // 启用状态
        user.setRole(Constants.ROLE_STUDENT); // 强制设置为学生角色
        user.setBalance(java.math.BigDecimal.ZERO); // 初始余额为0
        user.setDeleted(0); // 未删除
        
        // 保存用户
        try {
            this.save(user);
            System.out.println("用户注册成功，ID: " + user.getId());
            return Result.success("注册成功");
        } catch (Exception e) {
            System.err.println("保存用户失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    @Override
    public Result getUserInfo(Integer userId) {
        User user = this.getById(userId);
        if (user == null || user.getDeleted() == 1) {
            return Result.error("用户不存在");
        }
        user.setPassword(null); // 不返回密码
        // 确保返回完整的用户信息
        System.out.println("获取用户信息 - ID: " + userId + ", Username: " + user.getUsername() + ", RealName: " + user.getRealName() + ", Role: " + user.getRole());
        return Result.success(user);
    }

    @Override
    public Result getUserById(Integer userId) {
        User user = this.getById(userId);
        if (user == null || user.getDeleted() == 1) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @Override
    public Result updateUserInfo(User user) {
        User existUser = this.getById(user.getId());
        if (existUser == null || existUser.getDeleted() == 1) {
            return Result.error("用户不存在");
        }
        // 不允许修改用户名和角色
        user.setUsername(null);
        user.setRole(null);
        this.updateById(user);
        return Result.success("更新成功");
    }

    @Override
    public Result getUserList(String role, String keyword, Integer page, Integer size) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getStudentNo, keyword)
                    .or().like(User::getTeacherNo, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = this.page(pageParam, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @Override
    public Result createUser(User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername())
                .eq(User::getDeleted, 0);
        if (this.count(wrapper) > 0) {
            return Result.error("用户名已存在");
        }
        user.setStatus(1);
        this.save(user);
        return Result.success("创建成功");
    }

    @Override
    public Result updateUserStatus(Integer userId, Integer status) {
        User user = this.getById(userId);
        if (user == null || user.getDeleted() == 1) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        this.updateById(user);
        return Result.success("操作成功");
    }

    @Override
    public Result updateUserByAdmin(User user) {
        User existUser = this.getById(user.getId());
        if (existUser == null || existUser.getDeleted() == 1) {
            return Result.error("用户不存在");
        }
        // 管理员可以更新所有字段，但用户名和角色不能修改
        if (user.getUsername() != null && !user.getUsername().equals(existUser.getUsername())) {
            return Result.error("用户名不能修改");
        }
        if (user.getRole() != null && !user.getRole().equals(existUser.getRole())) {
            return Result.error("角色不能修改");
        }
        // 如果密码为空，则不更新密码
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(null);
        }
        // 保持用户名和角色不变
        user.setUsername(existUser.getUsername());
        user.setRole(existUser.getRole());
        this.updateById(user);
        return Result.success("更新成功");
    }

    @Override
    public Result changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null || user.getDeleted() == 1) {
            return Result.error("用户不存在");
        }
        if (!user.getPassword().equals(oldPassword)) {
            return Result.error("原密码错误");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return Result.error("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }
        user.setPassword(newPassword);
        this.updateById(user);
        return Result.success("密码修改成功");
    }

    @Override
    public Result rechargeBalance(Integer userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("充值金额必须大于0");
        }
        User user = this.getById(userId);
        if (user == null || user.getDeleted() == 1) {
            return Result.error("用户不存在");
        }
        BigDecimal currentBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
        user.setBalance(currentBalance.add(amount));
        this.updateById(user);
        return Result.success("充值成功，当前余额：" + user.getBalance());
    }

    @Override
    public Result getBalance(Integer userId) {
        User user = this.getById(userId);
        if (user == null || user.getDeleted() == 1) {
            return Result.error("用户不存在");
        }
        BigDecimal balance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
        Map<String, Object> data = new HashMap<>();
        data.put("balance", balance);
        return Result.success(data);
    }
}

