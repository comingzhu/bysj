package com.competition.controller;

import com.competition.common.Result;
import com.competition.entity.Competition;
import com.competition.service.CompetitionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/competition")
public class CompetitionController {
    @Autowired
    private CompetitionService competitionService;

    @PostMapping("/create")
    public Result createCompetition(@RequestBody Competition competition, HttpServletRequest request) {
        Integer publisherId = (Integer) request.getAttribute("userId");
        return competitionService.createCompetition(competition, publisherId);
    }

    @PutMapping("/update")
    public Result updateCompetition(@RequestBody Competition competition, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        return competitionService.updateCompetition(competition, userId, role);
    }

    @GetMapping("/list")
    public Result getCompetitionList(@RequestParam(required = false) String category,
                                     @RequestParam(required = false) Integer status,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) Integer isSystem,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size) {
        return competitionService.getCompetitionList(category, status, keyword, isSystem, page, size);
    }

    @GetMapping("/{id}")
    public Result getCompetitionDetail(@PathVariable Integer id) {
        return competitionService.getCompetitionDetail(id);
    }

    @PutMapping("/approve/{id}")
    public Result approveCompetition(@PathVariable Integer id,
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String rejectReason,
                                     @RequestParam(required = false) String judgeIds) {
        return competitionService.approveCompetition(id, status, rejectReason, judgeIds);
    }

    @PutMapping("/publish/{id}")
    public Result publishCompetition(@PathVariable Integer id) {
        return competitionService.publishCompetition(id);
    }

    @PutMapping("/pause/{id}")
    public Result pauseCompetition(@PathVariable Integer id) {
        return competitionService.pauseCompetition(id);
    }

    @PutMapping("/resume/{id}")
    public Result resumeCompetition(@PathVariable Integer id) {
        return competitionService.resumeCompetition(id);
    }

    @GetMapping("/my")
    public Result getMyCompetitions(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    HttpServletRequest request) {
        Integer publisherId = (Integer) request.getAttribute("userId");
        return competitionService.getMyCompetitions(publisherId, page, size);
    }

    @DeleteMapping("/{id}")
    public Result deleteCompetition(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        return competitionService.deleteCompetition(id, userId, role);
    }

    @PostMapping("/system/create")
    public Result createSystemCompetition(@RequestBody Competition competition) {
        return competitionService.createSystemCompetition(competition);
    }

    @PostMapping("/{id}/create-score-tasks")
    public Result createScoreTasks(@PathVariable Integer id) {
        return competitionService.createScoreTasks(id);
    }

    /**
     * 按竞赛分类获取可选评分员列表
     */
    @GetMapping("/judges")
    public Result getJudgesByCategory(@RequestParam String category) {
        return competitionService.getJudgesByCategory(category);
    }

    /**
     * 根据竞赛ID获取已分配的评委列表（用于详情展示）
     */
    @GetMapping("/{id}/judges")
    public Result getJudgesByCompetition(@PathVariable Integer id) {
        return competitionService.getJudgesByCompetition(id);
    }
}

