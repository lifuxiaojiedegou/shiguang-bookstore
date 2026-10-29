# 拾光书店 · 校园图书商城

> 网络应用架构设计与开发 课程实验

| 项目 | 内容 |
|---|---|
| 学号 | 202430330460 |
| 姓名 | 方喆 |
| 班级 | 24计科双学位班 |
| 指导教师 | 布社辉 |
| 代码仓库 | https://gitee.com/zhe_fang/shiguang-bookstore |
| 在线地址 | http://39.108.94.63:8080 |

## 一、项目简介

拾光书店是一个面向校园的 B/S 图书商城。顾客可以浏览、搜索图书，加入购物车、下单、模拟付款并查看订单；管理员可以维护商品目录、处理订单发货、查看销售统计报表。系统使用 Spring Boot + Thymeleaf + MySQL，部署为单个可执行 JAR。

## 二、功能清单

### 顾客端

- 注册、登录、注销
- 商品列表、关键词搜索、分类筛选
- 商品详情页 `/product/{id}`（书名、分类、价格、库存、简介，可选数量加购）
- 购物车：加入、修改数量、移除、合计
- 下单：填写收货信息、服务端计算金额、扣减库存、清空购物车
- 订单状态：待付款、已付款、已发货、已完成、已取消
- 模拟付款、取消订单（恢复库存）、确认收货
- 查看订单历史与明细

### 管理端

- 商品目录：新增、编辑、上下架、删除（有历史订单时拒绝删除）
- 订单管理：查看全部订单、填写物流单号发货
- 发货邮件通知：发件箱记录、失败重试
- 销售统计：累计销售额、已付款订单数、客单价、低库存商品、最近 30 天趋势、商品销量排行、分类销售额
- 浏览/购买日志 `/admin/logs`：记录商品浏览、搜索、加购、下单（含访客与 IP），按行为筛选，展示今日行为数、商品转化（浏览→加购→下单）和热门搜索词

## 三、技术栈

- Java 17（编译目标，运行环境 JDK 17+）
- Spring Boot 4.1.1
- Spring MVC + Thymeleaf
- Spring JDBC（JdbcTemplate）
- Spring Security（会话登录、角色权限、CSRF）
- MySQL 8
- JavaMail
- 原生 HTML / CSS / JavaScript（统计图表使用 SVG）

## 四、目录结构

```text
online-shop/
├─ src/main/java/com/example/onlineshop/
│  ├─ config/        安全配置
│  ├─ model/         商品、用户、购物车、订单、邮件等模型
│  ├─ repository/    数据访问层（JdbcTemplate）
│  ├─ service/       业务逻辑（下单事务、购物车、统计等）
│  └─ web/           控制器
├─ src/main/resources/
│  ├─ templates/     Thymeleaf 页面
│  ├─ static/        style.css 等静态资源
│  ├─ schema.sql     建表脚本
│  ├─ data.sql       演示数据（幂等插入）
│  └─ application.properties
├─ config/           本地数据库配置（不提交）
├─ docs/             测试用例与报告提纲
├─ pom.xml
└─ mvnw / mvnw.cmd
```

## 五、本地运行

环境要求：JDK 17 及以上、MySQL 8、无需单独安装 Maven。

1. 创建数据库和专用账号（密码请自行替换）：

```sql
CREATE DATABASE online_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'online_shop'@'localhost' IDENTIFIED BY '你的数据库密码';
GRANT ALL PRIVILEGES ON online_shop.* TO 'online_shop'@'localhost';
FLUSH PRIVILEGES;
```

2. 在项目根目录创建 `config/application-local.properties`（该目录已被 `.gitignore` 排除）：

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/online_shop?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
spring.datasource.username=online_shop
spring.datasource.password=你的数据库密码
```

3. 启动：

```bash
./mvnw spring-boot:run
```

4. 浏览器访问 `http://localhost:8080`。

启动时 Spring Boot 会自动执行 `schema.sql` 和 `data.sql`。两个脚本都是幂等的，重复启动不会重复插入数据。

## 六、数据库表

