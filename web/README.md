# 大学生竞赛活动管理平台 - 后端

## 技术栈
- Spring Boot 3.1.5
- MyBatis Plus 3.5.4.1
- MySQL 8.0
- JWT认证
- JDK 17

## 项目结构
```
Competition-web/
├── src/main/java/com/competition/
│   ├── common/          # 通用类（Result、Constants）
│   ├── config/          # 配置类
│   ├── controller/      # 控制器
│   ├── entity/          # 实体类
│   ├── interceptor/     # 拦截器
│   ├── mapper/          # Mapper接口
│   ├── service/         # 服务接口
│   │   └── impl/        # 服务实现
│   └── util/            # 工具类
└── src/main/resources/
    ├── application.yml   # 配置文件
    └── db/              # 数据库脚本
```

## 启动步骤

1. 创建MySQL数据库，执行 `src/main/resources/db/schema.sql`
2. 修改 `application.yml` 中的数据库连接信息
3. 运行 `CompetitionApplication.java`
4. 默认端口：8080，接口前缀：/api

## 默认管理员账号
- 用户名：admin
- 密码：admin123





