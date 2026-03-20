# 大学生竞赛活动管理平台 - 数据库使用说明

## 数据库信息
- **数据库名称**: Competition
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci

## 文件说明

### 1. Competition_database_design.sql
这是数据库建表脚本，包含12张核心表：
- **user**: 用户表（存储所有用户信息，支持4种角色）
- **competition_category**: 竞赛类别表（如蓝桥杯、数学建模等）
- **competition**: 竞赛表（存储竞赛详细信息）
- **team**: 团队表（存储团队信息）
- **team_member**: 团队成员表（存储团队成员关系）
- **registration**: 报名表（存储学生报名信息）
- **work**: 作品表（存储学生提交的作品）
- **scoring**: 评分表（存储评分员的评分）
- **payment**: 缴费表（存储缴费信息）
- **message**: 消息表（存储推送消息）
- **award**: 获奖表（存储获奖信息）
- **competition_statistics**: 竞赛统计表（存储竞赛统计数据）

### 2. Competition_test_data.sql
这是测试数据脚本，包含：
- 1个管理员账号
- 2个老师账号
- 2个评分员账号
- 5个学生账号
- 5个竞赛类别
- 5个竞赛（包括个人赛和团队赛）
- 2个团队
- 7个报名记录
- 4个作品
- 8个评分记录
- 5个缴费记录
- 5个消息
- 5个获奖记录
- 5个竞赛统计数据

## 使用步骤

### 步骤1：创建数据库
在MySQL中执行以下命令：
```sql
source d:/hhh/database/Competition_database_design.sql
```

或者在MySQL命令行中：
```bash
mysql -u root -p < d:/hhh/database/Competition_database_design.sql
```

### 步骤2：插入测试数据
数据库创建成功后，执行测试数据脚本：
```sql
source d:/hhh/database/Competition_test_data.sql
```

或者在MySQL命令行中：
```bash
mysql -u root -p Competition < d:/hhh/database/Competition_test_data.sql
```

### 步骤3：验证数据
执行测试数据脚本后，会自动显示各表的记录数统计。

## 测试账号信息

### 管理员账号
- 用户名: admin
- 密码: 123456

### 老师账号
- 用户名: teacher1 / teacher2
- 密码: 123456

### 评分员账号
- 用户名: judge1 / judge2
- 密码: 123456

### 学生账号
- 用户名: student1 / student2 / student3 / student4 / student5
- 密码: 123456

**注意**: 所有密码都经过BCrypt加密，原始密码为123456。

## 数据库设计特点

### 1. 支持四种角色
- **学生**: 可以报名竞赛、组建团队、提交作品、查看成绩
- **老师**: 可以发布竞赛、管理竞赛、查看报名情况
- **评分员**: 可以对作品进行评分
- **管理员**: 可以审核报名、管理用户、查看统计数据

### 2. 支持个人赛和团队赛
- 个人赛：学生单独报名
- 团队赛：学生可以组建团队，担任队长，邀请成员

### 3. 完整的竞赛流程
- 发布竞赛 → 学生报名 → 管理员审核 → 学生缴费 → 提交作品 → 评分员评分 → 公布获奖结果

### 4. 消息推送功能
- 竞赛消息：推送新的竞赛信息
- 缴费消息：提醒学生缴费
- 系统消息：系统通知
- 获奖消息：通知获奖信息

### 5. 数据可视化支持
- 竞赛统计表存储各种统计数据
- 可以查询报名人数、缴费情况、作品数量、平均分等

### 6. 作品评分功能
- 支持需要提交作品的竞赛（如美术、摄影等）
- 评分员可以对作品进行打分和评语
- 学生可以查看自己的分数和排名

## 常用查询示例

### 1. 查询所有竞赛
```sql
SELECT c.id, c.title, cc.name as category_name, c.competition_type, c.status, c.registration_start_time, c.registration_end_time
FROM competition c
LEFT JOIN competition_category cc ON c.category_id = cc.id
ORDER BY c.create_time DESC;
```

### 2. 查询某个竞赛的报名情况
```sql
SELECT r.id, u.real_name, u.student_id, r.status, p.payment_status
FROM registration r
LEFT JOIN user u ON r.user_id = u.id
LEFT JOIN payment p ON r.id = p.registration_id
WHERE r.competition_id = 1
ORDER BY r.create_time DESC;
```

### 3. 查询某个学生的获奖情况
```sql
SELECT a.id, c.title, a.award_level, a.award_name, a.award_time
FROM award a
LEFT JOIN competition c ON a.competition_id = c.id
WHERE a.user_id = 6
ORDER BY a.award_time DESC;
```

### 4. 查询某个作品的评分情况
```sql
SELECT s.id, u.real_name as judge_name, s.score, s.comment, s.score_time
FROM scoring s
LEFT JOIN user u ON s.judge_id = u.id
WHERE s.work_id = 1
ORDER BY s.score_time DESC;
```

### 5. 查询某个竞赛的统计数据
```sql
SELECT cs.*, c.title
FROM competition_statistics cs
LEFT JOIN competition c ON cs.competition_id = c.id
WHERE cs.competition_id = 1;
```

## 注意事项

1. **密码加密**: 所有密码都使用BCrypt加密，原始密码为123456
2. **外键约束**: 数据库设置了外键约束，删除数据时要注意级联关系
3. **JSON字段**: registration_info、work_files等字段使用JSON格式存储
4. **时间字段**: 所有时间字段都使用DATETIME类型，支持精确到秒
5. **状态枚举**: 使用ENUM类型存储状态，确保数据一致性

## 后续开发建议

1. **SpringBoot集成**: 使用MyBatis或JPA进行数据库操作
2. **Redis缓存**: 可以使用Redis缓存热点数据，如竞赛列表、用户信息等
3. **文件存储**: 作品文件、证书等建议使用云存储（如阿里云OSS）
4. **定时任务**: 可以使用Spring Task或Quartz实现定时推送消息、更新统计数据等
5. **权限控制**: 建议使用Spring Security或Shiro进行权限控制
6. **日志记录**: 建议使用Logback或Log4j记录操作日志

## 技术支持

如有问题，请联系开发团队。
