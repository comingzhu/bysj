package com.competition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.common.Result;
import com.competition.common.Constants;
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
import com.competition.mapper.ScoreTaskMapper;
import com.competition.entity.ScoreTask;
import com.competition.entity.CompetitionJudge;
import com.competition.mapper.CompetitionJudgeMapper;
import com.competition.entity.ScoreTask;
import com.competition.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkServiceImpl extends ServiceImpl<WorkMapper, Work> implements WorkService {
    @Autowired
    private CompetitionMapper competitionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private TeamMemberMapper teamMemberMapper;
    @Autowired
    private RegistrationMapper registrationMapper;
    @Autowired
    private ScoreTaskMapper scoreTaskMapper;
    @Autowired
    private CompetitionJudgeMapper competitionJudgeMapper;

    @Override
    public Result getWorkList(Integer competitionId, Integer page, Integer size, Integer publisherId) {
        Page<Work> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Work::getDeleted, 0);
        
        // 如果是老师，只显示该老师发布的校赛的作品
        if (publisherId != null) {
            // 先查询该老师发布的校赛ID列表
            LambdaQueryWrapper<Competition> compWrapper = new LambdaQueryWrapper<>();
            compWrapper.eq(Competition::getPublisherId, publisherId)
                      .eq(Competition::getIsSystem, 0) // 只查询校赛
                      .eq(Competition::getDeleted, 0);
            List<Competition> teacherCompetitions = competitionMapper.selectList(compWrapper);
            if (teacherCompetitions == null || teacherCompetitions.isEmpty()) {
                // 该老师没有发布的校赛，返回空结果
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("records", new ArrayList<>());
                emptyResult.put("total", 0);
                emptyResult.put("size", size);
                emptyResult.put("current", page);
                emptyResult.put("pages", 0);
                return Result.success(emptyResult);
            }
            List<Integer> teacherCompetitionIds = teacherCompetitions.stream()
                    .map(Competition::getId)
                    .collect(Collectors.toList());
            
            if (competitionId != null) {
                // 如果指定了competitionId，验证是否是该老师发布的
                if (!teacherCompetitionIds.contains(competitionId)) {
                    Map<String, Object> emptyResult = new HashMap<>();
                    emptyResult.put("records", new ArrayList<>());
                    emptyResult.put("total", 0);
                    emptyResult.put("size", size);
                    emptyResult.put("current", page);
                    emptyResult.put("pages", 0);
                    return Result.success(emptyResult);
                }
                wrapper.eq(Work::getCompetitionId, competitionId);
            } else {
                // 如果没有指定competitionId，只查询该老师发布的校赛的作品
                wrapper.in(Work::getCompetitionId, teacherCompetitionIds);
            }
        } else {
            // 管理员可以看到所有作品
            if (competitionId != null) {
                wrapper.eq(Work::getCompetitionId, competitionId);
            }
        }
        
        wrapper.orderByDesc(Work::getSubmitTime);
        Page<Work> result = this.page(pageParam, wrapper);
        
        // 关联查询竞赛名称、提交者信息
        List<Work> records = result.getRecords();
        if (records != null && !records.isEmpty()) {
            List<Integer> competitionIds = records.stream()
                    .map(Work::getCompetitionId)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<Integer> userIds = records.stream()
                    .filter(w -> w.getUserId() != null)
                    .map(Work::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<Integer> teamIds = records.stream()
                    .filter(w -> w.getTeamId() != null)
                    .map(Work::getTeamId)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<Integer, String> competitionMap = new HashMap<>();
            if (!competitionIds.isEmpty()) {
                List<Competition> competitions = competitionMapper.selectBatchIds(competitionIds);
                competitionMap = competitions.stream()
                        .collect(Collectors.toMap(Competition::getId, Competition::getName, (v1, v2) -> v1));
            }
            
            Map<Integer, String> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(userIds);
                userMap = users.stream()
                        .collect(Collectors.toMap(User::getId, 
                                u -> u.getRealName() != null ? u.getRealName() : u.getUsername(), 
                                (v1, v2) -> v1));
            }
            
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
            final Map<Integer, String> finalUserMap = userMap;
            final Map<Integer, Team> finalTeamMap = teamMap;
            final Map<Integer, String> finalTeamLeaderMap = teamLeaderMap;
            
            List<Map<String, Object>> recordList = records.stream().map(work -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", work.getId());
                map.put("competitionId", work.getCompetitionId());
                map.put("competitionName", finalCompetitionMap.getOrDefault(work.getCompetitionId(), "未知竞赛"));
                map.put("registrationId", work.getRegistrationId());
                map.put("userId", work.getUserId());
                map.put("teamId", work.getTeamId());
                
                // 提交者信息
                String submitterName;
                if (work.getTeamId() != null) {
                    Team team = finalTeamMap.get(work.getTeamId());
                    if (team != null) {
                        String leaderName = team.getLeaderId() != null ? 
                                finalTeamLeaderMap.getOrDefault(team.getLeaderId(), "未知队长") : "未知队长";
                        submitterName = team.getName() + "（队长：" + leaderName + "）";
                    } else {
                        submitterName = "未知团队";
                    }
                } else if (work.getUserId() != null) {
                    submitterName = finalUserMap.getOrDefault(work.getUserId(), "未知用户");
                } else {
                    submitterName = "未知用户";
                }
                map.put("submitterName", submitterName);
                
                map.put("title", work.getTitle());
                map.put("description", work.getDescription());
                map.put("filePath", work.getFilePath());
                map.put("fileName", work.getFileName());
                map.put("fileSize", work.getFileSize());
                map.put("submitTime", work.getSubmitTime());
                map.put("status", work.getStatus());
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
    public Result getWorkDetail(Integer workId) {
        Work work = this.getById(workId);
        if (work == null || (work.getDeleted() != null && work.getDeleted() == 1)) {
            return Result.error("作品不存在");
        }
        
        Competition competition = competitionMapper.selectById(work.getCompetitionId());
        User user = work.getUserId() != null ? userMapper.selectById(work.getUserId()) : null;
        Team team = work.getTeamId() != null ? teamMapper.selectById(work.getTeamId()) : null;
        User leader = team != null && team.getLeaderId() != null ? 
                      userMapper.selectById(team.getLeaderId()) : null;
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", work.getId());
        result.put("competitionId", work.getCompetitionId());
        result.put("competitionName", competition != null ? competition.getName() : "未知竞赛");
        result.put("title", work.getTitle());
        result.put("description", work.getDescription());
        result.put("filePath", work.getFilePath());
        result.put("fileName", work.getFileName());
        result.put("fileSize", work.getFileSize());
        result.put("submitTime", work.getSubmitTime());
        result.put("status", work.getStatus());
        
        if (team != null) {
            result.put("teamName", team.getName());
            result.put("leaderName", leader != null ? 
                       (leader.getRealName() != null ? leader.getRealName() : leader.getUsername()) : "未知");
        } else if (user != null) {
            result.put("submitterName", user.getRealName() != null ? user.getRealName() : user.getUsername());
        }
        
        return Result.success(result);
    }

    @Override
    public Result updateWorkStatus(Integer workId, Integer status) {
        Work work = this.getById(workId);
        if (work == null || work.getDeleted() == 1) {
            return Result.error("作品不存在");
        }
        work.setStatus(status);
        this.updateById(work);
        return Result.success("操作成功");
    }

    @Override
    public Result submitWork(Work work, Integer userId) {
        // 检查报名记录
        Registration registration = registrationMapper.selectById(work.getRegistrationId());
        if (registration == null || registration.getDeleted() == 1) {
            return Result.error("报名记录不存在");
        }
        if (registration.getStatus() != Constants.REGISTRATION_STATUS_APPROVED) {
            return Result.error("报名未通过审核，无法提交作品");
        }
        if (registration.getPaymentStatus() != Constants.PAYMENT_STATUS_PAID) {
            return Result.error("未缴费，无法提交作品");
        }
        
        Competition competition = competitionMapper.selectById(work.getCompetitionId());
        if (competition == null || competition.getDeleted() == 1) {
            return Result.error("竞赛不存在");
        }
        if (competition.getNeedWork() != 1) {
            return Result.error("该竞赛不需要提交作品");
        }
        
        // 检查是否已提交作品
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Work::getRegistrationId, work.getRegistrationId())
               .eq(Work::getDeleted, 0);
        Work existing = this.getOne(wrapper);
        if (existing != null) {
            // 更新作品
            existing.setTitle(work.getTitle());
            existing.setDescription(work.getDescription());
            existing.setFilePath(work.getFilePath());
            existing.setFileName(work.getFileName());
            existing.setFileSize(work.getFileSize());
            existing.setSubmitTime(java.time.LocalDateTime.now());
            this.updateById(existing);
            
            // 作品更新后，如果报名时间已结束，创建或更新评分任务
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if (competition.getRegistrationEnd() != null && now.isAfter(competition.getRegistrationEnd())) {
                createScoreTasks(existing.getId(), competition);
            }
            
            return Result.success("作品更新成功");
        }
        
        // 新建作品
        work.setUserId(registration.getUserId());
        work.setTeamId(registration.getTeamId());
        work.setSubmitTime(java.time.LocalDateTime.now());
        work.setStatus(0);
        this.save(work);
        
        // 作品提交成功后，自动创建评分任务
        // 检查报名时间是否已结束（报名结束后才能评分）
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (competition.getRegistrationEnd() != null && now.isAfter(competition.getRegistrationEnd())) {
            // 报名时间已结束，创建评分任务
            createScoreTasks(work.getId(), competition);
        }
        
        return Result.success("作品提交成功");
    }
    
    /**
     * 为作品创建评分任务
     * 根据竞赛分类匹配评分员（judge角色）
     */
    private void createScoreTasks(Integer workId, Competition competition) {
        // 查询该竞赛配置的评分员（competition_judge）
        LambdaQueryWrapper<CompetitionJudge> cjWrapper = new LambdaQueryWrapper<>();
        cjWrapper.eq(CompetitionJudge::getCompetitionId, competition.getId());
        List<CompetitionJudge> cjList = competitionJudgeMapper.selectList(cjWrapper);
        if (cjList == null || cjList.isEmpty()) {
            return;
        }
        List<Integer> judgeIds = cjList.stream()
                .map(CompetitionJudge::getJudgeId)
                .distinct()
                .collect(Collectors.toList());
        if (judgeIds.isEmpty()) {
            return;
        }
        List<User> judges = userMapper.selectBatchIds(judgeIds);
        
        if (judges == null || judges.isEmpty()) {
            return; // 没有评分员，不创建任务
        }
        
        // 为每个评分员创建评分任务
        // 设置评分截止时间：竞赛结束时间后7天；如果没有结束时间，则报名结束后7天
        java.time.LocalDateTime deadline;
        if (competition.getEndTime() != null) {
            deadline = competition.getEndTime().plusDays(7);
        } else if (competition.getRegistrationEnd() != null) {
            deadline = competition.getRegistrationEnd().plusDays(7);
        } else {
            deadline = java.time.LocalDateTime.now().plusDays(7);
        }
        
        for (User judge : judges) {
            // 检查是否已存在该评分员对该作品的任务
            LambdaQueryWrapper<ScoreTask> taskWrapper = new LambdaQueryWrapper<>();
            taskWrapper.eq(ScoreTask::getWorkId, workId)
                      .eq(ScoreTask::getJudgeId, judge.getId());
            if (scoreTaskMapper.selectCount(taskWrapper) == 0) {
                ScoreTask task = new ScoreTask();
                task.setCompetitionId(competition.getId());
                task.setJudgeId(judge.getId());
                task.setWorkId(workId);
                task.setStatus(0); // 待评分
                task.setDeadline(deadline);
                task.setCreateTime(java.time.LocalDateTime.now());
                task.setUpdateTime(java.time.LocalDateTime.now());
                scoreTaskMapper.insert(task);
            }
        }
    }

    @Override
    public Result getMyWorks(Integer userId, Integer competitionId) {
        // 先查询该用户的报名记录
        LambdaQueryWrapper<Registration> regWrapper = new LambdaQueryWrapper<>();
        regWrapper.eq(Registration::getUserId, userId)
                  .eq(Registration::getDeleted, 0);
        if (competitionId != null) {
            regWrapper.eq(Registration::getCompetitionId, competitionId);
        }
        List<Registration> registrations = registrationMapper.selectList(regWrapper);
        
        // 也查询该用户作为团队成员的报名记录
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getUserId, userId)
                     .eq(TeamMember::getStatus, 1);
        List<TeamMember> memberships = teamMemberMapper.selectList(memberWrapper);
        
        if (memberships != null && !memberships.isEmpty()) {
            List<Integer> teamIds = memberships.stream()
                    .map(TeamMember::getTeamId)
                    .collect(Collectors.toList());
            
            LambdaQueryWrapper<Registration> teamRegWrapper = new LambdaQueryWrapper<>();
            teamRegWrapper.in(Registration::getTeamId, teamIds)
                         .eq(Registration::getDeleted, 0);
            if (competitionId != null) {
                teamRegWrapper.eq(Registration::getCompetitionId, competitionId);
            }
            List<Registration> teamRegistrations = registrationMapper.selectList(teamRegWrapper);
            registrations.addAll(teamRegistrations);
        }
        
        if (registrations.isEmpty()) {
            return Result.success(List.of());
        }
        
        List<Integer> registrationIds = registrations.stream()
                .map(Registration::getId)
                .distinct()
                .collect(Collectors.toList());
        
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Work::getRegistrationId, registrationIds)
               .eq(Work::getDeleted, 0);
        List<Work> works = this.list(wrapper);
        
        // 关联查询竞赛信息
        List<Integer> competitionIds = works.stream()
                .map(Work::getCompetitionId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Integer, String> competitionMap = new HashMap<>();
        if (!competitionIds.isEmpty()) {
            List<Competition> competitions = competitionMapper.selectBatchIds(competitionIds);
            competitionMap = competitions.stream()
                    .collect(Collectors.toMap(Competition::getId, Competition::getName, (v1, v2) -> v1));
        }
        
        final Map<Integer, String> finalCompetitionMap = competitionMap;
        List<Map<String, Object>> workList = works.stream().map(work -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", work.getId());
            map.put("competitionId", work.getCompetitionId());
            map.put("competitionName", finalCompetitionMap.getOrDefault(work.getCompetitionId(), "未知竞赛"));
            map.put("registrationId", work.getRegistrationId());
            map.put("title", work.getTitle());
            map.put("description", work.getDescription());
            map.put("filePath", work.getFilePath());
            map.put("fileName", work.getFileName());
            map.put("fileSize", work.getFileSize());
            map.put("submitTime", work.getSubmitTime());
            map.put("status", work.getStatus());
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(workList);
    }
}


