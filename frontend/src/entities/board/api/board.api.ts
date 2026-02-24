import apiClient from '@/app/api';
import type { ListBoardResponse, ListBoardRequest } from '../model/board.types';

export const boardApi = {
  /**
   * 取得看板列表
   * GET /boards
   * @param params - 查詢參數 (page, pageSize, keyword)
   * @returns 看板列表回應
   * @throws 400 VALIDATION_FAILED (PAGE_INVALID, PAGE_SIZE_INVALID, KEYWORD_INVALID)
   * @throws 500 INTERNAL_ERROR (UNEXPECTED_ERROR)
   */
  async listBoards(params?: ListBoardRequest): Promise<ListBoardResponse> {
    const response = await apiClient.get<ListBoardResponse>('/boards', {
      params: {
        page: params?.page ?? 1,
        pageSize: params?.pageSize ?? 20,
        keyword: params?.keyword?.trim() || undefined
      }
    });
    return response.data;
  }
};
