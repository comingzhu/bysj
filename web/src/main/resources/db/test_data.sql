-- 测试数据脚本
-- 注意：执行前请先执行 schema.sql 创建表结构

USE Competition;

-- 清空现有数据（保留管理员账号）
DELETE FROM award WHERE id > 0;
DELETE FROM score WHERE id > 0;
DELETE FROM score_task WHERE id > 0;
DELETE FROM work WHERE id > 0;
DELETE FROM team_member WHERE id > 0;
DELETE FROM team WHERE id > 0;
DELETE FROM registration WHERE id > 0;
DELETE FROM message WHERE id > 0;
DELETE FROM competition WHERE id > 0;
DELETE FROM system_config WHERE id > 0;
DELETE FROM user WHERE id > 1; -- 保留管理员账号

-- 1. 插入用户数据
-- 老师
INSERT INTO `user` (`username`, `password`, `real_name`, `teacher_no`, `role`, `email`, `phone`, `college`, `status`) VALUES
('teacher001', '123456', '张教授', 'T001', 'teacher', 'zhang@university.edu.cn', '13800001001', '计算机学院', 1),
('teacher002', '123456', '李老师', 'T002', 'teacher', 'li@university.edu.cn', '13800001002', '软件学院', 1),
('teacher003', '123456', '王老师', 'T003', 'teacher', 'wang@university.edu.cn', '13800001003', '信息学院', 1);

-- 评分员
INSERT INTO `user` (`username`, `password`, `real_name`, `teacher_no`, `role`, `email`, `phone`, `college`, `status`) VALUES
('judge001', '123456', '赵评委', 'J001', 'judge', 'zhao@university.edu.cn', '13800002001', '计算机学院', 1),
('judge002', '123456', '钱评委', 'J002', 'judge', 'qian@university.edu.cn', '13800002002', '软件学院', 1),
('judge003', '123456', '孙评委', 'J003', 'judge', 'sun@university.edu.cn', '13800002003', '信息学院', 1),
('judge004', '123456', '李评委', 'J004', 'judge', 'li@university.edu.cn', '13800002004', '管理学院', 1),
('judge005', '123456', '周评委', 'J005', 'judge', 'zhou@university.edu.cn', '13800002005', '经济学院', 1),
('judge006', '123456', '吴评委', 'J006', 'judge', 'wu@university.edu.cn', '13800002006', '计算机学院', 1),
('judge007', '123456', '郑评委', 'J007', 'judge', 'zheng@university.edu.cn', '13800002007', '软件学院', 1),
('judge008', '123456', '王评委', 'J008', 'judge', 'wang@university.edu.cn', '13800002008', '信息学院', 1);

-- 评分员擅长类别映射（基于 competition_category：程序设计,算法竞赛,创新创业,数学建模,英语竞赛,体育竞赛）
INSERT INTO `judge_category` (`judge_id`, `category`) VALUES
((SELECT id FROM `user` WHERE username = 'judge001'), '程序设计'),
((SELECT id FROM `user` WHERE username = 'judge001'), '算法竞赛'),
((SELECT id FROM `user` WHERE username = 'judge002'), '程序设计'),
((SELECT id FROM `user` WHERE username = 'judge002'), '创新创业'),
((SELECT id FROM `user` WHERE username = 'judge003'), '数学建模'),
((SELECT id FROM `user` WHERE username = 'judge003'), '英语竞赛'),
((SELECT id FROM `user` WHERE username = 'judge004'), '创新创业'),
((SELECT id FROM `user` WHERE username = 'judge004'), '体育竞赛'),
((SELECT id FROM `user` WHERE username = 'judge005'), '数学建模'),
((SELECT id FROM `user` WHERE username = 'judge005'), '英语竞赛'),
((SELECT id FROM `user` WHERE username = 'judge005'), '体育竞赛'),
((SELECT id FROM `user` WHERE username = 'judge006'), '程序设计'),
((SELECT id FROM `user` WHERE username = 'judge006'), '算法竞赛'),
((SELECT id FROM `user` WHERE username = 'judge006'), '体育竞赛'),
((SELECT id FROM `user` WHERE username = 'judge007'), '程序设计'),
((SELECT id FROM `user` WHERE username = 'judge007'), '算法竞赛'),
((SELECT id FROM `user` WHERE username = 'judge007'), '数学建模'),
((SELECT id FROM `user` WHERE username = 'judge007'), '英语竞赛'),
((SELECT id FROM `user` WHERE username = 'judge008'), '程序设计'),
((SELECT id FROM `user` WHERE username = 'judge008'), '创新创业'),
((SELECT id FROM `user` WHERE username = 'judge008'), '数学建模'),
((SELECT id FROM `user` WHERE username = 'judge008'), '英语竞赛'),
((SELECT id FROM `user` WHERE username = 'judge008'), '体育竞赛');

