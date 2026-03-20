-- 批量更新0元报名费的记录为已缴费状态
-- 将报名费为0且缴费状态为未缴费的记录，自动更新为已缴费

USE Competition;

-- 更新报名费为0且未缴费的记录
-- 将缴费状态设置为已缴费，缴费时间设置为更新时间（如果更新时间为空则使用创建时间）
UPDATE `registration`
SET 
    `payment_status` = 1,  -- 已缴费
    `payment_time` = COALESCE(`update_time`, `create_time`, NOW())  -- 使用更新时间或创建时间，如果都为空则使用当前时间
WHERE 
    `deleted` = 0  -- 未删除的记录
    AND `payment_amount` = 0  -- 报名费为0
    AND `payment_status` = 0  -- 当前状态为未缴费
    AND `status` = 1;  -- 只更新已通过审核的记录（可选，如果希望包括待审核的也可以去掉这行）

-- 查看更新结果
SELECT 
    COUNT(*) AS updated_count,
    '已更新0元报名费且未缴费的记录为已缴费' AS message
FROM `registration`
WHERE 
    `deleted` = 0
    AND `payment_amount` = 0
    AND `payment_status` = 1
    AND `status` = 1;
