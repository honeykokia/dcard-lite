# rp-001-register-login
## Use Case
### Who?
- User
### What?
- 使用者註冊帳號密碼
- 使用者登入取得JWT Token
### Success?
- 註冊成功，201 Created
- 登入成功，200 OK

## DB Changes (MySQL + Liquibase)
- Table: `users`
    - columns（填你要的最小集合）：
        - user_id (BIGINT, PK, auto increment, NOT NULL)
        - email (VARCHAR(100), NOT NULL)
        - password_hash (VARCHAR(200), NOT NULL)
        - display_name (VARCHAR(20), NOT NULL)
        - role (VARCHAR(10), NOT NULL, default='USER')
        - created_at (DATETIME(6), NOT NULL)
    - constraints:
        - PK: `pk_users` (user_id)
        - UK: `uq_users_email` (email)

## Validation Rules
- name
    - 必填（不可為空字串 / 純空白；需先 `trim()`）
    - 最大長度：20
    - 限制內容：不接受純數字或只含符號
- email
    - 必填（不可為空字串／純空白）
    - 必須符合 Email 格式（後端驗證 e.g. regex / library）
    - 最大長度：100
    - 儲存前轉小寫（`toLowerCase()`），方便做唯一索引比較
- password
    - 必填（不可為空字串/純空白）
    - 至少要有一個英文字母和1個數字
    - 密碼長度：8~12
- confirmPassword
    - 必填（不可為空字串/純空白）
    - 必須與 `password` 完全相同

## Error Response Format（固定格式）

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

## Error Mapping（Domain → HTTP）
- **400 Bad Request**
    - `message`: `VALIDATION_FAILED`
    - `code`（例）：
        - `NAME_INVALID`
            - Name must not be blank
            - Name maximum length 20 characters
            - Values consisting only of digits or only symbols are not allowed
        - `EMAIL_INVALID`
            - Email must not be blank
            - Email must be in a valid email format
            - Email maximum length 100 characters
        - `PASSWORD_INVALID`
            - Password must not be blank
            - Password length must be between 8 and 12 characters
            - Password must contain at least one letter and one digit
        - `CONFIRM_PASSWORD_INVALID`
            - ConfirmPassword must not be blank
            - Confirm password must match password

- **401 Unauthorized**
    - `message`: `UNAUTHORIZED`
    - `code`：
        - `AUTHENTICATION_FAILED`
            - Email or password is incorrect

- **409 Conflict**
    - `message`: `CONFLICT`
    - `code`：
        - `EMAIL_ALREADY_EXISTS`
            - Email is existed

## API Contract
### `POST /users/register`
#### Request JSON
```json
{
  "name": "<string>",
  "email": "<string>",
  "password": "<string>",
  "confirmPassword": "<string>"
}
```
#### Success
- `201 Created`
```json
{
  "userId": 123,
  "displayName": "Leo",
  "email": "leo@example.com",
  "role": "USER",
  "createdAt": "2025-12-25T10:00:00Z"
}
```
#### Errors（本 API 會用到的）
- `400 Bad Request`
    - `VALIDATION_FAILED` 
      - `NAME_INVALID`
      - `EMAIL_INVALID`
      - `PASSWORD_INVALID`
      - `CONFIRM_PASSWORD_INVALID`
- `409 Conflict`
    - `CONFLICT`
      - `EMAIL_ALREADY_EXISTS`
---

### `POST /users/login`
#### Request JSON
```json
{
  "email": "<string>",
  "password": "<string>"
}
```
#### Success
- `200 OK`
```json
{
  "userId": 123,
  "displayName": "Leo",
  "role": "USER",
  "accessToken": "..."
}
```
#### Errors（本 API 會用到的）
- `400 Bad Request`
    - `VALIDATION_FAILED`
      - `EMAIL_INVALID`
      - `PASSWORD_INVALID`
- `401 Unauthorized`
    - `UNAUTHORIZED`
      - `AUTHENTICATION_FAILED`

## TEST
### `POST /users/register`
#### 註冊成功
- Given
    - request body 合法：
        - name = "Leo"
        - email = "leo@example.com"
        - password = "abc12345"
        - confirmPassword = "abc12345"
- When
    -  呼叫 `POST /users/register`
- Then
    - status = `201 Created`
    - response body：
        - `userId`（> 0）
        - `displayName` = "Leo"
        - `email` = "leo@example.com"
        - `role` = "USER"
        - `createdAt` 存在且為 ISO-8601 date-time
- And
    - DB 中新增一筆 users
    - `email` 以小寫儲存
    - `password_hash` 不是明文、且符合 BCrypt 格式（通常以 `$2a$`/`$2b$` 開頭）
    - `created_at` 不為 null（由 DB 填）

#### 註冊失敗
##### displayName 驗證失敗
- Given
    - request body：
        - name = "   "（純空白）
        - email = "leo@example.com"
        - password = "abc12345"
        - confirmPassword = "abc12345"
- When
    - 呼叫 `POST /users/register`