-- 学生（计算机学院）
INSERT INTO `user` (`username`, `password`, `real_name`, `student_no`, `role`, `email`, `phone`, `college`, `major`, `class_name`, `grade`, `id_card`, `status`) VALUES
('student001', '123456', '张三', '2021001001', 'student', 'zhangsan@student.edu.cn', '13900001001', '计算机学院', '计算机科学与技术', '计科2101', '2021', '110101200001011234', 1),
('student002', '123456', '李四', '2021001002', 'student', 'lisi@student.edu.cn', '13900001002', '计算机学院', '计算机科学与技术', '计科2101', '2021', '110101200001011235', 1),
('student003', '123456', '王五', '2021001003', 'student', 'wangwu@student.edu.cn', '13900001003', '计算机学院', '软件工程', '软工2101', '2021', '110101200001011236', 1),
('student004', '123456', '赵六', '2022001001', 'student', 'zhaoliu@student.edu.cn', '13900001004', '计算机学院', '计算机科学与技术', '计科2201', '2022', '110101200002011237', 1),
('student005', '123456', '钱七', '2022001002', 'student', 'qianqi@student.edu.cn', '13900001005', '计算机学院', '软件工程', '软工2201', '2022', '110101200002011238', 1);

-- 学生（软件学院）
INSERT INTO `user` (`username`, `password`, `real_name`, `student_no`, `role`, `email`, `phone`, `college`, `major`, `class_name`, `grade`, `id_card`, `status`) VALUES
('student006', '123456', '孙八', '2021002001', 'student', 'sunba@student.edu.cn', '13900001006', '软件学院', '软件工程', '软工2101', '2021', '110101200001011239', 1),
('student007', '123456', '周九', '2021002002', 'student', 'zhoujiu@student.edu.cn', '13900001007', '软件学院', '软件工程', '软工2101', '2021', '110101200001011240', 1),
('student008', '123456', '吴十', '2022002001', 'student', 'wushi@student.edu.cn', '13900001008', '软件学院', '软件工程', '软工2201', '2022', '110101200002011241', 1),
('student009', '123456', '郑十一', '2022002002', 'student', 'zhengshiyi@student.edu.cn', '13900001009', '软件学院', '软件工程', '软工2201', '2022', '110101200002011242', 1);

-- 学生（信息学院）
INSERT INTO `user` (`username`, `password`, `real_name`, `student_no`, `role`, `email`, `phone`, `college`, `major`, `class_name`, `grade`, `id_card`, `status`) VALUES
('student010', '123456', '冯十二', '2021003001', 'student', 'fengshier@student.edu.cn', '13900001010', '信息学院', '信息管理与信息系统', '信管2101', '2021', '110101200001011243', 1),
('student011', '123456', '陈十三', '2021003002', 'student', 'chenshisan@student.edu.cn', '13900001011', '信息学院', '信息管理与信息系统', '信管2101', '2021', '110101200001011244', 1),
('student012', '123456', '楚十四', '2022003001', 'student', 'chushisi@student.edu.cn', '13900001012', '信息学院', '信息管理与信息系统', '信管2201', '2022', '110101200002011245', 1);

