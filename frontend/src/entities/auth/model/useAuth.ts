import router from '@/router';
import { useAuthStore } from './auth.store';

/**
 * useAuth Composable
 * 提供認證相關的共用邏輯，包含登出功能
 */
export const useAuth = () => {
  const authStore = useAuthStore();

  /**
   * 登出處理
   * - 主動觸發：使用者點擊 UI 介面（如 Navbar）中的「登出」按鈕
   * - 被動觸發：當 API 回傳 401 Unauthorized 錯誤時，由 Axios 攔截器自動呼叫
   */
  const logout = () => {
    // 1. 清除認證狀態
    authStore.clearAuth();

    // 2. 重導向至登入頁
    // 使用 replace 而非 push，防止使用者透過瀏覽器「回退」按鈕返回已登出的頁面
    router.replace('/login');
  };

  return {
    // State (from store)
    isLoggedIn: authStore.isLoggedIn,
    user: authStore.user,
    token: authStore.token,

    // Actions
    logout
  };
};