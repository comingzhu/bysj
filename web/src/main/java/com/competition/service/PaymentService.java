package com.competition.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.competition.common.Result;
import com.competition.entity.Registration;

public interface PaymentService {
    Result getPaymentList(Integer status, String keyword, Integer page, Integer size);
    Result refund(Integer registrationId, String reason);
    Result sendPaymentNotice(Integer registrationId);
    Result getPaymentStatistics();
}





