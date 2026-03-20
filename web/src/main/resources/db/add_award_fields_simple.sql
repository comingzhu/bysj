-- 为 competition 表添加获奖规则相关字段（简化版本）
-- 如果字段已存在，会报错，可以忽略错误继续执行其他语句

USE Competition;

-- 添加获奖规则字段
ALTER TABLE `competition` 
ADD COLUMN `award_mode` INT DEFAULT 0 COMMENT '获奖规则：0-单一名额(1个一二三等奖)，1-按比例评奖(如前10%)' AFTER `category`;

-- 添加一等奖比例字段
ALTER TABLE `competition` 
ADD COLUMN `first_award_ratio` DECIMAL(5,2) DEFAULT 0.10 COMMENT '一等奖比例(0-1，小数)，仅在按比例模式下生效' AFTER `award_mode`;

-- 添加二等奖比例字段
ALTER TABLE `competition` 
ADD COLUMN `second_award_ratio` DECIMAL(5,2) DEFAULT 0.20 COMMENT '二等奖比例(0-1，小数)，仅在按比例模式下生效' AFTER `first_award_ratio`;

-- 添加三等奖比例字段
ALTER TABLE `competition` 
ADD COLUMN `third_award_ratio` DECIMAL(5,2) DEFAULT 0.30 COMMENT '三等奖比例(0-1，小数)，仅在按比例模式下生效' AFTER `second_award_ratio`;
