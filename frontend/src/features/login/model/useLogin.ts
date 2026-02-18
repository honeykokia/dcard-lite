import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { authApi } from '@/entities/auth/api/auth.api';
import { useAuthStore } from '@/entities/auth/model';
import { getPasswordValidationErrors, getEmailValidationErrors } from '@/shared/utils';
import type { LoginRequest } from '@/entities/auth/model';
import type { ErrorResponse } from '@/entities/error/model/types';


interface LoginForm {
  email: string;
  password: string;
}

interface FormErrors {
  email: string[];
  password: string[];
}

export function useLogin() {
  const router = useRouter();
  const authStore = useAuthStore();

  // 表單狀態
  const form = ref<LoginForm>({
    email: '',
    password: ''
  });

  // 錯誤狀態
  const errors = ref<FormErrors>({
    email: [],
    password: []
  });

  // Loading 狀態
  const isLoading = ref(false);

  // API 錯誤訊息
  const apiError = ref('');

  // 表單是否有效
  const isValid = computed(() => {
    return errors.value.email.length === 0 &&
           errors.value.password.length === 0 &&
           form.value.email !== '' &&
           form.value.password !== '';
  });

  /**
   * 驗證 Email
   */
  const validateEmail = () => {
    errors.value.email = getEmailValidationErrors(form.value.email);
  };

  /**
   * 驗證 Password
   */
  const validatePassword = () => {
    errors.value.password = getPasswordValidationErrors(form.value.password);
  };

  /**
   * 驗證所有欄位
   */
  const validateAll = () => {
    validateEmail();
    validatePassword();
    return isValid.value;
  };

  /**
   * 處理 API 錯誤
   */
  const handleApiError = (error: any) => {
    if (error.response?.data) {
      const errorData = error.response.data as ErrorResponse;

      // 根據 error code 映射錯誤訊息
      switch (errorData.code) {
        case 'EMAIL_INVALID':
          errors.value.email = [errorData.message || 'Invalid email'];
          break;
        case 'PASSWORD_INVALID':
          errors.value.password = [errorData.message || 'Invalid password'];
          break;
        case 'AUTHENTICATION_FAILED':
          apiError.value = 'Email or password is incorrect';
          break;
        default:
          apiError.value = errorData.message || 'Login failed. Please try again.';
      }
    } else {
      apiError.value = 'Network error. Please try again.';
    }
  };

  /**
   * 提交登入
   */
  const handleLogin = async () => {
    // 清除之前的 API 錯誤
    apiError.value = '';

    // 驗證所有欄位
    if (!validateAll()) {
      return;
    }

    try {
      isLoading.value = true;

      const loginRequest: LoginRequest = {
        email: form.value.email.trim().toLowerCase(),
        password: form.value.password
      };

      const response = await authApi.login(loginRequest);

      // 使用 authStore 儲存認證資訊
      authStore.setAuth(response);

      // 跳轉到首頁
      router.push('/');
    } catch (error) {
      handleApiError(error);
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * 重置表單
   */
  const resetForm = () => {
    form.value = {
      email: '',
      password: ''
    };
    errors.value = {
      email: [],
      password: []
    };
    apiError.value = '';
  };

  return {
    // 狀態
    form,
    errors,
    isLoading,
    apiError,
    isValid,

    // 方法
    validateEmail,
    validatePassword,
    validateAll,
    handleLogin,
    resetForm
  };
}
