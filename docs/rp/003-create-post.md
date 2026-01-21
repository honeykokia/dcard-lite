# rp-003 發表文章
## Use Case
### Who?
- User
### What?
在指定看板下發表文章
### Success?
發表文章成功，回傳201 Created
### Failure?
發表文章失敗，回傳400 Bad Request
## DB Changes
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

## Validation Rules
- boardId (Path Parameter)
    - 必填
    - 格式檢查：必須為正整數 (Integer, > 0)
    - 業務檢查：該 ID 對應的看板必須存在 (若不存在則回傳 404)
- title
    - 必填 (不可為Null / 空字串 / 純空白)
    - 最大長度: 50
    - 簡單的XSS 防護不允許 `< >`
- body
    - 必填 (不可為Null / 空字串 / 純空白)
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

## Error Mapping
- **400 Bad Request**
    - `message`: `VALIDATION_FAILED`
    - `code`:
        - `TITLE_INVALID`
            - title = null
            - title = ""
            - title = "   "
            - title.include `< >`
            - title.length > 50
        - `BODY_INVALID`
            - body = null
            - body = ""
            - body = "    "
            - body.include `< >`
            - body.length > 300
        - `PATH_FORMAT_ERROR`
            - boardId = -1
            - boardId = "abc"
- **401 Unauthorized**
    - `message`: `UNAUTHORIZED`
    - `code`:
        - `SECURITY_UNAUTHORIZED`
            - Header 缺少 Authorization
            - Token 無效或過期
- **404 Not Found**
    - `message`: `NOT_FOUND`
    - `code`:
        - `BOARD_NOT_FOUND`
            - 資料庫找不到對應的 boardId
- **500 Internal Server Error**
    - `message` : `INTERNAL_ERROR`
    - `code`:
        - `UNEXPECTED_ERROR`
            - DB/Repository error 或未預期error
## API Contract
### `POST /boards/{boardId}/posts`
#### Request Path + JSON
##### Headers
- `Authorization`: `Bearer <token>` (Required)
##### Path
- `boardId` (Required)
##### Json
```json
{
	"title" : "<string>",
	"body" : "<string>"
}
```

#### Success
- `201 Created`
```json
{
	"postId": 1
}
```

#### Errors (本API會用到的)
- **400 Bad Request**
    - `VALIDATION_FAILED`
        - `TITLE_INVALID`
        - `BODY_INVALID`
- **401 Unauthorized**
    - `UNAUTHORIZED`
        - `SECURITY_UNAUTHORIZED`
- **404 Not Found**
    - `NOT_FOUND`
        - `BOARD_NOT_FOUND`
- **500 Internal Server Error**
    - `INTERNAL_ERROR`
        - `UNEXPECTED_ERROR`

## Internal Design (Service & Repository)
### Use Case: createPost
- **Method Signature**
```java
CreatePostResponse createPost(long boardId, long userId, CreatePostRequest request);
```
- **Transaction**
    - `@Transactional`
- **Input Model**
    - `boardId` (Long)
        - 來源: PathVariable (`/boards/{boardId}`)
    - `userId` (Long)
        - 來源: Auth Context (`@AuthenticationPrincipal`)
    - `request` (CreatePostRequest)
        - 來源: RequestBody
        - Fields: `title`, `body`
- **Return Model**
    - `response` (CreatePostResponse)
        - postId(long)
### Data Access
#### `PostRepository`
```java
save(Post post);
```
#### `BoardRepository`
```java
findById(long boardId);
```
#### UserRepository
```java
findById(long userId);
```

## Test
### `POST /boards/{boardId}/posts`
#### Test Seed
##### Board Seed
- DB 中`boards` 中存在二筆 board ：
    - 第一筆：
        - board_id = 1
        - name = "八卦版"
        - description = "想聊什麼就聊什麼"
        - created_at = "2025-12-25 10:00:00"
    - 第二筆：
        - board_id = 2
        - name = "軟體版"
        - description = "聊軟體相關的知識"
        - created_at = "2025-12-25 10:00:00"
##### User Seed
- User Context (使用者已登入)
    - userId = 1
    - display_name = "Leo"
    - role = "User"
    - accessToken = "mock_jwt_token"

