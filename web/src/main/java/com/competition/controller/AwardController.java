package com.competition.controller;

import com.competition.common.Constants;
import com.competition.common.Result;
import com.competition.entity.Award;
import com.competition.entity.Competition;
import com.competition.mapper.CompetitionMapper;
import com.competition.service.AwardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/award")
public class AwardController {
    @Autowired
    private AwardService awardService;
    @Autowired
    private CompetitionMapper competitionMapper;

    @GetMapping("/list")
    public Result getAwardList(@RequestParam(required = false) String competitionIds) {
        List<Integer> ids = null;
        if (competitionIds != null && !competitionIds.trim().isEmpty()) {
            ids = Arrays.stream(competitionIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(java.util.stream.Collectors.toList());
        }
        return awardService.getAwardList(ids);
    }

    @GetMapping("/my")
    public Result getMyAwards(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return awardService.getMyAwards(userId);
    }

    /**
     * 自动生成获奖记录
     * 教师：只能对自己发布的校赛生成；管理员：可以对所有竞赛生成
     */
    @PostMapping("/generate/{competitionId}")
    public Result generateAwards(@PathVariable Integer competitionId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        Result authResult = checkCompetitionPermission(competitionId, userId, role);
        if (authResult != null) {
            return authResult;
        }
        return awardService.generateAwards(competitionId);
    }

    /**
     * 按竞赛查询获奖记录
     * 教师：只能查看自己发布的校赛；管理员：可以查看所有竞赛
     */
    @GetMapping("/competition/{competitionId}")
    public Result getAwardListByCompetition(@PathVariable Integer competitionId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        Result authResult = checkCompetitionPermission(competitionId, userId, role);
        if (authResult != null) {
            return authResult;
        }
        return awardService.getAwardListByCompetition(competitionId);
    }

    /**
     * 新增获奖记录
     * 教师：只能给自己发布的校赛发奖；管理员：可以给所有竞赛发奖
     */
    @PostMapping("/create")
    public Result createAward(@RequestBody Award award, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (award.getCompetitionId() == null) {
            return Result.error("竞赛ID不能为空");
        }
        Result authResult = checkCompetitionPermission(award.getCompetitionId(), userId, role);
        if (authResult != null) {
            return authResult;
        }
        return awardService.createAward(award);
    }

    /**
     * 修改获奖记录
     * 教师：只能修改自己竞赛下的获奖记录；管理员：可以修改所有
     */
    @PutMapping("/update")
    public Result updateAward(@RequestBody Award award, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (award.getId() == null) {
            return Result.error("获奖记录ID不能为空");
        }
        Award existing = awardService.getById(award.getId());
        if (existing == null) {
            return Result.error("获奖记录不存在");
        }
        Result authResult = checkCompetitionPermission(existing.getCompetitionId(), userId, role);
        if (authResult != null) {
            return authResult;
        }
        return awardService.updateAward(award);
    }

    /**
     * 删除获奖记录
     * 教师：只能删除自己竞赛下的获奖记录；管理员：可以删除所有
     */
    @DeleteMapping("/{id}")
    public Result deleteAward(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        Award award = awardService.getById(id);
        if (award == null) {
            return Result.error("获奖记录不存在");
        }
        Result authResult = checkCompetitionPermission(award.getCompetitionId(), userId, role);
        if (authResult != null) {
            return authResult;
        }
        return awardService.deleteAward(id);
    }

    /**
     * 权限校验：
     * - 管理员：可以操作所有竞赛
     * - 教师：只能操作自己发布的校赛（isSystem = 0 && publisherId = 自己）
     * - 其他角色：无权操作
     */
    private Result checkCompetitionPermission(Integer competitionId, Integer userId, String role) {
        if (competitionId == null) {
            return Result.error("竞赛ID不能为空");
        }
        Competition competition = competitionMapper.selectById(competitionId);
        if (competition == null || competition.getDeleted() == 1) {
            return Result.error("竞赛不存在");
        }

        // 管理员可以操作所有竞赛
        if (Constants.ROLE_ADMIN.equals(role)) {
            return null;
        }

        // 教师只能操作自己发布的校赛
        if (Constants.ROLE_TEACHER.equals(role)) {
            if (competition.getIsSystem() != null && competition.getIsSystem() == 1) {
                return Result.error("教师只能对自己发布的校赛进行获奖管理");
            }
            if (!competition.getPublisherId().equals(userId)) {
                return Result.error("无权限操作该竞赛的获奖信息");
            }
            return null;
        }

        // 其他角色无权限
        return Result.error("无权限进行获奖管理操作");
    }
}

