package com.competition.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("competition")
public class Competition {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String description;
    private Integer type; // 0-个人赛，1-团队赛
    private Integer needWork; // 0-否，1-是
    private BigDecimal registrationFee;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationEnd;
    private String location;
    private String category;
    /**
     * 获奖模式：0-固定名额（各1个一二三等奖），1-按比例评奖（类似蓝桥杯，使用比例字段）
     */
    private Integer awardMode;
    /**
     * 一等奖比例（0-1），仅在按比例模式下生效
     */
    private BigDecimal firstAwardRatio;
    /**
     * 二等奖比例（0-1），仅在按比例模式下生效
     */
    private BigDecimal secondAwardRatio;
    /**
     * 三等奖比例（0-1），仅在按比例模式下生效
     */
    private BigDecimal thirdAwardRatio;
    private Integer publisherId;
    private Integer status; // 0-草稿，1-待审核，2-已通过，3-已驳回，4-已发布，5-已结束
    private String rejectReason;
    private Integer isSystem; // 0-否，1-是
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}

