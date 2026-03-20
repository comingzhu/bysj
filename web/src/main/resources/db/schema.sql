-- 创建数据库
CREATE DATABASE IF NOT EXISTS Competition DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE Competition;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（明文存储）',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `student_no` VARCHAR(20) COMMENT '学号（学生）',
  `teacher_no` VARCHAR(20) COMMENT '工号（老师）',
  `role` VARCHAR(20) NOT NULL COMMENT '角色：student/teacher/judge/admin',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `college` VARCHAR(100) COMMENT '学院',
  `major` VARCHAR(100) COMMENT '专业',
  `class_name` VARCHAR(50) COMMENT '班级',
  `grade` VARCHAR(20) COMMENT '年级',
  `id_card` VARCHAR(18) COMMENT '身份证号',
  `status` INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 竞赛表
CREATE TABLE IF NOT EXISTS `competition` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '竞赛ID',
  `name` VARCHAR(200) NOT NULL COMMENT '竞赛名称',
  `description` TEXT COMMENT '竞赛描述',
  `type` INT NOT NULL COMMENT '竞赛类型：0-个人赛，1-团队赛',
  `need_work` INT DEFAULT 0 COMMENT '是否需要提交作品：0-否，1-是',
  `registration_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '报名费',
  `start_time` DATETIME COMMENT '开始时间',
  `end_time` DATETIME COMMENT '结束时间',
  `registration_start` DATETIME COMMENT '报名开始时间',
  `registration_end` DATETIME COMMENT '报名结束时间',
  `location` VARCHAR(200) COMMENT '地点',
  `category` VARCHAR(50) COMMENT '竞赛分类',
  `award_mode` INT DEFAULT 0 COMMENT '获奖规则：0-单一名额(1个一二三等奖)，1-按比例评奖(如前10%)',
  `first_award_ratio` DECIMAL(5,2) DEFAULT 0.10 COMMENT '一等奖比例(0-1，小数)，仅在按比例模式下生效',
  `second_award_ratio` DECIMAL(5,2) DEFAULT 0.20 COMMENT '二等奖比例(0-1，小数)，仅在按比例模式下生效',
  `third_award_ratio` DECIMAL(5,2) DEFAULT 0.30 COMMENT '三等奖比例(0-1，小数)，仅在按比例模式下生效',
  `publisher_id` INT COMMENT '发布者ID（老师）',
  `status` INT DEFAULT 0 COMMENT '状态：0-草稿，1-待审核，2-已通过，3-已驳回，4-已发布，5-已结束',
  `reject_reason` TEXT COMMENT '驳回原因',
  `is_system` INT DEFAULT 0 COMMENT '是否系统推送：0-否（校赛），1-是（省赛等）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  FOREIGN KEY (`publisher_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛表';

