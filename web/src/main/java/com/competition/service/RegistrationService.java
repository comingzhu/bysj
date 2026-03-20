package com.competition.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.competition.common.Result;
import com.competition.entity.Registration;

public interface RegistrationService extends IService<Registration> {
    Result register(Registration registration, Integer userId);
    Result approveRegistration(Integer id, Integer status, String rejectReason, Integer operatorId, String operatorRole);
    Result getRegistrationList(Integer competitionId, Integer status, Integer isSystem, Integer page, Integer size);
    Result getMyRegistrations(Integer userId, Integer page, Integer size);
    Result pay(Integer registrationId, Integer userId);
}





