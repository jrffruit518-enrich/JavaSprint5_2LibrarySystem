<template>
  <div class="flex items-center gap-4 mb-6">
    <UButton
      v-if="!isIndexPage"
      icon="i-lucide-arrow-left"
      variant="ghost"
      color="neutral"
      @click="router.back()"
    />

    <nav class="flex items-center gap-2 text-sm text-muted">
      <ULink
        :to="rootPath"
        class="hover:text-primary flex items-center gap-1"
      >
        <UIcon
          name="i-lucide-home"
          class="w-4 h-4"
        />
        Home
      </ULink>
      <UIcon
        name="i-lucide-chevron-right"
        class="w-3 h-3"
      />
      <span class="text-highlighted font-medium">{{ currentPageTitle }}</span>
    </nav>
  </div>
</template>

<script setup>
const props = defineProps(['currentPageTitle'])
const route = useRoute()
const router = useRouter()

// 判断是否是角色的首页 (如果是 /user 或 /admin 则隐藏返回按钮)
const isIndexPage = computed(() => {
  return route.path === '/user' || route.path === '/admin'
})

// 根据路径判断根节点是 User 还是 Admin
const rootPath = computed(() => {
  return route.path.startsWith('/admin') ? '/admin' : '/user'
})
</script>
