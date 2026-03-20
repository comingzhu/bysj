package com.competition.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("score_task")
public class ScoreTask {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer competitionId;
    private Integer judgeId;
    private Integer workId;
    private Integer status; // 0-待评分，1-已评分
    private LocalDateTime deadline;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}





