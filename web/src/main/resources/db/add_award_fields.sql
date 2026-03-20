-- 为 competition 表添加获奖规则相关字段
-- 使用存储过程安全地添加字段（如果不存在）

USE Competition;

-- 创建存储过程来安全添加列
DELIMITER $$

DROP PROCEDURE IF EXISTS AddColumnIfNotExists$$
CREATE PROCEDURE AddColumnIfNotExists(
    IN tableName VARCHAR(64),
    IN columnName VARCHAR(64),
    IN columnDefinition TEXT
)
BEGIN
    DECLARE columnExists INT DEFAULT 0;
    
    SELECT COUNT(*) INTO columnExists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = tableName
      AND COLUMN_NAME = columnName;
    
    IF columnExists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', tableName, '` ADD COLUMN `', columnName, '` ', columnDefinition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

-- 使用存储过程添加字段
CALL AddColumnIfNotExists('competition', 'award_mode', 'INT DEFAULT 0 COMMENT ''获奖规则：0-单一名额(1个一二三等奖)，1-按比例评奖(如前10%)'' AFTER `category`');

CALL AddColumnIfNotExists('competition', 'first_award_ratio', 'DECIMAL(5,2) DEFAULT 0.10 COMMENT ''一等奖比例(0-1，小数)，仅在按比例模式下生效'' AFTER `award_mode`');

CALL AddColumnIfNotExists('competition', 'second_award_ratio', 'DECIMAL(5,2) DEFAULT 0.20 COMMENT ''二等奖比例(0-1，小数)，仅在按比例模式下生效'' AFTER `first_award_ratio`');

CALL AddColumnIfNotExists('competition', 'third_award_ratio', 'DECIMAL(5,2) DEFAULT 0.30 COMMENT ''三等奖比例(0-1，小数)，仅在按比例模式下生效'' AFTER `second_award_ratio`');

-- 删除临时存储过程
DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
