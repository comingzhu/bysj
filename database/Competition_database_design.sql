-- 大学生竞赛活动管理平台数据库设计
-- 数据库名：Competition

-- 创建数据库
CREATE DATABASE IF NOT EXISTS Competition DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE Competition;

-- 1. 用户表（存储所有用户信息）
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `role` ENUM('student', 'teacher', 'judge', 'admin') NOT NULL COMMENT '角色：学生、老师、评分员、管理员',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `student_id` VARCHAR(20) COMMENT '学号（学生必填）',
  `id_card` VARCHAR(18) COMMENT '身份证号',
  `department` VARCHAR(100) COMMENT '院系',
  `major` VARCHAR(100) COMMENT '专业',
  `grade` VARCHAR(20) COMMENT '年级',
  `class` VARCHAR(50) COMMENT '班级',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_username (`username`),
  INDEX idx_role (`role`),
  INDEX idx_student_id (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 竞赛类别表（如蓝桥杯、数学建模等）
DROP TABLE IF EXISTS `competition_category`;
CREATE TABLE `competition_category` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '类别ID',
  `name` VARCHAR(100) NOT NULL COMMENT '类别名称',
  `description` TEXT COMMENT '类别描述',
  `icon` VARCHAR(255) COMMENT '图标URL',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛类别表';

-- 3. 竞赛表（存储竞赛信息）
DROP TABLE IF EXISTS `competition`;
CREATE TABLE `competition` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '竞赛ID',
  `category_id` INT COMMENT '竞赛类别ID',
  `title` VARCHAR(200) NOT NULL COMMENT '竞赛标题',
  `description` TEXT COMMENT '竞赛描述',
  `competition_type` ENUM('personal', 'team') NOT NULL DEFAULT 'personal' COMMENT '竞赛类型：个人赛、团队赛',
  `team_size` INT DEFAULT 1 COMMENT '团队人数限制（团队赛时有效）',
  `need_work` TINYINT DEFAULT 0 COMMENT '是否需要提交作品：0-否，1-是',
  `official_url` VARCHAR(500) COMMENT '官网链接',
  `registration_fee` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '报名费',
  `max_participants` INT COMMENT '最大参与人数',
  `current_participants` INT DEFAULT 0 COMMENT '当前参与人数',
  `registration_start_time` DATETIME NOT NULL COMMENT '报名开始时间',
  `registration_end_time` DATETIME NOT NULL COMMENT '报名结束时间',
  `competition_start_time` DATETIME COMMENT '比赛开始时间',
  `competition_end_time` DATETIME COMMENT '比赛结束时间',
  `scoring_start_time` DATETIME COMMENT '评分开始时间',
  `scoring_end_time` DATETIME COMMENT '评分结束时间',
  `poster_url` VARCHAR(500) COMMENT '海报URL',
  `organizer` VARCHAR(200) COMMENT '主办方',
  `publisher_id` INT NOT NULL COMMENT '发布人ID（老师ID）',
  `status` ENUM('draft', 'published', 'registration', 'ongoing', 'scoring', 'finished', 'cancelled') DEFAULT 'draft' COMMENT '状态：草稿、已发布、报名中、进行中、评分中、已结束、已取消',
  `is_pushed` TINYINT DEFAULT 0 COMMENT '是否已推送：0-否，1-是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`category_id`) REFERENCES `competition_category`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`publisher_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  INDEX idx_status (`status`),
  INDEX idx_competition_type (`competition_type`),
  INDEX idx_publisher_id (`publisher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛表';

-- 4. 团队表（存储团队信息）
DROP TABLE IF EXISTS `team`;
CREATE TABLE `team` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '团队ID',
  `competition_id` INT NOT NULL COMMENT '竞赛ID',
  `team_name` VARCHAR(100) NOT NULL COMMENT '团队名称',
  `team_slogan` VARCHAR(200) COMMENT '团队口号',
  `captain_id` INT NOT NULL COMMENT '队长ID',
  `max_members` INT NOT NULL COMMENT '最大成员数',
  `current_members` INT DEFAULT 1 COMMENT '当前成员数',
  `status` ENUM('forming', 'complete', 'cancelled') DEFAULT 'forming' COMMENT '状态：组建中、已完成、已取消',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`captain_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  INDEX idx_competition_id (`competition_id`),
  INDEX idx_captain_id (`captain_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队表';

-- 5. 团队成员表（存储团队成员关系）
DROP TABLE IF EXISTS `team_member`;
CREATE TABLE `team_member` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '成员ID',
  `team_id` INT NOT NULL COMMENT '团队ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-已退出',
  FOREIGN KEY (`team_id`) REFERENCES `team`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  UNIQUE KEY uk_team_user (`team_id`, `user_id`),
  INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队成员表';

-- 6. 报名表（存储学生报名信息）
DROP TABLE IF EXISTS `registration`;
CREATE TABLE `registration` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '报名ID',
  `competition_id` INT NOT NULL COMMENT '竞赛ID',
  `user_id` INT NOT NULL COMMENT '用户ID（学生ID）',
  `team_id` INT COMMENT '团队ID（团队赛时必填）',
  `registration_info` JSON COMMENT '报名信息（JSON格式存储姓名、学号、身份证号等）',
  `status` ENUM('pending', 'approved', 'rejected', 'paid', 'completed') DEFAULT 'pending' COMMENT '状态：待审核、已通过、已拒绝、已缴费、已完成',
  `audit_time` DATETIME COMMENT '审核时间',
  `auditor_id` INT COMMENT '审核人ID（管理员ID）',
  `audit_remark` TEXT COMMENT '审核备注',
  `payment_time` DATETIME COMMENT '缴费时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`team_id`) REFERENCES `team`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`auditor_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
  INDEX idx_competition_id (`competition_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_status (`status`),
  INDEX idx_team_id (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名表';

-- 7. 作品表（存储学生提交的作品）
DROP TABLE IF EXISTS `work`;
CREATE TABLE `work` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '作品ID',
  `competition_id` INT NOT NULL COMMENT '竞赛ID',
  `registration_id` INT NOT NULL COMMENT '报名ID',
  `team_id` INT COMMENT '团队ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `work_title` VARCHAR(200) NOT NULL COMMENT '作品标题',
  `work_description` TEXT COMMENT '作品描述',
  `work_files` JSON COMMENT '作品文件（JSON格式存储文件URL）',
  `submit_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `status` ENUM('submitted', 'scoring', 'scored', 'cancelled') DEFAULT 'submitted' COMMENT '状态：已提交、评分中、已评分、已取消',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`registration_id`) REFERENCES `registration`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`team_id`) REFERENCES `team`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  INDEX idx_competition_id (`competition_id`),
  INDEX idx_registration_id (`registration_id`),
  INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作品表';

-- 8. 评分表（存储评分员的评分）
DROP TABLE IF EXISTS `scoring`;
CREATE TABLE `scoring` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '评分ID',
  `work_id` INT NOT NULL COMMENT '作品ID',
  `judge_id` INT NOT NULL COMMENT '评分员ID',
  `score` DECIMAL(5, 2) NOT NULL COMMENT '分数',
  `comment` TEXT COMMENT '评分评语',
  `score_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`work_id`) REFERENCES `work`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`judge_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  UNIQUE KEY uk_work_judge (`work_id`, `judge_id`),
  INDEX idx_work_id (`work_id`),
  INDEX idx_judge_id (`judge_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评分表';

-- 9. 缴费表（存储缴费信息）
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '缴费ID',
  `registration_id` INT NOT NULL COMMENT '报名ID',
  `competition_id` INT NOT NULL COMMENT '竞赛ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(10, 2) NOT NULL COMMENT '缴费金额',
  `payment_method` ENUM('wechat', 'alipay', 'bank', 'other') DEFAULT 'wechat' COMMENT '支付方式：微信、支付宝、银行卡、其他',
  `payment_status` ENUM('unpaid', 'paid', 'refunded') DEFAULT 'unpaid' COMMENT '支付状态：未支付、已支付、已退款',
  `transaction_id` VARCHAR(100) COMMENT '交易流水号',
  `payment_time` DATETIME COMMENT '支付时间',
  `refund_time` DATETIME COMMENT '退款时间',
  `refund_reason` VARCHAR(500) COMMENT '退款原因',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`registration_id`) REFERENCES `registration`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  INDEX idx_registration_id (`registration_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_payment_status (`payment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='缴费表';

-- 10. 消息表（存储推送消息）
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
  `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `message_type` ENUM('competition', 'payment', 'system', 'award') NOT NULL COMMENT '消息类型：竞赛、缴费、系统、获奖',
  `sender_id` INT COMMENT '发送人ID',
  `receiver_id` INT COMMENT '接收人ID（为空表示全员推送）',
  `competition_id` INT COMMENT '关联竞赛ID',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `read_time` DATETIME COMMENT '阅读时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`sender_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`receiver_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`) ON DELETE SET NULL,
  INDEX idx_receiver_id (`receiver_id`),
  INDEX idx_message_type (`message_type`),
  INDEX idx_is_read (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 11. 获奖表（存储获奖信息）
DROP TABLE IF EXISTS `award`;
CREATE TABLE `award` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '获奖ID',
  `competition_id` INT NOT NULL COMMENT '竞赛ID',
  `registration_id` INT NOT NULL COMMENT '报名ID',
  `team_id` INT COMMENT '团队ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `award_level` ENUM('first', 'second', 'third', 'excellent', 'participation') NOT NULL COMMENT '获奖等级：一等奖、二等奖、三等奖、优秀奖、参与奖',
  `award_name` VARCHAR(100) NOT NULL COMMENT '奖项名称',
  `certificate_url` VARCHAR(500) COMMENT '证书URL',
  `award_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '获奖时间',
  `publisher_id` INT COMMENT '发布人ID（管理员ID）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`registration_id`) REFERENCES `registration`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`team_id`) REFERENCES `team`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`publisher_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
  INDEX idx_competition_id (`competition_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_award_level (`award_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='获奖表';

-- 12. 竞赛统计表（存储竞赛统计数据）
DROP TABLE IF EXISTS `competition_statistics`;
CREATE TABLE `competition_statistics` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
  `competition_id` INT NOT NULL COMMENT '竞赛ID',
  `total_registrations` INT DEFAULT 0 COMMENT '总报名人数',
  `paid_registrations` INT DEFAULT 0 COMMENT '已缴费人数',
  `completed_registrations` INT DEFAULT 0 COMMENT '完成人数',
  `total_teams` INT DEFAULT 0 COMMENT '总团队数',
  `total_works` INT DEFAULT 0 COMMENT '总作品数',
  `average_score` DECIMAL(5, 2) COMMENT '平均分',
  `statistics_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '统计日期',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`) ON DELETE CASCADE,
  INDEX idx_competition_id (`competition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛统计表';
