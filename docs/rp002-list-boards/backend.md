# Backend Design
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

## DTOs
- **BoardItem**
    - boardId (`long`)
    - name (`String`)
    - description (`String`)
- **ListBoardsRequest**
    - page (`int`)
    - pageSize (`int`)
    - keyword (`String`)
- **ListBoardsResponse**
    - page (`int`)
    - pageSize (`int`)
    - total (`long`)
    - items (`List<BoardItem>`)

## Service Logic
### `BoardService`
#### Use Case: ListBoards
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

## Data Access
### `BoardRepository`
#### Use Case: 有 keyword 時
```Java
Page<Board> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
```
#### Use Case: 無 keyword 時
```Java
Page<Board> findAll(Pageable pageable);
```
