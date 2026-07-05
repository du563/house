# \# Minsu — 民宿智能线上预订系统

# 

# 一个基于 \*\*Spring Boot + Vue 2\*\* 的民宿智能线上预订平台，支持多角色权限（普通用户 / 商家 / 管理员）、高德地图选址、MinIO 图片存储、邮箱验证码、Redis 缓存、ECharts 数据看板等功能。

# 

# \---

# 

# \## 目录

# 

# \- \[技术栈](#技术栈)

# \- \[功能特性](#功能特性)

# \- \[系统架构](#系统架构)

# \- \[用户角色与权限](#用户角色与权限)

# \- \[数据库设计](#数据库设计)

# \- \[快速开始](#快速开始)

# \- \[配置文件说明](#配置文件说明)

# \- \[项目结构](#项目结构)

# \- \[API 概览](#api-概览)

# \- \[前端路由](#前端路由)

# 

# \---

# 

# \## 技术栈

# 

# | 层级            | 技术                  | 版本    |

# | --------------- | --------------------- | ------- |

# | 后端框架        | Spring Boot           | 2.7.18  |

# | JDK             | Java                  | 8       |

# | ORM             | MyBatis-Plus          | 3.5.3.1 |

# | 数据库          | MySQL                 | 8.0+    |

# | 缓存            | Redis                 | —       |

# | 对象存储        | MinIO                 | —       |

# | 鉴权            | JWT (jjwt 0.9.1)      | —       |

# | 邮件            | Spring Mail (QQ SMTP) | —       |

# | 工具库          | Hutool                | 5.8.18  |

# | 前端框架        | Vue.js                | 2.6.14  |

# | UI 库           | Element UI            | 2.15.14 |

# | 路由 / 状态管理 | Vue Router 3 + Vuex 3 | —       |

# | 图表            | ECharts               | 5.6.0   |

# | 地图            | 高德地图 JS API       | —       |

# | HTTP 客户端     | Axios                 | 0.27.2  |

# | 构建工具        | Maven / Vue CLI 5     | —       |

# 

# \---

# 

# \## 功能特性

# 

# \### 用户端

# 

# \- \*\*民宿浏览\*\* — 列表搜索、按区域 / 标签筛选、高德地图位置展示

# \- \*\*民宿详情\*\* — 多图轮播、设施标签、评论评分、社区帖子关联

# \- \*\*在线预订\*\* — 选择入住日期、人数，下单支付流程

# \- \*\*订单管理\*\* — 查看订单列表、详情，取消订单

# \- \*\*收藏夹\*\* — 收藏 / 取消收藏感兴趣的民宿

# \- \*\*浏览历史\*\* — 自动记录浏览足迹

# \- \*\*个人中心\*\* — 头像、邮箱、密码修改

# \- \*\*社区互动\*\* — 浏览话题、发布帖子、点赞、评论

# 

# \### 商家端

# 

# \- 民宿管理（CRUD、上下架、图片上传至 MinIO）

# \- 订单管理（确认入住、查看订单）

# \- 评论管理（回复用户评价）

# 

# \### 管理后台

# 

# \- 数据看板（ECharts 图表统计）

# \- 用户管理（启用 / 禁用）

# \- 民宿审核（上架审核通过 / 驳回）

# \- 全平台订单监控

# \- 公告管理

# \- 评论审核

# \- 标签管理

# \- 社区内容管理（帖子、话题）

# 

# \---

# 

# \## 系统架构

# 

# ```

# +-----------------------------------------------------------+

# |                       前端 (Vue 2)                         |

# |     Vue Router . Vuex . Axios . Element UI . ECharts      |

# +--------------------------+--------------------------------+

# &#x20;                          | HTTP (Axios -> /api)

# &#x20;                          v

# +-----------------------------------------------------------+

# |                 后端 (Spring Boot :9100)                    |

# |     Controller -> Service -> MyBatis-Plus Mapper -> DB    |

# |     JWT Interceptor . 统一异常处理 . 文件上传               |

# +--------+------------------+-------------------+------------+

# &#x20;        |                  |                   |

# &#x20;        v                  v                   v

# &#x20;    +--------+        +--------+          +---------+

# &#x20;    | MySQL  |        | Redis  |          | MinIO   |

# &#x20;    | 民宿DB  |        | 缓存   |          | 对象存储 |

# &#x20;    +--------+        +--------+          +---------+

# ```

# 

# \### 数据流

# 

# 1\. 前端页面通过 Axios 请求 `/api/\*`，开发环境下由 Vue CLI 代理转发到后端 `:9100`

