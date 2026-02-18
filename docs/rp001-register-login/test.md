# TEST
## `POST /users/register`
### 註冊成功
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

### 註冊失敗
#### displayName 驗證失敗
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

#### email 驗證失敗
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

#### password 驗證失敗
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
#### confirmPassword 驗證失敗
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
#### email 已存在
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
## `POST /users/login`
### Test Seed
- DB中`users`存在一筆 user：
    - user_id = 123
    - email = "leo@example.com"
    - password_hash = BCrypt("abc12345")
    - display_name = "Leo"
    - role = "USER"
    - created_at = "2025-12-25 10:00:00"
### 登入成功
- Given：Test Seed
- When：呼叫 `POST /users/login`：
- Then：
    - status = `200 OK`
    - response body：
        - `userId` = 123
        - `displayName` = "Leo"
        - `role` = "USER"
        - `accessToken` 存在且非空字串
- And：
    - `accessToken` 可成功解碼並驗證（e.g. 使用 JWT library）
    - `accessToken` payload 包含正確的 userId、displayName、role
    - 登入時間在 token 的有效期限內
### 登入失敗
#### Email格式驗證失敗
- Given：
    - request body：
        - email =   "" (空字串)
        - password = "abc12345"
- When：呼叫 `POST /users/login`
- Then：
    - status = `400 Bad request`
    - response body：
        - status = 400
        - error = "Bad Request"
        - message = "VALIDATION_FAILED"
        - code = "EMAIL_INVALID"
        - path = "/users/login"
        - timestamp = "..."
#### Password格式驗證失敗
- Given：
    - request body：
        - email =   "leo@example.com"
        - password = "" (空字串)
- When：
    - 呼叫 `POST /users/login`
- Then：
    - status = `400 Bad request`
    - response body：
        - status = 400
        - error = "Bad Request"
        - message = "VALIDATION_FAILED"
        - code = "PASSWORD_INVALID"
        - path = "/users/login"
        - timestamp = "..."

#### Email帳號不存在
- Given：
    - request body：
        - email = "John@example.com"
        - password = "abc12345"
- When：
    - 呼叫 `POST /users/login`
- Then：
    - status = `401 Unauthorized`
    - response body：
        - status = 400
        - error = "Unauthorized"
        - message = "UNAUTHORIZED"
        - code = "AUTHENTICATION_FAILED"
        - path = "/users/login"
        - timestamp = "..."
#### Password錯誤認證失敗
- Given：
    - request body：
        - email = "leo@example.com"
        - password = "wrongPassword"
- When：
    - 呼叫 `POST /users/login`
- Then：
    - status = `401 Unauthorized`
    - response body：
        - status = 400
        - error = "Unauthorized"
        - message = "UNAUTHORIZED"
        - code = "AUTHENTICATION_FAILED"
        - path = "/users/login"
        - timestamp = "..."