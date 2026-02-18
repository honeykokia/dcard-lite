import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { authApi } from '@/entities/auth/api/auth.api';
import { getEmailValidationErrors, getPasswordValidationErrors } from '@/shared/utils';
import type { RegisterRequest } from '@/entities/auth/model';
import type { ErrorResponse } from '@/entities/error/model/types';

interface RegisterForm {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
}

interface FormErrors {
  name: string[];
  email: string[];
  password: string[];
  confirmPassword: string[];
}

export function useRegister() {
  const router = useRouter();

  // 表單狀態
  const form = ref<RegisterForm>({
    name: '',
    email: '',
    password: '',
    confirmPassword: ''
  });

  // 錯誤狀態
  const errors = ref<FormErrors>({
    name: [],
    email: [],
    password: [],
    confirmPassword: []
  });

  // Loading 狀態
  const isLoading = ref(false);

  // API 錯誤訊息
  const apiError = ref('');

  // 表單是否有效
  const isValid = computed(() => {
    return errors.value.name.length === 0 &&
           errors.value.email.length === 0 &&
           errors.value.password.length === 0 &&
           errors.value.confirmPassword.length === 0 &&
           form.value.name.trim() !== '' &&
           form.value.email.trim() !== '' &&
           form.value.password !== '' &&
           form.value.confirmPassword !== '';
  });

  /**
   * 驗證 Name
   */
  const validateName = () => {
    errors.value.name = [];
    const name = form.value.name.trim();

    if (!name) {
      errors.value.name.push('Name must not be blank');
      return;
    }

    if (name.length > 20) {
      errors.value.name.push('Name maximum length 20 characters');
      return;
    }

    // 檢查是否只含數字
    if (/^\d+$/.test(name)) {
      errors.value.name.push('Values consisting only of digits or only symbols are not allowed');
      return;
    }

    // 檢查是否只含符號
    if (/^[^\w\s]+$/.test(name)) {
      errors.value.name.push('Values consisting only of digits or only symbols are not allowed');
      return;
    }
  };

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
   * 驗證 ConfirmPassword
   */
  const validateConfirmPassword = () => {
    errors.value.confirmPassword = [];
    const confirmPassword = form.value.confirmPassword;

    if (confirmPassword == null || confirmPassword === '') {
      errors.value.confirmPassword.push('ConfirmPassword must not be blank');
      return;
    }

    if (confirmPassword !== form.value.password) {
      errors.value.confirmPassword.push('Confirm password must match password');
      return;
    }
  };

  /**
   * 驗證所有欄位
   */
  const validateAll = () => {
    validateName();
    validateEmail();
    validatePassword();
    validateConfirmPassword();
    return isValid.value;
  };

  /**
   * 處理 API 錯誤
   */
  const handleApiError = (error: any) => {
    if (error?.response?.data) {
      const errorData = error.response.data as ErrorResponse;

      // 根據 error code 映射錯誤訊息
      switch (errorData.code) {
        case 'NAME_INVALID':
          errors.value.name = [errorData.message || 'Invalid name'];
          break;
        case 'EMAIL_INVALID':
          errors.value.email = [errorData.message || 'Invalid email'];
          break;
        case 'EMAIL_ALREADY_EXISTS':
          errors.value.email = ['Email is existed'];
          break;
        case 'PASSWORD_INVALID':
          errors.value.password = [errorData.message || 'Invalid password'];
          break;
        case 'CONFIRM_PASSWORD_INVALID':
          errors.value.confirmPassword = [errorData.message || 'Invalid confirm password'];
          break;
        default:
          apiError.value = errorData.message || 'Registration failed. Please try again.';
      }
    } else {
      apiError.value = 'Network error. Please try again.';
    }
  };

  /**
   * 提交註冊
   */
  const handleRegister = async () => {
    // 清除之前的 API 錯誤
    apiError.value = '';

    // 驗證所有欄位
    if (!validateAll()) {
      return;
    }

    try {
      isLoading.value = true;

      const registerRequest: RegisterRequest = {
        name: form.value.name.trim(),
        email: form.value.email.trim().toLowerCase(),
        password: form.value.password,
        confirmPassword: form.value.confirmPassword
      };

      await authApi.register(registerRequest);

      // 註冊成功，跳轉到登入頁面
      router.push('/login');

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
      name: '',
      email: '',
      password: '',
      confirmPassword: ''
    };
    errors.value = {
      name: [],
      email: [],
      password: [],
      confirmPassword: []
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
    validateName,
    validateEmail,
    validatePassword,
    validateConfirmPassword,
    validateAll,
    handleRegister,
    resetForm
  };
}