明白你的意思了。既然 Admin 本身就是 User 的一种角色，确实不需要单独的 “Admin Management”。

这是优化后的侧边栏布局，删除了重复的入口，并保持了整体逻辑的简洁。

📄 layouts/admin.vue
代码段
<template>
  <div class="min-h-screen flex flex-col">
    <header class="h-16 border-b border-default flex items-center justify-between px-6 bg-background sticky top-0 z-10">
      <div class="flex items-center gap-2">
        <UIcon
          name="i-lucide-shield-check"
          class="w-6 h-6 text-primary"
        />
        <span class="font-bold text-lg text-highlighted">Library Admin System</span>
      </div>

      <div class="flex items-center gap-4">
        <UAvatar
          src="https://avatars.githubusercontent.com/u/1?v=4"
          size="sm"
        />
      </div>
    </header>

    <div class="flex flex-1">
      <aside class="w-64 border-r border-default bg-elevated hidden md:flex flex-col">
        <nav class="p-4 space-y-2 flex-1">
          <UButton
            to="/admin"
            icon="i-lucide-layout-dashboard"
            label="Dashboard"
            variant="ghost"
            block
            class="justify-start"
            active-class="bg-primary/10 text-primary font-semibold"
          />

          <USeparator class="my-2" />

          <UButton
            to="/admin/books"
            icon="i-lucide-book-copy"
            label="Book Management"
            variant="ghost"
            block
            class="justify-start"
            active-class="bg-primary/10 text-primary font-semibold"
          />

          <UButton
            to="/admin/users"
            icon="i-lucide-users"
            label="User Management"
            variant="ghost"
            block
            class="justify-start"
            active-class="bg-primary/10 text-primary font-semibold"
          />
        </nav>

        <div class="p-4 border-t border-default">
          <UButton
            label="Logout"
            color="error"
            variant="ghost"
            block
            icon="i-lucide-log-out"
            class="justify-start"
            @click="handleLogout"
          />
        </div>
      </aside>

      <main class="flex-1 p-6">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 图书馆项目 (Library Project) - Admin Layout
 * Simplified navigation to focus on core management modules.
 */

const handleLogout = () => {
  // English Comment: Clear all auth-related cookies to ensure clean state
  const roleCookie = useCookie('user-role')
  const tokenCookie = useCookie('auth-token')

  roleCookie.value = null
  tokenCookie.value = null

  console.log('User logged out, cookies cleared.')
  // English Comment: Redirect to login page after logout
  navigateTo('/')
}
</script>
