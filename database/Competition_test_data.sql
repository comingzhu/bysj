-- 测试数据插入脚本
-- 使用前请先执行 Competition_database_design.sql

USE Competition;

-- 1. 插入用户数据（4个角色）
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `email`, `phone`, `student_id`, `id_card`, `department`, `major`, `grade`, `class`) VALUES
-- 管理员
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin', 'admin@competition.edu', '13800138000', NULL, NULL, '信息中心', NULL, NULL, NULL),

-- 老师
('teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张老师', 'teacher', 'zhang@competition.edu', '13800138001', NULL, NULL, '计算机学院', '软件工程', NULL, NULL),
('teacher2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李老师', 'teacher', 'li@competition.edu', '13800138002', NULL, NULL, '计算机学院', '计算机科学', NULL, NULL),

-- 评分员
('judge1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '王评分员', 'judge', 'wang@competition.edu', '13800138003', NULL, NULL, '艺术学院', '美术设计', NULL, NULL),
('judge2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '赵评分员', 'judge', 'zhao@competition.edu', '13800138004', NULL, NULL, '艺术学院', '摄影', NULL, NULL),

-- 学生
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张三', 'student', 'zhangsan@student.edu', '13800138005', '2021001', '110101200001011234', '计算机学院', '软件工程', '2021级', '软件1班'),
('student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李四', 'student', 'lisi@student.edu', '13800138006', '2021002', '110101200001022345', '计算机学院', '软件工程', '2021级', '软件1班'),
('student3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '王五', 'student', 'wangwu@student.edu', '13800138007', '2021003', '110101200001033456', '计算机学院', '计算机科学', '2021级', '计科1班'),
('student4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '赵六', 'student', 'zhaoliu@student.edu', '13800138008', '2021004', '110101200001044567', '艺术学院', '美术设计', '2021级', '美术1班'),
('student5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '钱七', 'student', 'qianqi@student.edu', '13800138009', '2021005', '110101200001055678', '艺术学院', '摄影', '2021级', '摄影1班');

-- 2. 插入竞赛类别数据
INSERT INTO `competition_category` (`name`, `description`, `sort_order`) VALUES
('程序设计类', '包括蓝桥杯、ACM等程序设计竞赛', 1),
('数学建模类', '包括全国大学生数学建模竞赛等', 2),
('创新创业类', '包括互联网+、挑战杯等创新创业竞赛', 3),
('艺术设计类', '包括美术、摄影、设计等艺术类竞赛', 4),
('学科竞赛类', '包括英语、物理、化学等学科竞赛', 5);

-- 3. 插入竞赛数据
INSERT INTO `competition` (`category_id`, `title`, `description`, `competition_type`, `team_size`, `need_work`, `official_url`, `registration_fee`, `max_participants`, `registration_start_time`, `registration_end_time`, `competition_start_time`, `competition_end_time`, `scoring_start_time`, `scoring_end_time`, `organizer`, `publisher_id`, `status`, `is_pushed`) VALUES
-- 个人赛（程序设计类）
(1, '第十五届蓝桥杯全国软件和信息技术专业人才大赛', '蓝桥杯全国软件和信息技术专业人才大赛是由工业和信息化部人才交流中心举办的全国性IT学科赛事，旨在促进软件和信息领域专业技术人才培养，向软件和信息技术行业输送具有创新能力和实践能力的高端人才。', 'personal', 1, 0, 'https://www.lanqiao.cn', 50.00, 100, '2025-01-01 00:00:00', '2025-03-31 23:59:59', '2025-04-15 09:00:00', '2025-04-15 12:00:00', NULL, NULL, '工业和信息化部人才交流中心', 2, 'registration', 1),

-- 个人赛（数学建模类）
(2, '2025年全国大学生数学建模竞赛', '全国大学生数学建模竞赛创办于1992年，每年一届，已成为全国高校规模最大的基础性学科竞赛，也是世界上规模最大的数学建模竞赛。', 'personal', 1, 1, 'https://www.mcm.edu.cn', 100.00, 50, '2025-02-01 00:00:00', '2025-05-31 23:59:59', '2025-06-01 09:00:00', '2025-06-03 09:00:00', '2025-06-04 09:00:00', '2025-06-10 18:00:00', '中国工业与应用数学学会', 2, 'published', 1),

-- 团队赛（创新创业类）
(3, '第十一届中国国际"互联网+"大学生创新创业大赛', '中国国际"互联网+"大学生创新创业大赛是由教育部等中央部门与地方政府联合主办，旨在深化高等教育综合改革，激发大学生的创造力，培养造就"大众创业、万众创新"的生力军。', 'team', 3, 1, 'https://cy.ncss.cn', 200.00, 30, '2025-03-01 00:00:00', '2025-06-30 23:59:59', '2025-07-01 09:00:00', '2025-07-03 18:00:00', '2025-07-04 09:00:00', '2025-07-10 18:00:00', '教育部', 3, 'published', 0),

-- 个人赛（艺术设计类）
(4, '2025年全国大学生美术作品大赛', '全国大学生美术作品大赛旨在展示当代大学生的艺术创作水平，促进美术教育的发展，为大学生提供一个展示才华的平台。', 'personal', 1, 1, NULL, 80.00, 40, '2025-01-15 00:00:00', '2025-04-15 23:59:59', '2025-04-20 09:00:00', '2025-04-25 18:00:00', '2025-04-26 09:00:00', '2025-04-30 18:00:00', '中国美术家协会', 3, 'registration', 0),

-- 个人赛（艺术设计类-摄影）
(4, '2025年全国大学生摄影大赛', '全国大学生摄影大赛旨在展示当代大学生的摄影艺术水平，促进摄影教育的发展，为大学生提供一个展示摄影才华的平台。', 'personal', 1, 1, NULL, 60.00, 30, '2025-02-01 00:00:00', '2025-05-01 23:59:59', '2025-05-05 09:00:00', '2025-05-10 18:00:00', '2025-05-11 09:00:00', '2025-05-15 18:00:00', '中国摄影家协会', 3, 'registration', 0);

-- 4. 插入团队数据（为团队赛创建团队）
INSERT INTO `team` (`competition_id`, `team_name`, `team_slogan`, `captain_id`, `max_members`, `current_members`, `status`) VALUES
(3, '创新先锋队', '创新引领未来，创业成就梦想', 6, 3, 1, 'forming'),
(3, '梦想启航队', '青春无畏，逐梦扬威', 7, 3, 1, 'forming');

-- 5. 插入团队成员数据
INSERT INTO `team_member` (`team_id`, `user_id`) VALUES
(1, 6),
(2, 7);

-- 6. 插入报名数据
INSERT INTO `registration` (`competition_id`, `user_id`, `team_id`, `registration_info`, `status`, `auditor_id`, `audit_time`, `audit_remark`, `payment_time`) VALUES
-- 个人赛报名
(1, 6, NULL, '{"name":"张三","studentId":"2021001","idCard":"110101200001011234","phone":"13800138005","email":"zhangsan@student.edu"}', 'approved', 1, '2025-02-01 10:00:00', '审核通过', '2025-02-01 14:30:00'),
(1, 7, NULL, '{"name":"李四","studentId":"2021002","idCard":"110101200001022345","phone":"13800138006","email":"lisi@student.edu"}', 'approved', 1, '2025-02-02 10:00:00', '审核通过', '2025-02-02 15:20:00'),
(2, 8, NULL, '{"name":"王五","studentId":"2021003","idCard":"110101200001033456","phone":"13800138007","email":"wangwu@student.edu"}', 'pending', NULL, NULL, NULL, NULL),
(4, 9, NULL, '{"name":"赵六","studentId":"2021004","idCard":"110101200001044567","phone":"13800138008","email":"zhaoliu@student.edu"}', 'approved', 1, '2025-02-03 11:00:00', '审核通过', '2025-02-03 16:40:00'),
(5, 10, NULL, '{"name":"钱七","studentId":"2021005","idCard":"110101200001055678","phone":"13800138009","email":"qianqi@student.edu"}', 'approved', 1, '2025-02-04 09:00:00', '审核通过', '2025-02-04 14:10:00'),

-- 团队赛报名
(3, 6, 1, '{"name":"张三","studentId":"2021001","idCard":"110101200001011234","phone":"13800138005","email":"zhangsan@student.edu"}', 'approved', 1, '2025-02-05 10:00:00', '审核通过', '2025-02-05 15:30:00'),
(3, 7, 2, '{"name":"李四","studentId":"2021002","idCard":"110101200001022345","phone":"13800138006","email":"lisi@student.edu"}', 'pending', NULL, NULL, NULL, NULL);

-- 7. 插入作品数据
INSERT INTO `work` (`competition_id`, `registration_id`, `team_id`, `user_id`, `work_title`, `work_description`, `work_files`, `status`) VALUES
(2, 3, NULL, 8, '基于机器学习的智能推荐系统', '本作品基于机器学习算法，设计并实现了一个智能推荐系统，能够根据用户的历史行为和偏好，为用户推荐个性化的内容。', '{"mainFile":"/uploads/works/recommendation_system.pdf","codeFile":"/uploads/works/recommendation_code.zip","demoVideo":"/uploads/works/recommendation_demo.mp4"}', 'submitted'),
(3, 6, 1, 6, '智能校园服务平台', '本作品是一个面向大学生的智能校园服务平台，整合了校园生活、学习、就业等多个方面的功能，为大学生提供一站式服务。', '{"mainFile":"/uploads/works/campus_platform.pdf","codeFile":"/uploads/works/campus_code.zip","demoVideo":"/uploads/works/campus_demo.mp4"}', 'submitted'),
(4, 4, NULL, 9, '青春记忆', '这是一幅描绘大学生校园生活的油画作品，展现了青春的活力和美好。', '{"mainFile":"/uploads/works/youth_memory.jpg","detailImages":["/uploads/works/youth_detail1.jpg","/uploads/works/youth_detail2.jpg"]}', 'submitted'),
(5, 5, NULL, 10, '城市之光', '这是一组城市夜景摄影作品，展现了城市的繁华与美丽。', '{"mainFile":"/uploads/works/city_light.jpg","detailImages":["/uploads/works/city_detail1.jpg","/uploads/works/city_detail2.jpg","/uploads/works/city_detail3.jpg"]}', 'submitted');

-- 8. 插入评分数据
INSERT INTO `scoring` (`work_id`, `judge_id`, `score`, `comment`) VALUES
(1, 4, 88.5, '作品思路清晰，算法选择合理，实现完整，具有良好的实用价值。建议进一步优化推荐算法的准确率。'),
(1, 5, 92.0, '创新性强，技术实现扎实，文档完整，演示效果好。是一个优秀的作品。'),
(2, 4, 85.0, '平台功能完整，用户体验良好，但在某些细节上还有优化空间。'),
(2, 5, 87.5, '作品整体质量不错，团队协作能力强，建议增加更多个性化功能。'),
(3, 4, 90.0, '色彩运用出色，构图优美，很好地表现了青春的主题。'),
(3, 5, 88.0, '作品情感丰富，技法娴熟，是一幅优秀的油画作品。'),
(4, 4, 86.5, '摄影技巧娴熟，光影处理得当，很好地展现了城市的夜景。'),
(4, 5, 89.0, '作品构图精美，色彩搭配和谐，具有很强的视觉冲击力。');

-- 9. 插入缴费数据
INSERT INTO `payment` (`registration_id`, `competition_id`, `user_id`, `amount`, `payment_method`, `payment_status`, `transaction_id`, `payment_time`) VALUES
(1, 1, 6, 50.00, 'wechat', 'paid', 'WX202502011430001', '2025-02-01 14:30:00'),
(2, 1, 7, 50.00, 'alipay', 'paid', 'ALI202502021520002', '2025-02-02 15:20:00'),
(4, 4, 9, 80.00, 'wechat', 'paid', 'WX202502031640003', '2025-02-03 16:40:00'),
(5, 5, 10, 60.00, 'wechat', 'paid', 'WX202502041410004', '2025-02-04 14:10:00'),
(6, 3, 6, 200.00, 'alipay', 'paid', 'ALI202502051530005', '2025-02-05 15:30:00');

-- 10. 插入消息数据
INSERT INTO `message` (`title`, `content`, `message_type`, `sender_id`, `receiver_id`, `competition_id`, `is_read`, `read_time`) VALUES
-- 竞赛消息
('第十五届蓝桥杯大赛报名开始', '第十五届蓝桥杯全国软件和信息技术专业人才大赛现已开始报名，欢迎各位同学踊跃参加！报名截止时间：2025年3月31日。', 'competition', 1, NULL, 1, 0, NULL),
('2025年全国大学生数学建模竞赛报名通知', '2025年全国大学生数学建模竞赛现已开始报名，欢迎各位同学踊跃参加！报名截止时间：2025年5月31日。', 'competition', 1, NULL, 2, 0, NULL),

-- 缴费消息
('您的报名已通过审核，请及时缴费', '您报名的"第十五届蓝桥杯全国软件和信息技术专业人才大赛"已通过审核，请及时缴纳报名费50元。', 'payment', 1, 6, 1, 1, '2025-02-01 10:30:00'),
('您的报名已通过审核，请及时缴费', '您报名的"第十五届蓝桥杯全国软件和信息技术专业人才大赛"已通过审核，请及时缴纳报名费50元。', 'payment', 1, 7, 1, 1, '2025-02-02 11:00:00'),

-- 系统消息
('系统维护通知', '系统将于2025年2月10日凌晨2:00-4:00进行维护，期间系统将无法访问，请提前做好准备。', 'system', 1, NULL, NULL, 0, NULL),

-- 获奖消息
('恭喜您获得一等奖！', '恭喜您在"第十五届蓝桥杯全国软件和信息技术专业人才大赛"中获得一等奖！', 'award', 1, 6, 1, 1, '2025-02-01 20:00:00');

-- 11. 插入获奖数据
INSERT INTO `award` (`competition_id`, `registration_id`, `team_id`, `user_id`, `award_level`, `award_name`, `certificate_url`, `award_time`, `publisher_id`) VALUES
(1, 1, NULL, 6, 'first', '一等奖', '/certificates/award_1_1.pdf', '2025-02-01 18:00:00', 1),
(1, 2, NULL, 7, 'second', '二等奖', '/certificates/award_1_2.pdf', '2025-02-01 18:00:00', 1),
(2, 3, NULL, 8, 'excellent', '优秀奖', '/certificates/award_2_1.pdf', '2025-02-05 18:00:00', 1),
(4, 4, NULL, 9, 'first', '一等奖', '/certificates/award_4_1.pdf', '2025-02-06 18:00:00', 1),
(5, 5, NULL, 10, 'second', '二等奖', '/certificates/award_5_1.pdf', '2025-02-06 18:00:00', 1);

-- 12. 插入竞赛统计数据
INSERT INTO `competition_statistics` (`competition_id`, `total_registrations`, `paid_registrations`, `completed_registrations`, `total_teams`, `total_works`, `average_score`) VALUES
(1, 2, 2, 2, 0, 0, NULL),
(2, 1, 0, 0, 0, 1, 90.25),
(3, 1, 1, 0, 2, 1, 86.25),
(4, 1, 1, 0, 0, 1, 89.00),
(5, 1, 1, 0, 0, 1, 87.75);

-- 更新竞赛的当前参与人数
UPDATE `competition` SET `current_participants` = 2 WHERE `id` = 1;
UPDATE `competition` SET `current_participants` = 1 WHERE `id` = 2;
UPDATE `competition` SET `current_participants` = 1 WHERE `id` = 3;
UPDATE `competition` SET `current_participants` = 1 WHERE `id` = 4;
UPDATE `competition` SET `current_participants` = 1 WHERE `id` = 5;

-- 查询测试数据
SELECT '用户数据' AS '数据表', COUNT(*) AS '记录数' FROM `user`
UNION ALL
SELECT '竞赛类别', COUNT(*) FROM `competition_category`
UNION ALL
SELECT '竞赛', COUNT(*) FROM `competition`
UNION ALL
SELECT '团队', COUNT(*) FROM `team`
UNION ALL
SELECT '团队成员', COUNT(*) FROM `team_member`
UNION ALL
SELECT '报名', COUNT(*) FROM `registration`
UNION ALL
SELECT '作品', COUNT(*) FROM `work`
UNION ALL
SELECT '评分', COUNT(*) FROM `scoring`
UNION ALL
SELECT '缴费', COUNT(*) FROM `payment`
UNION ALL
SELECT '消息', COUNT(*) FROM `message`
UNION ALL
SELECT '获奖', COUNT(*) FROM `award`
UNION ALL
SELECT '竞赛统计', COUNT(*) FROM `competition_statistics`;
