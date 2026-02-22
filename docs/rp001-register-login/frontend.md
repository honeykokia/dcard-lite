# Frontend Design
## Interface
### Auth
- Location : `/entities/auth/model/auth.types.ts`
- Types :
    - **User**
        - userId (`number`)
        - token (`string`)
    - **LoginRequest**
        - email (`string`)
        - password (`string`)
    - **LoginResponse**
        - userId (`number`)
        - displayName (`string`)
        - role (`string`)
        - accessToken (`string`)
    - **RegisterUserRequest**
        - name (`string`)
        - email (`string`)
        - password (`string`)
        - confirmPassword (`string`)
    - **LoginfForm**
        - email (`string`)
        - password (`string`)
    - **LoginFormErrors**
        - email (`string[]`)
        - password (`string[]`)
    - **RegisterForm**
        - name (`string`)
        - email (`string`)
        - password (`string`)
        - confirmPassword (`string`)
    - **RegisterFormErrors**
        - name (`string[]`)
        - email (`string[]`)
        - password (`string[]`)
        - confirmPassword (`string[]`)

## API
- Location : `entities/auth/api/auth.api/ts`
- Method
    - `register(RegisterUserRequest)` -> Ref: api-contract.md `POST /users/login`
    - `login(LoginRequest)` -> Ref: api-contract.md `POST /users/register`

## Stores
### Auth
- Location : `/entities/auth/model/auth.store.ts`
- States
    - `token` : `string`
    - `user` : `User`
- Getter
    - `isLoggedIn` : `boolean`
        - 確認使用者是否是已登入狀態
- Actions
    - setAuth(data)
    - clearAuth()

## Composable
### Auth
#### Use Case : useLogin
- Loaction : `features/login/useLogin.ts`
- Description : 使用者登入
- State :
    - `form` : `LoginForm`
    - `errors` : `LoginFormErrors`
    - `isLoadding` : `boolean`
    - `error` : `string`
- Getter :
    - `isValid` : `boolean`
        - 驗證帳號密碼長度及是否為空白
- Actions :
    - **validateEmail**
        - Trigger : `email` 欄位失去焦點 (`blur`) 或表單送出前。
        - Action : `getEmailValidationErrors` 並更新 `errors.email`
    - **validatePassword**
        - Trigget : `password` 欄位失去焦點 (`blur`) 或表單送出前。
        - Action : `getPasswordVlidateionErrors` 並更新 `errors.password`
    - **handleLogin**
        - Trigger: 點擊「登入」按鈕。
        - Action:
            1. 重置 `error` 並執行 `validateAll()`。
            2. 若 `isValid` 為 `false` 則終止流程。
            3. 設定 `isLoading = true`。
            4. 呼叫 `authApi.login` (Ref: **[[api-contract]]**)。
            5. **Success**: 呼叫 `authStore.setAuth` 存入 Token，並使用 `router.push('/')` 跳轉。
            6. **Error**: 進入 `handleApiError` 邏輯，將後端 Error Code (如 `AUTHENTICATION_FAILED`) 映射至 `apiError` 或 `errors`。
            7. **Finally**: 設定 `isLoading = false`。

    - **resetForm**
        - Trigger: 頁面銷毀或手動重置。
        - Action: 將 `form` 與 `errors` 恢復至初始空值，清空 `apiError`。
#### Use Case : useRegister
- Location: `features/register/useRegister.ts`
- Description: 處理新使用者註冊流程，包含多欄位即時驗證與密碼一致性檢查。
- State
    - `form`: `RegisterForm` (包含 `name`, `email`, `password`, `confirmPassword`)
    - `errors`: `RegisterFormErrors` (存放各欄位對應的錯誤訊息陣列)
    - `isLoading`: `boolean` (提交 API 時的狀態)
    - `error`: `string` (存放來自後端的全局錯誤訊息)
- Getter:
    - **`isValid`**: `boolean`
        - **Logic**:
            1. 檢查 `errors` 中所有陣列皆為空。
            2. 檢查 `form` 中所有欄位皆已填寫且非純空格。

