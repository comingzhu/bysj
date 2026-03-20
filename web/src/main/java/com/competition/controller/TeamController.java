package com.competition.controller;

import com.competition.common.Result;
import com.competition.entity.Team;
import com.competition.service.TeamService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/team")
public class TeamController {
    @Autowired
    private TeamService teamService;

    @PostMapping("/create")
    public Result createTeam(@RequestBody Team team, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"student".equals(role)) {
            return Result.error(403, "只有学生可以创建团队");
        }
        return teamService.createTeam(team, userId);
    }

    @PostMapping("/join/{teamId}")
    public Result joinTeam(@PathVariable Integer teamId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"student".equals(role)) {
            return Result.error(403, "只有学生可以加入团队");
        }
        return teamService.joinTeam(teamId, userId);
    }

    @GetMapping("/members/{teamId}")
    public Result getTeamMembers(@PathVariable Integer teamId) {
        return teamService.getTeamMembers(teamId);
    }

    @GetMapping("/my")
    public Result getMyTeams(@RequestParam(required = false) Integer competitionId,
                             HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return teamService.getMyTeams(userId, competitionId);
    }

    @GetMapping("/search")
    public Result searchTeams(@RequestParam Integer competitionId,
                              @RequestParam(required = false) String keyword) {
        return teamService.searchTeams(competitionId, keyword);
    }

    @PutMapping("/member/approve")
    public Result approveTeamMember(@RequestParam Integer teamId,
                                    @RequestParam Integer memberId,
                                    @RequestParam Integer status,
                                    HttpServletRequest request) {
        Integer leaderId = (Integer) request.getAttribute("userId");
        return teamService.approveTeamMember(teamId, memberId, status, leaderId);
    }

    @GetMapping("/pending/{teamId}")
    public Result getPendingMembers(@PathVariable Integer teamId,
                                     HttpServletRequest request) {
        Integer leaderId = (Integer) request.getAttribute("userId");
        return teamService.getPendingMembers(teamId, leaderId);
    }

    @PostMapping("/invite")
    public Result inviteMember(@RequestParam Integer teamId,
                               @RequestParam String studentNo,
                               HttpServletRequest request) {
        Integer leaderId = (Integer) request.getAttribute("userId");
        return teamService.inviteMember(teamId, studentNo, leaderId);
    }
}

