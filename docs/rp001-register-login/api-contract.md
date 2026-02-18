# API Contract
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
