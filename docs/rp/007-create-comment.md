# rp-007 文章留言
## Use Case
### Who?
- User
### What?
- 使用者可以在指定文章底下新增留言
### Success?
- 留言成功，201 Created
### Failure?
- 留言失敗，400 Bad Request
## DB Changes
- Table: comments
    - Column
        - `comment_id` (PK, BIGINT, AUTO INCREMENT, NOT NULL)
        - `post_id` (FK, BIGINT)
        - `author_id` (FK, BIGINT)
        - `body` (VARCHAR(200), NOT NULL)
        - `status` (VARCHAR(20), NOT NULL, DEFAULT = "ACTIVE")
        - `created_at` (DATETIME(6), NOT NULL)
        - `updated_at` (DATETIME(6), NOT NULL)
    - Constraints
        - `PK`
            - `pk_comments` (comment_id)
        - `FK`
            - `fk_comments_post_id`：comments(post_id) → posts(post_id)
            - `fk_comments_author_id`：comments(author_id) → users(user_id)

## Validation Rules
- `postId` (Path Parameter)
    - 必填 (NOT NULL, NOT BLANK)
    - 必須為正整數 (Integer > 0)
- `body`
    - 必填 (NOT NULL)
    - 不可為空字串或僅包含空白字元
    - 最大長度: 200
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
    - `code`:
        - `PATH_FORMAT_ERROR`
            - postId <= 0
            - postId = ""
            - postId = "   "
            - postId = null
            - postId = "invalid Integer"
        - `BODY_INVALID`
            - body = null
            - body = ""
            - body = "            "
            - body.include `< >`
            - body.length > 200
- **401 Unauthorized**
    - `message` : `UNAUTHORIZED`
    - `code` :
        - `SECURITY_UNAUTHORIZED`
            - Header 缺少 Authorization
            - Token 無效或過期
- **404 Not Found**
    - `message` : `NOT_FOUND`
    - `code` :
        - `POST_NOT_FOUND`
            - 請求的postId 資料庫並不存在該文章

## API Contract
### `POST /posts/{postId}/comments`
#### Request
##### Path
- `postId` (Require)
##### Body (application/json)
```json
{
	"body": "我覺得你的問題要再說詳細一點..."
}
```
#### Response (Success)
```json
{
	"commentId": 1
	
}
```

#### Response (Error)
- **400 Bad Request**
    - `VALIDATION_FAILED`
        - `PATH_FORMAT_ERROR`
        - `BODY_INVALID`
- **401 Unauthorized**
    - `UNAUTHORIZED`
        - `SECURITY_UNAUTHORIZED`
- **404 Not Found**
    - `NOT_FOUND`
        - `POST_NOT_FOUND`

## Service Logic
### `CommentService`
#### Use Case: createComment
- **Method Signature**
```java
	CreateCommentResponse createComment(long postId, User currentUser,  CreateCommentRequest request);
```
- **Transaction**
    - `@Transactional`
- **Input Model**
    - `postId` (long)
        - 來源: Pathvariable (/posts/{postId}/comments)
    - `request` (CreateCommentRequest)
        - 來源: `RequestBody`
        - `field`
            - `body` (String)
- **Return Model**
    - `CreateCommentResponse`
        - `commentId` (long)
- **Logic Steps**
    - **STEP1**
        - 搜尋ACTIVE文章: 呼叫 `findBasicByPostIdAndStatus(postId, PostStatus.ACTIVE)`
        - 若結果為空，拋出例外 `POST_NOT_FOUND`
    - **STEP2**
        - 如果文章存在，呼叫 `incrementCommentCount(postId)` 使文章留言數+1
        - 使用原子更新防止流量過大，讀取到舊數字
    - **STEP3**
        - 建立 `Comment`物件
        - 設定 `postId` : 關聯至STEP1查出的`Post`
        - 設定 `authorId` : 由 `currentUser` 關聯
        - 設定 `body` : 根據 `CreateCommentRequest.getBody()`內容寫入
        - 設定 `status` : 預設為 `ACTIVE`
        - 呼叫 `CommentRepository.save(comment)`
    - **STEP4**
        - 資料回傳`CreateCommentResponse`
## Data Access
### `PostRepository`
```java
@Modifying  
@Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.postId = :postId")  
void incrementCommentCount(long postId);  
  
Optional<Post> findBasicByPostIdAndStatus(long postId, PostStatus status);
```
### `CommentRepository`
```java
Comment save(Comment comment);
```

## Test
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

### `POST /posts/{postId}/comments`
#### 新增留言成功
- Given
    - postId = 1
- When
    - 呼叫 `POST /posts/1/comments`
    - **RequestBody**
      ```json
      {
          "body": "問題可以再描述清楚一點嗎?"
      }
      ```
- Then
    - `status`
        - 201 Created
    - `ResponseBody`
        - `commentId` = 1
#### 新增留言失敗
##### Path驗證失敗
- Given
    - **Scenario Inputs** (`postId_input`)
        - 0
        - -1
        - "abc"
- When
    - 呼叫 `POST /posts/{postId_input}/comments`
- Then
    - `status`
        - 400 Bad Request
    - `ResponseBody`
        - `status` = 400
        - `error` = "Bad Request"
        - `message` = "VALIDATION_FAILED"
        - `code` = "PATH_FORMAT_ERROR"
        - `path` = "/posts/{postId_input}/comments"
        - `timestamp` = "..."
##### Body驗證失敗
- Given
    - postId
    - **Scenario Inputs** (`body_input`)
        - "" (空白格式)
        - "    " (字元都是空白)
        - `a.repeat(201)` (超過最大長度 200)**
        - `<script>alert(1)</script>` (包含完整標籤)
- When
    - 呼叫 `POST /posts/{postId}/comments`
    - **RequestBody**
      ```json
      {
          "body": "{body_input}"
      }
      ```
- Then
    - `status`
        - 400 Bad Request
    - `ResponseBody`
        - `status` = 400
        - `error` = "Bad Request"
        - `message` = "VALIDATION_FAILED"
        - `code` = "BODY_INVALID"
        - `path` = "/posts/1/comments"
        - `timestamp` = "..."

##### 使用者驗證失敗
- Given
    - postId = 1
    - **Request Context**
        - 使用者以「匿名身分」發送請求 (未攜帶 Token)
        - 用者持有「無效憑證」發送請求 (Token 錯誤或過期)
- When
    - 呼叫 `POST /posts/1/comments`
- Then
    - `status`
        - 401 Unauthorized
    - `ResponseBody`
        - `status` = 401
        - `error` = "Unauthorized"
        - `message` = "UNAUTHORIZED"
        - `code` = "SECURITY_UNAUTHORIZED"
        - `path` = "/posts/1/comments"
        - `timestamp` = "..."
##### 文章不存在
- Given
    - **Scenario Inputs** (`postId_input`)
        - 2 (在Test Seed中 狀態是已被刪除 `DELETED`)
        - 3 (在Test Seed並不存在)
- When
    - 呼叫 `POST /posts/{postId_input}/comments`
- Then
    - `status`
        - 404 Not Found
    - `ResponseBody`
        - `status` = 404
        - `error` = "Not Found"
        - `message` = "NOT_FOUND"
        - `code` = "POST_NOT_FOUND"
        - `path` = "/posts/{postId_input}/comments"
        - `timestamp` = "..."