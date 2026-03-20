package com.competition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.common.Constants;
import com.competition.common.Result;
import com.competition.entity.*;
import com.competition.mapper.*;
import com.competition.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {
    @Autowired
    private TeamMemberMapper teamMemberMapper;
    @Autowired
    private CompetitionMapper competitionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RegistrationMapper registrationMapper;

    @Override
    @Transactional
    public Result createTeam(Team team, Integer userId) {
        Competition competition = competitionMapper.selectById(team.getCompetitionId());
        if (competition == null || competition.getDeleted() == 1) {
            return Result.error("竞赛不存在");
        }
        if (competition.getType() != Constants.COMPETITION_TYPE_TEAM) {
            return Result.error("该竞赛不是团队赛");
        }
        
        // 检查团队名称是否已存在
        LambdaQueryWrapper<Team> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(Team::getCompetitionId, team.getCompetitionId())
                   .eq(Team::getName, team.getName())
                   .eq(Team::getDeleted, 0);
        if (this.count(nameWrapper) > 0) {
            return Result.error("该竞赛中已存在同名团队");
        }
        
        // 检查是否已在该竞赛中创建或加入团队
        LambdaQueryWrapper<Team> teamWrapper = new LambdaQueryWrapper<>();
        teamWrapper.eq(Team::getCompetitionId, team.getCompetitionId())
                   .eq(Team::getDeleted, 0);
        List<Team> existingTeams = this.list(teamWrapper);
        
        for (Team t : existingTeams) {
            LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(TeamMember::getTeamId, t.getId())
                        .eq(TeamMember::getUserId, userId)
                        .eq(TeamMember::getStatus, 1);
            if (teamMemberMapper.selectCount(memberWrapper) > 0) {
                return Result.error("您已在该竞赛中创建或加入团队");
            }
        }
        
        team.setLeaderId(userId);
        team.setCurrentMembers(1);
        team.setStatus(1); // 团队创建后自动通过
        this.save(team);
        
        // 添加队长为团队成员
        TeamMember leader = new TeamMember();
        leader.setTeamId(team.getId());
        leader.setUserId(userId);
        leader.setStatus(1); // 已通过
        teamMemberMapper.insert(leader);
        
        return Result.success("团队创建成功", team);
    }

    @Override
    @Transactional
    public Result joinTeam(Integer teamId, Integer userId) {
        Team team = this.getById(teamId);
        if (team == null || team.getDeleted() == 1) {
            return Result.error("团队不存在");
        }
        
        // 检查是否已在该竞赛中创建或加入团队
        LambdaQueryWrapper<Team> teamWrapper = new LambdaQueryWrapper<>();
        teamWrapper.eq(Team::getCompetitionId, team.getCompetitionId())
                   .eq(Team::getDeleted, 0);
        List<Team> existingTeams = this.list(teamWrapper);
        
        for (Team t : existingTeams) {
            LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(TeamMember::getTeamId, t.getId())
                        .eq(TeamMember::getUserId, userId)
                        .in(TeamMember::getStatus, 0, 1); // 待审核或已通过
            if (teamMemberMapper.selectCount(memberWrapper) > 0) {
                return Result.error("您已在该竞赛中创建或加入团队");
            }
        }
        
        // 检查团队是否已满
        if (team.getCurrentMembers() >= team.getMaxMembers()) {
            return Result.error("团队已满，无法加入");
        }
        
        // 检查是否已申请
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
               .eq(TeamMember::getUserId, userId);
        TeamMember existing = teamMemberMapper.selectOne(wrapper);
        if (existing != null) {
            if (existing.getStatus() == 1) {
                return Result.error("您已是该团队成员");
            } else if (existing.getStatus() == 0) {
                return Result.error("您已申请加入该团队，等待队长审核");
            }
        }
        
        // 创建加入申请
        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setStatus(0); // 待审核
        teamMemberMapper.insert(member);
        
        return Result.success("申请已提交，等待队长审核");
    }

    @Override
    public Result getTeamMembers(Integer teamId) {
        Team team = this.getById(teamId);
        if (team == null || team.getDeleted() == 1) {
            return Result.error("团队不存在");
        }
        
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
               .eq(TeamMember::getStatus, 1); // 只查询已通过的成员
        List<TeamMember> members = teamMemberMapper.selectList(wrapper);
        
        List<Integer> userIds = members.stream()
                .map(TeamMember::getUserId)
                .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return Result.success(List.of());
        }
        
        List<User> users = userMapper.selectBatchIds(userIds);
        List<Map<String, Object>> memberList = users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("realName", user.getRealName());
            map.put("studentNo", user.getStudentNo());
            map.put("college", user.getCollege());
            map.put("major", user.getMajor());
            map.put("isLeader", user.getId().equals(team.getLeaderId()));
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(memberList);
    }

    @Override
    public Result getPendingMembers(Integer teamId, Integer leaderId) {
        Team team = this.getById(teamId);
        if (team == null || team.getDeleted() == 1) {
            return Result.error("团队不存在");
        }
        if (!team.getLeaderId().equals(leaderId)) {
            return Result.error("只有队长可以查看待审核成员");
        }
        
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
               .eq(TeamMember::getStatus, 0); // 待审核
        List<TeamMember> pendingMembers = teamMemberMapper.selectList(wrapper);
        
        if (pendingMembers.isEmpty()) {
            return Result.success(List.of());
        }
        
        List<Integer> userIds = pendingMembers.stream()
                .map(TeamMember::getUserId)
                .collect(Collectors.toList());
        
        List<User> users = userMapper.selectBatchIds(userIds);
        List<Map<String, Object>> memberList = users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("realName", user.getRealName());
            map.put("studentNo", user.getStudentNo());
            map.put("college", user.getCollege());
            map.put("major", user.getMajor());
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(memberList);
    }

    @Override
    @Transactional
    public Result inviteMember(Integer teamId, String studentNo, Integer leaderId) {
        Team team = this.getById(teamId);
        if (team == null || team.getDeleted() == 1) {
            return Result.error("团队不存在");
        }
        if (!team.getLeaderId().equals(leaderId)) {
            return Result.error("只有队长可以邀请成员");
        }
        
        // 检查团队是否已满
        if (team.getCurrentMembers() >= team.getMaxMembers()) {
            return Result.error("团队已满，无法邀请");
        }
        
        // 根据学号或用户名查找用户（支持两种方式）
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.and(wrapper -> wrapper
                .eq(User::getStudentNo, studentNo)
                .or()
                .eq(User::getUsername, studentNo))
                   .eq(User::getRole, "student");
        User user = userMapper.selectOne(userWrapper);
        if (user == null) {
            return Result.error("未找到该学号或用户名的学生");
        }
        
        // 检查用户是否已在该竞赛中创建或加入团队
        LambdaQueryWrapper<Team> teamWrapper = new LambdaQueryWrapper<>();
        teamWrapper.eq(Team::getCompetitionId, team.getCompetitionId())
                   .eq(Team::getDeleted, 0);
        List<Team> existingTeams = this.list(teamWrapper);
        
        for (Team t : existingTeams) {
            LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(TeamMember::getTeamId, t.getId())
                        .eq(TeamMember::getUserId, user.getId())
                        .in(TeamMember::getStatus, 0, 1);
            if (teamMemberMapper.selectCount(memberWrapper) > 0) {
                return Result.error("该学生已在该竞赛中创建或加入团队");
            }
        }
        
        // 检查是否已申请或已邀请
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
               .eq(TeamMember::getUserId, user.getId());
        TeamMember existing = teamMemberMapper.selectOne(wrapper);
        if (existing != null) {
            if (existing.getStatus() == 1) {
                return Result.error("该学生已是团队成员");
            } else if (existing.getStatus() == 0) {
                return Result.error("该学生已申请加入，请审核");
            }
        }
        
        // 创建邀请（直接通过）
        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setUserId(user.getId());
        member.setStatus(1); // 邀请直接通过
        teamMemberMapper.insert(member);
        
        // 更新团队成员数
        team.setCurrentMembers(team.getCurrentMembers() + 1);
        this.updateById(team);
        
        return Result.success("邀请成功，该学生已加入团队");
    }

    @Override
    public Result getMyTeams(Integer userId, Integer competitionId) {
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getUserId, userId)
                     .eq(TeamMember::getStatus, 1);
        List<TeamMember> memberships = teamMemberMapper.selectList(memberWrapper);
        
        if (memberships.isEmpty()) {
            return Result.success(List.of());
        }
        
        List<Integer> teamIds = memberships.stream()
                .map(TeamMember::getTeamId)
                .collect(Collectors.toList());
        
        LambdaQueryWrapper<Team> teamWrapper = new LambdaQueryWrapper<>();
        teamWrapper.in(Team::getId, teamIds)
                   .eq(Team::getDeleted, 0);
        if (competitionId != null) {
            teamWrapper.eq(Team::getCompetitionId, competitionId);
        }
        List<Team> teams = this.list(teamWrapper);
        
        // 转换为Map列表，添加isLeader字段
        final Integer finalUserId = userId;
        List<Map<String, Object>> teamList = teams.stream().map(team -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", team.getId());
            map.put("name", team.getName());
            map.put("competitionId", team.getCompetitionId());
            map.put("leaderId", team.getLeaderId());
            map.put("maxMembers", team.getMaxMembers());
            map.put("currentMembers", team.getCurrentMembers());
            map.put("status", team.getStatus());
            map.put("createTime", team.getCreateTime());
            map.put("updateTime", team.getUpdateTime());
            // 判断是否是队长
            map.put("isLeader", team.getLeaderId() != null && team.getLeaderId().equals(finalUserId));
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(teamList);
    }

    @Override
    public Result searchTeams(Integer competitionId, String keyword) {
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Team::getCompetitionId, competitionId)
               .eq(Team::getDeleted, 0)
               .eq(Team::getStatus, 1); // 只查询已通过的团队
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Team::getName, keyword);
        }
        List<Team> teams = this.list(wrapper);
        
        // 查询每个团队的成员数
        for (Team team : teams) {
            LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(TeamMember::getTeamId, team.getId())
                        .eq(TeamMember::getStatus, 1);
            team.setCurrentMembers(teamMemberMapper.selectCount(memberWrapper).intValue());
        }
        
        return Result.success(teams);
    }

    @Override
    @Transactional
    public Result approveTeamMember(Integer teamId, Integer memberId, Integer status, Integer leaderId) {
        Team team = this.getById(teamId);
        if (team == null || team.getDeleted() == 1) {
            return Result.error("团队不存在");
        }
        if (!team.getLeaderId().equals(leaderId)) {
            return Result.error("只有队长可以审核成员");
        }
        
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
               .eq(TeamMember::getUserId, memberId);
        TeamMember member = teamMemberMapper.selectOne(wrapper);
        if (member == null) {
            return Result.error("成员申请不存在");
        }
        
        if (status == 1) { // 通过
            // 检查团队是否已满
            if (team.getCurrentMembers() >= team.getMaxMembers()) {
                return Result.error("团队已满，无法通过");
            }
            member.setStatus(1);
            teamMemberMapper.updateById(member);
            
            // 更新团队成员数
            team.setCurrentMembers(team.getCurrentMembers() + 1);
            this.updateById(team);
        } else if (status == 2) { // 拒绝
            member.setStatus(2);
            teamMemberMapper.updateById(member);
        }
        
        return Result.success(status == 1 ? "已通过申请" : "已拒绝申请");
    }
}

