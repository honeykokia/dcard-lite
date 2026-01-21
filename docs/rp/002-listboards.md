# rp-002 看板列表
## Use Case
### Who?
- Guest
- User
### What?
- 列出所有看板列表
### Success?
- 查詢列表成功，200 OK
### Failure
- 查詢列表失敗，400 Bad Request

## DB Changes (MySQL + Liquibase)
- Table: `boards`
    - Columns
        - board_id (BIGINT, PK , auto increment, NOT NULL)
        - name (VARCHAR(20), UK, NOT NULL)
        - description (VARCHAR(100), NOT NULL)
        - created_at (DATETIME(6), NOT NULL)
    - Constraints
        - PK: pk_boards (board_id)
        - UK: uk_boards_name (name)


## Validation Rules
- page
    - default =1
    - 必須為正整數 且 `page >=1`
- pageSize
    - default = 20
    - 必須為正整數 且 `1 <= pageSize <= 100`
- keyword
    - optional
    - 若提供: `trim()`後長度 `1..50`
    - 空白字串視為未提供 (等同於沒有keyword)

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
## Error Mapping （Domain → HTTP）
- **400 Bad Request**
    - `message`：`VALIDATION_FAILED`
    - `code`：
        - `PAGE_INVALID`
            - page <1
            - page 非正整數
        - `PAGE_SIZE_INVALID`
            - pageSize 不在1 ..100
            - pageSize 非正整數
        - `KEYWORD_INVALID`
            - keyword提供但trim()後不在 1 .. 50
- **500 Internal Server Error**
    - `message`：`INTERNAL_ERROR`
    - `code`：
        - `UNEXPECTED_ERROR`
            - DB/Repository error 或未預期error

## API Contract
### `GET /boards`
#### Request Query
- `page`( optional，default = 1)
- `pageSize` ( optional，default = 20，max=100 )
- `keyword` ( optional，用於name模糊查詢；空白視為未提供 )
#### Success
- 200 `OK`
```json
{
	"page": 1,
	"pageSize":20,
	"total": 2,
	"items":[
		{
			"boardId": 1,
			"name": "八卦版",
			"description": "想聊什麼就聊什麼"
		},
		{
			"boardId": 2,
			"name": "軟體版",
			"description": "聊軟體相關的知識"
		}
	]
}
```
- 沒有看板時不是error，仍回`200`:
```json
{
  "page": 1,
  "pageSize": 20,
  "total": 0,
  "items": []
}
```

#### Errors（本API會用到的）
- **400 Bad Request**
    - `VALIDATION_FAILED`
        - `PAGE_INVALID`
        - `PAGE_SIZE_INVALID`
        - `KEYWORD_INVALID`
- **500 Internal Server Error**
    - `INTERNAL_ERROR`
        - `UNEXPECTED_ERROR`

## Internal Design (Service & Repository)
### Use Case: ListBoards
#### `BoardService`
- **Method Signature**:
```Java
ListBoardsResponse listBoards(ListBoardsRequest request);
```
- **Transaction**:
    - `@Transactional(readOnly = true)` (優化讀取效能)
- **Input Model (`ListBoardsRequest`)**:
    - `page` (int)
    - `pageSize` (int)
    - `keyword` (String, nullable)
- **Return Model (`ListBoardsResponse`)**:
    - `page`
    - `pageSize`
    - `total` (long)
    - `items` (`List<BoardItem>`)
- **Logic Flow**:
    1. 若 `keyword` 存在且不為空 → 呼叫 Repository 進行模糊搜尋。
    2. 若 `keyword` 為空 → 呼叫 Repository 查詢全部。
    3. 將 DB Entity (`Board`) 轉換為 Domain/DTO (`BoardItem`)。


### Repository Strategy (Data Access)
#### `BoardRepository`
- 有 keyword 時
```Java
Page<Board> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
```
- 無 keyword 時
```Java
Page<Board> findAll(Pageable pageable);
```

## TEST
### `GET /boards`
#### Test Seed
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
#### 看板查詢成功
##### 無 keyword
- Given：Test Seed
- When：呼叫 `GET /boards`
- Then：
    - status：
        - 200 ok
    - response body：
        - `page` = 1
        - `pageSize` = 20
        - `total` = 2
        - `items`
            - 第一筆
                - `boardId` = 1
                - `name` = "八卦版"
                - `description` = "想聊什麼就聊什麼"
            - 第二筆
                - `boardId` = 2
                - `name` = "軟體版"
                - `description` = "聊軟體相關的知識"
##### 有 keyword
- Given：Test Seed
- When：呼叫 `GET /boards?keyword="八卦"`
- Then：
    - status：
        - 200 ok
    - response body：
        - `page` = 1
        - `pageSize` = 20
        - `total` = 1
        - `items`
            - 第一筆
                - `boardId` = 1
                - `name` = "八卦版"
                - `description` = "想聊什麼就聊什麼"

##### 有keyword但查無資料
- Given：Test Seed
- When：呼叫 `GET /boards?keyword="棒球"`
- Then：
    - status：
        - 200 ok
    - response body：
        - `page` = 1
        - `pageSize` = 20
        - `total` = 0
        - `items` none

#### 查詢看板失敗
##### page 驗證失敗
- Given：
    - Test Seed
    - **Scenarios (Input vs Expectation)**：

| **page_input** | **expected_code**      | **說明**    |
| -------------- | ---------------------- | --------- |
| `-1`           | **PAGE_INVALID**       | 數值違反最小值限制 |
| `"invalid"`    | **PARAM_FORMAT_ERROR** | 資料型別格式錯誤  |

- When：
    - 呼叫 `GET /boards?page=page_input`
- Then：
    - status：
        - 400 Bad Request
    - response body：
        - `status` = 400
        - `error` = "Bad Request"
        - `message` = "VALIDATION_FAILED"
        - `code` = {expected_code}
        - `timestamp` ="..."
##### pageSize 驗證失敗
- Given：
    - Test Seed
    - **Scenario Inputs** (`page_size_input`)：
        - 0
        - 101
        - "invalid"
- When：
    - 呼叫 `GET /boards?pageSize=page_size_input`
- Then：
    - status：
        - 400 Bad Request
    - response body：
        - `status` = 400
        - `error` = "Bad Request"
        - `message` = "VALIDATION_FAILED"
        - `code` = "PAGE_SIZE_INVALID"
        - `timestamp` ="..."
##### keyword 驗證失敗
- Given：
    - Test Seed
    - **Scenario Inputs** (`keyword_input`)：
        - a.repeat(51)
        - " " + "a".repeat(51) + " "
- When：
    - 呼叫 `GET /boards?keyword=keyword_input`
- Then：
    - status：
        - 400 Bad Request
    - response body：
        - `status` = 400
        - `error` = "Bad Request"
        - `message` = "VALIDATION_FAILED"
        - `code` = "KEYWORD_INVALID"
        - `timestamp` ="..."