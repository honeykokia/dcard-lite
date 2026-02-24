import { ref } from 'vue';
import { boardApi } from '@/entities/board/api/board.api';
import { useBoardStore } from '@/entities/board/model/board.store';
import type { ListBoardRequest } from '@/entities/board/model/board.types';
import type { ErrorResponse } from '@/entities/error/model/error.types';

export const useListBoards = () => {
  const boardStore = useBoardStore();

  // State
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  const currentPage = ref(1);
  const pageSize = ref(20);
  const total = ref(0);
  const keyword = ref('');

  /**
   * 載入看板列表
   */
  const loadBoards = async (params?: ListBoardRequest) => {
    isLoading.value = true;
    error.value = null;

    try {
      const response = await boardApi.listBoards({
        page: params?.page ?? currentPage.value,
        pageSize: params?.pageSize ?? pageSize.value,
        keyword: params?.keyword ?? keyword.value
      });

      // 更新 store
      boardStore.setBoardList(response.items);

      // 更新分頁資訊
      currentPage.value = response.page;
      pageSize.value = response.pageSize;
      total.value = response.total;

      return response;
    } catch (err: any) {
      // 處理錯誤
      const errorResponse = err.response?.data as ErrorResponse;

      if (errorResponse?.code) {
        // 根據 error code 顯示對應的錯誤訊息
        switch (errorResponse.code) {
          case 'PAGE_INVALID':
            error.value = '頁碼無效，請確認頁碼大於等於 1';
            break;
          case 'PAGE_SIZE_INVALID':
            error.value = '每頁筆數無效，請確認範圍為 1-100';
            break;
          case 'KEYWORD_INVALID':
            error.value = '關鍵字長度無效，請確認長度為 1-50 字元';
            break;
          case 'UNEXPECTED_ERROR':
            error.value = '伺服器發生異常，請稍後再試';
            break;
          default:
            error.value = errorResponse.message || '載入看板列表失敗';
        }
      } else {
        error.value = err.message || '載入看板列表失敗，請稍後再試';
      }

      // 清空看板列表
      boardStore.clearBoardList();
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * 重新載入當前頁面
   */
  const reload = () => {
    return loadBoards({
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: keyword.value
    });
  };

  /**
   * 搜尋看板
   */
  const searchBoards = (searchKeyword: string) => {
    keyword.value = searchKeyword;
    currentPage.value = 1; // 重置到第一頁
    return loadBoards();
  };

  /**
   * 切換頁面
   */
  const changePage = (page: number) => {
    currentPage.value = page;
    return loadBoards();
  };

  /**
   * 清除錯誤
   */
  const clearError = () => {
    error.value = null;
  };

  return {
    // State
    isLoading,
    error,
    currentPage,
    pageSize,
    total,
    keyword,

    // Actions
    loadBoards,
    reload,
    searchBoards,
    changePage,
    clearError
  };
};
