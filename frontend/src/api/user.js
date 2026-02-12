import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

/**
 * 用戶登入
 * @param {string} email - 用戶郵箱
 * @param {string} password - 用戶密碼
 * @returns {Promise} 登入響應
 */
export const loginUser = async (email, password) => {
  const response = await axios.post(`${API_BASE_URL}/users/login`, {
    email: email.trim(),
    password: password.trim()
  })
  return response.data
}

/**
 * 用戶註冊
 * @param {string} name - 用戶名稱
 * @param {string} email - 用戶郵箱
 * @param {string} password - 用戶密碼
 * @param {string} confirmPassword - 確認密碼
 * @returns {Promise} 註冊響應
 */
export const registerUser = async (name, email, password, confirmPassword) => {
  const response = await axios.post(`${API_BASE_URL}/users/register`, {
    name: name.trim(),
    email: email.trim(),
    password: password.trim(),
    confirmPassword: confirmPassword.trim()
  })
  return response.data
}
