<template>
  <div class="flex items-center gap-4 mb-8 animate-spring-in">
    <UButton
      v-if="!isIndexPage"
      icon="i-heroicons-arrow-left-20-solid"
      variant="ghost"
      color="gray"
      class="rounded-full hover:bg-emerald-500/10 hover:text-emerald-600 dark:hover:text-emerald-400 transition-all active:scale-90"
      @click="router.back()"
    />

    <nav class="flex items-center gap-3 text-xs tracking-widest uppercase font-black">
      <ULink
        :to="rootPath"
        class="flex items-center gap-1.5 text-slate-400 hover:text-emerald-600 dark:hover:text-emerald-400 transition-colors duration-300"
      >
        <UIcon
          name="i-heroicons-home-20-solid"
          class="w-4 h-4"
        />
        <span class="hidden sm:inline">Portal</span>
      </ULink>

      <UIcon
        name="i-heroicons-chevron-right-20-solid"
        class="w-3 h-3 text-slate-300 dark:text-slate-600"
      />

      <span class="text-slate-900 dark:text-white border-b-2 border-emerald-500/50 pb-0.5">
        {{ currentPageTitle }}
      </span>
    </nav>
  </div>
</template>

<script setup lang="ts">
/**
 * AppBreadcrumb (Jules v4.0 - Glass Navigation)
 * Refactor: Aligned with the immersive library aesthetic.
 */

const props = defineProps<{
  currentPageTitle: string
}>()

const route = useRoute()
const router = useRouter()

// 逻辑：判断是否是角色的首页 (如果是 /user 或 /admin 则隐藏返回按钮)
const isIndexPage = computed(() => {
  const path = route.path.replace(/\/$/, '')
  return path === '/user' || path === '/admin'
})

// 逻辑：动态确定根节点名称
const rootPath = computed(() => {
  return route.path.startsWith('/admin') ? '/admin' : '/user'
})
</script>

<style scoped>
/* 简单的入场动画，增加质感 */
.animate-spring-in {
  animation: spring-in 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes spring-in {
  from { opacity: 0; transform: translateX(-10px); }
  to { opacity: 1; transform: translateX(0); }
}
</style>
