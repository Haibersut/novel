<div align="center">

# 拼好书

韩轻机翻站开源代码

</div>

## 项目简介

项目源于 https://github.com/ygfhgf213/novel

拼好书是一个功能完整的在线小说阅读平台，支持小说浏览、章节阅读、用户笔记、收藏管理等功能。

项目采用前后端分离架构，但镜像将其封装在一起。

后端使用 Spring Boot，前端使用 Vue3 + Element Plus。

### 主要功能

- 小说浏览与搜索
- 章节在线阅读
- 用户笔记与评论
- 收藏与历史记录
- 作品上传与分享
- 多平台小说聚合

## 快速开始

### 使用 Docker 部署(推荐)

项目提供了预构建的 Docker 镜像,可以快速部署。

#### 1. 拉取镜像

开发版(dev)镜像更大：

```bash
docker pull mattgideon/freenovel:v1.0.16-dev
```

生产版(prod)镜像更小:

```bash
docker pull mattgideon/freenovel:v1.0.16-prod
```

目前没什么区别（

#### 2. 准备配置文件

项目提供了多种 Docker Compose 配置:

- `docker-compose.local-single.yml` - 单数据库本地部署
- `docker-compose.local-dual.yml` - 双数据库本地部署
- `docker-compose.external-single.yml` - 外部单数据库部署
- `docker-compose.external-dual.yml` - 外部双数据库部署

选择合适的配置文件,并根据需要修改环境变量:

```yaml
# 主要配置项
SERVER_PORT: 8081                     # 应用端口
MYSQL_PRIMARY_PASSWORD: your_password # 数据库密码
CLOUDFLARE_R2_ENABLED: true           # 是否启用 R2 存储
PROXY_CLIENT: false                   # 是否启用代理
```

#### 3. 下载字体文件

项目需要字体文件如下，请从以下链接下载，不下应该也没关系

**阿里云盘下载地址**: https://www.alipan.com/s/7tTHzoLf7Yf

**提取码**: `qh59`

需要下载以下两个字体文件:
- `NotoSansKR-VariableFont_wght`
- `obfuscatedNotoSansKR2`

下载后请将字体文件放置到`/app/file/`目录中。

#### 4. 创建必要目录

```bash
mkdir -p app/tmp app/file app/logs
```

#### 5. 启动服务

```bash
docker-compose -f docker-compose.local-dual.yml up -d
```

#### 6. 访问应用

应用启动后，访问 `http://localhost:8081` 即可使用。

首次启动时，在使用全新数据库的情况下，系统会自动创建默认账户。

您需要查看容器日志获取初始账号信息：

```bash
docker logs <container_name> （默认是novel-app）
```

日志中会包含类似以下内容的初始化账号信息：

```
====================================
novel-app                | 2025-10-18 00:19:28.844 [main] INFO  c.w.novel.Config.DatabaseInitializer - 成功创建默认用户!
novel-app                | 2025-10-18 00:19:28.844 [main] INFO  c.w.novel.Config.DatabaseInitializer - 用户名: admin@novel.com
novel-app                | 2025-10-18 00:19:28.844 [main] INFO  c.w.novel.Config.DatabaseInitializer - 密码: FATVwtgY77xv
novel-app                | 2025-10-18 00:19:28.844 [main] INFO  c.w.novel.Config.DatabaseInitializer - 初始积分: 10000
novel-app                | 2025-10-18 00:19:28.844 [main] INFO  c.w.novel.Config.DatabaseInitializer - 请务必保存此密码,并在首次登录后立即修改!
novel-app                | 2025-10-18 00:19:28.844 [main] INFO  c.w.novel.Config.DatabaseInitializer - ====================================
```

### 环境变量说明

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SERVER_PORT | 应用服务端口 | 8081 |
| DATABASE_MODE | 数据库模式(single/dual) | dual |
| MYSQL_PRIMARY_PASSWORD | 主数据库密码 | novel_password |
| MYSQL_SECONDARY_PASSWORD | 从数据库密码 | novel_password |
| CLOUDFLARE_R2_ENABLED | 是否启用 R2 存储 | true |
| PROXY_CLIENT | 是否启用代理 | false |
| SITEMAP_RUN | 是否生成站点地图 | false |

更多配置项请参考 `docker-compose.*.yml` 文件。

### 外部数据库配置示例

如果使用外部数据库(如云数据库或独立部署的 MariaDB/MySQL),需要配置完整的 JDBC 连接字符串。

#### 单数据库模式配置示例

使用 `docker-compose.external-single.yml`,配置环境变量:

```bash
# .env 文件或直接在 docker-compose.yml 中配置
PRIMARY_DB_URL=jdbc:mariadb://your-database-host:3306/novel?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&connectTimeout=30000&socketTimeout=60000
PRIMARY_DB_USERNAME=your_username
PRIMARY_DB_PASSWORD=your_password
PRIMARY_DB_POOL_SIZE=15
PRIMARY_DB_MIN_IDLE=5
```

**JDBC URL 参数说明**:
- `your-database-host`: 数据库主机地址(IP 或域名)
- `3306`: 数据库端口
- `novel`: 数据库名称
- `allowPublicKeyRetrieval=true`: 允许公钥检索
- `useSSL=false`: 是否使用 SSL(生产环境建议设为 true)
- `serverTimezone=UTC`: 服务器时区
- `connectTimeout=30000`: 连接超时时间(毫秒)
- `socketTimeout=60000`: Socket 超时时间(毫秒)

#### 双数据库模式配置示例(读写分离)

使用 `docker-compose.external-dual.yml`,配置环境变量:

```bash
# 主数据库
PRIMARY_DB_URL=jdbc:mariadb://primary-db-host:3306/novel?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&connectTimeout=30000&socketTimeout=60000
PRIMARY_DB_USERNAME=primary_user
PRIMARY_DB_PASSWORD=primary_password
PRIMARY_DB_POOL_SIZE=15
PRIMARY_DB_MIN_IDLE=5

# 从数据库
SECONDARY_DB_URL=jdbc:mariadb://secondary-db-host:3306/novel?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&connectTimeout=30000&socketTimeout=60000
SECONDARY_DB_USERNAME=secondary_user
SECONDARY_DB_PASSWORD=secondary_password
SECONDARY_DB_POOL_SIZE=8
SECONDARY_DB_MIN_IDLE=3
```

**启动命令**:

```bash
# 外部单数据库模式
docker-compose -f docker-compose.external-single.yml up -d

# 外部双数据库模式
docker-compose -f docker-compose.external-dual.yml up -d
```

## 已知问题

- 爬虫等工具目前无法自动启动，需要手动触发（？）


## 许可证

本项目采用 [LICENSE](LICENSE) 中规定的许可证。

## 贡献

欢迎提交 Issue 和 Pull Request。
