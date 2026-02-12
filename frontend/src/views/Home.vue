<template>
  <div class="flex min-h-screen flex-col bg-[#f2f3f5]">
    <!-- 導航欄 -->
    <header class="bg-[#3397cf] shadow-sm">
      <div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6">
        <div class="flex items-center">
          <h1 class="text-2xl font-bold text-white">Dcard Lite</h1>
        </div>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2 text-sm text-white">
            <span v-if="userRole === 'ADMIN'" class="rounded bg-orange-500 px-2 py-1 text-xs font-bold">
              管理員
            </span>
            <span class="font-medium">{{ displayName }}</span>
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
      <!-- 歡迎區塊 -->
      <div class="mb-6 rounded-2xl bg-white p-8 shadow-[0_2px_16px_rgba(0,0,0,0.08)]">
        <h2 class="mb-2 text-3xl font-bold text-gray-900">歡迎回來！</h2>
        <p class="mb-6 text-lg text-gray-600">您好，{{ displayName }}，歡迎使用 Dcard Lite</p>

        <div class="space-y-3 rounded-lg bg-gray-50 p-5">
          <div class="flex items-center text-base">
            <span class="w-32 font-medium text-gray-700">使用者 ID</span>
            <span class="text-gray-900">{{ userId }}</span>
          </div>
          <div class="flex items-center text-base">
            <span class="w-32 font-medium text-gray-700">顯示名稱</span>
            <span class="text-gray-900">{{ displayName }}</span>
          </div>
          <div class="flex items-center text-base">
            <span class="w-32 font-medium text-gray-700">角色</span>
            <span class="text-gray-900">{{ userRole }}</span>
          </div>
        </div>
      </div>

      <!-- 功能選單 -->
      <div class="rounded-2xl bg-white p-8 shadow-[0_2px_16px_rgba(0,0,0,0.08)]">
        <h3 class="mb-6 text-2xl font-bold text-gray-900">功能選單</h3>
        <div class="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          <!-- 看板列表 -->
          <div class="group rounded-xl p-6 transition-all hover:shadow-md hover:-translate-y-1">
            <div class="mb-4 text-4xl">📋</div>
            <h4 class="mb-2 text-xl font-bold text-gray-900">看板列表</h4>
            <p class="mb-4 text-sm text-gray-600">瀏覽所有討論看板</p>
            <button
              @click="router.push('/boards')"
              class="w-full rounded-lg bg-[#3397cf] py-2.5 text-sm font-semibold text-white transition-all duration-200 hover:bg-[#2b7fb3] hover:shadow-md active:scale-[0.98]"
            >
              前往看板
            </button>
          </div>

          <!-- 發表文章 -->
          <div class="group rounded-xl p-6 transition-all hover:shadow-md hover:-translate-y-1">
            <div class="mb-4 text-4xl">📝</div>
            <h4 class="mb-2 text-xl font-bold text-gray-900">發表文章</h4>
            <p class="mb-4 text-sm text-gray-600">在看板中發表新文章</p>
            <button
              class="w-full rounded-lg bg-gray-300 py-2.5 text-sm font-semibold text-gray-500 cursor-not-allowed"
              disabled
            >
              即將推出
            </button>
          </div>

          <!-- 我的留言 -->
          <div class="group rounded-xl p-6 transition-all hover:shadow-md hover:-translate-y-1">
            <div class="mb-4 text-4xl">💬</div>
            <h4 class="mb-2 text-xl font-bold text-gray-900">我的留言</h4>
            <p class="mb-4 text-sm text-gray-600">查看我的留言紀錄</p>
            <button
              class="w-full rounded-lg bg-gray-300 py-2.5 text-sm font-semibold text-gray-500 cursor-not-allowed"
              disabled
            >
              即將推出
            </button>
          </div>
        </div>
      </div>
    </main>

    <!-- 頁尾 -->
    <footer class="bg-gray-800 py-6 text-center">
      <p class="text-sm text-gray-300">&copy; 2025 Dcard Lite. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 使用 computed 從 store 獲取資料
const userId = computed(() => userStore.userId)
const displayName = computed(() => userStore.displayName || '訪客')
const userRole = computed(() => userStore.role || 'USER')

// 處理登出
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>

</style>
