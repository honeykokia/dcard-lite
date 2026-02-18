# Backend Design
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