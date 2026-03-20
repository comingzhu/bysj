package com.competition.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("team")
public class Team {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private Integer competitionId;
    private Integer leaderId;
    private Integer maxMembers;
    private Integer currentMembers;
    private Integer status; // 0-待审核，1-已通过，2-已驳回
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}





