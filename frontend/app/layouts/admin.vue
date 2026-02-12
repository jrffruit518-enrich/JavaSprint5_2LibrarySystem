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
        <slot v-if="isVerified" />
        <div
          v-else
          class="flex flex-col items-center justify-center h-full"
        >
          <UIcon
            name="i-heroicons-arrow-path"
            class="animate-spin text-4xl mb-2"
          />
          <p>Authenticating...</p>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 图书馆项目 - Admin Layout
 * Core Fix: Standardized to 'user_role' and 'auth_token'.
 */

const isVerified = ref(false)

const handleLogout = () => {
  // English Comment: Clear the correct standardized cookies
  useCookie('user_role').value = null
  useCookie('auth_token').value = null
  navigateTo('/')
}

onMounted(() => {
  // English Comment: Critical - Changed 'user-role' to 'user_role'
  const role = useCookie('user_role').value

  console.log('--- Jules Debug: Admin Layout Verification ---')
  console.log('Role found in cookie:', role)

  if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
    isVerified.value = true
  } else {
    console.error('Unauthorized access to admin layout. Role:', role)
    // Clear potentially corrupt data and redirect
    handleLogout()
  }
})
</script>
