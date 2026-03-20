package com.competition.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.competition.common.Result;
import com.competition.entity.Competition;
import com.competition.entity.Work;
import com.competition.mapper.CompetitionMapper;
import com.competition.service.WorkService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/work")
public class WorkController {
    @Autowired
    private WorkService workService;
    @Autowired
    private CompetitionMapper competitionMapper;

    @GetMapping("/list")
    public Result getWorkList(@RequestParam(required = false) Integer competitionId,
                              @RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size,
                              HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Integer userId = (Integer) request.getAttribute("userId");
        // 如果是老师，只返回该老师发布的校赛的作品
        if ("teacher".equals(role)) {
            if (competitionId != null) {
                // 验证竞赛是否是该老师发布的
                Competition competition = competitionMapper.selectById(competitionId);
                if (competition == null || competition.getPublisherId() == null || !competition.getPublisherId().equals(userId)) {
                    return Result.error(403, "无权限查看该竞赛的作品");
                }
            }
            // 传递userId给service，用于过滤只显示该老师发布的校赛的作品
            return workService.getWorkList(competitionId, page, size, userId);
        }
        // 管理员可以看到所有作品
        return workService.getWorkList(competitionId, page, size, null);
    }

    @GetMapping("/{id}")
    public Result getWorkDetail(@PathVariable Integer id, HttpServletRequest request) {
        return workService.getWorkDetail(id);
    }

    @PutMapping("/status/{id}")
    public Result updateWorkStatus(@PathVariable Integer id,
                                   @RequestParam Integer status,
                                   HttpServletRequest request) {
        // 验证权限：只有老师和管理员可以标记异常
        String role = (String) request.getAttribute("role");
        if (!"teacher".equals(role) && !"admin".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        return workService.updateWorkStatus(id, status);
    }

    @PostMapping("/submit")
    public Result submitWork(@RequestBody Work work, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"student".equals(role)) {
            return Result.error(403, "只有学生可以提交作品");
        }
        return workService.submitWork(work, userId);
    }

    @GetMapping("/my")
    public Result getMyWorks(@RequestParam(required = false) Integer competitionId,
                             HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return workService.getMyWorks(userId, competitionId);
    }
}

