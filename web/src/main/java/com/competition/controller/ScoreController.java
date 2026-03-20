package com.competition.controller;

import com.competition.common.Result;
import com.competition.entity.Score;
import com.competition.service.ScoreService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/score")
public class ScoreController {
    @Autowired
    private ScoreService scoreService;

    @GetMapping("/tasks")
    public Result getScoreTasks(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size,
                               HttpServletRequest request) {
        Integer judgeId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"judge".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        return scoreService.getScoreTasks(judgeId, page, size);
    }

    @GetMapping("/task/{id}")
    public Result getScoreTaskDetail(@PathVariable Integer id, HttpServletRequest request) {
        Integer judgeId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"judge".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        return scoreService.getScoreTaskDetail(id, judgeId);
    }

    @PostMapping("/submit")
    public Result submitScore(@RequestBody Score score, HttpServletRequest request) {
        Integer judgeId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"judge".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        return scoreService.submitScore(score, judgeId);
    }

    @PutMapping("/update")
    public Result updateScore(@RequestBody Score score, HttpServletRequest request) {
        Integer judgeId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"judge".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        return scoreService.updateScore(score, judgeId);
    }

    @GetMapping("/detail/{taskId}")
    public Result getScoreDetail(@PathVariable Integer taskId, HttpServletRequest request) {
        Integer judgeId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"judge".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        return scoreService.getScoreDetail(taskId, judgeId);
    }

    @GetMapping("/statistics")
    public Result getScoreStatistics(HttpServletRequest request) {
        Integer judgeId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"judge".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        return scoreService.getScoreStatistics(judgeId);
    }

    @GetMapping("/records")
    public Result getScoreRecords(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size,
                                  HttpServletRequest request) {
        Integer judgeId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"judge".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        return scoreService.getScoreRecords(judgeId, page, size);
    }
}