#### 創建文章成功
- Given:
    - User Seed
    - `PathVariable`
        - boardId = 2
    - `RequestBody`
        - title = "關於SpringBoot的問題"
        - body = "請問怎麼建立一個SpringBoot Maven專案"
- When:
    - 呼叫 `POST /boards/2/posts`
- Then:
    - `status`
        - 201 created
    - `ResponseBody`
        - postId  Is Not Null
- And:
    - DB 中新增一筆 posts
        - title = "關於SpringBoot的問題"
        - body = "請問怎麼建立一個SpringBoot Maven專案"
        - **board_id = 2** (驗證真的存對看板)
        - **author_id = 1** (驗證作者是當初登入的人)
        - like_count = 0
        - comment_count = 0
        - hot_score = 0
        - status = "ACTIVE"

#### 創建文章失敗
##### title 驗證失敗
- Given:
    - `PathVariable`
        - boardId = 2
    - `RequestBody`
        - **Scenario Inputs** (`title_input`)：
            - ""
            - null
            - "    "
            - `<idsahjgoasdg>`
            - a.repeat(51)
        - title = `title_input`
        - body = "請問怎麼建立一個SpringBoot Maven專案"
- When:
    - 呼叫 `POST /boards/2/posts`
- Then:
    - `status`
        - 400 Bad Request
    - `ResponseBody`
        - status = 400
        - error = "Bad Request"
        - message = "VALIDATION_FAILED"
        - code = "TITLE_INVALID"
        - path = "/boards/2/posts"
        - timestamp = "..."
##### body 驗證失敗
- Given:
    - `PathVariable`
        - boardId = 2
    - `RequestBody`
        - **Scenario Inputs** (`body_input`)：
            - ""
            - null
            - "    "
            - `<idsahjgoasdg>`
            - a.repeat(301)
        - title = "關於SpringBoot的問題"
        - body = `body_input`
- When:
    - 呼叫 `POST /boards/2/posts`
- Then:
    - `status`
        - 400 Bad Request
    - `ResponseBody`
        - status = 400
        - error = "Bad Request"
        - message = "VALIDATION_FAILED"
        - code = "BODY_INVALID"
        - path = "/boards/2/posts"
        - timestamp = "..."
##### path驗證失敗
- Given:
    - `PathVariable
        - **Scenario Inputs** (`boardId_input`)：
            - "abc"
            - -1
        - boardId = `boardId_input`
    - `RequestBody`
        - title = "關於SpringBoot的問題"
        - body = "請問怎麼建立一個SpringBoot Maven專案"
- When:
    - 呼叫 `POST /boards/boardId_input/posts`
- Then:
    - `status`
        - 400 Bad Request
    - `ResponseBody`
        - status = 400
        - error = "Bad Request"
        - message = "VALIDATION_FAILED"
        - code = "PATH_FORMAT_ERROR"
        - path = "/boards/`boardId_input`/posts"
        - timestamp = "..."
##### 使用者驗證失敗
- Given:
    - `Request Context`
        - **Scenario A (Anonymous):** 使用者以「匿名身分」發送請求 (未攜帶 Token)。
        - **Scenario B (Invalid Credential):** 使用者持有「無效憑證」發送請求 (Token 錯誤或過期)。
    - `PathVariable`
        - boardId = 2
    - `RequestBody`
        - title = "關於SpringBoot的問題"
        - body = "請問怎麼建立一個SpringBoot Maven專案"
- When:
    - 呼叫 `POST /boards/2/posts`
- Then:
    - `status`
        - 401 Unauthorized
    - `ResponseBody`
        - status = 401
        - error = "Unauthorized"
        - message = "UNAUTHORIZED"
        - code = "SECURITY_UNAUTHORIZED"
        - path = "/boards/2/posts"
        - timestamp = "..."
##### 看板不存在
- Given:
    - `PathVariable`
        - boardId = 3 (DB不存在)
    - `RequestBody`
        - title = "關於SpringBoot的問題"
        - body = "請問怎麼建立一個SpringBoot Maven專案"
- When:
    - 呼叫 `POST /boards/3/posts`
- Then:
    - `status`
        - 404 Not Found
    - `ResponseBody`
        - status = 404
        - error = "Not Found"
        - message = "NOT_FOUND"
        - code = "BOARD_NOT_FOUND"
        - path = "/boards/3/posts"
        - timestamp = "..." 
