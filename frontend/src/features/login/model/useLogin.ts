import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { authApi } from '@/entities/auth/api/auth.api';
import type { LoginRequest } from '@/entities/auth/model/types';
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
           form.value.email.trim() !== '' &&
           form.value.password.trim() !== '';
  });

  /**
   * 驗證 Email
   */
  const validateEmail = () => {
    errors.value.email = [];
    const email = form.value.email.trim();

    if (!email) {
      errors.value.email.push('Email must not be blank');
      return;
    }

    if (email.length > 100) {
      errors.value.email.push('Email maximum length 100 characters');
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      errors.value.email.push('Email must be in a valid email format');
      return;
    }
  };

  /**
   * 驗證 Password
   */
  const validatePassword = () => {
    errors.value.password = [];
    const password = form.value.password;

    if (!password || password.trim() === '') {
      errors.value.password.push('Password must not be blank');
      return;
    }

    if (password.length < 8 || password.length > 12) {
      errors.value.password.push('Password length must be between 8 and 12 characters');
      return;
    }

    const hasLetter = /[a-zA-Z]/.test(password);
    const hasDigit = /\d/.test(password);

    if (!hasLetter || !hasDigit) {
      errors.value.password.push('Password must contain at least one letter and one digit');
      return;
    }
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

      // 儲存 token
      localStorage.setItem('accessToken', response.accessToken);

      // 可選：儲存使用者資訊
      localStorage.setItem('displayName', response.displayName);

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
