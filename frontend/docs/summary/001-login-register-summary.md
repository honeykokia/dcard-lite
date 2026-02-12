# Users Feature - Implementation Complete ✅

## 📋 實作內容

根據 **rp-001-register-login** 和 **api-spec.yaml** 規範，已完整實作使用者註冊與登入功能。

## 📂 新增/修改的檔案

### 1. **新增：`src/api/user.js`**
API 請求函數，用於與後端通訊：
```javascript
export const loginUser = async (email, password) => {
  // 呼叫 POST /users/login
}

export const registerUser = async (name, email, password, confirmPassword) => {
  // 呼叫 POST /users/register
}
```

### 2. **修改：`src/stores/user.js`**
Pinia 狀態管理，負責：
- 儲存使用者登入狀態（`accessToken`、`displayName` 等）
- 提供登入、註冊、登出功能
- 自動處理 JWT Token 解碼與過期檢查

### 3. **新增：`src/views/Login.vue`**
登入頁面組件，包含：
- 表單驗證（Email、密碼格式）
- 錯誤提示（API 錯誤、欄位驗證錯誤）
- 載入狀態（按鈕禁用、旋轉圖示）
- 成功登入後導向首頁

### 4. **新增：`src/views/Register.vue`**
註冊頁面組件，包含：
- 表單驗證（顯示名稱、Email、密碼、確認密碼格式）
- 錯誤提示（API 錯誤、欄位驗證錯誤）
- 載入狀態（按鈕禁用、旋轉圖示）
- 成功註冊後導向登入頁

## 🎨 UI/UX 規範遵循

### 配色
- ✅ 背景色：`#f2f3f5`
- ✅ 主色調：`#3397cf` (Dcard 藍)
- ✅ 表單欄位：白色背景、灰色邊框
- ✅ 錯誤提示：紅色文字與邊框

### 組件樣式
- ✅ 圓角：`rounded-lg` (8px)
- ✅ 陰影：`shadow-[0_2px_16px_rgba(0,0,0,0.08)]`
- ✅ 按鈕互動：`hover:bg-[#2b7fb3] active:scale-[0.98]`
- ✅ 錯誤訊息：紅色背景與文字

## 🔧 功能特點

### 登入功能
- 表單驗證：
  - Email 格式檢查
  - 密碼不可為空
- 錯誤處理：
  - Email 或密碼錯誤顯示提示
  - 網路錯誤顯示通用錯誤訊息
- 成功登入後：
  - 儲存 JWT Token 與使用者資訊
  - 自動導向首頁

### 註冊功能
- 表單驗證：
  - 顯示名稱：不可為空、長度限制、不可為純數字或符號
  - Email 格式檢查
  - 密碼：長度限制、需包含英文字母與數字
  - 確認密碼需與密碼一致
- 錯誤處理：
  - Email 已存在顯示提示
  - 網路錯誤顯示通用錯誤訊息
- 成功註冊後：
  - 自動導向登入頁面

## 🧪 測試建議

### 1. 啟動開發伺服器
```bash
cd frontend
npm run dev
```

### 2. 測試情境

#### 登入功能
- [ ] 輸入正確的 Email 和密碼，確認能成功登入並導向首頁
- [ ] 輸入錯誤的 Email 或密碼，確認顯示正確的錯誤訊息
- [ ] 測試網路錯誤情境，確認顯示通用錯誤訊息

#### 註冊功能
- [ ] 輸入正確的資料，確認能成功註冊並導向登入頁
- [ ] 測試以下錯誤情境：
  - 顯示名稱為空或格式錯誤
  - Email 格式錯誤或已存在
  - 密碼格式錯誤
  - 確認密碼與密碼不一致
- [ ] 測試網路錯誤情境，確認顯示通用錯誤訊息

## 📝 API 規範遵循

### 登入 API
#### Request
```
POST /users/login
```
```json
{
  "email": "leo@example.com",
  "password": "abc12345"
}
```

#### Success Response
```json
{
  "userId": 123,
  "displayName": "Leo",
  "role": "USER",
  "accessToken": "..."
}
```

#### Error Response
- `400 Bad Request`（Email 或密碼格式錯誤）
- `401 Unauthorized`（Email 或密碼錯誤）

---

### 註冊 API
#### Request
```
POST /users/register
```
```json
{
  "name": "Leo",
  "email": "leo@example.com",
  "password": "abc12345",
  "confirmPassword": "abc12345"
}
```

#### Success Response
```json
{
  "userId": 123,
  "displayName": "Leo",
  "email": "leo@example.com",
  "role": "USER",
  "createdAt": "2025-12-25T10:00:00Z"
}
```

#### Error Response
- `400 Bad Request`（欄位驗證失敗）
- `409 Conflict`（Email 已存在）
