package com.competition.controller;

import com.competition.common.Result;
import com.competition.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/registration")
    public Result getRegistrationStatistics(@RequestParam(required = false, defaultValue = "competition") String dimension,
                                            @RequestParam(required = false) String value) {
        return statisticsService.getRegistrationStatistics(dimension, value);
    }

    @GetMapping("/payment")
    public Result getPaymentStatistics() {
        return statisticsService.getPaymentStatistics();
    }

    @GetMapping("/award")
    public Result getAwardStatistics(@RequestParam(required = false, defaultValue = "competition") String dimension) {
        return statisticsService.getAwardStatistics(dimension);
    }

    @GetMapping("/dashboard")
    public Result getDashboardStatistics() {
        return statisticsService.getDashboardStatistics();
    }
}

