import apiClient from '@/app/api';
import type { LoginRequest, LoginResponse, RegisterUserRequest, RegisterUserResponse } from '../model';

export const authApi = {
    /**
     * 使用者註冊
     * @param request 註冊請求資料
     * @returns 註冊回應資料
     */
    register: async (request: RegisterUserRequest): Promise<RegisterUserResponse> => {
        const response = await apiClient.post<RegisterUserResponse>('/users/register', request);
        return response.data;
    },

    /**
     * 使用者登入
     * @param request 登入請求資料
     * @returns 登入回應資料
     */
    login: async (request: LoginRequest): Promise<LoginResponse> => {
        const response = await apiClient.post<LoginResponse>('/users/login', request);
        return response.data;
    }
};