- Then
    - status = `400 Bad request`
    - response body：
        - status = 400
        - error = "Bad Request"
        - message = "VALIDATION_FAILED"
        - code = "NAME_INVALID"
        - path = "/users/register"
        - timestamp = "..."

##### email 驗證失敗
- Given
    - request body：
        - name = "Leo"
        - email = "leoexample.com"
        - password = "abc12345"
        - confirmPassword = "abc12345"
- When
    - 呼叫 `POST /users/register`
- Then
    - status = `400 Bad request`
    - response body：
        - status = 400
        - error = "Bad Request"
        - message = "VALIDATION_FAILED"
        - code = "EMAIL_INVALID"
        - path = "/users/register"
        - timestamp = "..."

##### password 驗證失敗
- Given
    - request body：
        - name = "Leo"
        - email = "leo@example.com"
        - password = "33312345" or "38542 ass" or "ab12" or "VeryLongPassword123"
        - confirmPassword = "33312345" or "38542 ass" or "ab12" or "VeryLongPassword123"
        - （測試多組不合法密碼）
- When
    - 呼叫 `POST /users/register`
    - （測試多組不合法密碼）
- Then
    - status = `400 Bad request`
    - response body：
        - status = 400
        - error = "Bad Request"
        - message = "VALIDATION_FAILED"
        - code = "PASSWORD_INVALID"
        - path = "/users/register"
        - timestamp = "..."
##### confirmPassword 驗證失敗
- Given
    - request body：
        - name = "Leo"
        - email = "leo@example.com"
        - password = "abc12345"
        - confirmPassword = "differentPassword"
- When
    - 呼叫 `POST /users/register`
- Then
    - status = `400 Bad request`
    - response body：
        - status = 400
        - error = "Bad Request"
        - message = "VALIDATION_FAILED"
        - code = "CONFIRM_PASSWORD_INVALID"
        - path = "/users/register"
        - timestamp = "..."
##### email 已存在
- Given
    - DB 中已存在 email = `leo@example.com`
    - request body：
        - name = "Leo"
        - email = "leo@example.com"
        - password = "abc12345"
        - confirmPassword = "abc12345"
- When
    - 呼叫 `POST /users/register`
- Then
    - status = `409 Conflict`
    - response body：
        - status = 409
        - error = "Conflict"
        - message = "CONFLICT"
        - code = "EMAIL_ALREADY_EXISTS"
        - path = "/users/register"
        - timestamp = "..."

---  
### `POST /users/login`
#### Test Seed
- DB 中存在一筆 users：
    - user_id = 123
    - email = "leo@example.com"
    - password_hash = BCrypt("abc12345")
    - display_name = "Leo"
    - role = "USER"
    - created_at = "2025-12-25 10:00:00"
#### 登入成功
- Given : Test Seed
- When
    - 呼叫 `POST /users/login`：
- Then
    - status = `200 OK`
    - response body：
        - `userId` = 123
        - `displayName` = "Leo"
        - `role` = "USER"
        - `accessToken` 存在且非空字串
- And
    - `accessToken` 可成功解碼並驗證（e.g. 使用 JWT library）
    - `accessToken` payload 包含正確的 userId、displayName、role
    - 登入時間在 token 的有效期限內
#### 登入失敗
##### Email格式驗證失敗
- Given
  - request body：
      - email = "leoexample.com" or "   " (純空白) or "" (空字串) or thisEmailIsTooLongOverOneHundredCharacters
      - password = "abc12345"
- When
  - 呼叫 `POST /users/login`
- Then
  - status = `400 Bad request`
  - response body：
      - status = 400
      - error = "Bad Request"
      - message = "VALIDATION_FAILED"
      - code = "EMAIL_INVALID"
      - path = "/users/login"
      - timestamp = "..."
      - 
##### Password格式驗證失敗
- Given
    - request body：
        - email =   "leo@example.com"
        - password = "" (空字串)
- When
    - 呼叫 `POST /users/login`
- Then
    - status = `400 Bad request`
    - response body：
        - status = 400
        - error = "Bad Request"
        - message = "VALIDATION_FAILED"
        - code = "PASSWORD_INVALID"
        - path = "/users/login"
        - timestamp = "..."

##### Email帳號不存在
- Given
    - request body：
        - email = "John@example.com"
        - password = "abc12345"
- When
    - 呼叫 `POST /users/login`
- Then
    - status = `401 Unauthorized`
    - response body：
        - status = 400
        - error = "Unauthorized"
        - message = "UNAUTHORIZED"
        - code = "AUTHENTICATION_FAILED"
        - path = "/users/login"
        - timestamp = "..."
##### Password錯誤認證失敗
- Given
    - request body：
        - email = "leo@example.com"
        - password = "wrongPassword"
- When
    - 呼叫 `POST /users/login`
- Then
    - status = `401 Unauthorized`
    - response body：
        - status = 400
        - error = "Unauthorized"
        - message = "UNAUTHORIZED"
        - code = "AUTHENTICATION_FAILED"
        - path = "/users/login"
        - timestamp = "..."
