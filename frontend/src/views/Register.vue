<template>
  <div class="flex min-h-screen items-center justify-center bg-[#f2f3f5] px-4 py-8">
    <div class="w-full max-w-[440px] rounded-2xl bg-white px-8 py-10 shadow-[0_2px_16px_rgba(0,0,0,0.08)]">
      <!-- Logo/Title -->
      <div class="mb-8 text-center">
        <h1 class="mb-2 text-3xl font-bold tracking-tight text-gray-900">註冊 Dcard Lite</h1>
        <p class="text-sm text-gray-500">加入我們，開始你的討論之旅</p>
      </div>

      <form @submit.prevent="handleRegister" class="space-y-5">
        <!-- 顯示名稱欄位 -->
        <div class="space-y-1.5">
          <label for="name" class="block text-sm font-medium text-gray-700">顯示名稱</label>
          <input
            id="name"
            v-model="formData.name"
            type="text"
            placeholder="請輸入顯示名稱"
            class="w-full rounded-lg border px-4 py-3 text-sm transition-all duration-200 placeholder:text-gray-400 focus:outline-none focus:ring-2"
            :class="errors.name
              ? 'border-red-300 focus:border-red-400 focus:ring-red-100'
              : 'border-gray-200 focus:border-[#3397cf] focus:ring-[#3397cf]/20'"
            @blur="validateName"
          />
          <p v-if="errors.name" class="text-xs text-red-500 mt-1">{{ errors.name }}</p>
        </div>

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
            placeholder="8-12字元，需包含英文和數字"
            class="w-full rounded-lg border px-4 py-3 text-sm transition-all duration-200 placeholder:text-gray-400 focus:outline-none focus:ring-2"
            :class="errors.password
              ? 'border-red-300 focus:border-red-400 focus:ring-red-100'
              : 'border-gray-200 focus:border-[#3397cf] focus:ring-[#3397cf]/20'"
            @blur="validatePassword"
          />
          <p v-if="errors.password" class="text-xs text-red-500 mt-1">{{ errors.password }}</p>
        </div>

        <!-- Confirm Password 欄位 -->
        <div class="space-y-1.5">
          <label for="confirmPassword" class="block text-sm font-medium text-gray-700">確認密碼</label>
          <input
            id="confirmPassword"
            v-model="formData.confirmPassword"
            type="password"
            placeholder="請再次輸入密碼"
            class="w-full rounded-lg border px-4 py-3 text-sm transition-all duration-200 placeholder:text-gray-400 focus:outline-none focus:ring-2"
            :class="errors.confirmPassword
              ? 'border-red-300 focus:border-red-400 focus:ring-red-100'
              : 'border-gray-200 focus:border-[#3397cf] focus:ring-[#3397cf]/20'"
            @blur="validateConfirmPassword"
          />
          <p v-if="errors.confirmPassword" class="text-xs text-red-500 mt-1">{{ errors.confirmPassword }}</p>
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
          <span v-if="!isLoading">註冊</span>
          <span v-else class="flex items-center justify-center gap-2">
            <svg class="h-5 w-5 animate-spin" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            註冊中...
          </span>
        </button>
      </form>

      <!-- 登入連結 -->
      <div class="mt-6 text-center">
        <p class="text-sm text-gray-600">
          已經有帳號了？
          <router-link
            to="/login"
            class="font-semibold text-[#3397cf] hover:text-[#2b7fb3] hover:underline transition-colors"
          >
            立即登入
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
  name: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const errors = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const apiError = ref('')
const isLoading = ref(false)

// Name 驗證
const validateName = () => {
  const name = formData.name.trim()

  if (!name) {
    errors.name = '顯示名稱不可為空'
    return false
  }

  if (name.length > 20) {
    errors.name = '顯示名稱長度不可超過 20 字元'
    return false
  }

  // 不接受純數字或只含符號
  const onlyDigits = /^\d+$/.test(name)
  const onlySymbols = /^[^a-zA-Z0-9\u4e00-\u9fa5]+$/.test(name)

  if (onlyDigits || onlySymbols) {
    errors.name = '顯示名稱不可為純數字或純符號'
    return false
  }

  errors.name = ''
  return true
}

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

  if (password.length < 8 || password.length > 12) {
    errors.password = '密碼長度必須為 8-12 字元'
    return false
  }

  // 至少要有一個英文字母和一個數字
  const hasLetter = /[a-zA-Z]/.test(password)
  const hasDigit = /\d/.test(password)

  if (!hasLetter || !hasDigit) {
    errors.password = '密碼必須包含至少一個英文字母和一個數字'
    return false
  }

  errors.password = ''
  return true
}

// Confirm Password 驗證
const validateConfirmPassword = () => {
  const confirmPassword = formData.confirmPassword.trim()

  if (!confirmPassword) {
    errors.confirmPassword = '確認密碼不可為空'
    return false
  }

  if (confirmPassword !== formData.password.trim()) {
    errors.confirmPassword = '確認密碼必須與密碼相同'
    return false
  }

  errors.confirmPassword = ''
  return true
}

// 表單驗證
const validateForm = () => {
  const nameValid = validateName()
  const emailValid = validateEmail()
  const passwordValid = validatePassword()
  const confirmPasswordValid = validateConfirmPassword()

  return nameValid && emailValid && passwordValid && confirmPasswordValid
}

// 處理註冊
const handleRegister = async () => {
  apiError.value = ''

  if (!validateForm()) {
    return
  }

  isLoading.value = true

  try {
    await userStore.register(
      formData.name,
      formData.email,
      formData.password,
      formData.confirmPassword
    )

    // 註冊成功，導向登入頁
    router.push({
      name: 'login',
      query: { registered: 'true' }
    })
  } catch (error) {
    if (error.response) {
      const { status, data } = error.response

      // 根據錯誤代碼顯示訊息
      if (status === 400) {
        if (data.code === 'NAME_INVALID') {
          errors.name = '顯示名稱格式不正確'
        } else if (data.code === 'EMAIL_INVALID') {
          errors.email = 'Email 格式不正確'
        } else if (data.code === 'PASSWORD_INVALID') {
          errors.password = '密碼格式不正確'
        } else if (data.code === 'CONFIRM_PASSWORD_INVALID') {
          errors.confirmPassword = '確認密碼不正確'
        } else {
          apiError.value = '輸入資料有誤，請檢查後重試'
        }
      } else if (status === 409) {
        if (data.code === 'EMAIL_ALREADY_EXISTS') {
          errors.email = '此 Email 已被註冊'
        } else {
          apiError.value = '註冊失敗，此帳號可能已存在'
        }
      } else {
        apiError.value = '註冊失敗，請稍後再試'
      }
    } else {
      apiError.value = '網路錯誤，請檢查您的網路連線'
    }
  } finally {
    isLoading.value = false
  }
}
</script>
