<template>
  <div class="min-h-screen font-sans selection:bg-emerald-500/30">
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>

    <UNotifications />
    <UModals />
  </div>
</template>

<script setup lang="ts">
/**
 * Global App Entry (Jules v4.1 - Clean Version)
 * Removed the monitor button, preserved all layout & SEO configs.
 */
useSeoMeta({
  title: 'CloudLibrary | System Core',
  description: 'A modern, glassmorphism-inspired library management experience.'
})
</script>

<style>
/* 1. 页面切换平滑动画 - 必须保留，否则跳转会生硬 */
.page-enter-active,
.page-leave-active {
  transition: all 0.3s ease-in-out;
}
.page-enter-from,
.page-leave-to {
  opacity: 0;
  filter: blur(10px);
  transform: translateY(5px);
}

/* 2. 全局滚动条美化 - 对齐全站 Emerald 风格 */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  @apply bg-transparent;
}
::-webkit-scrollbar-thumb {
  @apply bg-emerald-500/20 rounded-full hover:bg-emerald-500/40 transition-colors;
}

/* 3. 玻璃质感全局定义 - 确保 user/admin 页面效果不崩 */
.glass-effect {
  @apply bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-white/20 dark:border-white/5;
}

/* 4. 动画类定义 */
@keyframes spring-in {
  0% { transform: scale(0.95); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
.animate-spring-in {
  animation: spring-in 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}
</style>