# 2\. 后端 `JwtInterceptor` 拦截需认证的请求，解析 `Authorization: Bearer <token>`

# 3\. Controller 接收参数 -> Service 层处理业务逻辑 -> Mapper 操作 MySQL

# 4\. 统一返回 `Result<T>` 格式：`{ code: 200, message: "...", data: ... }`

# 5\. 民宿图片通过 MinIO 存储，返回 URL 供前端直接访问

# 6\. 邮箱验证码由 Spring Mail（QQ SMTP）发送

# 

# \---

# 

# \## 用户角色与权限

# 

# | 角色值 | 角色     | 说明                             |

# | ------ | -------- | -------------------------------- |

# | `0`    | 普通用户 | 浏览、预订、收藏、评论、社区互动 |

# | `1`    | 管理员   | 全部管理后台权限                 |

# | `2`    | 商家     | 发布民宿、处理订单、回复评论     |

# 

# \---

# 

# \## 数据库设计

# 

# 数据库名：`minsu`

# 

# \### 核心表

# 

# | 表名             | 说明     | 关键字段                                                     |

# | ---------------- | -------- | ------------------------------------------------------------ |

# | `user`           | 用户     | id, username, password, phone, email, avatar, role(0/1/2), status |

# | `house`          | 民宿     | id, name, price, area, address, longitude/latitude, type, capacity, score, images, facilities, tag\_ids, merchant\_id, audit\_status, stock |

# | `orders`         | 订单     | id, order\_no, user\_id, house\_id, price, total\_price, check\_in/out\_date, status(0待支付/1已支付/2已完成/3已取消) |

# | `comment`        | 评论     | id, user\_id, house\_id, order\_id, content, score, images, audit\_status, reply\_content |

# | `favorite`       | 收藏     | id, user\_id, house\_id                                        |

# | `browse\_history` | 浏览历史 | id, user\_id, house\_id, browse\_time                           |

# | `announcement`   | 公告     | id, title, content, status, sort\_order                       |

# | `house\_tag`      | 民宿标签 | id, name, sort\_order                                         |

# | `post`           | 社区帖子 | id, user\_id, content, images, likes, comments                |

# | `topic`          | 社区话题 | id, name, sort\_order                                         |

# | `post\_like`      | 帖子点赞 | id, post\_id, user\_id                                         |

# 

# \---

# 

# \## 快速开始

# 

# \### 环境要求

# 

# \- JDK 8+

# \- Maven 3.6+

# \- MySQL 8.0+

# \- Redis（可选，默认 `127.0.0.1:6379`）

# \- MinIO 对象存储（可选，默认 `localhost:9000`）

# \- Node.js 14+ \& npm

# 

# \### 1. 数据库初始化

# 

# ```sql

# \-- 创建数据库

# CREATE DATABASE IF NOT EXISTS minsu DEFAULT CHARSET utf8mb4 COLLATE utf8mb4\_0900\_ai\_ci;

# 

# \-- 导入表结构与初始数据

# source /path/to/sql/minsu.sql;

# ```

# 

# \### 2. 启动后端

# 

# ```bash

# cd minsu-backend

# 

# \# 编译打包

# mvn clean package -DskipTests

# 

# \# 启动（默认端口 9100）

# java -jar target/minsu-backend-1.0.0.jar

# ```

# 

# 或者在 IDEA 中直接运行 `MinsuApplication.java`。

# 

# \### 3. 启动前端

# 

# ```bash

# cd minsu-frontend

# 

# npm install

# 

# \# 开发模式（默认端口 8000，自动代理 /api 到 :9100）

# npm run serve

# 

# \# 生产构建

# npm run build

# ```

# 

# \### 4. 访问

# 

# \- 前端页面：`http://localhost:8000`

# \- 后端 API：`http://localhost:9100`

# 

# \---

# 

# \## 配置文件说明

# 

# \### 后端 `application.yml`

# 

# | 配置项                  | 说明                | 默认值                              |

# | ----------------------- | ------------------- | ----------------------------------- |

# | `server.port`           | 后端端口            | `9100`                              |

# | `spring.datasource.url` | MySQL 连接地址      | `jdbc:mysql://localhost:3306/minsu` |

# | `spring.redis.host`     | Redis 地址          | `127.0.0.1`                         |

# | `spring.mail.username`  | QQ 邮箱账号         | `3367756499@qq.com`                 |

# | `spring.mail.password`  | QQ 邮箱 SMTP 授权码 | —                                   |

# | `jwt.secret`            | JWT 签名密钥        | `minsu-secret-key-...`              |