-- 2. 插入系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('default_registration_fee', '50.00', '默认报名费标准'),
('payment_notice_template', '您的报名已通过审核，请及时缴纳报名费 ¥{amount}，缴费截止时间：{deadline}', '缴费通知模板'),
('award_notice_template', '恭喜您在{competition}竞赛中获得{level}，请及时查看获奖详情', '获奖通知模板'),
('competition_category', '程序设计,算法竞赛,创新创业,数学建模,英语竞赛,体育竞赛', '竞赛分类'),
('award_mode_description', '0=单一名额(1个一二三等奖);1=按比例评奖(如前10%)', '获奖规则说明'),
('award_ratio_default', '{"first":0.10,"second":0.20,"third":0.30}', '默认按比例评奖比例配置'),
('max_team_members', '5', '团队最大成员数'),
('college_list', '计算机学院,软件学院,信息学院,管理学院,经济学院', '学院列表');

-- 3. 插入竞赛数据
-- 校赛（老师发布）- 2025年
INSERT INTO `competition` (`name`, `description`, `type`, `need_work`, `registration_fee`, `start_time`, `end_time`, `registration_start`, `registration_end`, `location`, `category`, `award_mode`, `first_award_ratio`, `second_award_ratio`, `third_award_ratio`, `publisher_id`, `status`, `is_system`) VALUES
('2025年程序设计大赛', '面向全校学生的程序设计竞赛，考察算法和编程能力', 0, 0, 30.00, '2025-06-01 09:00:00', '2025-06-01 17:00:00', '2025-05-01 00:00:00', '2025-05-25 23:59:59', '计算机学院机房', '程序设计', 0, 0.10, 0.20, 0.30, 2, 4, 0),
('2025年创新创业大赛', '鼓励学生创新创业，提交商业计划书和项目展示', 1, 1, 50.00, '2025-07-01 09:00:00', '2025-07-01 17:00:00', '2025-06-01 00:00:00', '2025-06-25 23:59:59', '创新创业中心', '创新创业', 0, 0.10, 0.20, 0.30, 2, 4, 0),
('2025年数学建模竞赛', '三人组队参加，提交数学建模论文', 1, 1, 40.00, '2025-08-01 09:00:00', '2025-08-03 17:00:00', '2025-07-01 00:00:00', '2025-07-25 23:59:59', '数学学院', '数学建模', 1, 0.10, 0.20, 0.30, 3, 2, 0),
('2025年网页设计大赛', '个人或团队参赛，提交网页设计作品', 1, 1, 35.00, '2025-09-01 09:00:00', '2025-09-01 17:00:00', '2025-08-01 00:00:00', '2025-08-25 23:59:59', '软件学院', '程序设计', 1, 0.10, 0.20, 0.30, 3, 1, 0);

-- 省赛（系统推送）- 2025-2026年
INSERT INTO `competition` (`name`, `description`, `type`, `need_work`, `registration_fee`, `start_time`, `end_time`, `registration_start`, `registration_end`, `location`, `category`, `award_mode`, `first_award_ratio`, `second_award_ratio`, `third_award_ratio`, `publisher_id`, `status`, `is_system`) VALUES
('2025年蓝桥杯全国软件和信息技术专业人才大赛', '由工业和信息化部人才交流中心主办的全国性IT学科竞赛', 0, 0, 300.00, '2025-04-13 09:00:00', '2025-04-13 13:00:00', '2025-03-01 00:00:00', '2025-03-31 23:59:59', '各高校考点', '算法竞赛', 1, 0.10, 0.20, 0.30, NULL, 4, 1),
('2025年全国大学生数学建模竞赛', '面向全国大学生的数学建模竞赛', 1, 1, 200.00, '2025-09-12 08:00:00', '2025-09-15 20:00:00', '2025-08-01 00:00:00', '2025-08-31 23:59:59', '各高校', '数学建模', 1, 0.10, 0.20, 0.30, NULL, 4, 1),
('2026年"互联网+"大学生创新创业大赛', '教育部主办的全国性创新创业竞赛', 1, 1, 0.00, '2026-10-01 09:00:00', '2026-10-01 17:00:00', '2026-09-01 00:00:00', '2026-09-20 23:59:59', '各高校', '创新创业', 1, 0.10, 0.20, 0.30, NULL, 4, 1);

