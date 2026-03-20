-- 为用户表添加余额字段
USE Competition;

ALTER TABLE `user` ADD COLUMN `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额' AFTER `id_card`;

-- 为现有用户设置初始余额
UPDATE `user` SET `balance` = 0.00 WHERE `balance` IS NULL;




