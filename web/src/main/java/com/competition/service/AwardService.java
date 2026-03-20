package com.competition.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.competition.common.Result;
import com.competition.entity.Award;

import java.util.List;

public interface AwardService extends IService<Award> {
    Result getAwardList(List<Integer> competitionIds);
    Result getMyAwards(Integer userId);
    Result generateAwards(Integer competitionId);
    Result createAward(Award award);
    Result updateAward(Award award);
    Result deleteAward(Integer id);
    Result getAwardListByCompetition(Integer competitionId);
}