-- 3.1 为每个竞赛配置至少3名评委（通过 competition_judge 表），这里示范性地绑定3个评委
-- 依赖于上面 competition 插入顺序，竞赛ID 从1开始自增
INSERT INTO `competition_judge` (`competition_id`, `judge_id`) VALUES
(1, (SELECT id FROM `user` WHERE username = 'judge001')),
(1, (SELECT id FROM `user` WHERE username = 'judge002')),
(1, (SELECT id FROM `user` WHERE username = 'judge003')),
(1, (SELECT id FROM `user` WHERE username = 'judge006')),
(1, (SELECT id FROM `user` WHERE username = 'judge007')), -- 程序设计大赛：5名评委
(2, (SELECT id FROM `user` WHERE username = 'judge001')),
(2, (SELECT id FROM `user` WHERE username = 'judge002')),
(2, (SELECT id FROM `user` WHERE username = 'judge004')),
(2, (SELECT id FROM `user` WHERE username = 'judge006')),
(2, (SELECT id FROM `user` WHERE username = 'judge008')), -- 创新创业大赛：5名评委
(3, (SELECT id FROM `user` WHERE username = 'judge003')),
(3, (SELECT id FROM `user` WHERE username = 'judge005')),
(3, (SELECT id FROM `user` WHERE username = 'judge007')),
(3, (SELECT id FROM `user` WHERE username = 'judge008')), -- 数学建模竞赛：4名评委
(4, (SELECT id FROM `user` WHERE username = 'judge001')),
(4, (SELECT id FROM `user` WHERE username = 'judge002')),
(4, (SELECT id FROM `user` WHERE username = 'judge006')),
(4, (SELECT id FROM `user` WHERE username = 'judge007')),
(4, (SELECT id FROM `user` WHERE username = 'judge008')), -- 网页设计大赛：5名评委
(5, (SELECT id FROM `user` WHERE username = 'judge001')),
(5, (SELECT id FROM `user` WHERE username = 'judge002')),
(5, (SELECT id FROM `user` WHERE username = 'judge006')),
(5, (SELECT id FROM `user` WHERE username = 'judge007')),
(5, (SELECT id FROM `user` WHERE username = 'judge008')), -- 蓝桥杯：5名评委
(6, (SELECT id FROM `user` WHERE username = 'judge003')),
(6, (SELECT id FROM `user` WHERE username = 'judge005')),
(6, (SELECT id FROM `user` WHERE username = 'judge007')),
(6, (SELECT id FROM `user` WHERE username = 'judge008')), -- 全国大学生数学建模竞赛：4名评委
(7, (SELECT id FROM `user` WHERE username = 'judge002')),
(7, (SELECT id FROM `user` WHERE username = 'judge004')),
(7, (SELECT id FROM `user` WHERE username = 'judge005')),
(7, (SELECT id FROM `user` WHERE username = 'judge006')),
(7, (SELECT id FROM `user` WHERE username = 'judge008')); -- “互联网+”大赛：5名评委

-- 4. 插入报名记录（只允许学生报名）
-- 个人赛报名（程序设计大赛）- 使用学生ID：8, 9, 10, 11
INSERT INTO `registration` (`competition_id`, `user_id`, `status`, `payment_status`, `payment_amount`, `payment_time`, `payment_voucher`) VALUES
(1, 8, 1, 1, 30.00, '2025-05-15 10:30:00', '模拟支付凭证-20250515103000'),
(1, 9, 1, 1, 30.00, '2025-05-16 14:20:00', '模拟支付凭证-20250516142000'),
(1, 10, 1, 0, 30.00, NULL, NULL),
(1, 11, 0, 0, 30.00, NULL, NULL);

-- 个人赛报名（蓝桥杯）- 使用学生ID：8, 9, 10
INSERT INTO `registration` (`competition_id`, `user_id`, `status`, `payment_status`, `payment_amount`, `payment_time`, `payment_voucher`) VALUES
(5, 8, 1, 1, 300.00, '2025-03-15 09:00:00', '模拟支付凭证-20250315090000'),
(5, 9, 1, 1, 300.00, '2025-03-16 10:30:00', '模拟支付凭证-20250316103000'),
(5, 10, 1, 0, 300.00, NULL, NULL);

