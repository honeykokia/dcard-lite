<script setup lang="ts">
import { useRouter } from 'vue-router';
import type { Board } from '../model/board.types';

// Props
interface Props {
  board: Board;
}

const props = defineProps<Props>();
const router = useRouter();

/**
 * 處理進入看板
 */
const handleEnterBoard = () => {
  // 導航到看板詳情頁面
  router.push(`/boards/${props.board.boardId}`);
};
</script>

<template>
  <div
    @click="handleEnterBoard"
    class="bg-white rounded-2xl shadow-lg hover:shadow-xl transition-all duration-200 p-8 border border-gray-100 cursor-pointer"
  >
    <div class="flex flex-col h-full">
      <!-- 看板名稱 -->
      <h3 class="text-xl font-semibold text-gray-800 mb-3">
        {{ board.name }}
      </h3>

      <!-- 看板描述 -->
      <p class="text-sm text-gray-600 line-clamp-3">
        {{ board.description }}
      </p>
    </div>
  </div>
</template>

<style scoped>
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>