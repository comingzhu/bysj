package com.competition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.common.Constants;
import com.competition.common.Result;
import com.competition.entity.Competition;
import com.competition.entity.CompetitionJudge;
import com.competition.entity.Registration;
import com.competition.entity.User;
import com.competition.mapper.CompetitionMapper;
import com.competition.mapper.CompetitionJudgeMapper;
import com.competition.mapper.RegistrationMapper;
import com.competition.mapper.UserMapper;
import com.competition.mapper.WorkMapper;
import com.competition.mapper.ScoreTaskMapper;
import com.competition.entity.Work;
import com.competition.entity.ScoreTask;
import com.competition.service.CompetitionService;
import com.competition.service.MessageService;
import com.competition.service.PaymentService;
import com.competition.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CompetitionServiceImpl extends ServiceImpl<CompetitionMapper, Competition> implements CompetitionService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkMapper workMapper;
    @Autowired
    private ScoreTaskMapper scoreTaskMapper;
    @Autowired
    private CompetitionJudgeMapper competitionJudgeMapper;
    @Autowired
    private RegistrationMapper registrationMapper;
    @Autowired
    private MessageService messageService;
    @Autowired
    private PaymentService paymentService;

    /**
     * 校验竞赛的时间逻辑：
     * 1. 报名开始时间不能晚于报名结束时间
     * 2. 竞赛开始时间不能晚于竞赛结束时间
     * 3. 报名结束时间不能晚于竞赛结束时间（如果都填写了）
     *
     * @param competition 竞赛对象
     * @return 校验不通过时返回错误提示，通过时返回 null
     */
    private String validateCompetitionTime(Competition competition) {
        LocalDateTime registrationStart = competition.getRegistrationStart();
        LocalDateTime registrationEnd = competition.getRegistrationEnd();
        LocalDateTime startTime = competition.getStartTime();
        LocalDateTime endTime = competition.getEndTime();

        if (registrationStart != null && registrationEnd != null && registrationStart.isAfter(registrationEnd)) {
            return "报名开始时间不能晚于报名结束时间";
        }
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            return "竞赛开始时间不能晚于结束时间";
        }
        if (registrationEnd != null && endTime != null && registrationEnd.isAfter(endTime)) {
            return "报名结束时间不能晚于竞赛结束时间";
        }
        return null;
    }
    @Override
    public Result createCompetition(Competition competition, Integer publisherId) {
        // 校验时间逻辑
        String timeError = validateCompetitionTime(competition);
        if (timeError != null) {
            return Result.error(timeError);
        }
        competition.setPublisherId(publisherId);
        competition.setStatus(Constants.COMPETITION_STATUS_PENDING);
        competition.setIsSystem(0);
        this.save(competition);
        return Result.success("创建成功，等待审核");
    }
  /**教师更新竞赛**/
    @Override
    public Result updateCompetition(Competition competition, Integer userId, String role) {
        Competition exist = this.getById(competition.getId());
        if (exist == null || exist.getDeleted() == 1) {
            return Result.error("竞赛不存在");
        }
        // 只有发布者或管理员可以修改
        if (!exist.getPublisherId().equals(userId) && !Constants.ROLE_ADMIN.equals(role)) {
            return Result.error("无权限修改");
        }

        // 校验时间逻辑（使用前端传入的新时间）
        String timeError = validateCompetitionTime(competition);
        if (timeError != null) {
            return Result.error(timeError);
        }

        // 默认继承原有状态与驳回原因，避免被前端覆盖为 null
        competition.setStatus(exist.getStatus());
        competition.setRejectReason(exist.getRejectReason());

        // 教师在“已通过 / 已发布 / 已暂停 / 已驳回”等状态下修改竞赛时，必须重新进入管理员审核
        // 即：状态重置为“待审核”，并清空驳回原因
        if (Constants.ROLE_TEACHER.equals(role)) {
            Integer oldStatus = exist.getStatus();
            if (oldStatus != null &&
                    (oldStatus.equals(Constants.COMPETITION_STATUS_APPROVED)  // 已通过
                            || oldStatus.equals(Constants.COMPETITION_STATUS_PUBLISHED) // 已发布
                            || oldStatus.equals(Constants.COMPETITION_STATUS_PAUSED) // 已暂停
                            || oldStatus.equals(Constants.COMPETITION_STATUS_REJECTED))) { // 已驳回
                competition.setStatus(Constants.COMPETITION_STATUS_PENDING);
                competition.setRejectReason(null);
            }
        }

        // 查询该竞赛下所有已缴费的报名记录并进行退款
        try {
            LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Registration::getCompetitionId, competition.getId())
                    .eq(Registration::getPaymentStatus, Constants.PAYMENT_STATUS_PAID)
                    .eq(Registration::getDeleted, 0);
            List<Registration> registrations = registrationMapper.selectList(wrapper);
            if (registrations != null && !registrations.isEmpty()) {
                for (Registration registration : registrations) {
                    // 调用退款服务
                    paymentService.refund(registration.getId(), "竞赛信息修改，需要重新缴费");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 退款失败不影响竞赛修改，但需要记录日志
        }

        this.updateById(competition);
        return Result.success("更新成功，已提交管理员审核");
    }

    @Override
    public Result getCompetitionList(String category, Integer status, String keyword, Integer isSystem, Integer page, Integer size) {
        Page<Competition> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getDeleted, 0);
        
        // 自动检查并更新竞赛状态（报名时间结束后自动暂停）
        autoUpdateCompetitionStatus();
        
        if (StringUtils.hasText(category)) {
            wrapper.eq(Competition::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Competition::getStatus, status);
        }
        if (isSystem != null) {
            wrapper.eq(Competition::getIsSystem, isSystem);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Competition::getName, keyword);
        }
        wrapper.orderByDesc(Competition::getCreateTime);
        Page<Competition> result = this.page(pageParam, wrapper);
        
        // 关联查询发布者信息并转换为Map格式
        List<Competition> records = result.getRecords();
        if (records != null && !records.isEmpty()) {
            List<Integer> publisherIds = records.stream()
                    .filter(c -> c.getPublisherId() != null)
                    .map(Competition::getPublisherId)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<Integer, String> publisherMap = new HashMap<>();
            if (!publisherIds.isEmpty()) {
                List<User> publishers = userMapper.selectBatchIds(publisherIds);
                publisherMap = publishers.stream()
                        .collect(Collectors.toMap(User::getId, 
                                u -> u.getRealName() != null ? u.getRealName() : u.getUsername(), 
                                (v1, v2) -> v1));
            }
            
            // 转换为Map列表，添加发布者名称
            final Map<Integer, String> finalPublisherMap = publisherMap;
            List<Map<String, Object>> recordList = records.stream().map(competition -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", competition.getId());
                map.put("name", competition.getName());
                map.put("description", competition.getDescription());
                map.put("type", competition.getType());
                map.put("needWork", competition.getNeedWork());
                map.put("registrationFee", competition.getRegistrationFee());
                map.put("registrationStart", competition.getRegistrationStart());
                map.put("registrationEnd", competition.getRegistrationEnd());
                map.put("startTime", competition.getStartTime());
                map.put("endTime", competition.getEndTime());
                map.put("location", competition.getLocation());
                map.put("category", competition.getCategory());
                map.put("awardMode", competition.getAwardMode());
                map.put("firstAwardRatio", competition.getFirstAwardRatio());
                map.put("secondAwardRatio", competition.getSecondAwardRatio());
                map.put("thirdAwardRatio", competition.getThirdAwardRatio());
                map.put("publisherId", competition.getPublisherId());
                map.put("publisherName", competition.getPublisherId() != null ? 
                        finalPublisherMap.getOrDefault(competition.getPublisherId(), "未知") : "系统");
                map.put("status", competition.getStatus());
                map.put("rejectReason", competition.getRejectReason());
                map.put("isSystem", competition.getIsSystem());
                map.put("createTime", competition.getCreateTime());
                map.put("updateTime", competition.getUpdateTime());
                map.put("deleted", competition.getDeleted());
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
    public Result getCompetitionDetail(Integer id) {
        Competition competition = this.getById(id);
        if (competition == null || competition.getDeleted() == 1) {
            return Result.error("竞赛不存在");
        }
        return Result.success(competition);
    }

    @Override
    public Result approveCompetition(Integer id, Integer status, String rejectReason, String judgeIds) {
        Competition competition = this.getById(id);
        if (competition == null || competition.getDeleted() == 1) {
            return Result.error("竞赛不存在");
        }
        // 审核通过“需要作品”的普通校赛时，必须选择至少3名评委；
        // 对于不需要作品的校赛，则不强制选择评委，由老师线下打分后直接发奖
        if (status != null && status.equals(Constants.COMPETITION_STATUS_APPROVED)
                && competition.getIsSystem() != null && competition.getIsSystem() == 0
                && competition.getNeedWork() != null && competition.getNeedWork() == 1) {
            if (judgeIds == null || judgeIds.trim().isEmpty()) {
                return Result.error("请至少选择3名评委");
            }
            String[] idArr = judgeIds.split(",");
            if (idArr.length < 3) {
                return Result.error("请选择至少3名评委");
            }
            // 先清空原有评委
            LambdaQueryWrapper<CompetitionJudge> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(CompetitionJudge::getCompetitionId, id);
            competitionJudgeMapper.delete(deleteWrapper);

            // 保存新评委
            for (String s : idArr) {
                try {
                    Integer judgeId = Integer.valueOf(s.trim());
                    CompetitionJudge cj = new CompetitionJudge();
                    cj.setCompetitionId(id);
                    cj.setJudgeId(judgeId);
                    competitionJudgeMapper.insert(cj);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        competition.setStatus(status);
        if (status == Constants.COMPETITION_STATUS_REJECTED) {
            competition.setRejectReason(rejectReason);
        }
        this.updateById(competition);
        
        // 发送通知给竞赛发布者
        sendCompetitionAuditNotification(competition, status, rejectReason);
        
        return Result.success("操作成功");
    }

    @Override
    public Result publishCompetition(Integer id) {
        Competition competition = this.getById(id);
        if (competition == null || competition.getDeleted() == 1) {
            return Result.error("竞赛不存在");
        }
        if (competition.getStatus() != Constants.COMPETITION_STATUS_APPROVED) {
            return Result.error("只能发布已审核通过的竞赛");
        }
        competition.setStatus(Constants.COMPETITION_STATUS_PUBLISHED);
        this.updateById(competition);
        return Result.success("发布成功");
    }

    @Override
    public Result pauseCompetition(Integer id) {
        Competition competition = this.getById(id);
        if (competition == null || competition.getDeleted() == 1) {
            return Result.error("竞赛不存在");
        }
        if (competition.getStatus() != Constants.COMPETITION_STATUS_PUBLISHED) {
            return Result.error("只能暂停已发布的竞赛");
        }
        competition.setStatus(Constants.COMPETITION_STATUS_PAUSED);
        this.updateById(competition);
        
        // 如果报名时间已结束，为所有已提交的作品创建评分任务
        LocalDateTime now = LocalDateTime.now();
        if (competition.getRegistrationEnd() != null && now.isAfter(competition.getRegistrationEnd())) {
            createScoreTasksForCompetition(competition);
        }
        
        return Result.success("暂停成功");
    }

    @Override
    public Result resumeCompetition(Integer id) {
        Competition competition = this.getById(id);
        if (competition == null || competition.getDeleted() == 1) {
            return Result.error("竞赛不存在");
        }
        if (competition.getStatus() != Constants.COMPETITION_STATUS_PAUSED) {
            return Result.error("只能恢复已暂停的竞赛");
        }
        competition.setStatus(Constants.COMPETITION_STATUS_PUBLISHED);
        this.updateById(competition);
        return Result.success("恢复成功");
    }

    @Override
    public Result getMyCompetitions(Integer publisherId, Integer page, Integer size) {
        Page<Competition> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getPublisherId, publisherId)
                .eq(Competition::getDeleted, 0)
                .orderByDesc(Competition::getCreateTime);
        Page<Competition> result = this.page(pageParam, wrapper);
        return Result.success(result);
    }

    @Override
    public Result deleteCompetition(Integer id, Integer userId, String role) {
        LambdaQueryWrapper<Competition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Competition::getId, id).eq(Competition::getDeleted, 0);
        Competition competition = this.getOne(queryWrapper);
        if (competition == null) {
            return Result.error("竞赛不存在");
        }
        if (!competition.getPublisherId().equals(userId) && !Constants.ROLE_ADMIN.equals(role)) {
            return Result.error("无权限删除");
        }
        // 使用LambdaUpdateWrapper明确更新deleted字段
        LambdaUpdateWrapper<Competition> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Competition::getId, id)
                     .eq(Competition::getDeleted, 0)
                     .set(Competition::getDeleted, 1);
        boolean updated = this.update(updateWrapper);
        if (!updated) {
            return Result.error("删除失败");
        }
        return Result.success("删除成功");
    }

    @Override
    public Result createSystemCompetition(Competition competition) {
        // 校验时间逻辑
        String timeError = validateCompetitionTime(competition);
        if (timeError != null) {
            return Result.error(timeError);
        }
        competition.setIsSystem(1);
        competition.setStatus(Constants.COMPETITION_STATUS_PUBLISHED); // 系统推送直接发布
        competition.setPublisherId(null); // 系统推送没有发布者
        this.save(competition);
        return Result.success("系统竞赛创建成功");
    }

    @Override
    public Result getJudgesByCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return Result.error("竞赛分类不能为空");
        }
        // 根据分类从 user + judge_category 中查找评委
        java.util.List<User> judges = userMapper.findJudgesByCategory(category);
        return Result.success(judges);
    }
    
    @Override
    public Result getJudgesByCompetition(Integer competitionId) {
        if (competitionId == null) {
            return Result.error("竞赛ID不能为空");
        }
        LambdaQueryWrapper<CompetitionJudge> cjWrapper = new LambdaQueryWrapper<>();
        cjWrapper.eq(CompetitionJudge::getCompetitionId, competitionId);
        java.util.List<CompetitionJudge> list = competitionJudgeMapper.selectList(cjWrapper);
        if (list == null || list.isEmpty()) {
            return Result.success(java.util.List.of());
        }
        java.util.List<Integer> judgeIds = list.stream()
                .map(CompetitionJudge::getJudgeId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        if (judgeIds.isEmpty()) {
            return Result.success(java.util.List.of());
        }
        java.util.List<User> judges = userMapper.selectBatchIds(judgeIds);
        return Result.success(judges);
    }
    
    /**
     * 自动更新竞赛状态
     * 报名时间结束后，如果竞赛状态为"已发布"，自动暂停
     * 同时为所有已提交的作品创建评分任务
     * 也检查已暂停的竞赛，如果报名时间已结束，确保评分任务已创建
     */
    private void autoUpdateCompetitionStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 处理状态为"已发布"的竞赛，报名时间结束后自动暂停
        LambdaQueryWrapper<Competition> publishedWrapper = new LambdaQueryWrapper<>();
        publishedWrapper.eq(Competition::getDeleted, 0)
               .eq(Competition::getStatus, Constants.COMPETITION_STATUS_PUBLISHED)
               .isNotNull(Competition::getRegistrationEnd)
               .le(Competition::getRegistrationEnd, now);
        
        List<Competition> publishedCompetitions = this.list(publishedWrapper);
        if (!publishedCompetitions.isEmpty()) {
            for (Competition competition : publishedCompetitions) {
                // 报名时间已过，自动暂停
                competition.setStatus(Constants.COMPETITION_STATUS_PAUSED);
                this.updateById(competition);
                
                // 为所有已提交的作品创建评分任务
                createScoreTasksForCompetition(competition);
            }
        }
        
        // 2. 检查已暂停的竞赛，如果报名时间已结束，确保评分任务已创建
        LambdaQueryWrapper<Competition> pausedWrapper = new LambdaQueryWrapper<>();
        pausedWrapper.eq(Competition::getDeleted, 0)
                    .eq(Competition::getStatus, Constants.COMPETITION_STATUS_PAUSED)
                    .isNotNull(Competition::getRegistrationEnd)
                    .le(Competition::getRegistrationEnd, now);
        
        List<Competition> pausedCompetitions = this.list(pausedWrapper);
        if (!pausedCompetitions.isEmpty()) {
            for (Competition competition : pausedCompetitions) {
                // 确保评分任务已创建
                createScoreTasksForCompetition(competition);
            }
        }
    }
    
    @Override
    public Result createScoreTasks(Integer competitionId) {
        Competition competition = this.getById(competitionId);
        if (competition == null || competition.getDeleted() == 1) {
            return Result.error("竞赛不存在");
        }
        
        // 检查报名时间是否已结束
        LocalDateTime now = LocalDateTime.now();
        if (competition.getRegistrationEnd() != null && now.isBefore(competition.getRegistrationEnd())) {
            return Result.error("报名时间未结束，无法创建评分任务");
        }
        
        int createdCount = createScoreTasksForCompetition(competition);
        return Result.success("成功创建 " + createdCount + " 个评分任务");
    }
    
    /**
     * 为竞赛的所有已提交作品创建评分任务
     * @return 创建的评分任务数量
     */
    private int createScoreTasksForCompetition(Competition competition) {
        // 只处理需要提交作品的竞赛
        if (competition.getNeedWork() == null || competition.getNeedWork() != 1) {
            return 0;
        }
        
        // 查询该竞赛的所有已提交作品
        LambdaQueryWrapper<Work> workWrapper = new LambdaQueryWrapper<>();
        workWrapper.eq(Work::getCompetitionId, competition.getId())
                  .eq(Work::getDeleted, 0);
        List<Work> works = workMapper.selectList(workWrapper);
        
        if (works.isEmpty()) {
            return 0; // 没有作品，不需要创建任务
        }
        
        // 查询该竞赛配置的评分员（competition_judge）
        LambdaQueryWrapper<CompetitionJudge> cjWrapper = new LambdaQueryWrapper<>();
        cjWrapper.eq(CompetitionJudge::getCompetitionId, competition.getId());
        List<CompetitionJudge> cjList = competitionJudgeMapper.selectList(cjWrapper);
        if (cjList == null || cjList.isEmpty()) {
            return 0; // 未配置评委，不创建任务
        }
        List<Integer> judgeIds = cjList.stream()
                .map(CompetitionJudge::getJudgeId)
                .distinct()
                .collect(Collectors.toList());
        if (judgeIds.isEmpty()) {
            return 0;
        }

        List<User> judges = userMapper.selectBatchIds(judgeIds);
        
        if (judges == null || judges.isEmpty()) {
            return 0; // 没有评分员，不创建任务
        }
        
        // 设置评分截止时间：竞赛结束时间后7天；如果没有结束时间，则报名结束后7天
        LocalDateTime deadline = null;
        if (competition.getEndTime() != null) {
            deadline = competition.getEndTime().plusDays(7);
        } else if (competition.getRegistrationEnd() != null) {
            deadline = competition.getRegistrationEnd().plusDays(7);
        } else {
            deadline = LocalDateTime.now().plusDays(7);
        }
        
        // 为每个作品和每个评分员创建评分任务
        int createdCount = 0;
        for (Work work : works) {
            for (User judge : judges) {
                // 检查是否已存在该评分员对该作品的任务
                LambdaQueryWrapper<ScoreTask> taskWrapper = new LambdaQueryWrapper<>();
                taskWrapper.eq(ScoreTask::getWorkId, work.getId())
                          .eq(ScoreTask::getJudgeId, judge.getId());
                if (scoreTaskMapper.selectCount(taskWrapper) == 0) {
                    ScoreTask task = new ScoreTask();
                    task.setCompetitionId(competition.getId());
                    task.setJudgeId(judge.getId());
                    task.setWorkId(work.getId());
                    task.setStatus(0); // 待评分
                    task.setDeadline(deadline);
                    task.setCreateTime(LocalDateTime.now());
                    task.setUpdateTime(LocalDateTime.now());
                    scoreTaskMapper.insert(task);
                    createdCount++;
                }
            }
        }
        return createdCount;
    }
    
    /**
     * 发送竞赛审核通知
     */
    private void sendCompetitionAuditNotification(Competition competition, Integer status, String rejectReason) {
        // 只有非系统竞赛才有发布者，需要发送通知
        if (competition.getIsSystem() != null && competition.getIsSystem() == 1) {
            return;
        }
        
        Integer publisherId = competition.getPublisherId();
        if (publisherId == null) {
            return;
        }
        
        Message message = new Message();
        message.setUserId(publisherId);
        message.setCreateTime(LocalDateTime.now());
        message.setIsRead(0);
        message.setType("竞赛通知");
        
        if (status == Constants.COMPETITION_STATUS_APPROVED) {
            message.setTitle("竞赛审核通过通知");
            message.setContent("您发布的竞赛《" + competition.getName() + "》已通过审核，请及时查看竞赛状态。");
        } else if (status == Constants.COMPETITION_STATUS_REJECTED) {
            message.setTitle("竞赛审核驳回通知");
            if (StringUtils.hasText(rejectReason)) {
                message.setContent("您发布的竞赛《" + competition.getName() + "》未通过审核，驳回原因：" + rejectReason);
            } else {
                message.setContent("您发布的竞赛《" + competition.getName() + "》未通过审核，请联系管理员了解详情。");
            }
        }
        
        try {
            messageService.sendMessage(message);
        } catch (Exception e) {
            // 通知发送失败不影响审核流程
            e.printStackTrace();
        }
    }
}

