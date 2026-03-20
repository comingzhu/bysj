package com.competition.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.competition.common.Result;
import com.competition.entity.Team;

public interface TeamService extends IService<Team> {
    Result createTeam(Team team, Integer userId);
    Result joinTeam(Integer teamId, Integer userId);
    Result getTeamMembers(Integer teamId);
    Result getMyTeams(Integer userId, Integer competitionId);
    Result searchTeams(Integer competitionId, String keyword);
    Result approveTeamMember(Integer teamId, Integer memberId, Integer status, Integer leaderId);
    Result getPendingMembers(Integer teamId, Integer leaderId);
    Result inviteMember(Integer teamId, String studentNo, Integer leaderId);
}

