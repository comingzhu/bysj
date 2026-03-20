package com.competition.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("score")
public class Score {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer scoreTaskId;
    private Integer workId;
    private Integer judgeId;
    private BigDecimal totalScore;
    private String scoreDetails; // JSON格式
    private String comment;
    private LocalDateTime scoreTime;
    private LocalDateTime updateTime;
}





