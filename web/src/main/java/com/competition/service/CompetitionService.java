package com.competition.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.competition.common.Result;
import com.competition.entity.Competition;

public interface CompetitionService extends IService<Competition> {
    Result createCompetition(Competition competition, Integer publisherId);
    Result updateCompetition(Competition competition, Integer userId, String role);
    Result getCompetitionList(String category, Integer status, String keyword, Integer isSystem, Integer page, Integer size);
    Result getCompetitionDetail(Integer id);
    Result approveCompetition(Integer id, Integer status, String rejectReason, String judgeIds);
    Result publishCompetition(Integer id);
    Result pauseCompetition(Integer id);
    Result resumeCompetition(Integer id);
    Result getMyCompetitions(Integer publisherId, Integer page, Integer size);
    Result deleteCompetition(Integer id, Integer userId, String role);
    Result createSystemCompetition(Competition competition);
    Result createScoreTasks(Integer competitionId);
    /**
     * 按竞赛分类获取可选评分员列表
     */
    Result getJudgesByCategory(String category);

    /**
     * 根据竞赛ID获取已分配的评委列表
     */
    Result getJudgesByCompetition(Integer competitionId);
}

