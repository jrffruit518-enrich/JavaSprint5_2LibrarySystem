<template>
  <div class="space-y-10 animate-spring-in">
    <div class="relative">
      <h1 class="text-3xl font-black text-slate-900 dark:text-white uppercase tracking-tighter">
        Hello, <span class="text-emerald-600 dark:text-emerald-400">Reader!</span>
      </h1>
      <p class="text-sm font-bold text-slate-600 dark:text-slate-300 mt-1 uppercase tracking-widest">
        Welcome back to your digital reading sanctuary.
      </p>
    </div>

    <ClientOnly>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
        
        <UCard 
          class="glass-effect border-none shadow-xl hover:scale-105 transition-transform duration-300 relative overflow-hidden group"
          :ui="{ body: { base: 'relative z-10' } }"
        >
          <div class="absolute -right-4 -top-4 w-24 h-24 bg-emerald-500/15 rounded-full blur-2xl group-hover:bg-emerald-500/25 transition-colors"></div>
          <div class="text-center py-6">
            <UIcon name="i-heroicons-book-open-solid" class="w-12 h-12 text-emerald-600 dark:text-emerald-400 mx-auto mb-4" />
            <p class="text-xs font-black text-slate-800 dark:text-slate-200 uppercase tracking-[0.2em] mb-3">Books Borrowed</p>
            <p class="text-6xl font-black text-slate-900 dark:text-white tracking-tighter">
              {{ status === 'pending' ? '...' : stats.borrowed }}
            </p>
          </div>
        </UCard>

        <UCard 
          class="glass-effect border-none shadow-xl hover:scale-105 transition-transform duration-300 relative overflow-hidden group"
          :ui="{ body: { base: 'relative z-10' } }"
        >
          <div class="absolute -right-4 -top-4 w-24 h-24 bg-blue-500/15 rounded-full blur-2xl group-hover:bg-blue-500/25 transition-colors"></div>
          <div class="text-center py-6">
            <UIcon name="i-heroicons-calendar-days-solid" class="w-12 h-12 text-blue-600 dark:text-blue-400 mx-auto mb-4" />
            <p class="text-xs font-black text-slate-800 dark:text-slate-200 uppercase tracking-[0.2em] mb-3">Active Reservations</p>
            <p class="text-6xl font-black text-slate-900 dark:text-white tracking-tighter">0</p>
          </div>
        </UCard>

        <UCard 
          class="glass-effect border-none shadow-xl hover:scale-105 transition-transform duration-300 relative overflow-hidden group"
          :ui="{ body: { base: 'relative z-10' } }"
        >
          <div class="absolute -right-4 -top-4 w-24 h-24 bg-rose-500/15 rounded-full blur-2xl group-hover:bg-rose-500/25 transition-colors"></div>
          <div class="text-center py-6">
            <UIcon name="i-heroicons-exclamation-circle-solid" class="w-12 h-12 text-rose-600 dark:text-rose-400 mx-auto mb-4" />
            <p class="text-xs font-black uppercase tracking-[0.2em] mb-3 text-rose-700 dark:text-rose-300">Overdue Notices</p>
            <p class="text-6xl font-black text-rose-600 dark:text-rose-500 tracking-tighter">
              {{ status === 'pending' ? '...' : stats.overdue }}
            </p>
          </div>
        </UCard>
      </div>

      <div class="mt-12 bg-white/60 dark:bg-white/10 p-10 rounded-3xl border-2 border-white/20 shadow-inner">
        <h3 class="text-sm font-black uppercase tracking-[0.3em] mb-8 text-slate-800 dark:text-slate-200 flex items-center gap-3">
          <div class="w-2 h-2 bg-emerald-500 rounded-full animate-ping"></div>
          Terminal Operations
        </h3>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <UButton 
            to="/user/books" 
            icon="i-heroicons-magnifying-glass-solid" 
            color="emerald" 
            class="btn-glow font-black py-4 rounded-2xl active:scale-95 transition-all text-base flex justify-center"
            size="xl"
            block
          >
            Explore Library
          </UButton>
          
          <UButton 
            to="/user/loans" 
            icon="i-heroicons-bookmark-solid" 
            variant="ghost"
            class="font-black py-4 rounded-2xl border-2 border-slate-300 dark:border-white/20 hover:bg-white/30 text-base transition-all text-slate-800 dark:text-slate-100 flex justify-center"
            size="xl"
            block
          >
            Manage My Loans
          </UButton>

          <UButton 
            to="/user/profile" 
            icon="i-heroicons-user-circle-solid" 
            variant="soft"
            color="indigo"
            class="font-black py-4 rounded-2xl border-2 border-transparent hover:border-indigo-500/50 text-base transition-all flex justify-center"
            size="xl"
            block
          >
            My Profile
          </UButton>
        </div>
      </div>

      <template #fallback>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
          <div v-for="i in 3" :key="i" class="h-56 glass-effect border-none rounded-2xl animate-pulse"></div>
        </div>
      </template>
    </ClientOnly>
  </div>
</template>

<script setup lang="ts">
/**
 * User Dashboard Index (Jules v4.9.2 - Uniform Layout)
 * 1. Fixed button width inconsistency using Grid.
 * 2. Maintained visual restore + Hydration guard.
 */
definePageMeta({
  layout: 'user',
  middleware: 'auth'
})

const { data: loans, status } = await useApi<any[]>('borrowings/user/loans')

const stats = computed(() => {
  const list = Array.isArray(loans.value) ? loans.value : []
  const activeList = list.filter(l => l && String(l.status).toUpperCase() === 'BORROWED')
  
  const overdueCount = activeList.filter(l => {
    if (!l.borrowDate) return false
    const bDate = new Date(l.borrowDate)
    const diffDays = (Date.now() - bDate.getTime()) / (1000 * 60 * 60 * 24)
    return diffDays > 30
  }).length

  return { borrowed: activeList.length, overdue: overdueCount }
})
</script>

<style scoped>
.animate-spring-in {
  animation: spring-in 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes spring-in {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>