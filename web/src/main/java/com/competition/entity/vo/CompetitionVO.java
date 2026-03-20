package com.competition.entity.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CompetitionVO {
    private Integer id;
    private String name;
    private String description;
    private Integer type;
    private Integer needWork;
    private BigDecimal registrationFee;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private String location;
    private String category;
    private Integer publisherId;
    private String publisherName; // 发布者姓名
    private Integer status;
    private String rejectReason;
    private Integer isSystem;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}





