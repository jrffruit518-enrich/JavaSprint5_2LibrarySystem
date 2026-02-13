<template>
  <div class="flex items-center gap-4 mb-6">
    <UButton
      v-if="!isIndexPage"
      icon="i-heroicons-arrow-left"
      variant="ghost"
      color="gray"
      @click="router.back()"
    />

    <nav class="flex items-center gap-2 text-sm text-gray-500">
      <ULink
        :to="rootPath"
        class="hover:text-primary-600 flex items-center gap-1 transition-colors"
      >
        <UIcon
          name="i-heroicons-home"
          class="w-4 h-4"
        />
        Home
      </ULink>

      <UIcon
        name="i-heroicons-chevron-right"
        class="w-3 h-3 text-gray-400"
      />

      <span class="text-gray-900 font-semibold">{{ currentPageTitle }}</span>
    </nav>
  </div>
</template>

<script setup lang="ts">
/**
 * Breadcrumb Component (Jules v2 Stability Version)
 * Comments: Chinese Logic | UI: English
 */

const props = defineProps<{
  currentPageTitle: string
}>()

const route = useRoute()
const router = useRouter()

// 逻辑：判断是否是角色的首页 (如果是 /user 或 /admin 则隐藏返回按钮)
const isIndexPage = computed(() => {
  const path = route.path.replace(/\/$/, '') // 去掉末尾斜杠
  return path === '/user' || path === '/admin'
})

// 逻辑：根据当前路径判断根节点是 User Portal 还是 Admin System
const rootPath = computed(() => {
  return route.path.startsWith('/admin') ? '/admin' : '/user'
})
</script>
