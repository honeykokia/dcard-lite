<template>
  <div class="flex min-h-screen items-center justify-center bg-[#f2f3f5] px-4 py-8">
    <div class="w-full max-w-[440px] rounded-2xl bg-white px-8 py-10 shadow-[0_2px_16px_rgba(0,0,0,0.08)]">
      <!-- Logo/Title -->
      <div class="mb-8 text-center">
        <h1 class="mb-2 text-3xl font-bold tracking-tight text-gray-900">登入 Dcard Lite</h1>
        <p class="text-sm text-gray-500">歡迎回來，繼續你的討論之旅</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-5">
        <!-- Email 欄位 -->
        <div class="space-y-1.5">
          <label for="email" class="block text-sm font-medium text-gray-700">Email</label>
          <input
            id="email"
            v-model="formData.email"
            type="email"
            placeholder="example@email.com"
            class="w-full rounded-lg border px-4 py-3 text-sm transition-all duration-200 placeholder:text-gray-400 focus:outline-none focus:ring-2"
            :class="errors.email
              ? 'border-red-300 focus:border-red-400 focus:ring-red-100'
              : 'border-gray-200 focus:border-[#3397cf] focus:ring-[#3397cf]/20'"
            @blur="validateEmail"
          />
          <p v-if="errors.email" class="text-xs text-red-500 mt-1">{{ errors.email }}</p>
        </div>

        <!-- Password 欄位 -->
        <div class="space-y-1.5">
          <label for="password" class="block text-sm font-medium text-gray-700">密碼</label>
          <input
            id="password"
            v-model="formData.password"
            type="password"
            placeholder="請輸入密碼"
            class="w-full rounded-lg border px-4 py-3 text-sm transition-all duration-200 placeholder:text-gray-400 focus:outline-none focus:ring-2"
            :class="errors.password
              ? 'border-red-300 focus:border-red-400 focus:ring-red-100'
              : 'border-gray-200 focus:border-[#3397cf] focus:ring-[#3397cf]/20'"
            @blur="validatePassword"
          />
          <p v-if="errors.password" class="text-xs text-red-500 mt-1">{{ errors.password }}</p>
        </div>

        <!-- API 錯誤訊息 -->
        <div
          v-if="apiError"
          class="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-red-700"
        >
          {{ apiError }}
        </div>

        <!-- 提交按鈕 -->
        <button
          type="submit"
          class="w-full rounded-lg bg-[#3397cf] py-3.5 text-base font-semibold text-white shadow-sm transition-all duration-200 hover:bg-[#2b7fb3] hover:shadow-md active:scale-[0.98] disabled:cursor-not-allowed disabled:bg-gray-300 disabled:hover:shadow-none"
          :disabled="isLoading"
        >
          <span v-if="!isLoading">登入</span>
          <span v-else class="flex items-center justify-center gap-2">
            <svg class="h-5 w-5 animate-spin" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            登入中...
          </span>
        </button>
      </form>

      <!-- 註冊連結 -->
      <div class="mt-6 text-center">
        <p class="text-sm text-gray-600">
          還沒有帳號？
          <router-link
            to="/register"
            class="font-semibold text-[#3397cf] hover:text-[#2b7fb3] hover:underline transition-colors"
          >
            立即註冊
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formData = reactive({
  email: '',
  password: ''
})

const errors = reactive({
  email: '',
  password: ''
})

const apiError = ref('')
const isLoading = ref(false)

// Email 驗證
const validateEmail = () => {
  const email = formData.email.trim()

  if (!email) {
    errors.email = 'Email 不可為空'
    return false
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email)) {
    errors.email = 'Email 格式不正確'
    return false
  }

  if (email.length > 100) {
    errors.email = 'Email 長度不可超過 100 字元'
    return false
  }

  errors.email = ''
  return true
}

// Password 驗證
const validatePassword = () => {
  const password = formData.password.trim()

  if (!password) {
    errors.password = '密碼不可為空'
    return false
  }

  errors.password = ''
  return true
}

// 表單驗證
const validateForm = () => {
  const emailValid = validateEmail()
  const passwordValid = validatePassword()
  return emailValid && passwordValid
}

// 處理登入
const handleLogin = async () => {
  apiError.value = ''

  if (!validateForm()) {
    return
  }

  isLoading.value = true

  try {
    await userStore.login(formData.email, formData.password)

    // 導向首頁
    router.push('/')
  } catch (error) {
    if (error.response) {
      const { status, data } = error.response

      // 根據錯誤代碼顯示訊息
      if (status === 400) {
        if (data.code === 'EMAIL_INVALID') {
          errors.email = 'Email 格式不正確'
        } else if (data.code === 'PASSWORD_INVALID') {
          errors.password = '密碼格式不正確'
        } else {
          apiError.value = '輸入資料有誤，請檢查後重試'
        }
      } else if (status === 401) {
        if (data.code === 'AUTHENTICATION_FAILED') {
          apiError.value = 'Email 或密碼錯誤'
        }
      } else {
        apiError.value = '登入失敗，請稍後再試'
      }
    } else {
      apiError.value = '網路錯誤，請檢查您的網路連線'
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.login-title {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #555;
}

.form-group input {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #006aa6;
}

.form-group input.input-error {
  border-color: #dc3545;
}

.error-message {
  font-size: 12px;
  color: #dc3545;
}

.api-error {
  padding: 12px;
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  border-radius: 4px;
  color: #721c24;
  font-size: 14px;
  text-align: center;
}

.login-button {
  padding: 12px;
  background-color: #006aa6;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-button:hover:not(:disabled) {
  background-color: #005a8c;
}

.login-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.register-link a {
  color: #006aa6;
  text-decoration: none;
  font-weight: 500;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>
