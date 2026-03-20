package com.competition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.common.Result;
import com.competition.entity.Award;
import com.competition.entity.Competition;
import com.competition.entity.Team;
import com.competition.entity.User;
import com.competition.mapper.AwardMapper;
import com.competition.mapper.CompetitionMapper;
import com.competition.mapper.TeamMapper;
import com.competition.mapper.UserMapper;
import com.competition.mapper.ScoreMapper;
import com.competition.mapper.WorkMapper;
import com.competition.mapper.RegistrationMapper;
import com.competition.mapper.TeamMemberMapper;
import com.competition.mapper.MessageMapper;
import com.competition.entity.Score;
import com.competition.entity.Work;
import com.competition.entity.Registration;
import com.competition.entity.TeamMember;
import com.competition.entity.Message;
import com.competition.entity.SystemConfig;
import com.competition.service.AwardService;
import com.competition.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AwardServiceImpl extends ServiceImpl<AwardMapper, Award> implements AwardService {
    @Autowired
    private CompetitionMapper competitionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private ScoreMapper scoreMapper;
    @Autowired
    private WorkMapper workMapper;
    @Autowired
    private RegistrationMapper registrationMapper;
    @Autowired
    private TeamMemberMapper teamMemberMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public Result getAwardList(List<Integer> competitionIds) {
        LambdaQueryWrapper<Award> wrapper = new LambdaQueryWrapper<>();
        if (competitionIds != null && !competitionIds.isEmpty()) {
            wrapper.in(Award::getCompetitionId, competitionIds);
        }
        List<Award> awards = this.list(wrapper);
        
        if (awards == null || awards.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        
        // 关联查询竞赛名称、获奖者信息
        List<Integer> compIds = awards.stream()
                .map(Award::getCompetitionId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Integer> userIds = awards.stream()
                .filter(a -> a.getUserId() != null)
                .map(Award::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Integer> teamIds = awards.stream()
                .filter(a -> a.getTeamId() != null)
                .map(Award::getTeamId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Integer, String> competitionMap = new HashMap<>();
        if (!compIds.isEmpty()) {
            List<Competition> competitions = competitionMapper.selectBatchIds(compIds);
            competitionMap = competitions.stream()
                    .collect(Collectors.toMap(Competition::getId, Competition::getName, (v1, v2) -> v1));
        }
        
        Map<Integer, String> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, 
                            u -> u.getRealName() != null ? u.getRealName() : u.getUsername(), 
                            (v1, v2) -> v1));
        }
        
        Map<Integer, Team> teamMap = new HashMap<>();
        Map<Integer, String> teamLeaderMap = new HashMap<>();
        if (!teamIds.isEmpty()) {
            List<Team> teams = teamMapper.selectBatchIds(teamIds);
            teamMap = teams.stream()
                    .collect(Collectors.toMap(Team::getId, t -> t, (v1, v2) -> v1));
            
            List<Integer> leaderIds = teams.stream()
                    .filter(t -> t.getLeaderId() != null)
                    .map(Team::getLeaderId)
                    .distinct()
                    .collect(Collectors.toList());
            
            if (!leaderIds.isEmpty()) {
                List<User> leaders = userMapper.selectBatchIds(leaderIds);
                teamLeaderMap = leaders.stream()
                        .collect(Collectors.toMap(User::getId, 
                                u -> u.getRealName() != null ? u.getRealName() : u.getUsername(), 
                                (v1, v2) -> v1));
            }
        }
        
        final Map<Integer, String> finalCompetitionMap = competitionMap;
        final Map<Integer, String> finalUserMap = userMap;
        final Map<Integer, Team> finalTeamMap = teamMap;
        final Map<Integer, String> finalTeamLeaderMap = teamLeaderMap;
        
        List<Map<String, Object>> resultList = awards.stream().map(award -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", award.getId());
            map.put("competitionId", award.getCompetitionId());
            map.put("registrationId", award.getRegistrationId());
            map.put("competitionName", finalCompetitionMap.getOrDefault(award.getCompetitionId(), "未知竞赛"));
            map.put("awardLevel", award.getAwardLevel());
            map.put("rank", award.getRank());
            map.put("score", award.getScore());
            map.put("certificateNumber", award.getCertificateNumber());
            map.put("userId", award.getUserId());
            map.put("teamId", award.getTeamId());
            
            // 获奖者信息
            String submitterName;
            if (award.getTeamId() != null) {
                Team team = finalTeamMap.get(award.getTeamId());
                if (team != null) {
                    String leaderName = team.getLeaderId() != null ? 
                            finalTeamLeaderMap.getOrDefault(team.getLeaderId(), "未知队长") : "未知队长";
                    submitterName = team.getName() + "（队长：" + leaderName + "）";
                } else {
                    submitterName = "未知团队";
                }
            } else if (award.getUserId() != null) {
                submitterName = finalUserMap.getOrDefault(award.getUserId(), "未知用户");
            } else {
                submitterName = "未知用户";
            }
            map.put("submitterName", submitterName);
            
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(resultList);
    }

    @Override
    public Result getMyAwards(Integer userId) {
        // 查询该用户的个人获奖记录
        LambdaQueryWrapper<Award> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(Award::getUserId, userId);
        List<Award> userAwards = this.list(userWrapper);
        
        // 查询该用户所在的团队ID
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getUserId, userId)
                    .eq(TeamMember::getStatus, 1); // 已通过的成员
        List<TeamMember> memberships = teamMemberMapper.selectList(memberWrapper);
        List<Integer> userTeamIds = memberships.stream()
                .map(TeamMember::getTeamId)
                .collect(Collectors.toList());
        
        // 查询该用户所在团队的获奖记录
        List<Award> teamAwards = new ArrayList<>();
        if (!userTeamIds.isEmpty()) {
            LambdaQueryWrapper<Award> teamWrapper = new LambdaQueryWrapper<>();
            teamWrapper.in(Award::getTeamId, userTeamIds);
            teamAwards = this.list(teamWrapper);
        }
        
        // 合并结果
        List<Award> allAwards = new ArrayList<>(userAwards);
        allAwards.addAll(teamAwards);
        
        // 关联查询竞赛名称
        if (!allAwards.isEmpty()) {
            List<Integer> compIds = allAwards.stream()
                    .map(Award::getCompetitionId)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<Integer, String> competitionMap = new HashMap<>();
            if (!compIds.isEmpty()) {
                List<Competition> competitions = competitionMapper.selectBatchIds(compIds);
                competitionMap = competitions.stream()
                        .collect(Collectors.toMap(Competition::getId, Competition::getName, (v1, v2) -> v1));
            }
            
            final Map<Integer, String> finalCompetitionMap = competitionMap;
            List<Map<String, Object>> resultList = allAwards.stream().map(award -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", award.getId());
                map.put("competitionId", award.getCompetitionId());
                map.put("competitionName", finalCompetitionMap.getOrDefault(award.getCompetitionId(), "未知竞赛"));
                map.put("awardLevel", award.getAwardLevel());
                map.put("rank", award.getRank());
                map.put("score", award.getScore());
                map.put("certificateNumber", award.getCertificateNumber());
                map.put("createTime", award.getCreateTime());
                return map;
            }).collect(Collectors.toList());
            
            return Result.success(resultList);
        }
        
        return Result.success(new ArrayList<>());
    }

    @Override
    @Transactional
    public Result generateAwards(Integer competitionId) {
        Competition competition = competitionMapper.selectById(competitionId);
        if (competition == null || competition.getDeleted() == 1) {
            return Result.error("竞赛不存在");
        }
        
        // 检查竞赛是否已结束
        LocalDateTime now = LocalDateTime.now();
        if (competition.getEndTime() != null && now.isBefore(competition.getEndTime())) {
            return Result.error("竞赛尚未结束，无法生成获奖记录");
        }
        
        List<Award> createdAwards = new ArrayList<>(); // 保存创建的获奖记录，用于发送通知
        int createdCount = 0;
        
        if (competition.getNeedWork() != null && competition.getNeedWork() == 1) {
            // 处理需要作品的竞赛
            // 查询该竞赛的所有已提交作品
            LambdaQueryWrapper<Work> workWrapper = new LambdaQueryWrapper<>();
            workWrapper.eq(Work::getCompetitionId, competitionId)
                      .eq(Work::getDeleted, 0);
            List<Work> works = workMapper.selectList(workWrapper);
            
            if (works.isEmpty()) {
                return Result.error("该竞赛没有已提交的作品");
            }
            
            // 查询每个作品的评分，计算总分（所有评委评分求和）
            List<Map<String, Object>> workScores = new ArrayList<>();
            for (Work work : works) {
                LambdaQueryWrapper<Score> scoreWrapper = new LambdaQueryWrapper<>();
                scoreWrapper.eq(Score::getWorkId, work.getId());
                List<Score> scores = scoreMapper.selectList(scoreWrapper);
                
                if (scores.isEmpty()) {
                    continue; // 没有评分的作品不参与评奖
                }
                
                // 计算总分（所有评委评分之和）
                BigDecimal totalScore = BigDecimal.ZERO;
                for (Score score : scores) {
                    if (score.getTotalScore() != null) {
                        totalScore = totalScore.add(score.getTotalScore());
                    }
                }
                
                // 查询报名记录
                Registration registration = registrationMapper.selectById(work.getRegistrationId());
                if (registration == null || registration.getDeleted() == 1) {
                    continue;
                }
                
                Map<String, Object> workScore = new HashMap<>();
                workScore.put("workId", work.getId());
                workScore.put("registrationId", work.getRegistrationId());
                workScore.put("userId", work.getUserId());
                workScore.put("teamId", work.getTeamId());
                workScore.put("totalScore", totalScore.setScale(2, RoundingMode.HALF_UP));
                workScores.add(workScore);
            }
            
            if (workScores.isEmpty()) {
                return Result.error("没有已评分的作品，无法生成获奖记录");
            }
            
            // 按总分降序排序
            workScores.sort((a, b) -> {
                BigDecimal scoreA = (BigDecimal) a.get("totalScore");
                BigDecimal scoreB = (BigDecimal) b.get("totalScore");
                return scoreB.compareTo(scoreA);
            });
            
            // 删除该竞赛的旧获奖记录
            LambdaQueryWrapper<Award> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(Award::getCompetitionId, competitionId);
            this.remove(deleteWrapper);
            
            // 根据竞赛配置的获奖模式分配奖项
            int totalCount = workScores.size();
            
            Integer awardMode = competition.getAwardMode() == null ? 0 : competition.getAwardMode();
            int rank = 1;

            if (awardMode == 0) {
                // 模式0：固定名额，前3名分别获得一等、二等、三等奖，其他选手获得优秀奖
                for (int i = 0; i < workScores.size(); i++) {
                    Map<String, Object> workScore = workScores.get(i);
                    String awardLevel;
                    if (i == 0) {
                        awardLevel = "一等奖";
                    } else if (i == 1) {
                        awardLevel = "二等奖";
                    } else if (i == 2) {
                        awardLevel = "三等奖";
                    } else {
                        awardLevel = "优秀奖";
                    }

                    Award award = new Award();
                    award.setCompetitionId(competitionId);
                    award.setRegistrationId((Integer) workScore.get("registrationId"));
                    award.setUserId((Integer) workScore.get("userId"));
                    award.setTeamId((Integer) workScore.get("teamId"));
                    award.setAwardLevel(awardLevel);
                    award.setRank(rank);
                    award.setScore((BigDecimal) workScore.get("totalScore"));
                    award.setCreateTime(LocalDateTime.now());
                    // 生成证书编号：时间+竞赛编号+排名+随机两位数
                    String certificateNumber = generateCertificateNumber(competitionId, rank);
                    award.setCertificateNumber(certificateNumber);

                    this.save(award);
                    createdAwards.add(award);
                    createdCount++;
                    rank++;
                }
            } else {
                // 模式1：按比例评奖，比例从竞赛配置中读取，若为空则使用默认 0.10 / 0.20 / 0.30
                BigDecimal firstRatio = competition.getFirstAwardRatio() != null ? competition.getFirstAwardRatio() : BigDecimal.valueOf(0.10);
                BigDecimal secondRatio = competition.getSecondAwardRatio() != null ? competition.getSecondAwardRatio() : BigDecimal.valueOf(0.20);
                BigDecimal thirdRatio = competition.getThirdAwardRatio() != null ? competition.getThirdAwardRatio() : BigDecimal.valueOf(0.30);

                int firstPrizeCount = firstRatio.compareTo(BigDecimal.ZERO) > 0
                        ? Math.max(1, firstRatio.multiply(BigDecimal.valueOf(totalCount)).setScale(0, RoundingMode.CEILING).intValue())
                        : 0;
                int secondPrizeCount = secondRatio.compareTo(BigDecimal.ZERO) > 0
                        ? Math.max(1, secondRatio.multiply(BigDecimal.valueOf(totalCount)).setScale(0, RoundingMode.CEILING).intValue())
                        : 0;
                int thirdPrizeCount = thirdRatio.compareTo(BigDecimal.ZERO) > 0
                        ? Math.max(1, thirdRatio.multiply(BigDecimal.valueOf(totalCount)).setScale(0, RoundingMode.CEILING).intValue())
                        : 0;

                for (int i = 0; i < workScores.size(); i++) {
                    Map<String, Object> workScore = workScores.get(i);
                    String awardLevel;

                    if (rank <= firstPrizeCount) {
                        awardLevel = "一等奖";
                    } else if (rank <= firstPrizeCount + secondPrizeCount) {
                        awardLevel = "二等奖";
                    } else if (rank <= firstPrizeCount + secondPrizeCount + thirdPrizeCount) {
                        awardLevel = "三等奖";
                    } else {
                        awardLevel = "优秀奖";
                    }

                    Award award = new Award();
                    award.setCompetitionId(competitionId);
                    award.setRegistrationId((Integer) workScore.get("registrationId"));
                    award.setUserId((Integer) workScore.get("userId"));
                    award.setTeamId((Integer) workScore.get("teamId"));
                    award.setAwardLevel(awardLevel);
                    award.setRank(rank);
                    award.setScore((BigDecimal) workScore.get("totalScore"));
                    award.setCreateTime(LocalDateTime.now());
                    // 生成证书编号：时间+竞赛编号+排名+随机两位数
                    String certificateNumber = generateCertificateNumber(competitionId, rank);
                    award.setCertificateNumber(certificateNumber);

                    this.save(award);
                    createdAwards.add(award);
                    createdCount++;
                    rank++;
                }
            }
        } else {
            // 处理不需要作品的竞赛，使用已录入的成绩
            LambdaQueryWrapper<Award> awardWrapper = new LambdaQueryWrapper<>();
            awardWrapper.eq(Award::getCompetitionId, competitionId);
            List<Award> existingAwards = this.list(awardWrapper);
            
            if (existingAwards.isEmpty()) {
                return Result.error("该竞赛没有已录入的成绩，无法生成获奖记录");
            }
            
            // 按成绩降序排序
            existingAwards.sort((a, b) -> {
                BigDecimal scoreA = a.getScore() != null ? a.getScore() : BigDecimal.ZERO;
                BigDecimal scoreB = b.getScore() != null ? b.getScore() : BigDecimal.ZERO;
                return scoreB.compareTo(scoreA);
            });
            
            // 根据竞赛配置的获奖模式分配奖项
            int totalCount = existingAwards.size();
            
            Integer awardMode = competition.getAwardMode() == null ? 0 : competition.getAwardMode();
            int rank = 1;

            if (awardMode == 0) {
                // 模式0：固定名额，前3名分别获得一等、二等、三等奖，其他选手获得优秀奖
                for (int i = 0; i < existingAwards.size(); i++) {
                    Award award = existingAwards.get(i);
                    String awardLevel;
                    if (i == 0) {
                        awardLevel = "一等奖";
                    } else if (i == 1) {
                        awardLevel = "二等奖";
                    } else if (i == 2) {
                        awardLevel = "三等奖";
                    } else {
                        awardLevel = "优秀奖";
                    }

                    award.setAwardLevel(awardLevel);
                    award.setRank(rank);
                    // 生成证书编号：时间+竞赛编号+排名+随机两位数
                    String certificateNumber = generateCertificateNumber(competitionId, rank);
                    award.setCertificateNumber(certificateNumber);
                    this.updateById(award);
                    createdAwards.add(award);
                    createdCount++;
                    rank++;
                }
            } else {
                // 模式1：按比例评奖，比例从竞赛配置中读取，若为空则使用默认 0.10 / 0.20 / 0.30
                BigDecimal firstRatio = competition.getFirstAwardRatio() != null ? competition.getFirstAwardRatio() : BigDecimal.valueOf(0.10);
                BigDecimal secondRatio = competition.getSecondAwardRatio() != null ? competition.getSecondAwardRatio() : BigDecimal.valueOf(0.20);
                BigDecimal thirdRatio = competition.getThirdAwardRatio() != null ? competition.getThirdAwardRatio() : BigDecimal.valueOf(0.30);

                int firstPrizeCount = firstRatio.compareTo(BigDecimal.ZERO) > 0
                        ? Math.max(1, firstRatio.multiply(BigDecimal.valueOf(totalCount)).setScale(0, RoundingMode.CEILING).intValue())
                        : 0;
                int secondPrizeCount = secondRatio.compareTo(BigDecimal.ZERO) > 0
                        ? Math.max(1, secondRatio.multiply(BigDecimal.valueOf(totalCount)).setScale(0, RoundingMode.CEILING).intValue())
                        : 0;
                int thirdPrizeCount = thirdRatio.compareTo(BigDecimal.ZERO) > 0
                        ? Math.max(1, thirdRatio.multiply(BigDecimal.valueOf(totalCount)).setScale(0, RoundingMode.CEILING).intValue())
                        : 0;

                for (int i = 0; i < existingAwards.size(); i++) {
                    Award award = existingAwards.get(i);
                    String awardLevel;

                    if (rank <= firstPrizeCount) {
                        awardLevel = "一等奖";
                    } else if (rank <= firstPrizeCount + secondPrizeCount) {
                        awardLevel = "二等奖";
                    } else if (rank <= firstPrizeCount + secondPrizeCount + thirdPrizeCount) {
                        awardLevel = "三等奖";
                    } else {
                        awardLevel = "优秀奖";
                    }

                    award.setAwardLevel(awardLevel);
                    award.setRank(rank);
                    // 生成证书编号：时间+竞赛编号+排名+随机两位数
                    String certificateNumber = generateCertificateNumber(competitionId, rank);
                    award.setCertificateNumber(certificateNumber);
                    this.updateById(award);
                    createdAwards.add(award);
                    createdCount++;
                    rank++;
                }
            }
        }
        
        // 自动发送获奖通知
        try {
            sendAwardNotifications(createdAwards, competition);
        } catch (Exception e) {
            // 通知发送失败不影响获奖记录生成，只记录日志
            System.err.println("发送获奖通知失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Result.success("成功生成 " + createdCount + " 条获奖记录，并已发送获奖通知");
    }

    @Override
    public Result createAward(Award award) {
        // 基础校验：同一竞赛内，同一报名记录 / 同一个人 / 同一团队只能录入一条获奖记录，避免重复成绩
        if (award.getCompetitionId() == null) {
            return Result.error("竞赛ID不能为空");
        }

        LambdaQueryWrapper<Award> duplicateWrapper = new LambdaQueryWrapper<>();
        duplicateWrapper.eq(Award::getCompetitionId, award.getCompetitionId());

        // 优先按报名记录去重，其次按个人/团队去重
        if (award.getRegistrationId() != null) {
            duplicateWrapper.eq(Award::getRegistrationId, award.getRegistrationId());
        } else if (award.getUserId() != null) {
            duplicateWrapper.eq(Award::getUserId, award.getUserId());
        } else if (award.getTeamId() != null) {
            duplicateWrapper.eq(Award::getTeamId, award.getTeamId());
        }

        // 只有在提供了报名记录 / 用户 / 团队任一信息时才进行重复校验
        if (duplicateWrapper.getExpression() != null && this.count(duplicateWrapper) > 0) {
            return Result.error("该学生在本次竞赛中已存在获奖记录，请勿重复录入");
        }

        award.setCreateTime(LocalDateTime.now());
        this.save(award);
        return Result.success("创建成功");
    }

    @Override
    public Result updateAward(Award award) {
        Award existing = this.getById(award.getId());
        if (existing == null) {
            return Result.error("获奖记录不存在");
        }
        this.updateById(award);
        return Result.success("更新成功");
    }

    @Override
    public Result deleteAward(Integer id) {
        Award award = this.getById(id);
        if (award == null) {
            return Result.error("获奖记录不存在");
        }
        this.removeById(id);
        return Result.success("删除成功");
    }

    @Override
    public Result getAwardListByCompetition(Integer competitionId) {
        LambdaQueryWrapper<Award> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Award::getCompetitionId, competitionId)
               .orderByAsc(Award::getRank);
        List<Award> awards = this.list(wrapper);
        
        if (awards.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        
        // 关联查询竞赛名称、获奖者信息
        Competition competition = competitionMapper.selectById(competitionId);
        String competitionName = competition != null ? competition.getName() : "未知竞赛";
        
        List<Integer> userIds = awards.stream()
                .filter(a -> a.getUserId() != null)
                .map(Award::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Integer> teamIds = awards.stream()
                .filter(a -> a.getTeamId() != null)
                .map(Award::getTeamId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Integer, String> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, 
                            u -> u.getRealName() != null ? u.getRealName() : u.getUsername(), 
                            (v1, v2) -> v1));
        }
        
        Map<Integer, Team> teamMap = new HashMap<>();
        Map<Integer, String> teamLeaderMap = new HashMap<>();
        if (!teamIds.isEmpty()) {
            List<Team> teams = teamMapper.selectBatchIds(teamIds);
            teamMap = teams.stream()
                    .collect(Collectors.toMap(Team::getId, t -> t, (v1, v2) -> v1));
            
            List<Integer> leaderIds = teams.stream()
                    .filter(t -> t.getLeaderId() != null)
                    .map(Team::getLeaderId)
                    .distinct()
                    .collect(Collectors.toList());
            
            if (!leaderIds.isEmpty()) {
                List<User> leaders = userMapper.selectBatchIds(leaderIds);
                teamLeaderMap = leaders.stream()
                        .collect(Collectors.toMap(User::getId, 
                                u -> u.getRealName() != null ? u.getRealName() : u.getUsername(), 
                                (v1, v2) -> v1));
            }
        }
        
        final Map<Integer, String> finalUserMap = userMap;
        final Map<Integer, Team> finalTeamMap = teamMap;
        final Map<Integer, String> finalTeamLeaderMap = teamLeaderMap;
        
        List<Map<String, Object>> resultList = awards.stream().map(award -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", award.getId());
            map.put("competitionId", award.getCompetitionId());
            map.put("competitionName", competitionName);
            map.put("registrationId", award.getRegistrationId());
            map.put("awardLevel", award.getAwardLevel());
            map.put("rank", award.getRank());
            map.put("score", award.getScore());
            map.put("certificateNumber", award.getCertificateNumber());
            map.put("createTime", award.getCreateTime());
            
            // 获奖者信息
            String submitterName;
            if (award.getTeamId() != null) {
                Team team = finalTeamMap.get(award.getTeamId());
                if (team != null) {
                    String leaderName = team.getLeaderId() != null ? 
                            finalTeamLeaderMap.getOrDefault(team.getLeaderId(), "未知队长") : "未知队长";
                    submitterName = team.getName() + "（队长：" + leaderName + "）";
                } else {
                    submitterName = "未知团队";
                }
            } else if (award.getUserId() != null) {
                submitterName = finalUserMap.getOrDefault(award.getUserId(), "未知用户");
            } else {
                submitterName = "未知用户";
            }
            map.put("submitterName", submitterName);
            map.put("userId", award.getUserId());
            map.put("teamId", award.getTeamId());
            
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(resultList);
    }
    
    /**
     * 发送获奖通知给获奖用户
     * @param awards 获奖记录列表
     * @param competition 竞赛信息
     */
    private void sendAwardNotifications(List<Award> awards, Competition competition) {
        if (awards == null || awards.isEmpty() || competition == null) {
            return;
        }
        
        // 获取获奖通知模板
        String template = "恭喜您在{competition}竞赛中获得{level}，请及时查看获奖详情";
        try {
            Result<SystemConfig> configResult = systemConfigService.getConfigByKey("award_notice_template");
            if (configResult.getCode() != null && configResult.getCode() == 200 && configResult.getData() != null) {
                SystemConfig config = configResult.getData();
                if (config.getConfigValue() != null && !config.getConfigValue().trim().isEmpty()) {
                    template = config.getConfigValue();
                }
            }
        } catch (Exception e) {
            System.err.println("获取获奖通知模板失败，使用默认模板: " + e.getMessage());
        }
        
        String competitionName = competition.getName() != null ? competition.getName() : "竞赛";
        
        // 收集需要发送通知的用户ID（去重）
        java.util.Set<Integer> notifiedUserIds = new java.util.HashSet<>();
        
        for (Award award : awards) {
            String awardLevel = award.getAwardLevel() != null ? award.getAwardLevel() : "奖项";
            
            // 替换模板中的变量
            String content = template.replace("{competition}", competitionName)
                                     .replace("{level}", awardLevel);
            // 添加证书编号
            if (award.getCertificateNumber() != null) {
                content += "，证书编号：" + award.getCertificateNumber();
            }
            
            // 如果是团队赛，给所有团队成员发送通知
            if (award.getTeamId() != null) {
                LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
                memberWrapper.eq(TeamMember::getTeamId, award.getTeamId())
                            .eq(TeamMember::getStatus, 1); // 已通过的成员
                List<TeamMember> members = teamMemberMapper.selectList(memberWrapper);
                
                for (TeamMember member : members) {
                    if (member.getUserId() != null && !notifiedUserIds.contains(member.getUserId())) {
                        Message message = new Message();
                        message.setUserId(member.getUserId());
                        message.setTitle("获奖通知");
                        message.setContent(content);
                        message.setType("获奖通知");
                        message.setIsRead(0);
                        message.setCreateTime(LocalDateTime.now());
                        messageMapper.insert(message);
                        notifiedUserIds.add(member.getUserId());
                    }
                }
            } 
            // 如果是个人赛，给用户发送通知
            else if (award.getUserId() != null && !notifiedUserIds.contains(award.getUserId())) {
                Message message = new Message();
                message.setUserId(award.getUserId());
                message.setTitle("获奖通知");
                message.setContent(content);
                message.setType("获奖通知");
                message.setIsRead(0);
                message.setCreateTime(LocalDateTime.now());
                messageMapper.insert(message);
                notifiedUserIds.add(award.getUserId());
            }
        }
    }
    
    /**
     * 生成证书编号
     * 格式：时间+竞赛编号+排名+随机两位数
     * 例如：20260218+552+001+99 → 2026021855200199
     */
    private String generateCertificateNumber(Integer competitionId, Integer rank) {
        // 获取当前时间，格式：yyyyMMdd
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 竞赛编号
        String competitionStr = String.valueOf(competitionId);
        // 排名，不足3位补零
        String rankStr = String.format("%03d", rank);
        // 随机两位数
        String randomStr = String.format("%02d", (int) (Math.random() * 100));
        // 组合证书编号
        return timeStr + competitionStr + rankStr + randomStr;
    }
}


