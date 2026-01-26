# rp-004 文章列表
## Use Case
### Who?
- Guest
- User
### What?
瀏覽看板文章，支援分頁、排序（latest/hot）
### Success?
傳回指定看板文章列表，回傳200 Ok
### Failure?
文章列表查詢失敗，回傳400 Bad Request
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
## DB Changes
> *本次需新增的索引*
- Target Table: `posts`
- Actions: **Add Indexes**
    1. **`idx_posts_board_created`**
        - Columns: `(board_id, created_at)`
        - Purpose: 優化看板文章列表「最新」排序
    2. **`idx_posts_board_hot`**
        - Columns: `(board_id, hot_score)`
        - Purpose: 優化看板文章列表「熱門」排序
## Validation Rules
- boardId (Path Parameter)
    - **格式檢查**
        - 必填 (NOT NULL, NOT BLANK)
        - 必須為正整數 (Integer, >0)
    - **業務檢查**
        - 該ID對應的看板必須存在
- page
    - **格式檢查**
        - 預設值為1
        - 必須為正整數 且 `page >= 1`
- pageSize
    - **格式檢查**
        - 預設值為40
        - 必須為正整數 且 `1 <= pageSize <= 200`
- sort
    - **格式檢查**
        - 預設值為 latest
        - 僅接受以下值 (Case Insensitive 忽略大小寫)
            - `latest`
            - `hot`
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

## Error Mapping ( Domian → HTTP )
- **400 Bad Request**
    - `message` : `VALIDATION_FAILED`
    - `code` :
        - `PATH_FORMAT_ERROR`
            - boardId < 0
            - boardId = "invalidInteger"
            - boardId = null
            - boardId = "   "
            - boardId = ""
        - `PAGE_INVALID`
            - page <1
            - page 非正整數
        - `PAGE_SIZE_INVALID`
            - pageSize 不在 1..200
            - pageSize 非正整數
        - `PARAM_FORMAT_ERROR`
            - sort 值不在允許清單內 (latest, hot)
            - sort = ""
            - sort = "  "
            - sort = null
- **404 Not Found**
    - `message` : `NOT_FOUND`
    - `code` :
        - `BOARD_NOT_FOUND`
            - 請求的boardId 資料庫並不存在該看板
- **500 Internal Server Erro**
    - `message` : `INTERNAL_ERROR`
    - `code` :
        - `UNEXPECTED_ERROR`
            - DB/Repository error 或未預期error

## API Contract
### `GET /boards/{boardId}/posts`
#### Request
##### Path
- `boardId` (Require)
##### Query
- `page` (Optional, default = 1)
- `pageSize` (Optional, default = 40)
- `sort` (Optional, default = "latest")
    - `latest`: 依發文時間排序 (最新在前)
    - `hot`: 依熱門分數排序 (高分在前)
#### Response (Success)
```json
{
	"page": 1,
	"pageSize": 40,
	"total": 2,
	"items":[
		{
			"postId": 2,
			"authorId": 1,
			"authorName": "Leo",
			"boardId": 2,
			"boardName":"軟體版",
			"title": "關於Java問題",
			"likeCount": 0,
			"hot_score": 0.0,
			"status": "ACTIVE",
			"createdAt": "2026-01-01 12:00:00"
		},
		{
			"postId": 1,
			"authorId": 1,
			"authorName": "Leo",
			"boardId": 2,
			"boardName":"軟體版",
			"title": "關於SpringBoot問題",
			"likeCount": 0,
			"hot_score": 0.0,
			"status": "ACTIVE",
			"createdAt": "2025-12-25 10:00:00"
		}
	]

}
```

#### Response (Error)
- **400 Bad Request**
    - `VALIDATION_FAILDE`
        - `PATH_FORMAT_ERROR`
        - `PAGE_INVALID`
        - `PARAM_FORMAT_ERROR`
- **404 Not Found**
    - `NOT_FOUND`
        - `BOARD_NOT_FOUND`
- **500 Internal Server Error**
    - `INTERNAL_ERROR`
        - `UNEXPECTED_ERROR`
## Service Logic
### `PostService`
#### Use Case: listPosts
- **Method Signature**
```java
ListPostsResponse listPosts(long boardId, ListPostsRequest request);
```
- **Transaction**
    - `@Transactional(readOnly = true)` (優化讀取效能)
- **Input Model
    - `boardId` (Long)
        - 來源: PathVariable (`/boards/{boardId}`)
    - `request` (ListPostsRequest)
        - 來源: QueryParam
        - `Fields`
            - page (int)
            - pageSize (int)
            - sort (String)
- **Return Model**
    - `response` (ListPostsResponse)
        - page (int)
        - pageSize (int)
        - total (long)
        - items (List)
            - postId (long)
            - authorId (long)
            - authorName (String)
            - boardId (long)
            - boardName (String)
            - title (String)
            - likeCount (int)
            - hotScore (Double)
            - status (String)
            - createdAt (Instant)

- **Logic Steps**
    1. **Validate Board**: 呼叫 `BoardRepo.existsById(boardId)`，若為 false 拋出 `BOARD_NOT_FOUND`。
    2. **Determine Sort**:
        - 若 `request.sort == "hot"` -> 使用 `Sort.by(DESC, "hotScore")`
        - 否則 (預設) -> 使用 `Sort.by(DESC, "createdAt")`
    3. **Query**: 建立 `Pageable` (page-1, pageSize, sort)，呼叫 `PostRepo.findByBoardId`。
    4. **Map**: 將回傳的 `Page<PostEntity>` 轉換為 `ListPostsResponse`。
