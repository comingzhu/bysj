package com.competition.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("competition_judge")
public class CompetitionJudge {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer competitionId;
    private Integer judgeId;
}

