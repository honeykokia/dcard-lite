<template>
  <div class="flex min-h-screen flex-col bg-[#f2f3f5]">
    <!-- 導航欄 -->
    <header class="bg-[#3397cf] shadow-sm">
      <div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6">
        <div class="flex items-center gap-4">
          <button
            @click="router.push('/')"
            class="rounded-lg p-2 text-white transition-colors hover:bg-white/10"
            title="返回首頁"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
          </button>
          <h1 class="text-2xl font-bold text-white">Dcard Lite</h1>
        </div>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2 text-sm text-white">
            <span v-if="userStore.role === 'ADMIN'" class="rounded bg-orange-500 px-2 py-1 text-xs font-bold">
              管理員
            </span>
            <span class="font-medium">{{ userStore.displayName }}</span>
          </div>
          <button
            @click="handleLogout"
            class="rounded-lg border border-white/30 bg-white/10 px-4 py-2 text-sm font-medium text-white transition-all hover:bg-white/20"
          >
            登出
          </button>
        </div>
      </div>
    </header>

    <!-- 主要內容 -->
    <main class="mx-auto w-full max-w-7xl flex-1 px-4 py-8 sm:px-6">
      <!-- 標題與搜尋 -->
      <div class="mb-6 rounded-2xl bg-white p-8 shadow-[0_2px_16px_rgba(0,0,0,0.08)]">
        <h2 class="mb-6 text-3xl font-bold text-gray-900">看板列表</h2>

        <!-- 搜尋欄 -->
        <div class="flex gap-3">
          <div class="flex-1">
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜尋看板名稱..."
              class="w-full rounded-lg border border-gray-200 px-4 py-3 text-sm transition-all duration-200 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:border-[#3397cf] focus:ring-[#3397cf]/20"
              @keyup.enter="handleSearch"
            />
          </div>
          <button
            @click="handleSearch"
            class="rounded-lg bg-[#3397cf] px-6 py-3 text-sm font-semibold text-white shadow-sm transition-all duration-200 hover:bg-[#2b7fb3] hover:shadow-md active:scale-[0.98]"
          >
            搜尋
          </button>
          <button
            v-if="keyword"
            @click="handleClearSearch"
            class="rounded-lg border border-gray-300 bg-white px-6 py-3 text-sm font-semibold text-gray-700 transition-all duration-200 hover:bg-gray-50 active:scale-[0.98]"
          >
            清除
          </button>
        </div>
      </div>

      <!-- 錯誤訊息 -->
      <div
        v-if="errorMessage"
        class="mb-6 rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-red-700"
      >
        {{ errorMessage }}
      </div>

      <!-- 載入中 -->
      <div v-if="isLoading" class="flex justify-center py-12">
        <svg class="h-10 w-10 animate-spin text-[#3397cf]" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
      </div>

      <!-- 看板列表 -->
      <div v-else-if="boards.length > 0" class="space-y-4">
        <div
          v-for="board in boards"
          :key="board.boardId"
          class="group cursor-pointer rounded-2xl bg-white p-6 shadow-[0_2px_16px_rgba(0,0,0,0.08)] transition-all duration-200 hover:shadow-lg hover:-translate-y-1"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <h3 class="mb-2 text-xl font-bold text-gray-900 group-hover:text-[#3397cf]">
                {{ board.name }}
              </h3>
              <p class="text-sm text-gray-600">{{ board.description }}</p>
            </div>
            <div class="ml-4 text-gray-400 group-hover:text-[#3397cf]">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </div>
        </div>

        <!-- 分頁控制 -->
        <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 pt-4">
          <button
            @click="goToPage(currentPage - 1)"
            :disabled="currentPage === 1"
            class="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-all hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            上一頁
          </button>

          <div class="flex gap-1">
            <button
              v-for="page in displayPages"
              :key="page"
              @click="goToPage(page)"
              :class="[
                'min-w-[40px] rounded-lg px-4 py-2 text-sm font-medium transition-all',
                page === currentPage
                  ? 'bg-[#3397cf] text-white'
                  : 'border border-gray-300 bg-white text-gray-700 hover:bg-gray-50'
              ]"
            >
              {{ page }}
            </button>
          </div>

          <button
            @click="goToPage(currentPage + 1)"
            :disabled="currentPage === totalPages"
            class="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-all hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            下一頁
          </button>
        </div>

        <!-- 分頁資訊 -->
        <div class="text-center text-sm text-gray-600">
          共 {{ total }} 個看板，第 {{ currentPage }} / {{ totalPages }} 頁
        </div>
      </div>

      <!-- 無資料 -->
      <div v-else class="rounded-2xl bg-white p-12 text-center shadow-[0_2px_16px_rgba(0,0,0,0.08)]">
        <div class="mb-4 text-6xl">📋</div>
        <h3 class="mb-2 text-xl font-bold text-gray-900">找不到看板</h3>
        <p class="text-sm text-gray-600">
          {{ keyword ? '沒有符合搜尋條件的看板，請嘗試其他關鍵字' : '目前沒有任何看板' }}
        </p>
      </div>
    </main>

    <!-- 頁尾 -->
    <footer class="bg-gray-800 py-6 text-center">
      <p class="text-sm text-gray-300">&copy; 2025 Dcard Lite. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { listBoards } from '@/api/board.js'

const router = useRouter()
const userStore = useUserStore()

// State
const boards = ref([])
const isLoading = ref(false)
const errorMessage = ref('')
const searchKeyword = ref('')
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// Computed
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const displayPages = computed(() => {
  const pages = []
  const maxDisplay = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxDisplay / 2))
  let end = Math.min(totalPages.value, start + maxDisplay - 1)

  // 調整起始頁，確保顯示足夠的頁碼
  if (end - start + 1 < maxDisplay) {
    start = Math.max(1, end - maxDisplay + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

// Methods
const fetchBoards = async () => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const data = await listBoards({
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: keyword.value
    })

    boards.value = data.items
    total.value = data.total
    currentPage.value = data.page
  } catch (error) {
    if (error.response) {
      const { status, data } = error.response

      if (status === 400) {
        if (data.code === 'PAGE_INVALID') {
          errorMessage.value = '頁碼格式不正確'
        } else if (data.code === 'PAGE_SIZE_INVALID') {
          errorMessage.value = '每頁筆數格式不正確'
        } else if (data.code === 'KEYWORD_INVALID') {
          errorMessage.value = '關鍵字長度不正確（限制 1-50 字元）'
        } else {
          errorMessage.value = '查詢參數錯誤，請檢查後重試'
        }
      } else {
        errorMessage.value = '載入失敗，請稍後再試'
      }
    } else {
      errorMessage.value = '網路錯誤，請檢查您的網路連線'
    }
  } finally {
    isLoading.value = false
  }
}

const handleSearch = () => {
  keyword.value = searchKeyword.value.trim()
  currentPage.value = 1
  fetchBoards()
}

const handleClearSearch = () => {
  searchKeyword.value = ''
  keyword.value = ''
  currentPage.value = 1
  fetchBoards()
}

const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchBoards()
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// Lifecycle
onMounted(() => {
  fetchBoards()
})
</script>

<style scoped>
/* 可選：如果需要額外的樣式 */
</style>
