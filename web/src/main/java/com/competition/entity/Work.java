package com.competition.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("work")
public class Work {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer competitionId;
    private Integer registrationId;
    private Integer userId;
    private Integer teamId;
    private String title;
    private String description;
    private String filePath;
    private String fileName;
    private Long fileSize;
    private LocalDateTime submitTime;
    private Integer status; // 0-正常，1-异常
    private Integer deleted;
}