| 表名 | 用途 |
|---|---|
| product | 商品目录、价格（分）、库存、上下架状态 |
| shop_user | 顾客与管理员账号、BCrypt 密码哈希、角色 |
| cart_item | 购物车条目，用户与商品联合主键 |
| orders | 订单主表、状态、金额、收货信息、物流单号 |
| order_item | 订单明细，保存下单时的商品名与单价快照 |
| mail_outbox | 邮件发件箱，记录发送状态、失败原因和重试 |
| activity_log | 浏览/购买日志：用户（访客为空）、商品与商品名快照、行为、详情、IP、时间 |

金额统一使用整数“分”保存，避免浮点误差。

## 七、测试账号

公开仓库中不保存任何真实密码。

- 顾客：在注册页创建账号。
- 管理员：先注册一个账号，然后在数据库中执行：

```sql
UPDATE shop_user SET role = 'ADMIN' WHERE email = '你的邮箱';
```

退出并重新登录后生效。

## 八、邮件配置

本地默认使用文件模式，不会连接邮件服务器：

```properties
shop.mail.mode=file
shop.mail.directory=./data/mail
```

发货后会在 `data/mail/` 下生成 `mail-*.eml` 文件，可直接用邮件客户端打开。

真实发送需要配置 SMTP 环境变量：

```text
MAIL_MODE=smtp
SMTP_HOST=smtp.qq.com
SMTP_PORT=465
SMTP_USER=你的邮箱
SMTP_PASSWORD=邮箱授权码（不是登录密码）
SMTP_FROM=你的邮箱
```

## 九、构建与部署

```bash
./mvnw clean package
java -jar target/online-shop-0.0.1-SNAPSHOT.jar
```

生产环境建议：

- MySQL 与应用部署在同一台云服务器；
- 使用 Nginx 反向代理 8080；
- 使用 systemd 管理进程并设置开机自启；
- 通过环境变量注入数据库密码和 SMTP 授权码；
- 只开放必要端口，定期备份数据库。

详细部署步骤见 [deploy/README.md](deploy/README.md)。

## 十、设计与一致性

- 密码使用 BCrypt 加盐哈希，数据库不保存明文；
- 登录后更换 Session，退出时失效并删除 Cookie；
- 表单 POST 使用 Spring Security CSRF 保护；
- 所有 SQL 使用参数绑定，避免 SQL 注入；
- 下单使用 `@Transactional`：扣库存、写订单、写明细、清空购物车在同一事务中；
- 扣库存使用 `stock >= 数量` 条件更新，防止超卖；
- 下单前锁定用户行，避免同一用户并发重复下单；
- 订单明细保存商品名和单价快照，商品调价不影响历史订单；
- 商品删除：默认逻辑下架；无历史订单可物理删除；有历史订单拒绝删除，保留订单快照和关联；
- 发货邮件使用发件箱模式，订单事务提交后再发送，失败可重试。

## 十一、AI 使用情况

本项目在开发过程中使用 Claude Code 辅助完成代码骨架、SQL、模板和调试建议。所有 AI 生成内容均经过本人运行验证、修改和测试。

- 使用工具：Claude Code
- 使用模块：商品、注册登录、购物车、下单、订单状态、后台管理、邮件、统计、文档
- 本人负责：需求与设计确认、数据库操作、代码修改、运行验证、功能测试
- 详细记录：[docs/AI使用说明.md](docs/AI使用说明.md)、[docs/开发日志.md](docs/开发日志.md)
- 核心 AI 辅助代码已在注释中标注

## 十二、已知限制

- 付款为模拟流程，不接入真实支付；
- Session 保存在内存中，应用重启后需要重新登录；
- 商品列表暂未分页（当前数据量较小）；
- 商品图片上传未实现；
- 真实 SMTP 已在部署环境完成测试；
- 浏览日志每次打开详情页都会记录，未做去重和定期清理；

## 十三、测试

手工测试用例见 [docs/测试用例.md](docs/测试用例.md)，覆盖注册登录、搜索、购物车、下单、库存回滚、订单状态、后台发货、邮件、统计报表和浏览日志。

自动化测试 `src/test/java/.../ActivityLogTests.java` 覆盖日志记录、空搜索不记录、商品 404、后台筛选和权限，需本地 MySQL，运行 `./mvnw test`；测试在事务中执行并回滚。
