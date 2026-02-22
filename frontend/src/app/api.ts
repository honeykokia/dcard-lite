import axios from 'axios';
import { useAuthStore, useAuth } from '@/entities/auth/model';
import router from '@/router';

// 創建 Axios 實例
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
});

// 請求攔截器
apiClient.interceptors.request.use(
  (config) => {
    // 在這裡添加通用的請求頭，例如 Authorization
    const authStore = useAuthStore();
    const accessToken = authStore.token;
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => {
    // 處理請求錯誤
    return Promise.reject(error);
  }
);

// 回應攔截器
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
      const { status } = error.response || {};

    if (!error.response) {
        console.error('網路連線異常，請檢查您的網路設定');
        return Promise.reject({ message: '網路連線異常，請稍後再試' });
    }

      // 1. 處理「系統級」錯誤：全域處理
      if (status === 401) {
        // 觸發全域登出邏輯
        useAuth().logout();
        router.replace('/login');
        return Promise.reject(error);
      }

      if (status === 500) {
        console.error('伺服器發生異常');
        return Promise.reject(error);
      }

      // 2. 處理「業務級」錯誤 (例如 400, 404, 409)：
      // 直接 Reject，讓個別的 feature api 或是組件去 catch 並顯示專屬訊息
      return Promise.reject(error);
    }
);
export default apiClient;
