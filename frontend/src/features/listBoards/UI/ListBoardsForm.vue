<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useListBoards } from '../model/useListBoards';
import { useBoardStore } from '@/entities/board/model/board.store';
import BoardCard from '@/entities/board/UI/BoardCard.vue';

const { loadBoards, isLoading, error, clearError, searchBoards, changePage, currentPage, total, pageSize } = useListBoards();
const boardStore = useBoardStore();
const { boardList } = storeToRefs(boardStore);

const totalPages = computed(() => Math.ceil(total.value / pageSize.value));
const searchKeyword = ref('');

onMounted(() => {
  loadBoards();
});

const handleSearch = () => {
  searchBoards(searchKeyword.value);
};

const handleClearSearch = () => {
  searchKeyword.value = '';
  searchBoards('');
};

const handlePrevPage = () => {
  if (currentPage.value > 1) {
    changePage(currentPage.value - 1);
  }
};

const handleNextPage = () => {
  if (currentPage.value < totalPages.value) {
    changePage(currentPage.value + 1);
  }
};
</script>

<template>
  <div>
    <!-- 標題與搜尋列 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-800 mb-6">看板列表</h1>

      <!-- 搜尋框 -->
      <div class="flex gap-3">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜尋看板名稱..."
          class="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#3397cf] focus:border-[#3397cf] transition-all duration-200"
          @keyup.enter="handleSearch"
        />
        <button
          @click="handleSearch"
          :disabled="isLoading"
          class="px-6 py-3 bg-[#3397cf] text-white rounded-lg hover:bg-opacity-90 active:scale-95 transition-all duration-200 font-medium shadow-md disabled:bg-gray-400 disabled:cursor-not-allowed"
        >
          搜尋
        </button>
        <button
          v-if="searchKeyword"
          @click="handleClearSearch"
          :disabled="isLoading"
          class="px-6 py-3 bg-gray-200 text-gray-600 rounded-lg hover:bg-gray-300 active:scale-95 transition-all duration-200 font-medium disabled:opacity-50 disabled:cursor-not-allowed"
        >
          清除
        </button>
      </div>
    </div>

    <!-- 錯誤訊息 -->
    <div
      v-if="error"
      class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start justify-between"
    >
      <div class="flex items-start">
        <svg class="w-5 h-5 text-red-500 mt-0.5 mr-2" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
        </svg>
        <span class="text-red-700">{{ error }}</span>
      </div>
      <button @click="clearError" class="text-red-500 hover:text-red-700">
        <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
        </svg>
      </button>
    </div>

    <!-- Loading 狀態 -->
    <div v-if="isLoading" class="flex justify-center items-center py-20">
      <div class="animate-spin rounded-full h-16 w-16 border-b-4 border-[#3397cf]"></div>
    </div>

    <!-- 空狀態 -->
    <div
      v-else-if="boardList.length === 0"
      class="text-center py-20 bg-white rounded-2xl shadow-lg"
    >
      <svg class="mx-auto h-16 w-16 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
      <h3 class="mt-4 text-lg font-medium text-gray-800">沒有找到看板</h3>
      <p class="mt-2 text-sm text-gray-600">
        {{ searchKeyword ? '請嘗試其他搜尋關鍵字' : '目前沒有任何看板' }}
      </p>
    </div>

    <!-- 看板列表 -->
    <div
      v-else
      class="grid grid-cols-3 md:grid-cols-2 lg:grid-cols-1 gap-6"
    >
      <BoardCard
        v-for="board in boardList"
        :key="board.boardId"
        :board="board"
      />
    </div>

    <!-- 分頁控制 -->
    <div
      v-if="totalPages > 1 && !isLoading"
      class="mt-10 flex items-center justify-center gap-4"
    >
      <button
        @click="handlePrevPage"
        :disabled="currentPage === 1"
        class="px-5 py-2 bg-white border border-gray-300 rounded-lg text-gray-600 hover:bg-gray-50 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 shadow-sm font-medium"
      >
        上一頁
      </button>

      <span class="text-gray-800 font-medium">
        第 {{ currentPage }} / {{ totalPages }} 頁 (共 {{ total }} 個看板)
      </span>

      <button
        @click="handleNextPage"
        :disabled="currentPage === totalPages"
        class="px-5 py-2 bg-white border border-gray-300 rounded-lg text-gray-600 hover:bg-gray-50 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 shadow-sm font-medium"
      >
        下一頁
      </button>
    </div>
  </div>
</template>

<style scoped>
</style>