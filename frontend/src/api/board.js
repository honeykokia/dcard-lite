import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

/**
 * 取得看板列表
 * @param {Object} params - 查詢參數
 * @param {number} [params.page=1] - 頁碼 (>=1)
 * @param {number} [params.pageSize=20] - 每頁筆數 (1..100)
 * @param {string} [params.keyword] - 關鍵字搜尋 (可選)
 * @returns {Promise} API 回應
 */
export const listBoards = async (params = {}) => {
  const { page = 1, pageSize = 20, keyword = '' } = params

  const queryParams = new URLSearchParams()
  queryParams.append('page', page)
  queryParams.append('pageSize', pageSize)

  if (keyword && keyword.trim()) {
    queryParams.append('keyword', keyword.trim())
  }

  const response = await axios.get(`${API_BASE_URL}/boards?${queryParams.toString()}`)
  return response.data
}
