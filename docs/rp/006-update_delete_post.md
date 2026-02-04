# rp-006 編輯/刪除文章
## Use Case
### Who?
- admin
- author (文章作者)
### What?
- 文章作者或管理員可以編輯 / 刪除文章
### Success
- 刪除 / 編輯成功回傳200 ok
### Failure
- 刪除 / 編輯失敗 回傳400 Bad Request
## DB Reference
- Table: `posts`
	- Column:
		- post_id (PK , BIGINT , AUTO INCREMENT ,NOT NULL)
		- board_id (FK ,BIGINT)
		- author_id (FK ,BIGINT)
		- title (VARCHAR(50) ,NOT NULL)
		- body (VARCHAR(300), NOT NULL)
		- like_count (INT, NOT NULL, DEFAULT=0)
		- comment_count (INT, NOT NULL, DEFAULT=0)
		- hot_score (DOUBLE, NOT NULL, DEFAULT=0)
		- status (VARCHAR(20), NOT NULL, DEFAULT='ACTIVE')
		- created_at (DATETIME(6), NOT NULL)
		- updated_at (DATETIME(6), NOT NULL)
	- Constraints
		- PK: `pk_posts` (post_id)
		- FK:  
		  - `fk_posts_board_id` : posts(board_id) → boards(board_id)  
		  - `fk_posts_author_id`: posts(author_id) → users(user_id)
- Indexes
	- **`idx_posts_board_created`**
		- Columns: `(board_id, created_at)`
		- Purpose: 優化看板文章列表「最新」排序
	- **`idx_posts_board_hot`**
		- Columns: `(board_id, hot_score)`
		- Purpose: 優化看板文章列表「熱門」排序

## Validation Rules
- `postId` (Path Parameter)
	- 必填 (NOT NULL, NOT BLANK)
	- 必須為正整數 (Integer > 0)
- `title` (RequestBody)
	- optional (可不提供；若提供則須通過以下驗證)
	- 不可為空字串或僅包含空白字元
	- 最大長度: 50
	- 簡單的XSS 防護不允許 `< >`
- `body` (RequestBody)
	- optional (可不提供；若提供則須通過以下驗證)
    - 不可為空字串或僅包含空白字元
	- 最大長度: 300
	- 簡單的XSS 防護 允許換行（`\n`），但不允許 `< >`
