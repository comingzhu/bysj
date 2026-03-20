package com.competition.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.competition.common.Result;
import com.competition.entity.Score;

public interface ScoreService extends IService<Score> {
    Result getScoreTasks(Integer judgeId, Integer page, Integer size);
    Result getScoreTaskDetail(Integer taskId, Integer judgeId);
    Result submitScore(Score score, Integer judgeId);
    Result updateScore(Score score, Integer judgeId);
    Result getScoreDetail(Integer taskId, Integer judgeId);
    Result getScoreStatistics(Integer judgeId);
    Result getScoreRecords(Integer judgeId, Integer page, Integer size);
}




