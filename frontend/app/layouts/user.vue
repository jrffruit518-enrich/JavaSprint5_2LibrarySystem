<template>
  <div 
    v-bind="$attrs"
    class="h-screen flex overflow-hidden bg-user-reading transition-colors duration-300 relative text-slate-900 dark:text-slate-100"
  >
    <aside class="w-64 bg-slate-900/40 dark:bg-slate-950/60 backdrop-blur-3xl border-r border-white/10 hidden md:flex flex-col shrink-0 relative z-20 shadow-2xl">
      <div class="p-8 shrink-0">
        <div class="flex items-center gap-2 mb-1">
          <UIcon name="i-heroicons-book-open" class="text-2xl text-emerald-500 animate-pulse" />
          <h2 class="text-xl font-black bg-clip-text text-transparent bg-gradient-to-r from-emerald-400 to-green-500">
            Jules Lib
          </h2>
        </div>
        <p class="text-[10px] uppercase tracking-[0.3em] text-emerald-400/80 font-black px-1">
          Reader Edition
        </p>
      </div>

      <nav class="flex-1 px-4 space-y-1.5 overflow-y-auto">
        <UButton to="/user" variant="ghost" icon="i-heroicons-squares-2x2" block class="nav-item text-slate-200">
          Dashboard
        </UButton>
        <UButton to="/user/books" variant="ghost" icon="i-heroicons-magnifying-glass" block class="nav-item text-slate-200">
          Book Catalog
        </UButton>
        <UButton to="/user/loans" variant="ghost" icon="i-heroicons-clock" block class="nav-item text-slate-200">
          My Loans
        </UButton>
        <UButton to="/user/profile" variant="ghost" icon="i-heroicons-user-circle" block class="nav-item text-slate-200">
          My Profile
        </UButton>
      </nav>

      <div class="px-4 mb-6">
        <div class="bg-white/10 dark:bg-emerald-500/5 backdrop-blur-md rounded-2xl p-4 border border-white/20 flex items-center gap-3 shadow-xl">
          <UAvatar src="https://api.dicebear.com/7.x/avataaars/svg?seed=Jules" size="sm" class="ring-2 ring-emerald-500/50" />
          <div class="overflow-hidden">
            <p class="text-xs font-black truncate text-white uppercase tracking-tight">Reader</p>
            <p class="text-[10px] text-emerald-400 truncate font-mono font-bold italic">ID: #0822-ACTIVE</p>
          </div>
        </div>
      </div>

      <div class="p-4 border-t border-white/5 shrink-0">
        <UButton
          variant="ghost" icon="i-heroicons-power" color="rose" label="Exit Portal"
          block class="justify-start hover:bg-rose-500/20 transition-all font-bold text-rose-400"
          @click="handleLogout"
        />
      </div>
    </aside>

    <main class="flex-1 overflow-y-auto relative z-10 animate-spring-in">
      <div class="max-w-7xl mx-auto p-4 md:p-8">
        <slot />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
/**
 * User Layout (Jules v4.0 - Total Transparency Edition)
 * Fixed: Removed internal background colors for full glassmorphism effect.
 */
defineOptions({
  inheritAttrs: false
})

const handleLogout = () => {
  const token = useCookie('auth_token')
  const userData = useCookie('user-data')
  const role = useCookie('user_role')
  token.value = null
  userData.value = null
  role.value = null
  navigateTo('/')
}
</script>

<style scoped>
.nav-item {
  @apply justify-start hover:text-white hover:bg-white/10 transition-all duration-300 active:scale-95 font-bold;
}

.router-link-active {
  @apply bg-emerald-500/20 text-emerald-400 font-black border-l-2 border-emerald-500 shadow-[0_0_15px_rgba(16,185,129,0.2)] !important;
}

/* Jules Clean-up: 
   Internal scrollbar CSS removed. 
   Now utilizing the global main.css configuration. 
*/
</style>
