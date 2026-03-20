package com.competition.service;

import com.competition.common.Result;

public interface StatisticsService {
    Result getRegistrationStatistics(String dimension, String value);
    Result getPaymentStatistics();
    Result getAwardStatistics(String dimension);
    Result getDashboardStatistics();
}

