# Frontend Component
## Wireframe
### LoginForm
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

#### UI Elements
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
- Register
    - `type`
        - link
    - `description`
        - 跳轉至註冊頁面
    - `function`
        - Go `/register`

#### User Flow
1. 使用者訪問 `/login` 頁面
2. 使用者輸入帳號和密碼
3. 使用者點擊「Login」按鈕
4. 系統驗證使用者的帳號和密碼
    - 如果驗證成功，重定向到首頁 `/`
    - 如果驗證失敗，顯示錯誤訊息並保持在登入頁面
5. 如果使用者沒有帳號，點擊「Register」連結，重定向到註冊頁面 `/register`

### RegisterForm
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
#### UI Elements
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
        - Go `/login`

#### User Flow
1. 使用者訪問 `/register` 頁面
2. 使用者輸入名稱、信箱、密碼和確認密碼
3. 使用者點擊「Register」按鈕
4. 系統驗證使用者的名稱、信箱、密碼和確認密碼
    - 如果驗證成功，重定向到登入 `/login`
    - 如果驗證失敗，顯示錯誤訊息並保持在註冊頁面
5. 如果使用者已經有帳號，點擊「Login」連結，重定向到登入頁面 `/login`

## State Management (Pinia)
### `authStore`
- Location：`src/entities/auth/model/auth.store.ts`
- State
    - token (`string` | `null`)
        - `localStorage` 取值
    - user (`object`)
        - `{userId: '', displayName: ''}`
- Getter
    - isLoggedIn (boolean)
        - 判斷 token是否存在
- Actions
    - `setAuth(data)`
        - 更新 `token` 與 `user` 狀態
    - `clearAuth()`
        - 重置 `token`為`null`
        - 清空 `user` 物件
        - 移除 `localStorage` 中的相關Key
## Logic Management (Composable)
### `logout`
- **觸發機制**：
    - **主動觸發**：使用者點擊 UI 介面（如 Navbar）中的「登出」按鈕。
    - **被動觸發**：當 API 回傳 `401 Unauthorized` 錯誤時，由 Axios 攔截器自動呼叫。

- **執行邏輯 (useAuth Composable)**：
    1. **狀態清理**：執行 `authStore.clearAuth()`。
        - 移除 `localStorage` 內所有認證相關 Key (`accessToken`, `userId`, `displayName`)。
        - 重置 Pinia Store 內的響應式變數。

    2. **路由重導向**：
        - 執行 `router.replace('/login')` 跳轉回登入頁面。
        - **注意**：使用 `replace` 而非 `push`，以防止使用者透過瀏覽器「回退」按鈕返回已登出的頁面。

- **預期效果**：
    - 全域 UI 立刻響應（導覽列切換為「登入/註冊」按鈕）。
    - 隨後發送的 API 請求將不再攜帶 `Authorization` Header。

## Form Validation Timing
- **即時驗證**: onBlur 時驗證單一欄位
- **提交驗證**: onClick 提交時驗證所有欄位
## Error Message Display
- 顯示位置: 輸入框下方
- 樣式: 紅色文字 (text-red-500)
- 同時只顯示該欄位的第一個錯誤
## Loading State
- 提交期間: 按鈕顯示 loading spinner
- 提交期間: 表單所有輸入框 disabled
## Success Handling
- 註冊成功: 顯示 toast 後跳轉至登入頁
- 登入成功: 儲存 token 後跳轉至首頁
## API Error Mapping
後端 `code` → 前端顯示文字對照表
## API Base URL
```env.development
VITE_API_BASE_URL=http://localhost:8080
```
## Token Management
- **儲存位置**: `localStorage`
- **Key 名稱**: `accessToken`
- **自動設定**: 登入成功後，所有後續 API 請求的 Header 自動帶上 `Authorization: Bearer {token}`
- **登出處理**: 清除 `localStorage` 中的 token
