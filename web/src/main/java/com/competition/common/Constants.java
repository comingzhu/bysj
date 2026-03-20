package com.competition.common;

public class Constants {
    // 角色常量
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_JUDGE = "judge";
    public static final String ROLE_ADMIN = "admin";

    // 竞赛状态
    public static final Integer COMPETITION_STATUS_DRAFT = 0; // 草稿
    public static final Integer COMPETITION_STATUS_PENDING = 1; // 待审核
    public static final Integer COMPETITION_STATUS_APPROVED = 2; // 已通过
    public static final Integer COMPETITION_STATUS_REJECTED = 3; // 已驳回
    public static final Integer COMPETITION_STATUS_PUBLISHED = 4; // 已发布
    public static final Integer COMPETITION_STATUS_ENDED = 5; // 已结束
    public static final Integer COMPETITION_STATUS_PAUSED = 6; // 已暂停

    // 报名状态
    public static final Integer REGISTRATION_STATUS_PENDING = 0; // 待审核
    public static final Integer REGISTRATION_STATUS_APPROVED = 1; // 已通过
    public static final Integer REGISTRATION_STATUS_REJECTED = 2; // 已驳回

    // 缴费状态
    public static final Integer PAYMENT_STATUS_UNPAID = 0; // 未缴费
    public static final Integer PAYMENT_STATUS_PAID = 1; // 已缴费
    public static final Integer PAYMENT_STATUS_REFUNDED = 2; // 已退款

    // 竞赛类型
    public static final Integer COMPETITION_TYPE_INDIVIDUAL = 0; // 个人赛
    public static final Integer COMPETITION_TYPE_TEAM = 1; // 团队赛

    // 是否需要作品
    public static final Integer NEED_WORK_NO = 0; // 不需要
    public static final Integer NEED_WORK_YES = 1; // 需要

    // 评分状态
    public static final Integer SCORE_STATUS_PENDING = 0; // 待评分
    public static final Integer SCORE_STATUS_SCORED = 1; // 已评分
}

