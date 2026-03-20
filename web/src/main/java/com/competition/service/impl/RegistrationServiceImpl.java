package com.competition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.common.Constants;
import com.competition.common.Result;
import com.competition.entity.Competition;
import com.competition.entity.Registration;
import com.competition.entity.Team;
import com.competition.entity.TeamMember;
import com.competition.entity.User;
import com.competition.entity.Work;
import com.competition.mapper.CompetitionMapper;
import com.competition.mapper.RegistrationMapper;
import com.competition.mapper.TeamMapper;
import com.competition.mapper.TeamMemberMapper;
import com.competition.mapper.UserMapper;
import com.competition.mapper.WorkMapper;
import com.competition.service.RegistrationService;
import com.competition.service.MessageService;
import com.competition.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService {
    @Autowired
    private CompetitionMapper competitionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private TeamMemberMapper teamMemberMapper;
    @Autowired
    private WorkMapper workMapper;
    @Autowired
    private MessageService messageService;

    @Override
    @Transactional
    public Result register(Registration registration, Integer userId) {
        // 验证竞赛是否存在
        Competition competition = competitionMapper.selectById(registration.getCompetitionId());
        if (competition == null || competition.getDeleted() == 1) {
            return Result.error("竞赛不存在");
        }

        // 验证竞赛是否已发布
        if (competition.getStatus() != Constants.COMPETITION_STATUS_PUBLISHED) {
            return Result.error("竞赛未发布，无法报名");
        }

        // 验证报名时间
        LocalDateTime now = LocalDateTime.now();
        if (competition.getRegistrationStart() != null && now.isBefore(competition.getRegistrationStart())) {
            return Result.error("报名尚未开始");
        }
        if (competition.getRegistrationEnd() != null && now.isAfter(competition.getRegistrationEnd())) {
            return Result.error("报名已结束");
        }

        // 检查是否已报名：
        // 仅当存在“未被驳回”的有效报名记录时，才视为已报名；
        // 如果之前的报名已被驳回（REGISTRATION_STATUS_REJECTED），允许重新报名。
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getCompetitionId, registration.getCompetitionId())
                .eq(Registration::getUserId, userId)
                .eq(Registration::getDeleted, 0)
                .ne(Registration::getStatus, Constants.REGISTRATION_STATUS_REJECTED);
        if (this.count(wrapper) > 0) {
            return Result.error("您已报名该竞赛");
        }

        // 设置报名信息
        registration.setUserId(userId);
        registration.setStatus(Constants.REGISTRATION_STATUS_PENDING);
        registration.setPaymentStatus(Constants.PAYMENT_STATUS_UNPAID);
        registration.setPaymentAmount(competition.getRegistrationFee());
        registration.setCreateTime(LocalDateTime.now());
        registration.setUpdateTime(LocalDateTime.now());
        registration.setDeleted(0);

        // 保存报名记录
        this.save(registration);
        return Result.success("报名成功，等待审核");
    }

    @Override
    @Transactional
    public Result approveRegistration(Integer id, Integer status, String rejectReason, Integer operatorId, String operatorRole) {
        Registration registration = this.getById(id);
        if (registration == null || registration.getDeleted() == 1) {
            return Result.error("报名记录不存在");
        }

        // 查出对应竞赛，判断是校赛还是省赛
        Competition competition = competitionMapper.selectById(registration.getCompetitionId());
        if (competition == null || competition.getDeleted() == 1) {
            return Result.error("对应竞赛不存在");
        }

        // 校赛：只能由发布该竞赛的老师审核
        if (competition.getIsSystem() != null && competition.getIsSystem() == 0) {
            if (!Constants.ROLE_TEACHER.equals(operatorRole) || !competition.getPublisherId().equals(operatorId)) {
                return Result.error("只有发布该校赛的老师可以审核该报名");
            }
        }
        // 省赛/系统竞赛：只能管理员审核
        if (competition.getIsSystem() != null && competition.getIsSystem() == 1) {
            if (!Constants.ROLE_ADMIN.equals(operatorRole)) {
                return Result.error("省赛报名只能由管理员审核");
            }
        }

        if (status == Constants.REGISTRATION_STATUS_APPROVED) {
            registration.setStatus(Constants.REGISTRATION_STATUS_APPROVED);
            registration.setRejectReason(null);
            
            // 如果报名费为0，自动设置为已缴费
            BigDecimal paymentAmount = registration.getPaymentAmount();
            if (paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) == 0) {
                registration.setPaymentStatus(Constants.PAYMENT_STATUS_PAID);
                registration.setPaymentTime(LocalDateTime.now());
            }
        } else if (status == Constants.REGISTRATION_STATUS_REJECTED) {
            registration.setStatus(Constants.REGISTRATION_STATUS_REJECTED);
            registration.setRejectReason(rejectReason);
        } else {
            return Result.error("无效的审核状态");
        }

        registration.setUpdateTime(LocalDateTime.now());
        this.updateById(registration);
        
        // 发送通知
        sendRegistrationNotification(registration, competition, status, rejectReason);
        
        return Result.success("审核成功");
    }

    @Override
    public Result getRegistrationList(Integer competitionId, Integer status, Integer isSystem, Integer page, Integer size) {
        Page<Registration> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getDeleted, 0);
        
        if (competitionId != null) {
            wrapper.eq(Registration::getCompetitionId, competitionId);
        }
        if (status != null) {
            wrapper.eq(Registration::getStatus, status);
        }

        // 如果需要按是否系统竞赛筛选，则先找出符合条件的竞赛ID集合
        if (isSystem != null) {
            LambdaQueryWrapper<Competition> cWrapper = new LambdaQueryWrapper<>();
            cWrapper.eq(Competition::getDeleted, 0)
                    .eq(Competition::getIsSystem, isSystem);
            List<Competition> competitions = competitionMapper.selectList(cWrapper);
            if (competitions == null || competitions.isEmpty()) {
                Map<String, Object> empty = new HashMap<>();
                empty.put("records", new ArrayList<>());
                empty.put("total", 0);
                empty.put("size", size);
                empty.put("current", page);
                empty.put("pages", 0);
                return Result.success(empty);
            }
            List<Integer> allowedIds = competitions.stream()
                    .map(Competition::getId)
                    .collect(Collectors.toList());
            wrapper.in(Registration::getCompetitionId, allowedIds);
        }
        
        wrapper.orderByDesc(Registration::getCreateTime);
        Page<Registration> result = this.page(pageParam, wrapper);
        
        List<Registration> records = result.getRecords();
        if (records != null && !records.isEmpty()) {
            // 获取所有竞赛ID和用户ID
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
            
            // 查询竞赛信息
            Map<Integer, String> competitionMap = new HashMap<>();
            Map<Integer, Competition> competitionDetailMap = new HashMap<>();
            if (!competitionIds.isEmpty()) {
                List<Competition> competitions = competitionMapper.selectBatchIds(competitionIds);
                competitionMap = competitions.stream()
                        .collect(Collectors.toMap(Competition::getId, Competition::getName, (v1, v2) -> v1));
                competitionDetailMap = competitions.stream()
                        .collect(Collectors.toMap(Competition::getId, c -> c, (v1, v2) -> v1));
            }
            
            // 查询用户信息
            Map<Integer, String> userNameMap = new HashMap<>();
            Map<Integer, User> userDetailMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(userIds);
                userDetailMap = users.stream()
                        .collect(Collectors.toMap(User::getId, u -> u, (v1, v2) -> v1));
                userNameMap = users.stream()
                        .collect(Collectors.toMap(User::getId,
                                u -> u.getRealName() != null ? u.getRealName() : u.getUsername(),
                                (v1, v2) -> v1));
            }
            
            // 查询团队信息
            Map<Integer, Team> teamMap = new HashMap<>();
            Map<Integer, String> teamLeaderMap = new HashMap<>();
            if (!teamIds.isEmpty()) {
                List<Team> teams = teamMapper.selectBatchIds(teamIds);
                teamMap = teams.stream()
                        .collect(Collectors.toMap(Team::getId, t -> t, (v1, v2) -> v1));
                
                List<Integer> leaderIds = teams.stream()
                        .filter(t -> t.getLeaderId() != null)
                        .map(Team::getLeaderId)
                        .distinct()
                        .collect(Collectors.toList());
                
                if (!leaderIds.isEmpty()) {
                    List<User> leaders = userMapper.selectBatchIds(leaderIds);
                    teamLeaderMap = leaders.stream()
                            .collect(Collectors.toMap(User::getId, 
                                    u -> u.getRealName() != null ? u.getRealName() : u.getUsername(), 
                                    (v1, v2) -> v1));
                }
            }
            
            final Map<Integer, String> finalCompetitionMap = competitionMap;
            final Map<Integer, Competition> finalCompetitionDetailMap = competitionDetailMap;
            final Map<Integer, String> finalUserNameMap = userNameMap;
            final Map<Integer, User> finalUserDetailMap = userDetailMap;
            final Map<Integer, Team> finalTeamMap = teamMap;
            final Map<Integer, String> finalTeamLeaderMap = teamLeaderMap;
            
            List<Map<String, Object>> recordList = records.stream().map(registration -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", registration.getId());
                map.put("competitionId", registration.getCompetitionId());
                map.put("competitionName", registration.getCompetitionId() != null ? 
                        finalCompetitionMap.getOrDefault(registration.getCompetitionId(), "未知竞赛") : "");
                // 添加竞赛的needWork字段
                Competition comp = registration.getCompetitionId() != null ? 
                        finalCompetitionDetailMap.get(registration.getCompetitionId()) : null;
                map.put("needWork", comp != null ? comp.getNeedWork() : 0);
                map.put("userId", registration.getUserId());
                
                // 判断是个人报名还是团队报名
                String userName;
                String studentNo = null;
                String className = null;
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
                    // 个人报名：显示用户姓名，并附带学号和班级信息
                    userName = finalUserNameMap.getOrDefault(registration.getUserId(), "未知用户");
                    User user = finalUserDetailMap.get(registration.getUserId());
                    if (user != null) {
                        studentNo = user.getStudentNo();
                        className = user.getClassName();
                    }
                } else {
                    userName = "未知用户";
                }
                map.put("userName", userName);
                map.put("studentNo", studentNo);
                map.put("className", className);
                
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
        
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("records", new ArrayList<>());
        resultMap.put("total", 0);
        resultMap.put("size", size);
        resultMap.put("current", page);
        resultMap.put("pages", 0);
        
        return Result.success(resultMap);
    }

    @Override
    public Result getMyRegistrations(Integer userId, Integer page, Integer size) {
        Page<Registration> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getUserId, userId)
                .eq(Registration::getDeleted, 0)
                .orderByDesc(Registration::getCreateTime);
        
        Page<Registration> result = this.page(pageParam, wrapper);
        
        List<Registration> records = result.getRecords();
        if (records != null && !records.isEmpty()) {
            // 获取竞赛信息
            List<Integer> competitionIds = records.stream()
                    .filter(r -> r.getCompetitionId() != null)
                    .map(Registration::getCompetitionId)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<Integer, Competition> competitionMap = new HashMap<>();
            if (!competitionIds.isEmpty()) {
                List<Competition> competitions = competitionMapper.selectBatchIds(competitionIds);
                competitionMap = competitions.stream()
                        .collect(Collectors.toMap(Competition::getId, c -> c, (v1, v2) -> v1));
            }
            
            final Map<Integer, Competition> finalCompetitionMap = competitionMap;
            
            // 查询作品提交情况
            List<Integer> registrationIds = records.stream()
                    .map(Registration::getId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Integer, Boolean> workSubmittedMap = new HashMap<>();
            if (!registrationIds.isEmpty()) {
                LambdaQueryWrapper<Work> workWrapper = new LambdaQueryWrapper<>();
                workWrapper.in(Work::getRegistrationId, registrationIds)
                           .eq(Work::getDeleted, 0);
                List<Work> works = workMapper.selectList(workWrapper);
                if (works != null && !works.isEmpty()) {
                    works.stream()
                            .map(Work::getRegistrationId)
                            .distinct()
                            .forEach(regId -> workSubmittedMap.put(regId, true));
                }
            }
            final Map<Integer, Boolean> finalWorkSubmittedMap = workSubmittedMap;
            
            List<Map<String, Object>> recordList = records.stream().map(registration -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", registration.getId());
                map.put("competitionId", registration.getCompetitionId());
                
                Competition competition = registration.getCompetitionId() != null ? 
                        finalCompetitionMap.get(registration.getCompetitionId()) : null;
                map.put("competitionName", competition != null ? competition.getName() : "未知竞赛");
                // 竞赛是否需要作品
                map.put("needWork", competition != null ? competition.getNeedWork() : 0);
                // 竞赛开始时间（用于控制作品提交开始时间）
                map.put("startTime", competition != null ? competition.getStartTime() : null);
                // 报名结束时间 & 竞赛结束时间（用于控制作品提交截止时间）
                map.put("registrationEnd", competition != null ? competition.getRegistrationEnd() : null);
                map.put("endTime", competition != null ? competition.getEndTime() : null);
                
                map.put("teamId", registration.getTeamId());
                map.put("status", registration.getStatus());
                map.put("rejectReason", registration.getRejectReason());
                map.put("paymentStatus", registration.getPaymentStatus());
                map.put("paymentAmount", registration.getPaymentAmount());
                map.put("paymentTime", registration.getPaymentTime());
                map.put("createTime", registration.getCreateTime());
                map.put("updateTime", registration.getUpdateTime());
                // 是否已提交作品
                map.put("workSubmitted", finalWorkSubmittedMap.getOrDefault(registration.getId(), false));
                // 竞赛报名费（用于判断是否需要缴费）
                map.put("registrationFee", competition != null ? competition.getRegistrationFee() : null);
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
        
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("records", new ArrayList<>());
        resultMap.put("total", 0);
        resultMap.put("size", size);
        resultMap.put("current", page);
        resultMap.put("pages", 0);
        
        return Result.success(resultMap);
    }

    @Override
    @Transactional
    public Result pay(Integer registrationId, Integer userId) {
        Registration registration = this.getById(registrationId);
        if (registration == null || registration.getDeleted() == 1) {
            return Result.error("报名记录不存在");
        }

        if (!registration.getUserId().equals(userId)) {
            return Result.error("无权限操作");
        }

        if (registration.getStatus() != Constants.REGISTRATION_STATUS_APPROVED) {
            return Result.error("报名未通过审核，无法缴费");
        }

        if (registration.getPaymentStatus() == Constants.PAYMENT_STATUS_PAID) {
            return Result.error("已缴费，无需重复缴费");
        }

        // 查询当前竞赛，兼容报名后修改报名费（含改为0元）的情况
        Competition competition = competitionMapper.selectById(registration.getCompetitionId());
        if (competition == null || competition.getDeleted() == 1) {
            return Result.error("对应竞赛不存在");
        }

        BigDecimal paymentAmount = registration.getPaymentAmount();
        BigDecimal currentFee = competition.getRegistrationFee();

        // 如果当前竞赛报名费为0或为空，则视为免费竞赛，直接标记为已缴费，不从余额扣钱
        if (currentFee == null || currentFee.compareTo(BigDecimal.ZERO) <= 0) {
            registration.setPaymentStatus(Constants.PAYMENT_STATUS_PAID);
            registration.setPaymentTime(LocalDateTime.now());
            registration.setUpdateTime(LocalDateTime.now());
            this.updateById(registration);
            return Result.success("该竞赛为免费竞赛，已自动标记为已缴费");
        }

        // 如果报名记录中的缴费金额无效（例如之前是0），则以当前竞赛报名费为准进行修正
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            paymentAmount = currentFee;
            registration.setPaymentAmount(paymentAmount);
        }

        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            return Result.error("用户不存在");
        }

        // 检查余额是否足够
        if (user.getBalance() == null || user.getBalance().compareTo(paymentAmount) < 0) {
            return Result.error("余额不足，请先充值");
        }

        // 扣除余额
        user.setBalance(user.getBalance().subtract(paymentAmount));
        userMapper.updateById(user);

        // 更新报名记录
        registration.setPaymentStatus(Constants.PAYMENT_STATUS_PAID);
        registration.setPaymentTime(LocalDateTime.now());
        registration.setUpdateTime(LocalDateTime.now());
        this.updateById(registration);

        return Result.success("缴费成功");
    }
    
    /**
     * 发送报名审核通知
     */
    private void sendRegistrationNotification(Registration registration, Competition competition, Integer status, String rejectReason) {
        List<Integer> userIds = new ArrayList<>();
        
        // 个人报名：只通知报名者
        if (registration.getTeamId() == null && registration.getUserId() != null) {
            userIds.add(registration.getUserId());
        }
        // 团队报名：通知团队所有成员
        else if (registration.getTeamId() != null) {
            LambdaQueryWrapper<TeamMember> teamMemberWrapper = new LambdaQueryWrapper<>();
            teamMemberWrapper.eq(TeamMember::getTeamId, registration.getTeamId());
            List<TeamMember> teamMembers = teamMemberMapper.selectList(teamMemberWrapper);
            if (teamMembers != null && !teamMembers.isEmpty()) {
                for (TeamMember member : teamMembers) {
                    if (member.getUserId() != null) {
                        userIds.add(member.getUserId());
                    }
                }
            }
        }
        
        // 发送通知给所有相关用户
        for (Integer userId : userIds) {
            Message message = new Message();
            message.setUserId(userId);
            message.setCreateTime(LocalDateTime.now());
            message.setIsRead(0);
            message.setType("报名通知");
            
            if (status == Constants.REGISTRATION_STATUS_APPROVED) {
                message.setTitle("报名审核通过通知");
                message.setContent("您报名的竞赛《" + competition.getName() + "》已通过审核，请及时查看报名状态。");
            } else if (status == Constants.REGISTRATION_STATUS_REJECTED) {
                message.setTitle("报名审核驳回通知");
                if (StringUtils.hasText(rejectReason)) {
                    message.setContent("您报名的竞赛《" + competition.getName() + "》未通过审核，驳回原因：" + rejectReason);
                } else {
                    message.setContent("您报名的竞赛《" + competition.getName() + "》未通过审核，请联系相关老师了解详情。");
                }
            }
            
            try {
                messageService.sendMessage(message);
            } catch (Exception e) {
                // 通知发送失败不影响审核流程
                e.printStackTrace();
            }
        }
    }
}
