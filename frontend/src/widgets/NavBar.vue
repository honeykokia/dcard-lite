<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '@/entities/auth/model/useAuth';

const router = useRouter();
const { isLoggedIn, user, logout } = useAuth();

const handleLogin = () => {
  router.push('/login');
};

const handleRegister = () => {
  router.push('/register');
};

const handleLogout = () => {
  logout();
};
</script>

<template>
  <nav class="bg-white shadow-lg border-b border-gray-200 sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between items-center h-16">
        <!-- Logo -->
        <div class="flex items-center">
          <router-link
            to="/"
            class="flex items-center hover:opacity-90 transition-opacity duration-200"
          >
            <span class="text-2xl font-bold text-[#3397cf]">Dcard Lite</span>
          </router-link>
        </div>

        <!-- 右側按鈕區域 -->
        <div class="flex items-center gap-4">
          <template v-if="isLoggedIn">
            <!-- 已登入狀態 -->
            <span class="text-gray-800 font-medium">{{ user?.displayName }}</span>
            <button
              @click="handleLogout"
              class="px-4 py-2 text-gray-600 hover:text-[#3397cf] transition-colors duration-200 font-medium"
            >
              登出
            </button>
          </template>

          <template v-else>
            <!-- 未登入狀態 -->
            <button
              @click="handleLogin"
              class="px-4 py-2 text-gray-600 hover:text-[#3397cf] transition-colors duration-200 font-medium"
            >
              登入
            </button>
            <button
              @click="handleRegister"
              class="px-6 py-2 bg-[#3397cf] text-white rounded-lg hover:bg-opacity-90 active:scale-95 transition-all duration-200 font-medium shadow-md"
            >
              註冊
            </button>
          </template>
        </div>
      </div>
    </div>
  </nav>
</template>

<style scoped>

</style>