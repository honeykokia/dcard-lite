# Frontend Design
## Interface
- Location : `/entities/board/model/board.types.ts`
- Types :
    - **Board**
        - boardId (`number`)
        - name (`name`)
        - description (`string`)
    - **ListBoardRequest**
        - page (`number`)
        - pageSize (`number`)
        - keyword (`string`)
    - **ListBoardResponse**
        - page (`number`)
        - pageSize (`number`)
        - total (`number`)
        - items (`BoardItem[]`)

## API
### Board
- Location : `entities/board/api/board.api/ts`
- Method
    - `getBoards(ListBoardRequest)` -> Ref: api-contract.md `GET /boards`
## Stores
### Board
- Location : `entities/board/model/board.store.ts`
- State :
    - `boardList`: `Board[]` (目前的看板列表)
    - `currentBoardId`: `number` (選取的看板ID)
- Getter
    - `currentBoard` : `Board`
        - 更新使用者選取的看板物件
- Actions
    - `setBoardList(Board[])`
        - 更新 `boardList`
    - `clearBoardList()`
        - 清除 `boardList`

## Composable
### Board
#### Use Case: listBoards
- Location : `features/listBoards/useListBoards.ts`
- Description: 封裝「看板列表頁」的頁面行為（如：初始化、搜尋）。
- State
    - `isLoading` : `boolean`
    - `error` : `string`
    - `currentPage` : `number`
    - `pageSize` : `number`
    - `total` : `number`
    - `keyword` : `string`
- Actions
    - **loadBoards**
        - Trigger : 頁面 Mounted、搜尋按鈕、分頁切換
        - Action
            1. 狀態初始化，設定 `isLoading = true` 並清空舊有的 `error`
            2. 呼叫API : 帶入當前 **Composable State** (即 `currentPage`, `keyword`) 中的分頁與關鍵字參數
                - 成功處理 : 更新 `boardStore` 的列表資料，並同步更新本地的 `total` 與分頁狀態
                - 錯誤處理: 攔截 Exception，將後端Error Code轉換為使用者可讀的文字訊息
            3. 程式執行完畢，`isLoading = false`
    - **searchBoards(keyword)**
        - Trigger : 搜尋框送出
        - Action
            - 更新本地 `keyword` 並將 `currentPage`重置為1，執行 `loadBoards`
    - **changePage(page)**
        - Trigger: 點擊分頁元件。
        - Action: 更新 `currentPage` 並執行 `loadBoards`。
## Components
### Feature
#### BoardList
- **Location**: `features/listBoards/UI/BoardList.vue`
- **Props**:
    - `boards`: `Board[]` (由父組件或 Store 傳入)
- **Logic**:
    - 渲染 `boardList` 中的每一個看板卡片。
    - 當點擊特定看板時，觸發 `router.push` 跳轉至看板詳情頁。

### Widdget
#### NavBar
- **Location**: `widgets/NavBar.vue`
- **Description**: 全域導覽列，提供品牌識別、主選單導向及使用者狀態顯示。
- **UI Elements**:
    - Logo
        - `type` : link
        - `description` : 顯示文字，點擊可以跳轉到首頁。
    - 登入
        - `type` : button
        - `description` : 點擊跳轉至登入頁面
    - 註冊
        - `type` : button
        - `description` : 點擊條轉至註冊頁面

## Pages
### HomePage
- **Location**: `pages/home/HomePage.vue`
- **Description**: 作為看板系統的主入口，整合導覽列、側邊欄與看板列表功能。
- **Composition**:
    - `NavBar` (Widget)
    - `ListBoardsForm` (Feature)
