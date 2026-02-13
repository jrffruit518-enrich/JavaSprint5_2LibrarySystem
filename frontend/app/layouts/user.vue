<template>
  <div class="h-screen flex overflow-hidden bg-gray-50">
    
    <aside class="w-64 bg-gray-50 border-r border-gray-200 hidden md:flex flex-col shrink-0">
      <div class="p-6 shrink-0">
        <h2 class="text-xl font-bold text-green-600">
          Library Project
        </h2>
        <p class="text-xs text-gray-400">
          User Portal
        </p>
      </div>

      <nav class="flex-1 px-4 space-y-2 overflow-y-auto">
        <UButton
          to="/user"
          variant="ghost"
          icon="i-heroicons-squares-2x2"
          block
          class="justify-start"
        >
          Dashboard
        </UButton>

        <UButton
          to="/user/books"
          variant="ghost"
          icon="i-heroicons-book-open"
          block
          class="justify-start"
        >
          Book Catalog
        </UButton>

        <UButton
          to="/user/loans"
          variant="ghost"
          icon="i-heroicons-building-library"
          block
          class="justify-start"
        >
          My Loans
        </UButton>

        <UButton
          to="/user/profile"
          variant="ghost"
          icon="i-heroicons-user"
          block
          class="justify-start"
        >
          My Profile
        </UButton>
      </nav>

      <div class="p-4 border-t border-gray-200 bg-gray-50 shrink-0">
        <UButton
          variant="ghost"
          icon="i-heroicons-arrow-left-on-rectangle"
          color="red"
          label="Logout"
          block
          class="justify-start"
          @click="handleLogout"
        />
      </div>
    </aside>

    <main class="flex-1 overflow-y-auto bg-white relative">
      <UContainer class="py-8">
        <slot />
      </UContainer>
    </main>
  </div>
</template>

<script setup lang="ts">
/**
 * User Layout (Jules v2.7 Fixed Sidebar Version)
 * 核心改动：采用 Flex-layout 锁定侧边栏，实现主内容区局部滚动。
 */

const handleLogout = () => {
  // 后台逻辑：清理 Cookie
  const token = useCookie('auth_token')
  const userData = useCookie('user-data')
  const role = useCookie('user_role')
  
  token.value = null
  userData.value = null
  role.value = null
  
  // 逻辑：跳转回首页
  navigateTo('/')
}
</script>

<style scoped>
/* 确保激活状态的按钮有明显视觉反馈 */
.router-link-active {
  @apply bg-primary-50 text-primary-700 font-semibold;
}
</style>
