# Dcard Lite Forum (Spring Boot)

以 Spring Boot 建立的「看板 / 文章 / 留言」最小可行論壇後端,支援註冊登入(JWT)、看板列表、發文、文章列表(latest/hot)、文章詳情、文章編輯/刪除(作者/管理者)、文章留言等功能。

> 💡 規格與測試案例以 [`/docs/**`](docs) 為準:FSD + 各 RP 文件。

---

## 📚 目錄

- [Tech Stack](#tech-stack)
- [專案特色](#project-features)
- [文件結構](#documentation-structure)
- [專案結構](#project-structure)
- [先決條件](#prerequisites)
- [快速開始](#quick-start)
- [測試](#testing)
- [API 概覽](#api-overview)
- [錯誤回應格式](#error-response)
- [資料庫架構](#database-schema)
- [參考資訊](#references)
- [打包部署](#deployment)
- [其他文件](#other-docs)
- [授權](#license)
- [貢獻](#contributing)
---
<a id="tech-stack"></a>
## 🛠 Tech Stack

- **Java 17 LTS**
- **Spring Boot 3.x** (REST API、Validation、Security)
- **Maven** (含 Wrapper:[`mvnw`](mvnw)、[`pom.xml`](pom.xml))
- **MySQL** + **Liquibase** (schema 變更:[`/src/main/resources/db/changelog/**`](src/main/resources/db/changelog))
- **JWT** 認證機制
- **JUnit 5** + **Mockito** (單元測試範例:[`PostServiceTest`](src/test/java/com/example/demo/post/service/PostServiceTest.java))

---
<a id="project-features"></a>
## ✨ 專案特色

- ✅ **完整的 RESTful API** - 遵循 REST 最佳實踐
- ✅ **JWT 身份驗證** - 安全的使用者認證機制
- ✅ **角色權限控制** - 支援 USER/ADMIN 角色
- ✅ **分頁與排序** - 列表查詢支援分頁與多種排序方式
- ✅ **資料驗證** - 完整的輸入驗證與錯誤處理
- ✅ **資料庫版本控制** - 使用 Liquibase 管理 schema 變更
- ✅ **索引優化** - 針對查詢場景建立適當索引
- ✅ **單元測試** - 完整的測試覆蓋率

---
<a id="documentation-structure"></a>
## 📖 文件結構

### 建議閱讀順序

1. **系統總覽** - [`docs/fsd/dcard-lite-forum-fsd-v1.md`](docs/fsd/dcard-lite-forum-fsd-v1.md)
   - 專案目標、功能列表、系統架構、非功能性需求

2. **API 規格** - [`docs/api/api-spec.yaml`](docs/api/api-spec.yaml)
   - OpenAPI 3.0 格式的完整 API 文件

3. **功能模組 (RP 文件)** - 每個模組包含詳細的設計與測試案例
   - RP-001: [註冊與登入](docs/rp/001-register-login.md)
   - RP-002: [看板列表](docs/rp/002-listboards.md)
   - RP-003: [發表文章](docs/rp/003-create-post.md)
   - RP-004: [文章列表](docs/rp/004-list-posts.md)
   - RP-005: [文章詳情](docs/rp/005-get-post.md)
   - RP-006: [編輯/刪除文章](docs/rp/006-update_delete_post.md)
   - RP-007: [文章留言](docs/rp/007-create-comment.md)

---
<a id="project-structure"></a>
## 📁 專案結構

```
demo/
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── common/
│   │   │   │   └── security/
│   │   │   │       └── JwtService.java          # JWT 服務
│   │   │   ├── user/
│   │   │   │   ├── dto/
│   │   │   │   │   └── RegisterUserRequest.java # 註冊 DTO
│   │   │   │   └── ...
│   │   │   ├── board/
│   │   │   │   ├── dto/
│   │   │   │   │   └── BoardItem.java           # 看板 DTO
│   │   │   │   └── ...
│   │   │   ├── post/
│   │   │   │   ├── service/
│   │   │   │   │   └── PostService.java         # 文章服務
│   │   │   │   ├── enums/
│   │   │   │   │   └── PostStatus.java          # 文章狀態枚舉
│   │   │   │   └── ...
│   │   │   └── comment/
│   │   │       └── ...
│   │   └── resources/
│   │       ├── application.yml                   # 應用配置
│   │       └── db/changelog/                     # Liquibase 變更集
│   │           ├── db.changelog-master.yaml
│   │           └── changes/
│   │               ├── 001-init.yaml
│   │               ├── 002-create-boards.yaml
│   │               ├── 003-create-posts.yaml
│   │               ├── 005-add-indexes-to-posts.yaml
│   │               └── 006-create-comments.yaml
│   └── test/                                     # 單元測試
│       └── java/com/example/demo/
└── docs/                                         # 文件目錄
    ├── fsd/                                      # 功能規格文件
    ├── api/                                      # API 規格
    ├── rp/                                       # 模組設計文件
    └── db/                                       # 資料庫文件
```

---
<a id="prerequisites"></a>
## 📋 先決條件

- **Java 17** 或更高版本
- **MySQL 8.0+** (可連線的資料庫實例)
- **Maven** (建議使用專案內的 Maven Wrapper)

---
<a id="quick-start"></a>
## 🚀 快速開始

### 1. 克隆專案

```bash
git clone <repository-url>
cd demo
```

### 2. 設定資料庫

在 MySQL 中建立資料庫:

```sql
CREATE DATABASE dcard_lite CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 設定應用配置

編輯 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dcard_lite
    username: your_username
    password: your_password

jwt:
  secret-key: your-secret-key-at-least-256-bits
  expiration: 86400000  # 24 小時 (毫秒)
```

> 💡 JWT 配置說明請參考 [`JwtService.java`](src/main/java/com/example/demo/common/security/JwtService.java)

### 4. 啟動應用

**使用 Maven Wrapper (推薦):**

```bash
# macOS/Linux
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

應用將在 `http://localhost:8080` 啟動

### 5. 驗證啟動

```bash
curl http://localhost:8080/actuator/health
```

---
<a id="testing"></a>
## 🧪 測試

### 執行所有測試

```bash
# macOS/Linux
./mvnw test

# Windows
mvnw.cmd test
```

### 執行特定測試類別

```bash
./mvnw test -Dtest=PostServiceTest
```

### 測試覆蓋範例

- 單元測試: [`PostServiceTest`](src/test/java/com/example/demo/post/service/PostServiceTest.java)
- 更多測試案例請參考各 RP 文件的 Test 章節

---
<a id="api-overview"></a>
## 📡 API 概覽

> 完整的 API 規格請參考 [`docs/api/api-spec.yaml`](docs/api/api-spec.yaml)

### 認證 (Auth)

| Method | Endpoint            | Description | RP Doc |
|--------|---------------------|-------------|--------|
| POST   | /users/register     | 使用者註冊 | [RP-001](docs/rp/001-register-login.md) |
| POST   | /users/login        | 使用者登入 | [RP-001](docs/rp/001-register-login.md) |

### 看板 (Boards)

| Method | Endpoint | Description | Auth | RP Doc |
|--------|----------|-------------|------|--------|
| GET    | /boards  | 查詢看板列表(支援分頁、keyword) | ❌ | [RP-002](docs/rp/002-listboards.md) |

### 文章 (Posts)

| Method | Endpoint                    | Description | Auth | RP Doc |
|--------|----------------------------|-------------|------|--------|
| POST   | /boards/{boardId}/posts    | 發表文章 | ✅ | [RP-003](docs/rp/003-create-post.md) |
| GET    | /boards/{boardId}/posts    | 查詢文章列表(分頁、排序) | ❌ | [RP-004](docs/rp/004-list-posts.md) |
| GET    | /posts/{postId}            | 查詢文章詳情 | ❌ | [RP-005](docs/rp/005-get-post.md) |
| PATCH  | /posts/{postId}            | 編輯文章(作者/ADMIN) | ✅ | [RP-006](docs/rp/006-update_delete_post.md) |
| DELETE | /posts/{postId}            | 刪除文章(作者/ADMIN) | ✅ | [RP-006](docs/rp/006-update_delete_post.md) |
| POST   | /posts/{postId}/comments   | 新增留言 | ✅ | [RP-007](docs/rp/007-create-comment.md) |

### 文章排序選項

- `latest` - 依照建立時間(新到舊)
- `hot` - 依照熱門分數(高到低)

---
<a id="error-response"></a>
## ⚠️ 錯誤回應格式

所有錯誤回應遵循統一格式:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "VALIDATION_FAILED",
  "code": "EMAIL_INVALID",
  "path": "/users/register",
  "timestamp": "2025-12-25T10:00:00Z"
}
```

### 常見錯誤碼

| HTTP Status | Message | Code | 說明 |
|-------------|---------|------|------|
| 400 | VALIDATION_FAILED | EMAIL_INVALID | Email 格式錯誤 |
| 400 | VALIDATION_FAILED | PASSWORD_INVALID | 密碼格式錯誤 |
| 400 | VALIDATION_FAILED | TITLE_INVALID | 標題驗證失敗 |
| 401 | UNAUTHORIZED | SECURITY_UNAUTHORIZED | 未提供或無效的 JWT Token |
| 401 | UNAUTHORIZED | AUTHENTICATION_FAILED | 登入失敗 |
| 403 | FORBIDDEN | NOT_POST_AUTHOR | 非文章作者或管理員 |
| 404 | NOT_FOUND | POST_NOT_FOUND | 文章不存在 |
| 404 | NOT_FOUND | BOARD_NOT_FOUND | 看板不存在 |
| 409 | CONFLICT | EMAIL_ALREADY_EXISTS | Email 已被註冊 |

---
<a id="database-schema"></a>
## 🗄️ 資料庫架構

### 主要資料表

#### users
- 使用者資料
- 支援角色控制 (USER/ADMIN)
- Email 唯一索引

#### boards
- 看板資料
- 看板名稱唯一索引

#### posts
- 文章資料
- 支援軟刪除 (status: ACTIVE/DELETED)
- 包含 like_count, comment_count, hot_score 快取欄位
- 索引優化:
  - `idx_posts_board_created` - 優化「最新」排序
  - `idx_posts_board_hot` - 優化「熱門」排序

#### comments
- 留言資料
- 關聯至 posts 與 users

### Schema 變更管理

使用 Liquibase 管理資料庫版本:

```
src/main/resources/db/changelog/
├── db.changelog-master.yaml          # 主控檔案
└── changes/
    ├── 001-init.yaml                  # 初始化 users 表
    ├── 002-create-boards.yaml         # 建立 boards 表
    ├── 003-create-posts.yaml          # 建立 posts 表
    ├── 004-fix-typo-posts.yaml        # 修正欄位拼寫
    ├── 005-add-indexes-to-posts.yaml  # 新增效能索引
    └── 006-create-comments.yaml       # 建立 comments 表
```

---
<a id="references"></a>
## 📚 參考資訊

### 排序與索引設計

文章列表查詢針對不同排序方式建立了對應索引:

- **最新排序** (`sort=latest`)
  - 使用索引: `idx_posts_board_created (board_id, created_at)`
  - 排序依據: `created_at DESC`

- **熱門排序** (`sort=hot`)
  - 使用索引: `idx_posts_board_hot (board_id, hot_score)`
  - 排序依據: `hot_score DESC`

詳細說明請參考 [RP-004](docs/rp/004-list-posts.md)

### 驗證規則範例

#### 註冊驗證
- Name: 1-20字元,不可純數字或純符號
- Email: 有效格式,最長100字元,儲存前轉小寫
- Password: 8-12字元,至少包含一個字母和數字

參考: [`RegisterUserRequest.java`](src/main/java/com/example/demo/user/dto/RegisterUserRequest.java)

#### 文章驗證
- Title: 1-50字元,不可純空白,禁止 `<` `>` 符號
- Body: 1-300字元,不可純空白,允許換行,禁止 `<` `>` 符號

參考: [RP-003](docs/rp/003-create-post.md)

---
<a id="deployment"></a>
## 🔧 打包部署

### 建立 JAR 檔案

```bash
./mvnw clean package
```

生成的 JAR 位於 `target/demo-*.jar`

### 執行 JAR

```bash
java -jar target/demo-*.jar
```

---
<a id="other-docs"></a>
## 📝 其他文件

- [Spring Boot 幫助文件](HELP.md)
- [OpenAPI 規格](docs/api/api-spec.yaml)
- [實體關聯圖](docs/db/er-map.png)
- [系統 FSD](docs/fsd/dcard-lite-forum-fsd-v1.md)

---
<a id="license"></a>
## 📄 授權

此專案為教學與面試展示用途。

---
<a id="contributing"></a>
## 🤝 貢獻

歡迎提交 Issue 和 Pull Request!

---

**最後更新:** 2025-12-25