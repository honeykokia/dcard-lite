/**
 * 看板基本資訊
 */
export interface Board {
  boardId: number;
  name: string;
  description: string;
}

/**
 * 取得看板列表的查詢參數
 */
export interface ListBoardRequest {
  page?: number;        // default = 1, must be >= 1
  pageSize?: number;    // default = 20, range: 1-100
  keyword?: string;     // optional, 用於 name 模糊查詢，trim() 後長度 1-50
}

/**
 * 看板列表回應
 */
export interface ListBoardResponse {
  page: number;
  pageSize: number;
  total: number;
  items: Board[];
}