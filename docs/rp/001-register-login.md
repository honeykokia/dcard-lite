# rp-001-register-login

## Use Case

### Who?
- User
### What?
- 使用者輸入顯示名稱、帳號、密碼，進行帳號註冊
- 使用者登入取得JWT Token
### Success?
- 註冊成功，201 Created
- 登入成功，200 OK
### Failure?
- 註冊失敗，400 Bad Request
- 登入失敗，401 Unauthorized

## Wireframe
### UI Layout
#### LoginPage
```
+-----------------------------+
|           Logo              |
|-----------------------------|
|        Login to Your Account |
|                             |
|  Email: [______________]   |
|  Password: [______________] |
|                             |
|  [ Login ]                    |
|                              |
|  Don't have an account?      |
|  [ Register ]                    |
+-----------------------------+
```

##### UI Elements
- Logo
    - `type`
        - img
    - `description`
        - 顯示網站或應用程式的標誌
- Login to Your Account
    - `type`
        - Heading
    - `description`
        - 作為頁面標題
- Email
    - `type`
        - input
    - `description`
        - 使用者輸入信箱
    - `validate`
        - 依據 **Validation Rules** 進行驗證
- Password
    - `type`
        - input[password]
    - `description`
        - 使用者輸入密碼
    - `validate`
        - 依據 **Validation Rules** 進行驗證
- Login
    - `type`
        - button
    - `description`
        - 用於提交登入表單的按鈕
    - `validate`
        - 確認表單欄位都驗證成功
    - `function`
        - `POST /users/login`
- Don't have an account?
    - `type`
        - label
    - `description`
        - 提醒使用者沒有帳號可以註冊
- Login
    - `type`
        - link
    - `description`
        - 跳轉至註冊頁面
    - `function`
        - Go `/users/register`

##### User Flow
1. 使用者訪問 `/login` 頁面
2. 使用者輸入帳號和密碼
3. 使用者點擊「Login」按鈕
4. 系統驗證使用者的帳號和密碼
    - 如果驗證成功，重定向到首頁 `/`
    - 如果驗證失敗，顯示錯誤訊息並保持在登入頁面
1. 如果使用者沒有帳號，點擊「Register」連結，重定向到註冊頁面 `/register`

#### RegisterPage
```
+---------------------------------------+
|                                       |
|              Logo                     |
|                                       |
+---------------------------------------+
|                                       |
|         Register Account              |
|                                       |
|                                       |
|                                       |
|    Name: [_______________]            |
|                                       |
|    Email: [______________]            |
|                                       |
|    Password: [____________]           |
|                                       |
|    ConfirmPassword: [____________]    |
|                                       |
|                                       |
|          [ Register ]                 |
|                                       |
|    Already have an account? [ Login ] |
|                                       |
|                                       |
+---------------------------------------+
```
##### UI Elements
- Logo
    - `type`
        - img
    - `description`
        - 顯示網站或應用程式的標誌
- Register Account
    - `type`
        - Heading
    - `description`
        - 作為頁面標題
- Name
    - `type`
        - input
    - `description`
        - 使用者輸入名稱
    - `validate`
        - 依據 **Validation Rules** 進行驗證
- Email
    - `type`
        - input
    - `description`
        - 使用者輸入信箱
    - `validate`
        - 依據 **Validation Rules** 進行驗證
- Password
    - `type`
        - input[password]
    - `description`
        - 使用者輸入密碼
    - `validate`
        - 依據 **Validation Rules** 進行驗證
- ConfirmPassword
    - `type`
        - input[password]
    - `description`
        - 使用者輸入確認密碼
    - `validate`
        - 依據 **Validation Rules** 進行驗證
- Register
    - `type`
        - button
    - `description`
        - 進行註冊帳號
    - `validate`
        - 用於提交註冊表單的按鈕
    - `function`
        - `POST /users/register`
- Already have an account?
    - `type`
        - label
    - `description`
        - 提醒使用者有帳號可以直接跳轉登入
- Login
    - `type`
        - link
    - `description`
        - 跳轉至登入頁面
    - `function`
        - Go `/users/login`

