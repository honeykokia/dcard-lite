# Test
## `GET /boards`
### Test Seed
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
### 看板查詢成功
#### 無 keyword
- Given：Test Seed
- When：呼叫 `GET /boards`
- Then：
    - `status`：
        - 200 ok
    - `response` body：
        - page = 1
        - pageSize = 20
        - total = 2
        - items
            - 第一筆
                - boardId = 1
                - name = "八卦版"
                - description = "想聊什麼就聊什麼"
            - 第二筆
                - boardId = 2
                - name = "軟體版"
                - description = "聊軟體相關的知識"
#### 有 keyword
- Given：Test Seed
- When：呼叫 `GET /boards?keyword="八卦"`
- Then：
    - `status`：
        - 200 ok
    - `response` body：
        - page = 1
        - pageSize = 20
        - total = 1
        - items
            - 第一筆
                - boardId = 1
                - name = "八卦版"
                - description = "想聊什麼就聊什麼"

#### 有keyword但查無資料
- Given：Test Seed
- When：呼叫 `GET /boards?keyword="棒球"`
- Then：
    - `status`：
        - 200 ok
    - `response` body：
        - page = 1
        - pageSize = 20
        - total = 0
        - items none

### 查詢看板失敗
#### page 驗證失敗
- Given：
    - Test Seed
    - **Scenarios (Input vs Expectation)**：

| **page_input** | **expected_code**      | **說明**    |
| -------------- | ---------------------- | --------- |
| `-1`           | **PAGE_INVALID**       | 數值違反最小值限制 |
| `"invalid"`    | **PARAM_FORMAT_ERROR** | 資料型別格式錯誤  |

- When：
    - 呼叫 `GET /boards?page={page_input}`
- Then：
    - `status`：
        - 400 Bad Request
    - `response` body：
        - status = 400
        - error = "Bad Request"
        - message = "VALIDATION_FAILED"
        - code = {expected_code}
        - path = "GET /boards?page={page_input}"
        - timestamp ="..."
#### pageSize 驗證失敗
- Given：
    - Test Seed
    - **Scenarios (Input vs Expectation)**：

| **page_size_input** | **expected_code**      | **說明**    |
| ------------------- | ---------------------- | --------- |
| `-1`                | **PAGE_SIZE_INVALID**  | 數值違反最小值限制 |
| `201`               | **PAGE_SIZE_INVALID**  | 數值違反最大值限制 |
| `"invalid"`         | **PARAM_FORMAT_ERROR** | 資料型別格式錯誤  |
- When：
    - 呼叫 `GET /boards?pageSize={page_size_input}`
- Then：
    - `status`：
        - 400 Bad Request
    - `response` body：
        - status = 400
        - error = "Bad Request"
        - message = "VALIDATION_FAILED"
        - code = {expected_code}
        - path = "GET /boards?pageSize={page_size_input}"
        - timestamp ="..."
#### keyword 驗證失敗
- Given：
    - Test Seed
    - **Scenario Inputs** (`keyword_input`)：
        - a.repeat(51)
        - " " + "a".repeat(51) + " "
- When：
    - 呼叫 `GET /boards?keyword={keyword_input}`
- Then：
    - `status`：
        - 400 Bad Request
    - `response` body：
        - status = 400
        - error = "Bad Request"
        - message = "VALIDATION_FAILED"
        - code = "KEYWORD_INVALID"
        - path = "GET /boards?keyword={keyword_input}"
        - timestamp ="..."