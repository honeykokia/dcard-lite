# rp-005 取得文章詳情
## Use Case
### Who?
- Guest
### What?
- 點選文章取得文章內容
### Success?
- 取得文章內容成功，回傳200 Ok
### Failure?
- 取得文章內容失敗，回傳400 Bad Request

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
- PostId (Path Parameter)
    - **格式檢查**
        - 必填 (NOT NULL, NOT BLANK)
        - 必須為正整數 (Integer > 0)
    - **業務檢查**
        - 該ID對應的文章必須存在
## Error Response Format
> 本系統錯誤回應採固定格式：`message` 表示錯誤大類（如 `VALIDATION_FAILED` / `UNAUTHORIZED` / `CONFLICT` / `NOT_FOUND` / `INTERNAL_ERROR`），`code` 表示細項原因（如 `NAME_INVALID` / `EMAIL_ALREADY_EXISTS`）。

```json  {  
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
            - postId <0
            - postId = "invalid Integer"
            - postId = null
            - postId = "   "
            - postId = ""
- **404 Not Found**
    - `message` : `NOT_FOUND`
    - `code` :
        - `POST_NOT_FOUND`
            - 請求的postId 資料庫並不存在該文章
            - 該文章status為 `DELETED`
## API Contract
### `GET /posts/{postId}`
#### Request
##### Path
- `postId` (Require)
#### Response (Success)
```json
{
	"postId": 1,
	"boardId": 2,
	"boardName": "軟體版",
	"authorId": 1,
	"authorName": "Leo",
	"title": "想請問關於SpringBoot問題",
	"body": "我在建立專案時遇到奇怪的問題，能幫我看看嗎?",
	"likeCount": 10,
    "commentCount": 5,
    "createdAt": "2026-01-28T15:30:00Z"
}
```
#### Response (Error)
- **400 Bad Request**
    - `VALIDATION_FAILED`
        - `PATH_FORMAT_ERROR`
- **404 Not Found**
    - `NOT_FOUND`
        - `POST_NOT_FOUND`

## Service Logic
### PostService
#### Use Case: getPost
- **Method Signature**
```java
GetPostResponse getPost(long postId);
```
- **Transaction**
    - `@Transactional(readOnly = true)` (優化讀取效能)
- **Input Model**
    - `postId` (long)
        - 來源 : ParamVariable (`/posts/{postId}`)
- **Return Model**
    - `response` (GetPostResponse)
        - `Fields`
            - postId (long)
            - authorId (long)
            - authorName (String)
            - boardId (long)
            - boardName (String)
            - title (String)
            - body (String)
            - likeCount (int)
            - commentCount (int)
            - createdAt (Instant)
- **Logic Steps**
    - **STEP1 搜尋 Active 文章** : 呼叫 `PostRepo.findByIdAndStatus(long postId, PostStatus status)`
    - **STEP2 處理例外** : 若結果為空，拋出例外(`POST_NOT_FOUND`)
    - **STEP3 資料轉換** : 將`Post` entity 轉換成dto `GetPostResponse`
    - **STEP4 回傳資料** : 回傳 `GetPostResponse`
## Data Access
### `PostRepository`
```java
@EntityGraph(attributePaths = {"author", "board"})
Optional<Post> findByPostIdAndStatus(long postId, PostStatus status);
```

## Test
### `GET /posts/{postId}`
#### Test Seed
- DB 中`posts` 中存在二筆 post ：
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

#### 查詢文章詳情成功
- Given :
    - Test Seed
    - PostId = 1 (PathVariable)
- When :
    - 呼叫 `GET /posts/1`
- Then :
    - `status`
        - 200 ok
    - `ResponseBody`
        - postId = 1
        - authorId = 1
        - authorName = "Leo" (從關聯取出)
        - boardId = 2
        - boardName = "軟體版" (從關聯取出)
        - title = "關於SpringBoot問題"
        - body = "請問如何創建專案?"
        - likeCount = 0
        - commentCount = 0
        - createdAt = "2025-12-25 10:00:00"

#### 查詢文章詳情失敗
##### path驗證失敗
- Given :
    - Test Seed
        - **Secnario Inputs (`postId_input`)**
        - 0
        - -1
        - "abc"
- When :
    - 呼叫 `GET /posts/{postId_input}`
- Then :
    - `status`
        - 400 Bad Request
    - `ResponseBody`
        - `status` = 400
        - `error` = "Bad Request"
        - `message` = "VALIDATION_FAILED"
        - `code` = "PATH_FORMAT_ERROR"
        - `path` = "/boards/{postId_input}/posts"
        - `timestamp` = "..."
##### 文章不存在
- Given :
    - Test Seed
    - **Secnario Inputs (`postId_input`)**
        - 2 (已被刪除)
        - 3 (DB不存在)
- When :
    - 呼叫 `GET /posts/postId_input`
- Then :
    - `status`
        - 404 Not Found
    - `ResponseBody`
        - `status` = 404
        - `error` = "Not Found"
        - `message` = "NOT_FOUND"
        - `code` = "POST_NOT_FOUND"
        - `path` = "/posts/{postId_input}"
        - `timestamp` ="..."