## Data Access
### `PostRepository`
```java
Page<PostEntity> findByBoardId(Long boardId, Pageable pageable);
```
### `BoardRepository`
```java
boolean existsById(long boardId);
```

## Test
### `GET /boards/{boardId}/posts`
#### Test Seed
- DB 中`boards` 中存在一筆 board ：
    - 第一筆：
        - board_id = 2
        - name = "軟體版"
        - description = "聊軟體相關的知識"
        - created_at = "2025-12-25 10:00:00"
- DB 中`posts` 中存在二筆 post ：
    - 第一筆 (舊文章)：
        - post_id = 1
        - author_id = 1
        - board_id = 2
        - title = "關於SpringBoot問題"
        - like_count = 0
        - hot_score = 0
        - status = "ACTIVE"
        - created_at = "2025-12-25 10:00:00"

    - 第二筆 (新文章)：
        - post_id = 2
        - author_id = 1
        - board_id = 2
        - title = "關於JAVA問題"
        - like_count = 0
        - hot_score = 0
        - status = "ACTIVE"
        - created_at = "2026-01-01 12:00:00"
#### 查詢文章列表成功
- Given : Test Seed
- When : 呼叫 `GET /boards/2/posts`
- Then :
    - `status` :
        - 200 ok
    - `ResponseBody:
        - page = 1
        - pageSize = 40
        - total = 2
        - items (注意預設為最新，由新到舊)
            - 第一筆：
                - post_id = 2
                - author_id = 1
                - authorName = "Leo"
                - board_id = 2
                - boardName = "軟體版"
                - title = "關於JAVA問題"
                - like_count = 0
                - hot_score = 0
                - status = "ACTIVE"
            - 第二筆：
                - post_id = 1
                - author_id = 1
                - authorName = "Leo"
                - board_id = 2
                - boardName = "軟體版"
                - title = "關於SpringBoot問題"
                - like_count = 0
                - hot_score = 0
                - status = "ACTIVE"

#### 查詢文章列表失敗
##### path驗證失敗
- Given :
    - Test Seed
    - **Scenario Inputs** (`boardId_input`)
        - -1
        - "abc"
- When : 呼叫 `GET /boards/{boardId_input}/posts`
- Then :
    - `status`
        - 400 Bad Request
    - `ResponseBody`
        - `status` = 400
        - `error` = "Bad Request"
        - `message` = "VALIDATION_FAILED"
        - `code` = "PATH_FORMAT_ERROR"
        - `path` = "/boards/{boardId_input}/posts"
        - `timestamp` = "..."
##### page驗證失敗
- Given :
    - Test Seed
    - **Scenario (Input vs Exception)**

| **page_input** | **expected_code**      | **說明**    |
| -------------- | ---------------------- | --------- |
| `-1`           | **PAGE_INVALID**       | 數值違反最小值限制 |
| `"invalid"`    | **PARAM_FORMAT_ERROR** | 資料型別格式錯誤  |

- When : 呼叫 `GET /boards/2/posts?page={page_input}`
- Then :
    - `status`
        - 400 Bad Request
    - `ResponseBody`
        - `status` = 400
        - `error` = "Bad Request"
        - `message` = "VALIDATION_FAILED"
        - `code` = {expected_code}
        - `path` = "GET /boards/2/posts?page={page_input}"
        - `timestamp` ="..."
##### pageSize驗證失敗
- Given :
    - Test Seed
    - **Scenarios (Input vs Expectation)**：

| **page_size_input** | **expected_code**      | **說明**    |
| ------------------- | ---------------------- | --------- |
| `-1`                | **PAGE_SIZE_INVALID**  | 數值違反最小值限制 |
| `201`               | **PAGE_SIZE_INVALID**  | 數值違反最大值限制 |
| `"invalid"`         | **PARAM_FORMAT_ERROR** | 資料型別格式錯誤  |
- When : 呼叫 `GET /boards/2/posts?pageSize={page_size_input}`
- Then :
    - `status`
        - 400 Bad Request
    - `ResponseBody`
        - `status` = 400
        - `error` = "Bad Request"
        - `message` = "VALIDATION_FAILED"
        - `code` = {expected_code}
        - `path` = "GET /boards/2/posts?pageSize={page_size_input}"
        - `timestamp` ="..."
##### sort驗證失敗
- Given :
    - Test Seed
    - **Scenarios (Input vs Expectation)**：

| **sort_input** | **expected_code**      | **說明**           |
| -------------- | ---------------------- | ---------------- |
| `"custom"`     | **PARAM_FORMAT_ERROR** | 只允許"latest, hot" |
| `1`            | **PARAM_FORMAT_ERROR** | 資料型別格式錯誤         |
- When : 呼叫 `GET /boards/2/posts?sort={sort_input}`
- Then :
    - `status`
        - 400 Bad Request
    - `ResponseBody`
        - `status` = 400
        - `error` = "Bad Request"
        - `message` = "VALIDATION_FAILED"
        - `code` = {expected_code}
        - `path` = "GET /boards/2/posts?sort={sort_input}"
        - `timestamp` ="..."

##### 看板不存在
- Given :
    - Test Seed
    - `PathVariable`
        - boardId = 3 (DB不存在)
- When : `GET /boards/3/posts`
- Then :
    - `status`
        - 404 Not Found
    - `ResponseBody`
        - `status` = 404
        - `error` = "Not Found"
        - `message` = "NOT_FOUND"
        - `code` = "BOARD_NOT_FOUND"
        - `path` = "/boards/3/posts"
        - `timestamp` ="..."

