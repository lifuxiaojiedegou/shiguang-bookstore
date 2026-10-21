# 在线部署指南

目标：把 Spring Boot 应用、MySQL 和 Nginx 部署到一台 Ubuntu 云服务器，通过 `http://服务器IP:8080` 从校园网访问，并配置真实 SMTP 发货邮件。

本文以 Ubuntu 22.04 / 24.04、2 核 2G、公网 IP 为例。阿里云、腾讯云、AWS 的步骤基本一致。

## 一、服务器准备

1. 购买一台云服务器，配置建议：
   - 2 vCPU、2 GB 内存、40 GB 磁盘
   - Ubuntu 22.04 LTS 或 24.04 LTS
   - 公网 IP
   - 使用期限覆盖提交后 3 个月
2. 安全组 / 防火墙只开放：
   - `22/tcp`：SSH，建议限制为自己的 IP
   - `8080/tcp`：网站访问
   - 不要开放 `3306`（MySQL 只监听本机）
3. 登录服务器：

```bash
ssh ubuntu@服务器IP
```

## 二、安装软件

```bash
sudo apt update
sudo apt install -y openjdk-17-jre-headless mysql-server nginx
java -version
mysql --version
nginx -v
```

## 三、创建数据库和账号

```bash
sudo mysql
```

在 MySQL 中执行（把密码替换成自己的强密码）：

```sql
CREATE DATABASE online_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'online_shop'@'localhost' IDENTIFIED BY '你的数据库密码';
GRANT ALL PRIVILEGES ON online_shop.* TO 'online_shop'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

确认 MySQL 只监听本机：

```bash
sudo ss -lntp | grep 3306
```

## 四、本地构建并上传 JAR

在本地项目目录执行：

```bash
./mvnw clean package
ls -lh target/online-shop-0.0.1-SNAPSHOT.jar
```

上传到服务器：

```bash
scp target/online-shop-0.0.1-SNAPSHOT.jar ubuntu@服务器IP:/tmp/
scp -r deploy ubuntu@服务器IP:/tmp/
```

服务器上准备目录和运行用户：

```bash
sudo useradd -r -s /usr/sbin/nologin onlineshop || true
sudo mkdir -p /opt/online-shop/data/mail
sudo mv /tmp/online-shop-0.0.1-SNAPSHOT.jar /opt/online-shop/online-shop.jar
sudo chown -R onlineshop:onlineshop /opt/online-shop
sudo chmod 600 /opt/online-shop/online-shop.jar
```

## 五、配置环境变量

复制模板：

```bash
sudo cp /tmp/deploy/online-shop.env.example /etc/online-shop.env
sudo chmod 600 /etc/online-shop.env
sudo nano /etc/online-shop.env
```

至少修改：

```properties
DB_PASSWORD=你的数据库密码
SMTP_USER=你的QQ邮箱
SMTP_PASSWORD=QQ邮箱SMTP授权码
SMTP_FROM=你的QQ邮箱
```

说明：

- 数据库密码、SMTP 授权码只写在这个文件里，不要提交到 Git；
- `MAIL_MODE=smtp` 才会真实发邮件；本地开发默认是 `file`；
- 应用只监听 `127.0.0.1:8081`，由 Nginx 对外提供 8080。

QQ 邮箱授权码获取：

1. 登录 QQ 邮箱网页版；
2. 设置 → 账户；
3. 开启 “POP3/SMTP 服务”；
4. 生成授权码，把它作为 `SMTP_PASSWORD`，不是 QQ 登录密码。

## 六、配置 systemd

```bash
sudo cp /tmp/deploy/online-shop.service /etc/systemd/system/online-shop.service
sudo systemctl daemon-reload
sudo systemctl enable --now online-shop
sudo systemctl status online-shop
```

查看日志：

```bash
sudo journalctl -u online-shop -n 100 --no-pager
```

首次启动会自动执行 `schema.sql` 和 `data.sql`，创建六张表和 5 件演示商品。

## 七、配置 Nginx

```bash
sudo cp /tmp/deploy/nginx.conf /etc/nginx/sites-available/online-shop
sudo ln -sf /etc/nginx/sites-available/online-shop /etc/nginx/sites-enabled/online-shop
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx
```

## 八、验证

服务器本机：

```bash
curl -I http://127.0.0.1:8081/
curl -I http://127.0.0.1:8080/
```

校园网浏览器访问：

```text
http://服务器IP:8080
```

验收项：

1. 首页能看到 5 件商品；
2. 注册顾客、登录、加入购物车、下单、付款正常；
3. 管理员后台 `/admin/orders` 能发货；
4. 真实邮箱能收到发货通知；
5. 数据库 `mail_outbox` 状态为 `SENT`。

## 九、创建测试账号

1. 在网站注册：
   - 顾客：`demo@example.com`
   - 管理员：`admin@example.com`
2. 把管理员角色改为 `ADMIN`：

```sql
UPDATE shop_user SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

3. 退出并重新登录，确认“管理后台”出现；
4. 实验报告中写明测试账号和口令，不要使用个人常用密码。

## 十、常见问题

### 应用启动失败

```bash
sudo journalctl -u online-shop -n 100 --no-pager
```

常见原因：

- `/etc/online-shop.env` 中数据库密码错误；
- MySQL 未启动：`sudo systemctl status mysql`；
- 端口冲突：`sudo ss -lntp | grep 8081`。

### 8080 访问不了

- 云厂商安全组是否放行 8080；
- 服务器防火墙：`sudo ufw status`；
- Nginx 是否运行：`sudo systemctl status nginx`；
- `curl -I http://127.0.0.1:8080/` 是否正常。

### 跳转后丢失 8080 端口

如果登录或注册后跳到 `http://服务器IP/login`（没有 8080），说明反向代理没有把端口传给应用：

- Nginx 必须使用 `proxy_set_header Host $http_host;`，并设置 `X-Forwarded-Host`、`X-Forwarded-Port 8080`；
- `/etc/online-shop.env` 中加入 `SERVER_FORWARD_HEADERS_STRATEGY=framework`；
- 修改后执行 `nginx -t && systemctl reload nginx`，再重启应用。

### 邮件发送失败

- `MAIL_MODE` 是否为 `smtp`；
- 是否使用授权码而不是登录密码；
- 端口 465、SSL 是否开启；
- 查看 `mail_outbox` 的 `status` 和 `error`；
- 检查收件箱垃圾邮件目录。

### 数据备份

```bash
mysqldump -u online_shop -p online_shop > online_shop_$(date +%F).sql
```

建议每周备份一次，至少保留最近三次。

## 十一、三个月可用性

- 确认服务器到期时间，设置续费提醒；
- 保持 `systemctl enable online-shop mysql nginx`；
- `Restart=always` 会自动拉起应用；
- 每月检查一次 `systemctl status` 和磁盘空间；
- 不要随意修改数据库密码或删除服务器。

## 十二、部署截图

部署完成后补：

- `docs/screenshots/23-deploy-home.png`：校园网浏览器访问公网地址；
- `docs/screenshots/24-deploy-service.png`：`systemctl status online-shop`、MySQL、Nginx；
- `docs/screenshots/25-smtp-inbox.png`：真实邮箱收到发货通知。
