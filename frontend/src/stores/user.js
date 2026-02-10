import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'
import { jwtDecode } from 'jwt-decode'

export const useUserStore = defineStore('user', () => {
  // State
  const accessToken = ref(localStorage.getItem('accessToken') || '')
  const userId = ref('')
  const displayName = ref('')
  const role = ref('')

  // Getters
  const isAuthenticated = computed(() => !!accessToken.value)
  const isAdmin = computed(() => role.value === 'ADMIN')

  // Actions
  const decodeToken = (token) => {
    try {
      const decoded = jwtDecode(token)
      userId.value = decoded.userId || decoded.sub || ''
      role.value = decoded.role || 'USER'
      return true
    } catch (error) {
      console.error('Token decode error:', error)
      return false
    }
  }

  const setToken = (token) => {
    accessToken.value = token
    localStorage.setItem('accessToken', token)

    // 解碼 token 獲取使用者資訊
    decodeToken(token)

    // 設置 axios 預設 header
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
  }

  const clearAuth = () => {
    accessToken.value = ''
    userId.value = ''
    displayName.value = ''
    role.value = ''

    localStorage.removeItem('accessToken')
    delete axios.defaults.headers.common['Authorization']
  }

  const login = async (email, password) => {
    const response = await axios.post('http://localhost:8080/users/login', {
      email: email.trim(),
      password: password.trim()
    })

    setToken(response.data.accessToken)
    if (response.data.displayName) {
      displayName.value = response.data.displayName
    }
    return response.data
  }

  const register = async (name, email, password, confirmPassword) => {
    const response = await axios.post('http://localhost:8080/users/register', {
      name: name.trim(),
      email: email.trim(),
      password: password.trim(),
      confirmPassword: confirmPassword.trim()
    })

    return response.data
  }

  const logout = () => {
    clearAuth()
  }

  // 初始化：如果有 token，解碼它
  const init = () => {
    if (accessToken.value) {
      decodeToken(accessToken.value)
      axios.defaults.headers.common['Authorization'] = `Bearer ${accessToken.value}`
    }
  }

  return {
    // State
    accessToken,
    userId,
    displayName,
    role,
    // Getters
    isAuthenticated,
    isAdmin,
    // Actions
    setToken,
    clearAuth,
    login,
    register,
    logout,
    init
  }
})
