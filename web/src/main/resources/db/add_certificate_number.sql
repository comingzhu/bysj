-- 在award表中添加certificate_number列
ALTER TABLE award ADD COLUMN certificate_number VARCHAR(50) DEFAULT NULL COMMENT '证书编号' AFTER create_time;