## Error Response Format
> 本系統錯誤回應採固定格式：`message` 表示錯誤大類（如 `VALIDATION_FAILED` / `UNAUTHORIZED` / `CONFLICT` / `NOT_FOUND` / `INTERNAL_ERROR`），`code` 表示細項原因（如 `NAME_INVALID` / `EMAIL_ALREADY_EXISTS`）。

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "VALIDATION_FAILED",
  "code": "NAME_INVALID",
  "path": "/users/register",
  "timestamp": "..."
}
```  
## Error Mapping ( Domain → HTTP )
- **400 Bad Request**
	- `message` : `VALIDATION_FAILED`
	- `code` :
		- `PATH_FORMAT_ERROR`
			- (postId or authorId) <=0
			- (postId or authorId) = null
			- (postId or authorId) = "    "
			- (postId or authorId) = ""
			- (postId or authorId) = "Invalid Integer"
		- `TITLE_INVALID`
			- title = ""
			- title = "    "
			- title.include `< >`
			- title.length > 50
		- `BODY_INVALID`
			- body = ""
			- body = "     "
			- body.include `< >`
			- body.length > 300
- **401 Unauthorized**
	- `message`: `UNAUTHORIZED`
	- `code`:
		- `SECURITY_UNAUTHORIZED`
			- Header 缺少 Authorization
			- Token 無效或過期
- **403 Forbidden**
	- `message` : `FORBIDDEN`
	- `code` :
		- `NOT_POST_AUTHOR`
			- User不是文章作者且也不是管理者
- **404 Not Found**
	- `message` : `NOT_FOUND`
	- `code` :
		- `POST_NOT_FOUND`
			- 請求的postId 資料庫並不存在該文章
			- 該文章status為 `DELETED`

## API Contract
### `DELETE /posts/{postId}`
#### Request
##### Path
- `postId` (Require)
#### Response (Success)
```json
{
	"postId": 1,
	"status": "DELETED"
}
```
#### Response (Error)
- **400 Bad Request**
	- `VALIDATION_FAILED`
		- `PATH_FORMAT_ERROR`
- **401 Unauthorized**
	- `UNAUTHORIZED`
		- `SECURITY_UNAUTHORIZED`
- **403 Forbidden**
	- `FORBIDDEN`
		- `NOT_POST_AUTHOR`
- **404 Not Found**
	- `NOT_FOUND`
		- `POST_NOT_FOUND`
### `PATCH /posts/{postId}`
#### Request
##### Path
- `postId` (Require)
##### Body (application/json)
```json
{ 
	"title": "修改後的標題",
	"body": "修改後的內容"
}
```

#### Response (Success)
```json
{
	"postId": 1,
	"title": "想問關於Java的問題",
	"body" : "Java Stack使用場合是在什麼地方?"
}
```
#### Response (Error)
- **400 Bad Request**
	- `VALIDATION_FAILED`
		- `PATH_FORMAT_ERROR`
- **401 Unauthorized**
	- `UNAUTHORIZED`
		- `SECURITY_UNAUTHORIZED`
- **403 Forbidden**
	- `FORBIDDEN`
		- `NOT_POST_AUTHOR`
- **404 Not Found**
	- `NOT_FOUND`
		- `POST_NOT_FOUND`
## Service Logic
### `PostService`
#### Use Case: deletePost
- **Method Signature**
```java
DeletePostResponse deletePost(long postId, User currentUser);
```
- **Transaction**
	- `@Transactional`
- **Input Model**
	- `postId` (long)
		- 來源: PathVariable (`/posts/{postId}`)
	- `currentUser` (long)
		- 來源: AuthenticationPrincipal (從Security 取得 user)
- **Return Model**
	- `DeletePostResponse`
		- postId (long)
		- status (String)
- **Logic Steps**
	- **STEP1**
		- 搜尋ACTIVE文章: 呼叫 `findByPostIdAndStatus(long postId, PostStatus status)`
		- 若結果為空，拋出例外 `POST_NOT_FOUND`
	- **STEP2**
		- 檢查 `post.getAuthor().getId()` 是否等於 `userId`
		- 若不一致，且使用者非`ADMIN`，拋出例外 `NOT_POST_AUTHOR`
	- **STEP3**
		- 進行文章狀態修改，將Post 的status設為 `DELETED`並儲存`save()`
	- **STEP4**
		- 資料轉換將`Post` entity 轉換成 dto `DeletePostResponse`
	- **STEP5**
		- 資料回傳 `DeletePostResponse`
#### Use Case: updatePost
- **Method Signature**
```java
UpdatePostResponse updatePost(long postId, User currentUser, UpdatePostRequest request);
```
- **Transaction**
	- `@Transactional`
- **Input Model**
	- `postId` (long)
		- 來源: `PathVariable` (/posts/{postId})
	- `currentUser` (long)
		- 來源: AuthenticationPrincipal (從Security 取得 user)
	- `request` (UpdatePostRequest)
		- 來源: `RequestBody`
		- `field`
			- `title` (String)
			- `body` (String)
- **Return Model**
	- `UpdatePostResponse`
		- `postId` (long)
		- `title` (String)
		- `body` (String)
- **Logic Steps**
	- **STEP1**
		- 搜尋ACTIVE文章: 呼叫 `findByPostIdAndStatus(long postId, PostStatus status)`
		- 若結果為空，拋出例外 `POST_NOT_FOUND`
	- **STEP2**
		- 檢查 `post.getAuthor().getId()` 是否等於 `userId`
		- 若不一致，且使用者非`ADMIN`，拋出例外 `NOT_POST_AUTHOR`
	- **STEP3**
		- 進行文章標題或內容修改並儲存 `save()`
	- **STEP4**
		- 資料轉換將`Post` entity 轉換成 dto `UpdatePostResponse`
	- **STEP5**
		- 資料回傳 `UpdatePostResponse`
## Data Access
### `PostRepository`
```java
@EntityGraph(attributePaths = {"author", "board"})
Optional<Post> findByPostIdAndStatus(long postId, PostStatus status);
```

## Test
### `DELETE /posts/{postId}`
#### Test Seed (共用資料庫狀態)
此區塊套用於下方所有測試案例：
- DB`posts`中存在二筆post:
	- 第一筆 (舊文章)：
		- post_id = 1
		- author_id = 1
		- board_id = 2
		- title = "關於SpringBoot問題"
		- body = "請問如何創建專案?"
		- like_count = 0
		- commentCount = 0
		- status = "ACTIVE"
		- created_at = "2025-12-25 10:00:00"

	- 第二筆 (新文章)：
		- post_id = 2
		- author_id = 1
		- board_id = 2
		- title = "關於JAVA問題"
		- body = "stream是怎麼使用呢?"
		- like_count = 0
		- commentCount = 0
		- status = "DELETED"
		- created_at = "2026-01-01 12:00:00"

#### 刪除文章成功
- Given
	- userId = 1 (Mock User)
	- postId = 1
- When
	- 呼叫 `DELETE /posts/1`
- Then
	- `status`
		- 200 ok
	- `ResponseBody`
		- `postId` = 1
		- `status` = "DELETED"

#### 刪除文章失敗
##### Path驗證失敗
- Given
	- userId = 1 (Mock User)
	- **Scenario Inputs** (`postId_input`)
		- 0
		- -1
		- abc
- When
	- 呼叫 `DELETE /posts/{postId_input}`
- Then
	- `status`
		- 400 Bad Request
	- `ResponseBody`
		- `status` = 400
		- `error` = "Bad Request"
		- `message` = "VALIDATION_FAILED"
		- `code` = "PATH_FORMAT_ERROR"
		- `path` = "/posts/{postId_input}"
		- `timestamp` = "..."

##### 使用者驗證失敗
- Given
	- **Request Context**
		- 使用者以「匿名身分」發送請求 (未攜帶 Token)
		- 用者持有「無效憑證」發送請求 (Token 錯誤或過期)
- When
	- 呼叫 `DELETE /posts/1`
- Then
	- `status`
		- 401 Unauthorized
	- `ResponseBody`
		- `status` = 401
		- `error` = "Unauthorized"
		- `message` = "UNAUTHORIZED"
		- `code` = "SECURITY_UNAUTHORIZED"
		- `path` = "/posts/1"
		- `timestamp` = "..."
##### 使用者非作者或管理員
- Given
	- userId = 2 (Mock User)
	- postId=1
- When
	- 呼叫 `DELETE /posts/1`
- Then
	- `status`
		- 403 Forbidden
	- `ResponseBody`
		- `status` = 403
		- `error` = "Forbidden"
		- `message` = "FORBIDDEN"
		- `code` = "NOT_POST_AUTHOR"
		- `path` = "/posts/1"
		- `timestamp` = "..."

##### 文章不存在
- Given
	- Test Seed
	- userId = 1 (Mock User)
	- postId = 3
- When
	- 呼叫 `DELETE /posts/3`
- Then
	- `status`
		- 404 Not Found
	- `ResponseBody`
		- `status` = 404
		- `error` = "Not Found"
		- `message` = "NOT_FOUND"
		- `code` = "POST_NOT_FOUND"
		- `path` = "/posts/3"
		- `timestamp` = "..."

### `PATCH /posts/{postId}`
#### 文章編輯成功
##### 完整更新
- Given
	- userId = 1 (Mock User)
	- postId = 1
	- **Request Body**
	  ```json
      {
        "title": "新標題",
        "body": "新內容"
      }
      ```
- When
	- 呼叫 `PATCH /posts/1` (帶入 Body)
- Then
	- `status`: 200 OK
	- `ResponseBody`:
		- `postId`: 1
		- `title`: "新標題"
		- `body`: "新內容"

##### 部份更新 - 只改 Title
- Given
	- userId = 1
	- postId = 1
	- **Request Body**
	  ```json
      {
        "title": "只改標題"
      }
      ```
- When
	- 呼叫 `PATCH /posts/1`
- Then
	- `status`: 200 OK
	- `ResponseBody`:
		- `title`: "只改標題"
		- `body`: "請問如何創建專案?" (維持原值)

#### 文章編輯失敗
##### Path 驗證失敗
- Given
	- userId = 1 (Mock User)
	- **Secnario Inputs** (`postId_input`)
		- 0
		- -1
		- abc
- When
	- 呼叫 `PATCH /posts/{postId_input}`
- Then
	- `status`
		- 400 Bad Request
	- `ResponseBody`
		- `status` = 400
		- `error` = "Bad Request"
		- `message` = "VALIDATION_FAILED"
		- `code` = "PATH_FORMAT_ERROR"
		- `path` = "/posts/{postId_input}"
		- `timestamp` = "..."
##### Title 驗證失敗
- Given
	- userId = 1 (Mock User)
	- postId = 1
	- **Scenario Inputs** (`title_input`)
		- "" (空白格式)
		- "    " (字元都是空白)
		- `a.repeat(51)` (超過最大長度 50)
		- `<script>alert(1)</script>` (包含完整標籤)
- When
	- 呼叫 `PATCH /posts/1?title={title_input}`
- Then
	- `status`
		- 400 Bad Request
	- `ResponseBody`
		- `status` = 400
		- `error` = "Bad Request"
		- `message` = "VALIDATION_FAILED"
		- `code` = "TITLE_INVALID"
		- `path` = "/posts/1"
		- `timestamp` = "..."

##### Body 驗證失敗
- Given
	- userId = 1 (Mock User)
	- postId = 1
	- **Scenario Inputs** (`body_input`)
		- "" (空白格式)
		- "    " (字元都是空白)
		- `a.repeat(301)` (超過最大長度 300)**
		- `<script>alert(1)</script>` (包含完整標籤)
- When
	- 呼叫 `PATCH /posts/1?body={body_input}`
- Then
	- `status`
		- 400 Bad Request
	- `ResponseBody`
		- `status` = 400
		- `error` = "Bad Request"
		- `message` = "VALIDATION_FAILED"
		- `code` = "BODY_INVALID"
		- `path` = "/posts/1"
		- `timestamp` = "..."

##### 使用者驗證失敗
- Given
	- **Request Context**
		- 使用者以「匿名身分」發送請求 (未攜帶 Token)
		- 用者持有「無效憑證」發送請求 (Token 錯誤或過期)
- When
	- 呼叫 `PATCH /posts/1`
- Then
	- `status`
		- 401 Unauthorized
	- `ResponseBody`
		- `status` = 401
		- `error` = "Unauthorized"
		- `message` = "UNAUTHORIZED"
		- `code` = "SECURITY_UNAUTHORIZED"
		- `path` = "/posts/1"
		- `timestamp` = "..."
##### 使用者非作者或管理員
- Given
	- userId = 2 (Mock User)
	- postId=1
- When
	- 呼叫 `PATCH /posts/1`
- Then
	- `status`
		- 403 Forbidden
	- `ResponseBody`
		- `status` = 403
		- `error` = "Forbidden"
		- `message` = "FORBIDDEN"
		- `code` = "NOT_POST_AUTHOR"
		- `path` = "/posts/1"
		- `timestamp` = "..."

##### 文章不存在
- Given
	- Test Seed
	- userId = 1 (Mock User)
	- postId = 3
- When
	- 呼叫 `PATCH /posts/3`
- Then
	- `status`
		- 404 Not Found
	- `ResponseBody`
		- `status` = 404
		- `error` = "Not Found"
		- `message` = "NOT_FOUND"
		- `code` = "POST_NOT_FOUND"
		- `path` = "/posts/3"
		- `timestamp` = "..." 
