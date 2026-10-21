# CLAUDE.md

## 项目

拾光书店 · 校园图书商城。Spring Boot 4.1.1 + Thymeleaf + Spring JDBC + MySQL 8。

## 环境

- JDK 17 及以上（本机 JDK 25）
- Maven 使用项目自带 `./mvnw`
- MySQL 8，数据库名 `online_shop`
- 本地配置：`config/application-local.properties`（已被 `.gitignore` 排除，不要提交）

## 常用命令

```bash
./mvnw -q -DskipTests compile
./mvnw test
./mvnw spring-boot:run
./mvnw clean package
```

启动后访问 `http://localhost:8080`。`schema.sql` 和 `data.sql` 会在启动时自动执行，两个脚本必须保持幂等。

## 代码约定

- 包结构：`config` / `model` / `repository` / `service` / `web`
- 使用 `jakarta.*`，不要引入 `javax.*`
- 金额统一用整数“分”：`price_cents`、`total_cents`、`unit_price_cents`
- 密码使用 BCrypt，不保存明文
- 表单 POST 使用 `th:action`，依赖 Spring Security 自动注入 CSRF 令牌
- 退出登录必须用 POST
- 订单查询和状态修改必须带 `user_id` 条件，防止越权
- 管理员路径 `/admin/**` 在 `SecurityConfig` 中限制为 `ADMIN`
- 商品默认逻辑下架（`active = 0`）；后台可删除无历史订单的商品；有 `order_item` 引用的商品拒绝物理删除，保留快照和关联
- 订单明细保存商品名和单价快照，不能只存 `product_id`
- 下单、取消等涉及多表写入的操作必须放在 `@Transactional` 中
- 扣库存使用条件更新 `WHERE stock >= ?`，不要先查后改
- 同一用户下单前锁定用户行，避免并发重复下单
- 邮件使用 `mail_outbox`，订单事务提交后再发送

## 数据库

表：`product`、`shop_user`、`cart_item`、`orders`、`order_item`、`mail_outbox`。

修改表结构前先确认，并同步更新 `schema.sql`、实体/记录类、Repository 和测试。不要直接修改生产数据。

## 验证

- 手工测试用例：`docs/测试用例.md`
- 主要页面：`/`、`/register`、`/login`、`/cart`、`/checkout`、`/orders`、`/admin/products`、`/admin/orders`、`/admin/stats`
- 数据库验证：`SHOW TABLES`、订单/库存/发件箱查询
- UI 改动后在浏览器实际检查桌面和手机宽度

## AI 使用与留痕

- AI 辅助的核心文件使用统一标注：`// AI-assisted: Claude Code, 2026-10; reviewed/modified by 方喆.`
- 每个模块的提示词摘要、验证结果和问题记录写入 `docs/开发日志.md`
- AI 使用范围和声明写入 `docs/AI使用说明.md`
- 不把数据库密码、SMTP 授权码、云服务密钥、真实个人信息输入 AI 或提交到仓库
- AI 生成的代码必须自己运行、修改、验证，不接受无法解释的实现

## 禁止

- 提交 `config/`、`data/`、`target/`、`.idea/`
- 在代码中硬编码密码或密钥
- 绕过 CSRF、越权校验或事务边界
- 未经确认删除数据、重建数据库或改写已发布的 Git 历史