-- 团队表
CREATE TABLE IF NOT EXISTS `team` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '团队ID',
  `name` VARCHAR(100) NOT NULL COMMENT '团队名称',
  `competition_id` INT NOT NULL COMMENT '竞赛ID',
  `leader_id` INT NOT NULL COMMENT '队长ID',
  `max_members` INT DEFAULT 5 COMMENT '最大成员数',
  `current_members` INT DEFAULT 1 COMMENT '当前成员数',
  `status` INT DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已驳回',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`),
  FOREIGN KEY (`leader_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队表';

-- 团队成员表
CREATE TABLE IF NOT EXISTS `team_member` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `team_id` INT NOT NULL COMMENT '团队ID',
  `user_id` INT NOT NULL COMMENT '成员ID',
  `status` INT DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`team_id`) REFERENCES `team`(`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  UNIQUE KEY `uk_team_user` (`team_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员表';

-- 报名表
CREATE TABLE IF NOT EXISTS `registration` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '报名ID',
  `competition_id` INT NOT NULL COMMENT '竞赛ID',
  `user_id` INT COMMENT '用户ID（个人赛）',
  `team_id` INT COMMENT '团队ID（团队赛）',
  `status` INT DEFAULT 0 COMMENT '报名状态：0-待审核，1-已通过，2-已驳回',
  `reject_reason` TEXT COMMENT '驳回原因',
  `payment_status` INT DEFAULT 0 COMMENT '缴费状态：0-未缴费，1-已缴费，2-已退款',
  `payment_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '缴费金额',
  `payment_time` DATETIME COMMENT '缴费时间',
  `payment_voucher` VARCHAR(255) COMMENT '缴费凭证',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`team_id`) REFERENCES `team`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名表';

-- 作品表
CREATE TABLE IF NOT EXISTS `work` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '作品ID',
  `competition_id` INT NOT NULL COMMENT '竞赛ID',
  `registration_id` INT NOT NULL COMMENT '报名ID',
  `user_id` INT COMMENT '提交者ID（个人赛）',
  `team_id` INT COMMENT '团队ID（团队赛）',
  `title` VARCHAR(200) COMMENT '作品标题',
  `description` TEXT COMMENT '作品描述',
  `file_path` VARCHAR(500) COMMENT '文件路径',
  `file_name` VARCHAR(255) COMMENT '文件名',
  `file_size` BIGINT COMMENT '文件大小（字节）',
  `submit_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `status` INT DEFAULT 0 COMMENT '状态：0-正常，1-异常',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`),
  FOREIGN KEY (`registration_id`) REFERENCES `registration`(`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`team_id`) REFERENCES `team`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品表';

-- 评委擅长类别表（评委-竞赛分类映射，用于按类型筛选评委）
CREATE TABLE IF NOT EXISTS `judge_category` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `judge_id` INT NOT NULL COMMENT '评分员ID',
  `category` VARCHAR(50) NOT NULL COMMENT '擅长的竞赛分类',
  FOREIGN KEY (`judge_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评委擅长类别表';

-- 竞赛评委关联表（某个具体竞赛实际选中的评分员，管理员在审核校赛时配置，至少选择3人）
CREATE TABLE IF NOT EXISTS `competition_judge` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `competition_id` INT NOT NULL COMMENT '竞赛ID',
  `judge_id` INT NOT NULL COMMENT '评分员ID',
  FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`),
  FOREIGN KEY (`judge_id`) REFERENCES `user`(`id`),
  UNIQUE KEY `uk_competition_judge` (`competition_id`, `judge_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛评委关联表';

-- 评分任务表
CREATE TABLE IF NOT EXISTS `score_task` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
  `competition_id` INT NOT NULL COMMENT '竞赛ID',
  `judge_id` INT NOT NULL COMMENT '评分员ID',
  `work_id` INT NOT NULL COMMENT '作品ID',
  `status` INT DEFAULT 0 COMMENT '状态：0-待评分，1-已评分',
  `deadline` DATETIME COMMENT '截止时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`),
  FOREIGN KEY (`judge_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`work_id`) REFERENCES `work`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分任务表';

-- 评分表
CREATE TABLE IF NOT EXISTS `score` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '评分ID',
  `score_task_id` INT NOT NULL COMMENT '评分任务ID',
  `work_id` INT NOT NULL COMMENT '作品ID',
  `judge_id` INT NOT NULL COMMENT '评分员ID',
  `total_score` DECIMAL(5,2) COMMENT '总分',
  `score_details` TEXT COMMENT '评分详情（JSON格式）',
  `comment` TEXT COMMENT '评语',
  `score_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`score_task_id`) REFERENCES `score_task`(`id`),
  FOREIGN KEY (`work_id`) REFERENCES `work`(`id`),
  FOREIGN KEY (`judge_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分表';

-- 获奖记录表
CREATE TABLE IF NOT EXISTS `award` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '获奖ID',
  `competition_id` INT NOT NULL COMMENT '竞赛ID',
  `registration_id` INT NOT NULL COMMENT '报名ID',
  `user_id` INT COMMENT '获奖者ID（个人赛）',
  `team_id` INT COMMENT '团队ID（团队赛）',
  `award_level` VARCHAR(50) COMMENT '奖项等级：一等奖/二等奖/三等奖/优秀奖',
  `rank` INT COMMENT '排名',
  `score` DECIMAL(5,2) COMMENT '得分',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `certificate_number` VARCHAR(50) COMMENT '证书编号',
  FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`),
  FOREIGN KEY (`registration_id`) REFERENCES `registration`(`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`team_id`) REFERENCES `team`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='获奖记录表';

-- 消息表
CREATE TABLE IF NOT EXISTS `message` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
  `user_id` INT COMMENT '接收用户ID（NULL表示全体）',
  `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
  `content` TEXT COMMENT '消息内容',
  `type` VARCHAR(50) COMMENT '消息类型：报名通知/缴费提醒/获奖通知等',
  `is_read` INT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 系统参数表
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
  `config_value` TEXT COMMENT '配置值',
  `description` VARCHAR(255) COMMENT '描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数表';

-- 插入初始管理员账号（密码：admin123）
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`) VALUES
('admin', 'admin123', '系统管理员', 'admin', 1);





