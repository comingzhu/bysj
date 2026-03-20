# 大学生竞赛活动管理平台

基于SpringBoot和Vue3的大学生竞赛活动管理平台的设计与实现

## 项目结构

```
Competition/
├── Competition-web/     # 后端项目（SpringBoot）
└── Competition-vue/     # 前端项目（Vue3）
```

## 技术栈

### 后端
- Spring Boot 3.1.5
- MyBatis Plus 3.5.4.1
- MySQL 8.0
- JWT认证
- JDK 17

### 前端
- Vue 3
- Vue Router 4
- Pinia
- Element Plus
- Axios
- Vite

## 功能模块

### 管理端
- 登录系统
- 系统管理（角色管理、系统参数设置）
- 竞赛审核（校赛审核、省赛信息发布）
- 报名审核
- 缴费管理
- 数据可视化

### 老师端
- 登录系统
- 竞赛管理（发布校赛、管理已发布竞赛）
- 报名管理
- 作品管理
- 数据可视化

### 评分员端
- 登录系统
- 评分任务接收
- 作品评分
- 评分统计

### 学生端
- 登录系统
- 首页（消息推送、竞赛浏览）
- 竞赛报名（个人赛/团队赛）
- 作品提交
- 缴费操作
- 个人中心（报名记录、获奖记录、个人信息）

## 快速开始

### 1. 数据库准备

1. 安装MySQL 8.0
2. 执行 `Competition-web/src/main/resources/db/schema.sql` 创建数据库和表
3. 修改 `Competition-web/src/main/resources/application.yml` 中的数据库连接信息

### 2. 启动后端

```bash
cd Competition-web
# 使用IDEA打开项目，运行 CompetitionApplication.java
# 或使用Maven命令
mvn spring-boot:run
```

后端服务启动在：http://localhost:8080/api

### 3. 启动前端

```bash
cd Competition-vue
npm install
npm run dev
```

前端服务启动在：http://localhost:3000

## 默认账号

- **管理员**：admin / admin123

## 数据库说明

- 数据库名：Competition
- 密码存储：明文存储（按要求）
- 所有表都包含逻辑删除字段（deleted）

## 开发说明

### 后端开发
- 使用IDEA打开 `Competition-web` 目录
- 确保JDK版本为17
- 修改 `application.yml` 中的数据库配置
- 运行主类 `CompetitionApplication`

### 前端开发
- 使用VSCode打开 `Competition-vue` 目录
- 安装依赖：`npm install`
- 启动开发服务器：`npm run dev`

## 接口说明

所有接口都需要在Header中携带JWT Token：
```
Authorization: Bearer {token}
```

登录接口除外，登录成功后会返回token，需要保存到localStorage。

## 注意事项

1. 密码采用明文存储（按要求）
2. 文件上传路径需要在 `application.yml` 中配置
3. 前端代理已配置，开发时直接访问 `/api` 即可
4. 生产环境需要配置跨域和文件上传路径

## 后续开发建议

1. 完善团队赛功能（创建团队、加入团队）
2. 实现作品上传和评分功能
3. 完善数据可视化统计
4. 添加消息推送功能
5. 实现文件上传功能
6. 完善异常处理和日志记录





