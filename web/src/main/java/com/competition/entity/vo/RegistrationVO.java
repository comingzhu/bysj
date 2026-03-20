package com.competition.entity.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RegistrationVO {
    private Integer id;
    private Integer competitionId;
    private String competitionName; // 竞赛名称
    private Integer userId;
    private String userName; // 报名人姓名
    private Integer teamId;
    private String teamName; // 团队名称
    private Integer status;
    private String rejectReason;
    private Integer paymentStatus;
    private BigDecimal paymentAmount;
    private LocalDateTime paymentTime;
    private String paymentVoucher;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}





