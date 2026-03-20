-- 修复管理员用户信息
USE Competition;

-- 确保admin用户的real_name字段正确
UPDATE `user` SET `real_name` = '系统管理员' WHERE `username` = 'admin' AND `role` = 'admin';

-- 如果admin用户不存在，创建它
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`) 
VALUES ('admin', 'admin123', '系统管理员', 'admin', 1)
ON DUPLICATE KEY UPDATE `real_name` = '系统管理员', `role` = 'admin', `status` = 1;





