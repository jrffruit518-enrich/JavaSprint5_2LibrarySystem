<template>
  <div 
    v-bind="$attrs"
    class="h-screen flex flex-col overflow-hidden bg-admin-grid transition-colors duration-300 relative text-slate-900 dark:text-slate-100"
  >
    <header class="h-16 border-b border-black/5 dark:border-white/5 flex items-center justify-between px-6 bg-white/20 dark:bg-slate-900/60 backdrop-blur-2xl shrink-0 z-30 shadow-lg">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 bg-indigo-600 rounded-lg flex items-center justify-center shadow-lg shadow-indigo-500/20 animate-pulse">
          <UIcon name="i-heroicons-shield-check" class="text-white w-6 h-6" />
        </div>
        <div class="flex flex-col">
          <span class="font-black text-sm tracking-wider uppercase">Library Control</span>
          <span class="text-[10px] text-indigo-600 dark:text-indigo-400 font-mono font-bold tracking-widest">ADMIN TERMINAL v4.0</span>
        </div>
      </div>
      
      <div class="flex items-center gap-4">
        <div class="hidden md:flex flex-col items-end mr-2">
          <span class="text-xs font-black uppercase tracking-tighter">System Root</span>
          <span class="text-[10px] text-emerald-600 dark:text-emerald-400 font-bold font-mono">NODE_ACTIVE: 01_SECURE</span>
        </div>
        <UAvatar
          src="https://api.dicebear.com/7.x/identicon/svg?seed=Admin"
          size="sm"
          class="ring-2 ring-indigo-500 shadow-indigo-500/50"
        />
      </div>
    </header>

    <div class="flex flex-1 overflow-hidden relative">
      <aside class="w-64 border-r border-black/5 dark:border-white/5 bg-white/10 dark:bg-slate-900/40 backdrop-blur-3xl hidden md:flex flex-col shrink-0 z-20">
        <nav class="p-4 space-y-1.5 flex-1 overflow-y-auto">
          <div class="px-3 py-2 text-[10px] font-black text-slate-500 uppercase tracking-[0.2em]">Management</div>
          <UButton
            to="/admin"
            icon="i-heroicons-presentation-chart-line"
            label="System Overview"
            variant="ghost"
            block
            class="admin-nav-item"
          />
          
          <div class="px-3 pt-6 pb-2 text-[10px] font-black text-slate-500 uppercase tracking-[0.2em]">Inventory</div>
          <UButton
            to="/admin/books"
            icon="i-heroicons-rectangle-stack"
            label="Book Inventory"
            variant="ghost"
            block
            class="admin-nav-item"
          />
          <UButton
            to="/admin/users"
            icon="i-heroicons-user-group"
            label="User Management"
            variant="ghost"
            block
            class="admin-nav-item"
          />

          <div class="px-3 pt-6 pb-2 text-[10px] font-black text-slate-500 uppercase tracking-[0.2em]">Security</div>
          <UButton
            to="/admin/logs"
            icon="i-heroicons-document-text"
            label="Access Logs"
            variant="ghost"
            block
            class="admin-nav-item"
          />
        </nav>
        
        <div class="p-4 border-t border-black/5 dark:border-white/5 bg-black/5 dark:bg-slate-950/20">
          <UButton
            label="Terminal Logout"
            color="rose"
            variant="soft"
            block
            icon="i-heroicons-power"
            class="font-black hover:bg-rose-500/20 transition-all active:scale-95"
            @click="handleLogout"
          />
        </div>
      </aside>

      <main class="flex-1 overflow-y-auto relative z-10 custom-scrollbar">
        <div class="p-6 lg:p-10 max-w-7xl mx-auto">
          <ClientOnly>
            <div v-if="isVerified" :key="$route.fullPath" class="animate-spring-in">
              <slot />
            </div>
            
            <div
              v-else
              class="flex flex-col items-center justify-center h-[60vh]"
            >
              <UIcon
                name="i-heroicons-finger-print"
                class="animate-pulse text-6xl mb-4 text-indigo-500"
              />
              <p class="text-indigo-600 dark:text-indigo-400 font-mono text-xs animate-pulse tracking-[0.3em]">SECURE_AUTH_REQUIRED...</p>
            </div>
          </ClientOnly>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * Admin Layout (Jules v4.0 - Light & Transparent)
 * Refactored for global grid and bright desk background.
 */
defineOptions({
  inheritAttrs: false
})

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
  if (['ADMIN', 'ROLE_ADMIN'].includes(role as string)) {
    isVerified.value = true
  } else {
    handleLogout()
  }
})
</script>

<style scoped>
.admin-nav-item {
  @apply justify-start text-slate-600 dark:text-slate-400 hover:text-indigo-600 dark:hover:text-white hover:bg-indigo-500/10 transition-all duration-300 font-bold border-l-2 border-transparent;
}

.router-link-active {
  @apply bg-indigo-500/20 text-indigo-600 dark:text-indigo-400 font-black border-l-2 border-indigo-500 shadow-[0_0_15px_rgba(99,102,241,0.2)] !important;
}

/* Local scrollbar cleanup: handled by main.css */
</style>
