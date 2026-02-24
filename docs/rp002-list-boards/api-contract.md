# API Contract
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

## API Endpoint
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
