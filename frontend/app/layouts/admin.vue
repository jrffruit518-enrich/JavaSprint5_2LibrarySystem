<template>
  <div class="h-screen flex flex-col overflow-hidden bg-gray-50">
    <header class="h-16 border-b border-gray-200 flex items-center justify-between px-6 bg-white shrink-0 z-20">
      <div class="flex items-center gap-2">
        <UIcon
          name="i-heroicons-shield-check"
          class="w-6 h-6 text-primary-600"
        />
        <span class="font-bold text-lg text-gray-800">Library Admin System</span>
      </div>
      <div class="flex items-center gap-4">
        <UAvatar
          src="https://avatars.githubusercontent.com/u/1?v=4"
          size="sm"
        />
      </div>
    </header>

    <div class="flex flex-1 overflow-hidden">
      <aside class="w-64 border-r border-gray-200 bg-gray-50 hidden md:flex flex-col shrink-0">
        <nav class="p-4 space-y-2 flex-1 overflow-y-auto">
          <UButton
            to="/admin"
            icon="i-heroicons-squares-2x2"
            label="Dashboard"
            variant="ghost"
            block
            class="justify-start"
          />
          
          <UDivider class="my-2" />

          <div class="px-2 pb-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">
            Resources
          </div>
          <UButton
            to="/admin/books"
            icon="i-heroicons-book-open"
            label="Book Management"
            variant="ghost"
            block
            class="justify-start"
          />
          <UButton
            to="/admin/users"
            icon="i-heroicons-users"
            label="User Management"
            variant="ghost"
            block
            class="justify-start"
          />

          <UDivider class="my-2" />

          <div class="px-2 pb-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">
            History
          </div>
          <UButton
            to="/admin/logs"
            icon="i-heroicons-clipboard-document-list"
            label="Borrowing Logs"
            variant="ghost"
            block
            class="justify-start"
          />
        </nav>
        
        <div class="p-4 border-t border-gray-200 bg-gray-50">
          <UButton
            label="Logout"
            color="red"
            variant="ghost"
            block
            icon="i-heroicons-arrow-left-on-rectangle"
            class="justify-start"
            @click="handleLogout"
          />
        </div>
      </aside>

      <main class="flex-1 overflow-y-auto bg-white relative">
        <div class="p-6">
          <ClientOnly>
            <div v-if="isVerified" :key="$route.fullPath">
              <slot />
            </div>
            
            <div
              v-else
              class="flex flex-col items-center justify-center h-[calc(100vh-100px)]"
            >
              <UIcon
                name="i-heroicons-arrow-path"
                class="animate-spin text-4xl mb-2 text-primary-500"
              />
              <p class="text-gray-500">Authenticating...</p>
            </div>
          </ClientOnly>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * Admin Layout (Jules v2.9 - Error Hardening)
 * 增加了 ClientOnly 和 :key="$route.fullPath" 以解决 parentNode null 报错
 */

const isVerified = ref(false)

const handleLogout = () => {
  const role = useCookie('user_role')
  const token = useCookie('auth_token')
  const userId = useCookie('user_id')
  
  role.value = null
  token.value = null
  userId.value = null
  
  navigateTo('/')
}

onMounted(() => {
  const role = useCookie('user_role').value

  console.log('--- [JULES DEBUG] Admin Layout Check ---')
  console.log('Current Role:', role)

  if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
    isVerified.value = true
  } else {
    console.error('Access denied. Redirecting to home...')
    handleLogout()
  }
})
</script>

<style scoped>
.router-link-active {
  @apply bg-primary-50 text-primary-700 font-semibold shadow-sm;
}
</style>