# | `jwt.expiration`        | JWT 过期时间(ms)    | `86400000` (1天)                    |

# | `minio.endpoint`        | MinIO 服务地址      | `http://localhost:9000`             |

# | `minio.bucket-name`     | MinIO 存储桶        | `upload`                            |

# 

# \### 前端 `.env`

# 

# | 变量                           | 说明                    |

# | ------------------------------ | ----------------------- |

# | `VUE\_APP\_AMAP\_KEY`             | 高德地图 Web JS API Key |

# | `VUE\_APP\_AMAP\_SECURITY\_JSCODE` | 高德地图安全密钥        |

# 

# \### 前端代理配置（`vue.config.js`）

# 

# 开发环境将所有 `/api` 请求代理到后端 `:9100`，避免跨域问题。

# 

# ```js

# proxy: {

# &#x20; '/api': {

# &#x20;   target: 'http://localhost:9100',

# &#x20;   changeOrigin: true,

# &#x20;   pathRewrite: { '^/api': '' }

# &#x20; }

# }

# ```

# 

# \---

# 

# \## 项目结构

# 

# ```

# minsu/

# |

# +-- minsu-backend/                    # 后端项目（Maven + Spring Boot）

# |   +-- pom.xml                       # Maven 依赖管理

# |   +-- src/main/java/com/minsu/

# |   |   +-- MinsuApplication.java     # 启动类

# |   |   +-- common/

# |   |   |   +-- Result.java           # 统一响应结果

# |   |   +-- config/

# |   |   |   +-- FileUploadConfig.java # 文件上传配置

# |   |   |   +-- MinioConfig.java      # MinIO 客户端配置

# |   |   |   +-- MybatisPlusConfig.java

# |   |   |   +-- MyMetaObjectHandler.java  # 自动填充

# |   |   |   +-- WebConfig.java        # CORS 配置

# |   |   +-- controller/               # REST 控制器

# |   |   |   +-- AdminController.java

# |   |   |   +-- AnalyticsController.java  # 数据统计

# |   |   |   +-- AnnouncementController.java

# |   |   |   +-- BrowseHistoryController.java

# |   |   |   +-- CommentController.java

# |   |   |   +-- CommunityController.java

# |   |   |   +-- FavoriteController.java

# |   |   |   +-- FileUploadController.java

# |   |   |   +-- HouseController.java

# |   |   |   +-- HouseTagController.java

# |   |   |   +-- OrderController.java

# |   |   |   +-- RecommendController.java

# |   |   |   +-- UserController.java

# |   |   +-- entity/                   # 数据实体

# |   |   +-- interceptor/

# |   |   |   +-- JwtInterceptor.java   # JWT 鉴权拦截器

# |   |   +-- mapper/                   # MyBatis-Plus Mapper

# |   |   +-- service/                  # Service 业务层

# |   |   +-- utils/

# |   |       +-- JwtUtils.java         # JWT 工具类

# |   +-- src/main/resources/

# |       +-- application.yml           # 全局配置文件

# |

# +-- minsu-frontend/                   # 前端项目（Vue 2 + Element UI）

# |   +-- package.json

# |   +-- vue.config.js                 # Vue CLI 配置

# |   +-- .env                          # 环境变量

# |   +-- public/index.html

# |   +-- src/

# |       +-- main.js                   # 入口文件

# |       +-- App.vue

# |       +-- api/                      # API 封装（Axios）

# |       +-- assets/css/               # 全局样式

# |       +-- components/               # 公共组件

# |       +-- config/amap.js            # 高德地图配置

# |       +-- router/index.js           # 路由 + 权限守卫

# |       +-- store/index.js            # Vuex 状态管理

# |       +-- utils/

# |       |   +-- request.js            # Axios 拦截器

# |       |   +-- amapLoader.js         # 高德地图加载

# |       |   +-- excel.js              # Excel 导出

# |       +-- views/                    # 页面视图

# |           +-- Home.vue

# |           +-- Login.vue / Register.vue

# |           +-- layout/               # 布局组件

# |           +-- house/                # 民宿列表 / 详情

# |           +-- order/                # 下单 / 订单管理

# |           +-- user/                 # 个人中心

# |           +-- community/            # 社区

# |           +-- admin/                # 管理后台 (8页面)

# |           +-- merchant/             # 商家后台

# |

# +-- sql/

# &#x20;   +-- minsu.sql                     # 建表脚本 + 示例数据

# ```

# 

# \---

# 

# \## API 概览

# 

# 所有 API 返回统一格式：

# 

# ```json

# {