- Actions:
    - **validateName**
        - **Trigger**: 姓名欄位輸入時或失去焦點。
        - **Logic**:
            - 不得為空。
            - 長度上限 20 字。
            - **注意**：禁止純數字或純符號組合。
    - **validateEmail / validatePassword**
        - **Action**: 呼叫 `shared/utils` 提供的驗證工具更新 `errors`。
    - **validateConfirmPassword**
        - **Trigger**: 二次確認密碼輸入時。
        - **Logic**: 檢查是否與 `form.password` 完全一致。
    - **handleRegister**
        - **Trigger**: 點擊「註冊」按鈕。
        - **Action**:
            1. 呼叫 `validateAll()` 確認前端驗證通過。
            2. 設定 `isLoading = true` 並清空舊的 `error`。
            3. 呼叫 `authApi.register` (Ref: **[[api-contract]]**)。
            4. **Success**: 註冊成功後，導向 `router.push('/login')`。
            5. **Error**:
                - 透過 `handleApiError` 進行代碼映射。
                - 特殊處理：`EMAIL_ALREADY_EXISTS` 應對應至 `errors.email`。
            6. **Finally**: `isLoading = false`。
    - **resetForm**
        - **Action**: 清空所有 `form` 資料、`errors` 陣列與 `error`。

## Components
### Feature
#### LoginForm
- **Location**: `features/login/UI/LoginForm.vue`
- **Description**: 提供使用者登入介面，包含帳號密碼輸入、即時驗證回饋與 API 錯誤顯示。
- **Props**: 無（透過 `useLogin` 直接管理狀態）。
- **Logic**:
- **狀態綁定**：將 `form.email` 與 `form.password` 透過 `v-model` 進行雙向綁定。
- **驗證觸發**：
    - 當 `input` 失去焦點 (`@blur`) 時，觸發對應的 `validateEmail` 或 `validatePassword`。
    - 根據 `errors` 陣列長度動態切換輸入框的樣式（Red border / Focus ring）。
- **表單提交**：攔截 `submit.prevent` 並呼叫 `handleLogin` 動作。
- **互動反饋**：
    - **Loading 狀態**：當 `isLoading` 為 `true` 時，禁用所有 `input` 與提交按鈕，並顯示 **Logging in...** 動態圖示。
    - **按鈕狀態**：當 `!isValid` 或 `isLoading` 時，將提交按鈕設為 `disabled`。
    - **全局錯誤**：若 `error` 有值，則顯示警示框（Alert）。
- **路由跳轉**：點擊底部連結使用 `router-link` 跳轉至 `/register`。
#### RegisterForm
- **Location**: `features/register/UI/RegisterForm.vue`
- **Description**: 提供使用者註冊介面，包含姓名、帳號、密碼及二次確認密碼的輸入，並整合即時驗證與註冊狀態顯示。
- **Props**: 無（透過 `useRegister` Composable 進行狀態管理）。
- **Logic**:
    - **雙向綁定**：將 `form` 中的 `name`, `email`, `password`, `confirmPassword` 與各個 `<input>` 進行綁定。
    - **驗證機制**：
        - **即時回饋**：每個輸入框觸發 `@blur` (失去焦點) 事件時，執行對應的 `validate` 函式。
        - **視覺提示**：當 `errors[field]` 陣列長度 > 0 時，輸入框邊框變為紅色並顯示第一條錯誤訊息。
    - **表單行為**：
        - **防重複提交**：當 `isLoading` 為 `true` 時，禁用所有輸入框與提交按鈕。
        - **提交控制**：僅當 `isValid` 為 `true` 且不處於 `isLoading` 狀態時，註冊按鈕才可點擊。
    - **交互效果**：
        - **載入動畫**：提交時按鈕顯示 `animate-spin` 圖示與 "Registering..." 文字。
        - **全局警示**：若 API 回傳 `error`，在表單上方顯示紅色錯誤提示區塊。
    - **導航邏輯**：提供 `router-link` 導向 `/login` 頁面供已有帳號的使用者跳轉。

## Pages

### LoginPage
- **Location**: `pages/login/LoginPage.vue`
- **Description**: 登入頁面，提供純淨的背景與佈局，作為 `LoginForm` 的載體。
- **Composition**:
    - `LoginForm` (Feature)
### RegisterPage
- **Location**: `pages/register/RegisterPage.vue`
- **Description**: 註冊頁面，負責呈現註冊流程的完整佈局。
- **Composition**:
    - `RegisterForm` (Feature)