-- 团队赛报名（创新创业大赛）- 使用学生ID：8, 9, 13, 14, 15
-- 先创建团队
INSERT INTO `team` (`name`, `competition_id`, `leader_id`, `max_members`, `current_members`, `status`) VALUES
('创新之星团队', 2, 8, 5, 3, 1),
('梦想启航队', 2, 10, 5, 2, 1);

-- 团队成员（全部是学生）
INSERT INTO `team_member` (`team_id`, `user_id`, `status`) VALUES
(1, 8, 1), -- 队长：student001
(1, 9, 1), -- student002
(1, 13, 1), -- student006
(2, 10, 1), -- 队长：student003
(2, 14, 1); -- student007

-- 团队报名记录
INSERT INTO `registration` (`competition_id`, `team_id`, `status`, `payment_status`, `payment_amount`, `payment_time`, `payment_voucher`) VALUES
(2, 1, 1, 1, 50.00, '2025-06-10 11:00:00', '模拟支付凭证-20250610110000'),
(2, 2, 1, 0, 50.00, NULL, NULL);

-- 团队赛报名（数学建模竞赛）- 使用学生ID：15, 16, 17
INSERT INTO `team` (`name`, `competition_id`, `leader_id`, `max_members`, `current_members`, `status`) VALUES
('数学建模A队', 3, 15, 3, 3, 1);

INSERT INTO `team_member` (`team_id`, `user_id`, `status`) VALUES
(3, 15, 1), -- student008
(3, 16, 1), -- student009
(3, 17, 1); -- student010

INSERT INTO `registration` (`competition_id`, `team_id`, `status`, `payment_status`, `payment_amount`, `payment_time`, `payment_voucher`) VALUES
(3, 3, 1, 1, 40.00, '2025-07-15 15:30:00', '模拟支付凭证-20250715153000');

-- 5. 插入作品数据（需要提交作品的竞赛）
-- 创新创业大赛作品
INSERT INTO `work` (`competition_id`, `registration_id`, `team_id`, `title`, `description`, `file_path`, `file_name`, `file_size`, `status`) VALUES
(2, 5, 1, '智能校园管理系统', '基于物联网技术的智能校园管理系统，实现设备监控、能耗管理等功能', '/uploads/work_20250610_001.pdf', '智能校园管理系统.pdf', 5242880, 0),
(2, 6, 2, '在线教育平台', '面向K12教育的在线学习平台，包含课程管理、作业批改等功能', '/uploads/work_20250611_001.pdf', '在线教育平台.pdf', 3145728, 0);

-- 数学建模竞赛作品
INSERT INTO `work` (`competition_id`, `registration_id`, `team_id`, `title`, `description`, `file_path`, `file_name`, `file_size`, `status`) VALUES
(3, 7, 3, '城市交通流量优化模型', '基于数据挖掘的城市交通流量预测与优化方案', '/uploads/work_20250720_001.pdf', '城市交通流量优化模型.pdf', 8388608, 0);

-- 6. 插入评分任务和评分记录
-- 为创新创业大赛作品分配评分任务（使用评分员ID：5, 6）
INSERT INTO `score_task` (`competition_id`, `judge_id`, `work_id`, `status`, `deadline`) VALUES
(2, 5, 1, 1, '2025-07-05 23:59:59'),
(2, 6, 1, 1, '2025-07-05 23:59:59'),
(2, 5, 2, 1, '2025-07-05 23:59:59'),
(2, 6, 2, 0, '2025-07-05 23:59:59');

-- 评分记录
INSERT INTO `score` (`score_task_id`, `work_id`, `judge_id`, `total_score`, `score_details`, `comment`, `score_time`) VALUES
(1, 1, 5, 85.50, '{"创新性":25,"实用性":20,"技术性":20,"展示效果":20.5}', '项目创新性强，技术实现完整，展示效果良好', '2025-07-01 10:30:00'),
(2, 1, 6, 88.00, '{"创新性":26,"实用性":22,"技术性":20,"展示效果":20}', '项目具有很好的市场前景，技术方案合理', '2025-07-01 14:20:00'),
(3, 2, 5, 82.00, '{"创新性":24,"实用性":20,"技术性":19,"展示效果":19}', '项目设计合理，但创新性有待提升', '2025-07-02 09:15:00'),
(4, 2, 6, 80.50, '{"创新性":23,"实用性":19.5,"技术性":19,"展示效果":19}', '项目基础扎实，建议加强创新点', '2025-07-02 15:45:00');

