package com.competition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.common.Result;
import com.competition.entity.*;
import com.competition.mapper.*;
import com.competition.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreService {
    @Autowired
    private ScoreTaskMapper scoreTaskMapper;
    @Autowired
    private WorkMapper workMapper;
    @Autowired
    private CompetitionMapper competitionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeamMapper teamMapper;

    @Override
    public Result getScoreTasks(Integer judgeId, Integer page, Integer size) {
        Page<ScoreTask> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ScoreTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreTask::getJudgeId, judgeId)
                .orderByDesc(ScoreTask::getCreateTime);
        Page<ScoreTask> result = scoreTaskMapper.selectPage(pageParam, wrapper);
        
        List<ScoreTask> records = result.getRecords();
        if (records != null && !records.isEmpty()) {
            // 关联查询竞赛、作品信息
            List<Integer> competitionIds = records.stream()
                    .map(ScoreTask::getCompetitionId)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<Integer> workIds = records.stream()
                    .map(ScoreTask::getWorkId)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<Integer, Competition> competitionMap = new HashMap<>();
            if (!competitionIds.isEmpty()) {
                List<Competition> competitions = competitionMapper.selectBatchIds(competitionIds);
                competitionMap = competitions.stream()
                        .collect(Collectors.toMap(Competition::getId, c -> c, (v1, v2) -> v1));
            }
            
            Map<Integer, Work> workMap = new HashMap<>();
            if (!workIds.isEmpty()) {
                List<Work> works = workMapper.selectBatchIds(workIds);
                workMap = works.stream()
                        .filter(w -> w.getDeleted() == null || w.getDeleted() == 0)
                        .collect(Collectors.toMap(Work::getId, w -> w, (v1, v2) -> v1));
            }
            
            final Map<Integer, Competition> finalCompetitionMap = competitionMap;
            final Map<Integer, Work> finalWorkMap = workMap;
            
            List<Map<String, Object>> recordList = records.stream().map(task -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", task.getId());
                map.put("competitionId", task.getCompetitionId());
                map.put("workId", task.getWorkId());
                map.put("status", task.getStatus());
                map.put("deadline", task.getDeadline());
                map.put("createTime", task.getCreateTime());
                
                Competition competition = finalCompetitionMap.get(task.getCompetitionId());
                map.put("competitionName", competition != null ? competition.getName() : "未知竞赛");
                
                Work work = finalWorkMap.get(task.getWorkId());
                if (work != null) {
                    map.put("workTitle", work.getTitle());
                    map.put("workDescription", work.getDescription());
                    map.put("filePath", work.getFilePath());
                    map.put("fileName", work.getFileName());
                    map.put("fileSize", work.getFileSize());
                } else {
                    map.put("workTitle", "作品不存在");
                    map.put("workDescription", "");
                    map.put("filePath", null);
                    map.put("fileName", null);
                    map.put("fileSize", null);
                }
                
                // 检查是否已过期
                boolean isExpired = task.getDeadline() != null && 
                                   LocalDateTime.now().isAfter(task.getDeadline());
                map.put("isExpired", isExpired);
                
                return map;
            }).collect(Collectors.toList());
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("records", recordList);
            resultMap.put("total", result.getTotal());
            resultMap.put("size", result.getSize());
            resultMap.put("current", result.getCurrent());
            resultMap.put("pages", result.getPages());
            
            return Result.success(resultMap);
        }
        
        return Result.success(result);
    }

    @Override
    public Result getScoreTaskDetail(Integer taskId, Integer judgeId) {
        ScoreTask task = scoreTaskMapper.selectById(taskId);
        if (task == null) {
            return Result.error("评分任务不存在");
        }
        if (!task.getJudgeId().equals(judgeId)) {
            return Result.error("无权限查看该任务");
        }
        
        Work work = workMapper.selectById(task.getWorkId());
        if (work == null || (work.getDeleted() != null && work.getDeleted() == 1)) {
            return Result.error("作品不存在");
        }
        
        Competition competition = competitionMapper.selectById(task.getCompetitionId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("task", task);
        result.put("work", work);
        result.put("competition", competition);
        
        // 检查是否已评分
        LambdaQueryWrapper<Score> scoreWrapper = new LambdaQueryWrapper<>();
        scoreWrapper.eq(Score::getScoreTaskId, taskId)
                    .eq(Score::getJudgeId, judgeId);
        Score existingScore = this.getOne(scoreWrapper);
        result.put("existingScore", existingScore);
        
        return Result.success(result);
    }

    @Override
    public Result submitScore(Score score, Integer judgeId) {
        ScoreTask task = scoreTaskMapper.selectById(score.getScoreTaskId());
        if (task == null) {
            return Result.error("评分任务不存在");
        }
        if (!task.getJudgeId().equals(judgeId)) {
            return Result.error("无权限评分");
        }
        
        // 检查截止时间
        if (task.getDeadline() != null && LocalDateTime.now().isAfter(task.getDeadline())) {
            return Result.error("评分已截止，无法提交");
        }
        
        // 检查是否已评分
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Score::getScoreTaskId, score.getScoreTaskId())
                .eq(Score::getJudgeId, judgeId);
        Score existingScore = this.getOne(wrapper);
        
        if (existingScore != null) {
            // 更新评分
            existingScore.setTotalScore(score.getTotalScore());
            existingScore.setScoreDetails(score.getScoreDetails());
            existingScore.setComment(score.getComment());
            existingScore.setUpdateTime(LocalDateTime.now());
            this.updateById(existingScore);
        } else {
            // 新建评分
            score.setJudgeId(judgeId);
            score.setWorkId(task.getWorkId());
            score.setScoreTime(LocalDateTime.now());
            score.setUpdateTime(LocalDateTime.now());
            this.save(score);
            
            // 更新任务状态
            task.setStatus(1);
            task.setUpdateTime(LocalDateTime.now());
            scoreTaskMapper.updateById(task);
        }
        
        return Result.success("评分提交成功");
    }

    @Override
    public Result updateScore(Score score, Integer judgeId) {
        Score existingScore = this.getById(score.getId());
        if (existingScore == null) {
            return Result.error("评分记录不存在");
        }
        if (!existingScore.getJudgeId().equals(judgeId)) {
            return Result.error("无权限修改");
        }
        
        ScoreTask task = scoreTaskMapper.selectById(existingScore.getScoreTaskId());
        if (task != null && task.getDeadline() != null && LocalDateTime.now().isAfter(task.getDeadline())) {
            return Result.error("评分已截止，无法修改");
        }
        
        existingScore.setTotalScore(score.getTotalScore());
        existingScore.setScoreDetails(score.getScoreDetails());
        existingScore.setComment(score.getComment());
        existingScore.setUpdateTime(LocalDateTime.now());
        this.updateById(existingScore);
        
        return Result.success("评分更新成功");
    }

    @Override
    public Result getScoreDetail(Integer taskId, Integer judgeId) {
        ScoreTask task = scoreTaskMapper.selectById(taskId);
        if (task == null) {
            return Result.error("评分任务不存在");
        }
        if (!task.getJudgeId().equals(judgeId)) {
            return Result.error("无权限查看");
        }
        
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Score::getScoreTaskId, taskId)
                .eq(Score::getJudgeId, judgeId);
        Score score = this.getOne(wrapper);
        
        if (score == null) {
            return Result.error("评分记录不存在");
        }
        
        Work work = workMapper.selectById(score.getWorkId());
        Competition competition = competitionMapper.selectById(task.getCompetitionId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        result.put("work", work);
        result.put("competition", competition);
        result.put("task", task);
        
        return Result.success(result);
    }

    @Override
    public Result getScoreStatistics(Integer judgeId) {
        LambdaQueryWrapper<ScoreTask> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(ScoreTask::getJudgeId, judgeId);
        List<ScoreTask> allTasks = scoreTaskMapper.selectList(taskWrapper);
        
        int total = allTasks.size();
        int scored = (int) allTasks.stream().filter(t -> t.getStatus() == 1).count();
        int pending = total - scored;
        
        LambdaQueryWrapper<Score> scoreWrapper = new LambdaQueryWrapper<>();
        scoreWrapper.eq(Score::getJudgeId, judgeId);
        List<Score> scores = this.list(scoreWrapper);
        
        double avgScore = 0;
        if (!scores.isEmpty()) {
            double sum = scores.stream()
                    .mapToDouble(s -> s.getTotalScore() != null ? s.getTotalScore().doubleValue() : 0)
                    .sum();
            avgScore = sum / scores.size();
        }
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", total);
        statistics.put("scored", scored);
        statistics.put("pending", pending);
        statistics.put("avgScore", avgScore);
        
        return Result.success(statistics);
    }

    @Override
    public Result getScoreRecords(Integer judgeId, Integer page, Integer size) {
        Page<Score> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Score::getJudgeId, judgeId)
                .orderByDesc(Score::getScoreTime);
        Page<Score> result = this.page(pageParam, wrapper);
        
        List<Score> records = result.getRecords();
        if (records != null && !records.isEmpty()) {
            List<Integer> taskIds = records.stream()
                    .map(Score::getScoreTaskId)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<Integer> workIds = records.stream()
                    .map(Score::getWorkId)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<Integer, ScoreTask> taskMap = new HashMap<>();
            if (!taskIds.isEmpty()) {
                List<ScoreTask> tasks = scoreTaskMapper.selectBatchIds(taskIds);
                taskMap = tasks.stream()
                        .collect(Collectors.toMap(ScoreTask::getId, t -> t, (v1, v2) -> v1));
            }
            
            Map<Integer, Work> workMap = new HashMap<>();
            if (!workIds.isEmpty()) {
                List<Work> works = workMapper.selectBatchIds(workIds);
                workMap = works.stream()
                        .filter(w -> w.getDeleted() == null || w.getDeleted() == 0)
                        .collect(Collectors.toMap(Work::getId, w -> w, (v1, v2) -> v1));
            }
            
            Map<Integer, Competition> competitionMap = new HashMap<>();
            List<Integer> competitionIds = taskMap.values().stream()
                    .map(ScoreTask::getCompetitionId)
                    .distinct()
                    .collect(Collectors.toList());
            if (!competitionIds.isEmpty()) {
                List<Competition> competitions = competitionMapper.selectBatchIds(competitionIds);
                competitionMap = competitions.stream()
                        .collect(Collectors.toMap(Competition::getId, c -> c, (v1, v2) -> v1));
            }
            
            final Map<Integer, ScoreTask> finalTaskMap = taskMap;
            final Map<Integer, Work> finalWorkMap = workMap;
            final Map<Integer, Competition> finalCompetitionMap = competitionMap;
            
            List<Map<String, Object>> recordList = records.stream().map(score -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", score.getId());
                map.put("totalScore", score.getTotalScore());
                map.put("scoreDetails", score.getScoreDetails());
                map.put("comment", score.getComment());
                map.put("scoreTime", score.getScoreTime());
                
                ScoreTask task = finalTaskMap.get(score.getScoreTaskId());
                if (task != null) {
                    Competition competition = finalCompetitionMap.get(task.getCompetitionId());
                    map.put("competitionName", competition != null ? competition.getName() : "未知竞赛");
                } else {
                    map.put("competitionName", "未知竞赛");
                }
                
                Work work = finalWorkMap.get(score.getWorkId());
                map.put("workTitle", work != null ? work.getTitle() : "作品不存在");
                
                return map;
            }).collect(Collectors.toList());
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("records", recordList);
            resultMap.put("total", result.getTotal());
            resultMap.put("size", result.getSize());
            resultMap.put("current", result.getCurrent());
            resultMap.put("pages", result.getPages());
            
            return Result.success(resultMap);
        }
        
        return Result.success(result);
    }
}

