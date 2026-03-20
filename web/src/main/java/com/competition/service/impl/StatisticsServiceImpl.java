package com.competition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.competition.common.Result;
import com.competition.entity.Award;
import com.competition.entity.Competition;
import com.competition.entity.Registration;
import com.competition.entity.User;
import com.competition.entity.Team;
import com.competition.mapper.AwardMapper;
import com.competition.mapper.CompetitionMapper;
import com.competition.mapper.RegistrationMapper;
import com.competition.mapper.TeamMapper;
import com.competition.mapper.UserMapper;
import com.competition.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private RegistrationMapper registrationMapper;
    @Autowired
    private CompetitionMapper competitionMapper;
    @Autowired
    private AwardMapper awardMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeamMapper teamMapper;

    @Override
    public Result getRegistrationStatistics(String dimension, String value) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        
        if ("competition".equals(dimension)) {
            // 按竞赛统计 - 只查询未删除的竞赛
            LambdaQueryWrapper<Competition> compWrapper = new LambdaQueryWrapper<>();
            compWrapper.eq(Competition::getDeleted, 0);
            List<Competition> competitions = competitionMapper.selectList(compWrapper);
            
            for (Competition comp : competitions) {
                LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Registration::getCompetitionId, comp.getId())
                        .eq(Registration::getDeleted, 0);
                long count = registrationMapper.selectCount(wrapper);
                
                // 只添加有报名记录的竞赛，或者显示所有竞赛（包括0报名的）
                Map<String, Object> item = new HashMap<>();
                item.put("competitionName", comp.getName());
                item.put("count", count);
                data.add(item);
            }
        } else if ("college".equals(dimension)) {
            // 按学院统计
            // 查询所有报名记录
            LambdaQueryWrapper<Registration> registrationWrapper = new LambdaQueryWrapper<>();
            registrationWrapper.eq(Registration::getDeleted, 0);
            List<Registration> registrations = registrationMapper.selectList(registrationWrapper);
            
            // 收集所有需要查询的用户ID和团队ID
            List<Integer> userIds = registrations.stream()
                    .filter(r -> r.getUserId() != null)
                    .map(Registration::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<Integer> teamIds = registrations.stream()
                    .filter(r -> r.getTeamId() != null)
                    .map(Registration::getTeamId)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 查询用户信息
            Map<Integer, User> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(userIds);
                userMap = users.stream()
                        .collect(Collectors.toMap(User::getId, u -> u, (v1, v2) -> v1));
            }
            
            // 查询团队信息
            Map<Integer, Team> teamMap = new HashMap<>();
            Map<Integer, User> leaderMap = new HashMap<>();
            if (!teamIds.isEmpty()) {
                List<Team> teams = teamMapper.selectBatchIds(teamIds);
                teamMap = teams.stream()
                        .collect(Collectors.toMap(Team::getId, t -> t, (v1, v2) -> v1));
                
                // 查询队长信息
                List<Integer> leaderIds = teams.stream()
                        .filter(t -> t.getLeaderId() != null)
                        .map(Team::getLeaderId)
                        .distinct()
                        .collect(Collectors.toList());
                
                if (!leaderIds.isEmpty()) {
                    List<User> leaders = userMapper.selectBatchIds(leaderIds);
                    leaderMap = leaders.stream()
                            .collect(Collectors.toMap(User::getId, u -> u, (v1, v2) -> v1));
                }
            }
            
            // 按学院统计报名数
            Map<String, Long> collegeCountMap = new HashMap<>();
            
            for (Registration registration : registrations) {
                String college = null;
                
                if (registration.getTeamId() != null) {
                    // 团队报名：使用队长的学院
                    Team team = teamMap.get(registration.getTeamId());
                    if (team != null && team.getLeaderId() != null) {
                        User leader = leaderMap.get(team.getLeaderId());
                        if (leader != null && leader.getCollege() != null && !leader.getCollege().trim().isEmpty()) {
                            college = leader.getCollege();
                        }
                    }
                } else if (registration.getUserId() != null) {
                    // 个人报名：使用用户的学院
                    User user = userMap.get(registration.getUserId());
                    if (user != null && user.getCollege() != null && !user.getCollege().trim().isEmpty()) {
                        college = user.getCollege();
                    }
                }
                
                if (college != null && !college.trim().isEmpty()) {
                    collegeCountMap.put(college, collegeCountMap.getOrDefault(college, 0L) + 1);
                }
            }
            
            // 转换为结果列表
            for (Map.Entry<String, Long> entry : collegeCountMap.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("college", entry.getKey());
                item.put("count", entry.getValue());
                data.add(item);
            }
        }
        
        result.put("data", data);
        return Result.success(result);
    }

    @Override
    public Result getPaymentStatistics() {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getDeleted, 0);
        
        long total = registrationMapper.selectCount(wrapper);
        
        wrapper.eq(Registration::getPaymentStatus, 1);
        long paid = registrationMapper.selectCount(wrapper);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", total);
        statistics.put("paid", paid);
        statistics.put("unpaid", total - paid);
        statistics.put("paidRate", total > 0 ? (paid * 100.0 / total) : 0);
        
        return Result.success(statistics);
    }

    @Override
    public Result getAwardStatistics(String dimension) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        
        if ("competition".equals(dimension)) {
            // 按竞赛统计
            List<Competition> competitions = competitionMapper.selectList(null);
            for (Competition comp : competitions) {
                LambdaQueryWrapper<Award> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Award::getCompetitionId, comp.getId());
                long count = awardMapper.selectCount(wrapper);
                
                Map<String, Object> item = new HashMap<>();
                item.put("competitionName", comp.getName());
                item.put("count", count);
                data.add(item);
            }
        } else if ("level".equals(dimension)) {
            // 按奖项等级统计
            LambdaQueryWrapper<Award> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(Award::getAwardLevel).groupBy(Award::getAwardLevel);
            List<Award> awards = awardMapper.selectList(wrapper);
            
            for (Award award : awards) {
                if (award.getAwardLevel() == null) continue;
                LambdaQueryWrapper<Award> countWrapper = new LambdaQueryWrapper<>();
                countWrapper.eq(Award::getAwardLevel, award.getAwardLevel());
                long count = awardMapper.selectCount(countWrapper);
                
                Map<String, Object> item = new HashMap<>();
                item.put("level", award.getAwardLevel());
                item.put("count", count);
                data.add(item);
            }
        } else if ("college".equals(dimension)) {
            // 按学院统计
            List<Award> awards = awardMapper.selectList(null);
            Map<String, Integer> collegeMap = new HashMap<>();
            
            for (Award award : awards) {
                User user = null;
                if (award.getUserId() != null) {
                    user = userMapper.selectById(award.getUserId());
                }
                if (user != null && user.getCollege() != null) {
                    collegeMap.put(user.getCollege(), collegeMap.getOrDefault(user.getCollege(), 0) + 1);
                }
            }
            
            for (Map.Entry<String, Integer> entry : collegeMap.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("college", entry.getKey());
                item.put("count", entry.getValue());
                data.add(item);
            }
        }
        
        result.put("data", data);
        return Result.success(result);
    }

    @Override
    public Result getDashboardStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 用户统计
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getDeleted, 0);
        long totalUsers = userMapper.selectCount(userWrapper);
        
        userWrapper.eq(User::getRole, "student");
        long studentCount = userMapper.selectCount(userWrapper);
        
        userWrapper.clear();
        userWrapper.eq(User::getDeleted, 0).eq(User::getRole, "teacher");
        long teacherCount = userMapper.selectCount(userWrapper);
        
        userWrapper.clear();
        userWrapper.eq(User::getDeleted, 0).eq(User::getRole, "judge");
        long judgeCount = userMapper.selectCount(userWrapper);
        
        // 竞赛统计
        LambdaQueryWrapper<Competition> compWrapper = new LambdaQueryWrapper<>();
        compWrapper.eq(Competition::getDeleted, 0);
        long totalCompetitions = competitionMapper.selectCount(compWrapper);
        
        compWrapper.eq(Competition::getStatus, 1); // 待审核
        long pendingCompetitions = competitionMapper.selectCount(compWrapper);
        
        compWrapper.clear();
        compWrapper.eq(Competition::getDeleted, 0).eq(Competition::getStatus, 4); // 已发布
        long publishedCompetitions = competitionMapper.selectCount(compWrapper);
        
        // 报名统计
        LambdaQueryWrapper<Registration> regWrapper = new LambdaQueryWrapper<>();
        regWrapper.eq(Registration::getDeleted, 0);
        long totalRegistrations = registrationMapper.selectCount(regWrapper);
        
        regWrapper.eq(Registration::getStatus, 0); // 待审核
        long pendingRegistrations = registrationMapper.selectCount(regWrapper);
        
        regWrapper.clear();
        regWrapper.eq(Registration::getDeleted, 0).eq(Registration::getStatus, 1); // 已通过
        long approvedRegistrations = registrationMapper.selectCount(regWrapper);
        
        regWrapper.clear();
        regWrapper.eq(Registration::getDeleted, 0).eq(Registration::getPaymentStatus, 1); // 已缴费
        long paidRegistrations = registrationMapper.selectCount(regWrapper);
        
        // 获奖统计
        long totalAwards = awardMapper.selectCount(null);
        
        // 团队统计
        LambdaQueryWrapper<Team> teamWrapper = new LambdaQueryWrapper<>();
        long totalTeams = teamMapper.selectCount(teamWrapper);
        
        Map<String, Object> usersMap = new HashMap<>();
        usersMap.put("total", totalUsers);
        usersMap.put("students", studentCount);
        usersMap.put("teachers", teacherCount);
        usersMap.put("judges", judgeCount);
        statistics.put("users", usersMap);
        
        Map<String, Object> competitionsMap = new HashMap<>();
        competitionsMap.put("total", totalCompetitions);
        competitionsMap.put("pending", pendingCompetitions);
        competitionsMap.put("published", publishedCompetitions);
        statistics.put("competitions", competitionsMap);
        
        Map<String, Object> registrationsMap = new HashMap<>();
        registrationsMap.put("total", totalRegistrations);
        registrationsMap.put("pending", pendingRegistrations);
        registrationsMap.put("approved", approvedRegistrations);
        registrationsMap.put("paid", paidRegistrations);
        statistics.put("registrations", registrationsMap);
        
        statistics.put("awards", totalAwards);
        statistics.put("teams", totalTeams);
        
        return Result.success(statistics);
    }
}

