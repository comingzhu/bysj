package com.competition.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.competition.common.Result;
import com.competition.entity.Work;

public interface WorkService extends IService<Work> {
    Result getWorkList(Integer competitionId, Integer page, Integer size, Integer publisherId);
    Result getWorkDetail(Integer workId);
    Result updateWorkStatus(Integer workId, Integer status);
    Result submitWork(Work work, Integer userId);
    Result getMyWorks(Integer userId, Integer competitionId);
}


