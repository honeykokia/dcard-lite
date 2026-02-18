import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { LoginResponse } from './auth.types';

const ACCESS_TOKEN_KEY = 'accessToken';

interface User {
  userId: number;
  displayName: string;
}

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref<string | null>(localStorage.getItem(ACCESS_TOKEN_KEY));
  const user = ref<User>({
    userId: 0,
    displayName: ''
  });

  // Getter
  const isLoggedIn = computed(() => token.value !== null);

  // Actions
  /**
   * 設定認證資訊
   * @param data 登入回應資料
   */
  const setAuth = (data: LoginResponse) => {
    token.value = data.accessToken;
    user.value = {
      userId: data.userId,
      displayName: data.displayName
    };
    localStorage.setItem(ACCESS_TOKEN_KEY, data.accessToken);
  };

  /**
   * 清除認證資訊
   */
  const clearAuth = () => {
    token.value = null;
    user.value = {
      userId: 0,
      displayName: ''
    };
    localStorage.removeItem(ACCESS_TOKEN_KEY);
  };

  return {
    // State
    token,
    user,
    
    // Getters
    isLoggedIn,
    
    // Actions
    setAuth,
    clearAuth
  };
});