-- 更新评分任务状态
UPDATE `score_task` SET `status` = 1 WHERE `id` IN (1, 2, 3, 4);

-- 7. 插入获奖记录
-- 程序设计大赛获奖（个人赛，按分数排名）- 使用学生ID：8, 9
INSERT INTO `award` (`competition_id`, `registration_id`, `user_id`, `award_level`, `rank`, `score`) VALUES
(1, 1, 8, '一等奖', 1, 95.00),
(1, 2, 9, '二等奖', 2, 88.50);

-- 蓝桥杯获奖 - 使用学生ID：8, 9
INSERT INTO `award` (`competition_id`, `registration_id`, `user_id`, `award_level`, `rank`, `score`) VALUES
(5, 4, 8, '一等奖', 1, 92.00),
(5, 5, 9, '三等奖', 3, 78.50);

-- 创新创业大赛获奖（团队赛，按平均分）
INSERT INTO `award` (`competition_id`, `registration_id`, `team_id`, `award_level`, `rank`, `score`) VALUES
(2, 5, 1, '一等奖', 1, 86.75), -- 平均分：(85.5+88)/2
(2, 6, 2, '二等奖', 2, 81.25); -- 平均分：(82+80.5)/2

-- 数学建模竞赛获奖
INSERT INTO `award` (`competition_id`, `registration_id`, `team_id`, `award_level`, `rank`, `score`) VALUES
(3, 7, 3, '二等奖', 2, 85.00);

-- 8. 插入消息数据
-- 缴费通知（使用学生ID：10, 11）
INSERT INTO `message` (`user_id`, `title`, `content`, `type`, `is_read`) VALUES
(10, '缴费通知', '您的报名已通过审核，请及时缴纳报名费 ¥30.00', '缴费提醒', 0),
(11, '缴费通知', '您的报名已通过审核，请及时缴纳报名费 ¥50.00', '缴费提醒', 0),
(8, '缴费通知', '您的报名已通过审核，请及时缴纳报名费 ¥300.00', '缴费提醒', 1);

-- 获奖通知（使用学生ID：8, 9）
INSERT INTO `message` (`user_id`, `title`, `content`, `type`, `is_read`) VALUES
(8, '获奖通知', '恭喜您在2025年程序设计大赛中获得一等奖，请及时查看获奖详情', '获奖通知', 1),
(9, '获奖通知', '恭喜您在2025年程序设计大赛中获得二等奖，请及时查看获奖详情', '获奖通知', 0),
(8, '获奖通知', '恭喜您在2025年蓝桥杯全国软件和信息技术专业人才大赛中获得一等奖，请及时查看获奖详情', '获奖通知', 1),
(9, '获奖通知', '恭喜您在2025年蓝桥杯全国软件和信息技术专业人才大赛中获得三等奖，请及时查看获奖详情', '获奖通知', 0);

-- 系统通知（全体用户）
INSERT INTO `message` (`user_id`, `title`, `content`, `type`, `is_read`) VALUES
(NULL, '系统通知', '2025年蓝桥杯全国软件和信息技术专业人才大赛报名开始，欢迎同学们踊跃报名！', '报名通知', 0),
(NULL, '系统通知', '2026年"互联网+"大学生创新创业大赛即将开始，请关注报名时间', '报名通知', 0);

-- 数据统计说明：
-- 用户：1个管理员 + 3个老师 + 3个评分员 + 12个学生 = 19个用户
-- 竞赛：4个校赛 + 3个省赛 = 7个竞赛（时间均为2025-2026年）
-- 报名：10条报名记录（个人赛7条，团队赛3条）- 全部为学生报名
-- 团队：3个团队，共8个团队成员 - 全部为学生
-- 作品：3个作品
-- 评分：4条评分记录
-- 获奖：7条获奖记录
-- 消息：9条消息
