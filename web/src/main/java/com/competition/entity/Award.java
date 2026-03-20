package com.competition.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("award")
public class Award {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer competitionId;
    private Integer registrationId;
    private Integer userId;
    private Integer teamId;
    private String awardLevel;
    @TableField("`rank`")
    private Integer rank;
    private BigDecimal score;
    private LocalDateTime createTime;
    private String certificateNumber;
}