##### User Flow
1. 使用者訪問 `/register` 頁面
2. 使用者輸入名稱、信箱、密碼和確認密碼
3. 使用者點擊「Register」按鈕
4. 系統驗證使用者的名稱、信箱、密碼和確認密碼
    - 如果驗證成功，重定向到登入 `/login`
    - 如果驗證失敗，顯示錯誤訊息並保持在註冊頁面
1. 如果使用者已經有帳號，點擊「Login」連結，重定向到登入頁面 `/login`

### Router
- `/login` - LoginPage
- `/register` - RegisterPage
### Form Validation Timing
- **即時驗證**: onBlur 時驗證單一欄位
- **提交驗證**: onClick 提交時驗證所有欄位
### Error Message Display
- 顯示位置: 輸入框下方
- 樣式: 紅色文字 (text-red-500)
- 同時只顯示該欄位的第一個錯誤
### Loading State
- 提交期間: 按鈕顯示 loading spinner
- 提交期間: 表單所有輸入框 disabled
### Success Handling
- 註冊成功: 顯示 toast 後跳轉至登入頁
- 登入成功: 儲存 token 後跳轉至首頁
### API Error Mapping
後端 `code` → 前端顯示文字對照表
### API Base URL
```env.development
VITE_API_BASE_URL=http://ocalhost:8080
```
### Token Management
- **儲存位置**: `localStorage`
- **Key 名稱**: `accessToken`
- **自動設定**: 登入成功後，所有後續 API 請求的 Header 自動帶上 `Authorization: Bearer {token}`
- **登出處理**: 清除 `localStorage` 中的 token

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
  "confirmPassword": "<string>"}  
```  
#### Success
- `201 Created`
```json  
{  
  "userId": 123,
  "displayName": "Leo",
  "email": "leo@example.com",
  "role": "USER",
  "createdAt": "2025-12-25T10:00:00Z"}  
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


## Service Logic
### `UserService`
#### Use Case: Register
- **Method Signature**:
```java  
RegisterResponse register(RegisterRequest request);  
```  
- **Transaction**:
    - `@Transactional` (確保資料一致性)
- **Input Model (`RegisterRequest`)**:
    - `name` (String)
    - `email` (String)
    - `password` (String)
    - `confirmPassword` (String)
- **Return Model (`RegisterResponse`)**:
    - `userId` (long)
    - `displayName` (String)
    - `email` (String)
    - `role` (String)
    - `createdAt` (LocalDateTime)
- **Logic Flow**:
    1. 驗證輸入資料是否符合規範（如名稱、Email、密碼格式）。
    2. 檢查 Email 是否已存在於資料庫中。
    3. 使用 BCrypt 將密碼進行雜湊處理。
    4. 將新使用者資料儲存至資料庫。
    5. 回傳包含使用者資訊的 `RegisterResponse`。
#### Use Case: Login
- **Method Signature**:
```java  
LoginResponse login(LoginRequest request);  
```  
- **Transaction**:
    - `@Transactional(readOnly = true)` (優化讀取效能)
- **Input Model (`LoginRequest`)**:
    - `email` (String)
    - `password` (String)
- **Return Model (`LoginResponse`)**:
    - `userId` (long)
    - `displayName` (String)
    - `role` (String)
    - `accessToken` (String)
- **Logic Flow**:
    1. 驗證輸入資料是否符合規範（如 Email 和密碼格式）。
    2. 根據 Email 查詢使用者資料。
    3. 使用 BCrypt 驗證密碼是否正確。
    4. 生成 JWT Token，包含使用者資訊。
    5. 回傳包含使用者資訊和 Token 的 `LoginResponse`。
## Data Access
### `UserRepository`
#### Use Case: 檢查 Email 是否存在
```java  
boolean existsByEmail(String email);
```  
#### Use Case: 儲存新使用者
```java  
User save(User user);  
```  
#### Use Case: 根據 Email 查詢使用者
```java  
Optional<User> findByEmail(String email);  
```
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
- DB中`users`存在一筆 user：
    - user_id = 123
    - email = "leo@example.com"
    - password_hash = BCrypt("abc12345")
    - display_name = "Leo"
    - role = "USER"
    - created_at = "2025-12-25 10:00:00"
#### 登入成功
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
#### 登入失敗
##### Email格式驗證失敗
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
##### Password格式驗證失敗
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

##### Email帳號不存在
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
##### Password錯誤認證失敗
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