# &#x20; "code": 200,

# &#x20; "message": "操作成功",

# &#x20; "data": { ... }

# }

# ```

# 

# | 分组     | 路径                           | 说明                       |

# | -------- | ------------------------------ | -------------------------- |

# | 用户     | `POST /api/user/login`         | 登录，返回 JWT token       |

# | 用户     | `POST /api/user/register`      | 注册（邮箱验证码）         |

# | 用户     | `POST /api/user/sendCode`      | 发送邮箱验证码             |

# | 用户     | `GET /api/user/profile`        | 获取个人信息               |

# | 用户     | `PUT /api/user/profile`        | 更新个人信息               |

# | 民宿     | `GET /api/house/list`          | 民宿列表（分页/搜索/筛选） |

# | 民宿     | `GET /api/house/{id}`          | 民宿详情                   |

# | 民宿     | `POST /api/house`              | 新增民宿（商家）           |

# | 民宿     | `PUT /api/house/{id}`          | 更新民宿（商家）           |

# | 民宿     | `POST /api/house/audit`        | 审核民宿（管理员）         |

# | 订单     | `POST /api/order`              | 创建订单                   |

# | 订单     | `GET /api/order/list`          | 订单列表                   |

# | 订单     | `PUT /api/order/cancel`        | 取消订单                   |

# | 订单     | `POST /api/order/checkIn`      | 确认入住（商家）           |

# | 评论     | `POST /api/comment`            | 发表评论                   |

# | 评论     | `PUT /api/comment/reply`       | 商家回复评论               |

# | 收藏     | `POST /api/favorite`           | 收藏/取消收藏              |

# | 收藏     | `GET /api/favorite/list`       | 收藏列表                   |

# | 浏览历史 | `GET /api/browseHistory/list`  | 浏览历史                   |

# | 统计     | `GET /api/analytics/dashboard` | 数据看板（管理员）         |

# | 社区     | `GET /api/community/posts`     | 帖子列表                   |

# | 社区     | `POST /api/community/post`     | 发布帖子                   |

# | 社区     | `POST /api/community/like`     | 点赞帖子                   |

# | 文件     | `POST /api/file/upload`        | 上传文件到 MinIO           |

# | 公告     | `GET /api/announcement/list`   | 公告列表                   |

# 

# \---

# 

# \## 前端路由

# 

# | 路径                | 页面     | 权限   |

# | ------------------- | -------- | ------ |

# | `/`                 | 首页     | 公开   |

# | `/house`            | 民宿列表 | 公开   |

# | `/house/:id`        | 民宿详情 | 公开   |

# | `/login`            | 登录     | 公开   |

# | `/register`         | 注册     | 公开   |

# | `/community`        | 社区     | 公开   |

# | `/order`            | 订单列表 | 需登录 |

# | `/order/:id`        | 订单详情 | 需登录 |

# | `/booking/:houseId` | 下单页   | 需登录 |

# | `/profile`          | 个人中心 | 需登录 |

# | `/history`          | 浏览历史 | 需登录 |

# | `/favorites`        | 收藏夹   | 需登录 |

# | `/admin`            | 管理后台 | 管理员 |

# | `/merchant`         | 商家后台 | 商家   |

# 

# \---

# 

# \## 开发参考

# 

# \### 统一响应类 `Result<T>`

# 

# ```java

# Result.success(data)          // -> { code: 200, message: "操作成功", data: ... }

# Result.error("错误信息")       // -> { code: 500, message: "错误信息" }

# ```

# 

# \### JWT 鉴权流程

# 

# 1\. 用户登录成功，后端生成 token（含 `userId`, `username`, `role`）

# 2\. 前端将 token 存入 `localStorage`，通过 Vuex 管理

# 3\. 每次请求 Axios 拦截器自动携带 `Authorization: Bearer <token>`

# 4\. 后端 `JwtInterceptor` 验证 token 有效性并解析用户身份

# 5\. 通过 `@RequestAttribute("userId")` 等在 Controller 中获取当前用户

# 

# \### 文件上传

# 

# \- 商家上传民宿图片到 MinIO，返回 URL 供前端展示

# \- 依赖 MinIO 服务，默认地址 `http://localhost:9000`，存储桶 `upload`

# 

# \### 地图集成

# 

# \- 房源新增 / 编辑时通过高德地图弹窗选择经纬度

# \- 民宿详情页展示高德地图位置标记

# \- 需自行申请高德地图 Web JS API Key，配置在 `.env` 中

# 

# \---

# 

# \## License

# 

# This project is for educational / demo purposes.

