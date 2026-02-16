## 🎨 前端 UI/UX 規範 (Style Spec) - 以 Dcard 為範本

這份規範旨在確保所有頁面（註冊、登入、看板、發文）具有一致的視覺語境。
### 1. 全域風格 (Global Constants)

|**項目**|**規範內容**|**Tailwind 類別參考**|
|---|---|---|
|**技術棧**|Vue 3 (Composition API) + Tailwind CSS v4|-|
|**底色 (Background)**|柔和淺灰色 `#f2f3f5`|`bg-[#f2f3f5]`|
|**主色 (Primary)**|Dcard 藍 `#3397cf`|`bg-[#3397cf]`|
|**卡片底色**|純白色 `#ffffff`|`bg-white`|
|**文字顏色**|深灰色 `#333333` (主標), 中灰 `#666666` (內文)|`text-gray-800`, `text-gray-600`|

### 2. 佈局原則 (Layout Principles)

- **置中容器**：表單類頁面（登入/註冊）必須在視窗中水平垂直置中。

- **卡片規範**：

    - 圓角：`rounded-2xl` (或 `16px`)。

    - 陰影：`shadow-lg` (柔和的長陰影)。

    - 寬度：最大寬度限制為 `max-w-md` (約 448px)。

- **間距**：卡片內部 Padding 統一為 `p-8` 至 `p-10`，確保視覺呼吸感。


### 3. 組件標準 (Component UI)

- **輸入框 (Inputs)**：

    - 預設：`border-gray-300`。

    - 聚焦 (Focus)：`border-[#3397cf]` 並帶有輕微藍色光暈。

    - 錯誤 (Error)：`border-red-500`。

- **按鈕 (Buttons)**：

    - 狀態：必須包含 `hover:bg-opacity-90` 與 `active:scale-95` 的微互動。

    - 禁用：`disabled:bg-gray-400` 且 `cursor-not-allowed`。