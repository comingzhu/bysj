package com.competition.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("registration")
public class Registration {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer competitionId;
    private Integer userId;
    private Integer teamId;
    private Integer status; // 0-待审核，1-已通过，2-已驳回
    private String rejectReason;
    private Integer paymentStatus; // 0-未缴费，1-已缴费，2-已退款
    private BigDecimal paymentAmount;
    private LocalDateTime paymentTime;
    private String paymentVoucher;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}





