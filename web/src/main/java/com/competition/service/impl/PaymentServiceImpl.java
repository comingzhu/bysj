package com.competition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.competition.common.Result;
import com.competition.common.Constants;
import com.competition.entity.Competition;
import com.competition.entity.Message;
import com.competition.entity.Registration;
import com.competition.entity.User;
import com.competition.entity.Team;
import com.competition.mapper.CompetitionMapper;
import com.competition.mapper.MessageMapper;
import com.competition.mapper.RegistrationMapper;
import com.competition.mapper.TeamMapper;
import com.competition.mapper.UserMapper;
import com.competition.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private RegistrationMapper registrationMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private CompetitionMapper competitionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeamMapper teamMapper;

    @Override
    public Result getPaymentList(Integer status, String keyword, Integer page, Integer size) {
        Page<Registration> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getDeleted, 0);
        if (status != null) {
            wrapper.eq(Registration::getPaymentStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Registration::getId, keyword);
        }
        wrapper.orderByDesc(Registration::getCreateTime);
        Page<Registration> result = registrationMapper.selectPage(pageParam, wrapper);
        
        // 关联查询竞赛名称和报名人信息
        List<Registration> records = result.getRecords();
        if (records != null && !records.isEmpty()) {
            List<Integer> competitionIds = records.stream()
                    .filter(r -> r.getCompetitionId() != null)
                    .map(Registration::getCompetitionId)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<Integer> userIds = records.stream()
                    .filter(r -> r.getUserId() != null)
                    .map(Registration::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<Integer> teamIds = records.stream()
                    .filter(r -> r.getTeamId() != null)
                    .map(Registration::getTeamId)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<Integer, String> competitionMap = new HashMap<>();
            if (!competitionIds.isEmpty()) {
                List<Competition> competitions = competitionMapper.selectBatchIds(competitionIds);
                competitionMap = competitions.stream()
                        .collect(Collectors.toMap(Competition::getId, 
                                Competition::getName, 
                                (v1, v2) -> v1));
            }
            
            Map<Integer, String> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(userIds);
                userMap = users.stream()
                        .collect(Collectors.toMap(User::getId, 
                                u -> u.getRealName() != null ? u.getRealName() : u.getUsername(), 
                                (v1, v2) -> v1));
            }
            
            // 查询团队信息，获取团队名称和队长信息
            Map<Integer, Team> teamMap = new HashMap<>();
            Map<Integer, String> teamLeaderMap = new HashMap<>();
            if (!teamIds.isEmpty()) {
                List<Team> teams = teamMapper.selectBatchIds(teamIds);
                teamMap = teams.stream()
                        .collect(Collectors.toMap(Team::getId, t -> t, (v1, v2) -> v1));
                
                // 获取队长ID列表
                List<Integer> leaderIds = teams.stream()
                        .filter(t -> t.getLeaderId() != null)
                        .map(Team::getLeaderId)
                        .distinct()
                        .collect(Collectors.toList());
                
                // 查询队长信息
                if (!leaderIds.isEmpty()) {
                    List<User> leaders = userMapper.selectBatchIds(leaderIds);
                    teamLeaderMap = leaders.stream()
                            .collect(Collectors.toMap(User::getId, 
                                    u -> u.getRealName() != null ? u.getRealName() : u.getUsername(), 
                                    (v1, v2) -> v1));
                }
            }
            
            // 转换为Map列表
            final Map<Integer, String> finalCompetitionMap = competitionMap;
            final Map<Integer, String> finalUserMap = userMap;
            final Map<Integer, Team> finalTeamMap = teamMap;
            final Map<Integer, String> finalTeamLeaderMap = teamLeaderMap;
            List<Map<String, Object>> recordList = records.stream().map(registration -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", registration.getId());
                map.put("competitionId", registration.getCompetitionId());
                map.put("competitionName", registration.getCompetitionId() != null ? 
                        finalCompetitionMap.getOrDefault(registration.getCompetitionId(), "未知竞赛") : "");
                map.put("userId", registration.getUserId());
                
                // 判断是个人报名还是团队报名
                String userName;
                if (registration.getTeamId() != null) {
                    // 团队报名：显示团队名称（队长姓名）
                    Team team = finalTeamMap.get(registration.getTeamId());
                    if (team != null) {
                        String leaderName = team.getLeaderId() != null ? 
                                finalTeamLeaderMap.getOrDefault(team.getLeaderId(), "未知队长") : "未知队长";
                        userName = team.getName() + "（队长：" + leaderName + "）";
                    } else {
                        userName = "未知团队";
                    }
                } else if (registration.getUserId() != null) {
                    // 个人报名：显示用户姓名
                    userName = finalUserMap.getOrDefault(registration.getUserId(), "未知用户");
                } else {
                    userName = "未知用户";
                }
                map.put("userName", userName);
                
                map.put("teamId", registration.getTeamId());
                map.put("status", registration.getStatus());
                map.put("rejectReason", registration.getRejectReason());
                map.put("paymentStatus", registration.getPaymentStatus());
                map.put("paymentAmount", registration.getPaymentAmount());
                map.put("paymentTime", registration.getPaymentTime());
                map.put("paymentVoucher", registration.getPaymentVoucher());
                map.put("createTime", registration.getCreateTime());
                map.put("updateTime", registration.getUpdateTime());
                map.put("deleted", registration.getDeleted());
                return map;
            }).collect(Collectors.toList());
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("records", recordList);
            resultMap.put("total", result.getTotal());
            resultMap.put("size", result.getSize());
            resultMap.put("current", result.getCurrent());
            resultMap.put("pages", result.getPages());
            
            return Result.success(resultMap);
        }
        
        return Result.success(result);
    }

    @Override
    public Result refund(Integer registrationId, String reason) {
        Registration registration = registrationMapper.selectById(registrationId);
        if (registration == null || registration.getDeleted() == 1) {
            return Result.error("报名记录不存在");
        }
        if (registration.getPaymentStatus() != Constants.PAYMENT_STATUS_PAID) {
            return Result.error("该记录未缴费，无法退款");
        }
        
        // 保存退款金额
        BigDecimal refundAmount = registration.getPaymentAmount();
        
        // 更新报名记录状态
        registration.setPaymentStatus(Constants.PAYMENT_STATUS_REFUNDED);
        registration.setPaymentVoucher(registration.getPaymentVoucher() + " [退款原因:" + reason + "]");
        registrationMapper.updateById(registration);
        
        // 更新用户余额
        if (registration.getUserId() != null && refundAmount != null) {
            User user = userMapper.selectById(registration.getUserId());
            if (user != null) {
                BigDecimal currentBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
                user.setBalance(currentBalance.add(refundAmount));
                userMapper.updateById(user);
            }
        }
        
        // 发送退款通知给学生
        try {
            Message message = new Message();
            message.setUserId(registration.getUserId());
            message.setTitle("退费通知");
            message.setContent("您的竞赛报名费用已退还，原因：" + reason + "。请重新查看竞赛信息并重新缴费报名。");
            message.setType("缴费通知");
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            messageMapper.insert(message);
        } catch (Exception e) {
            // 通知发送失败不影响退款流程
            e.printStackTrace();
        }
        
        return Result.success("退款成功");
    }

    @Override
    public Result sendPaymentNotice(Integer registrationId) {
        Registration registration = registrationMapper.selectById(registrationId);
        if (registration == null || registration.getDeleted() == 1) {
            return Result.error("报名记录不存在");
        }
        if (registration.getStatus() != Constants.REGISTRATION_STATUS_APPROVED) {
            return Result.error("报名未通过审核，无法发送缴费通知");
        }
        Message message = new Message();
        message.setUserId(registration.getUserId());
        message.setTitle("缴费通知");
        message.setContent("您的报名已通过审核，请及时缴纳报名费 ¥" + registration.getPaymentAmount());
        message.setType("缴费提醒");
        message.setIsRead(0);
        messageMapper.insert(message);
        return Result.success("缴费通知已发送");
    }

    @Override
    public Result getPaymentStatistics() {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getDeleted, 0);
        
        long total = registrationMapper.selectCount(wrapper);
        
        wrapper.eq(Registration::getPaymentStatus, Constants.PAYMENT_STATUS_PAID);
        long paid = registrationMapper.selectCount(wrapper);
        
        wrapper.clear();
        wrapper.eq(Registration::getDeleted, 0)
                .eq(Registration::getPaymentStatus, Constants.PAYMENT_STATUS_PAID);
        // 计算总金额需要查询所有已缴费记录
        // 简化处理，实际应该用SQL聚合查询
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", total);
        statistics.put("paid", paid);
        statistics.put("unpaid", total - paid);
        statistics.put("paidRate", total > 0 ? (paid * 100.0 / total) : 0);
        
        return Result.success(statistics);
    }
}

