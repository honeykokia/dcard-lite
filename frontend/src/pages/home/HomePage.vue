<script setup lang="ts">
import NavBar from "@/widgets/NavBar.vue";
import ListBoardsForm from "@/features/listBoards/UI/ListBoardsForm.vue";
</script>

<template>
  <div class="min-h-screen bg-[#f2f3f5]">
    <!-- NavBar -->
    <header>
      <NavBar />
    </header>

    <!-- 主要內容區域 - 置中容器 -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex gap-6">
        <!-- Sidebar -->
        <aside class="hidden lg:block">
           <!--<Sidebar />-->
        </aside>

        <!-- Main Content -->
        <main class="flex-1 min-w-0">
          <ListBoardsForm />
        </main>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>