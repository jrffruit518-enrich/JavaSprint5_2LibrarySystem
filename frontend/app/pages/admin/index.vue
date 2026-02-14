<template>
  <div class="space-y-8 animate-spring-in">
    <div class="bg-slate-900 text-white p-8 rounded-2xl shadow-2xl border border-slate-800 relative overflow-hidden">
      <div class="relative z-10 flex justify-between items-center">
        <div>
          <h1 class="text-3xl font-black uppercase tracking-tighter italic text-emerald-500">
            System Control <span class="text-white">Center</span>
          </h1>
          <p class="text-slate-400 font-bold uppercase text-[10px] tracking-[0.3em] mt-2">
            Infrastructure & Core Management
          </p>
        </div>
        <div class="hidden md:block text-right">
          <p class="text-[10px] font-black text-slate-500 uppercase tracking-widest">Node Status</p>
          <p class="text-emerald-500 font-mono font-bold text-sm">SYSTEM_READY</p>
        </div>
      </div>
      <UIcon name="i-heroicons-shield-check" class="absolute -right-8 -bottom-8 w-64 h-64 text-white/5 rotate-12" />
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      
      <UCard
        class="glass-effect group hover:ring-2 hover:ring-blue-500 transition-all duration-500 cursor-pointer overflow-hidden relative"
        :ui="{ body: { padding: 'p-8' } }"
        @click="navigateTo('/admin/users')"
      >
        <div class="flex flex-col items-center text-center space-y-4">
          <div class="p-4 rounded-2xl bg-blue-500/10 group-hover:scale-110 transition-transform duration-500">
            <UIcon name="i-heroicons-users-solid" class="w-12 h-12 text-blue-500" />
          </div>
          <h3 class="font-black text-xl text-slate-900 dark:text-white uppercase tracking-tighter">User Directory</h3>
          <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest leading-relaxed">
            Manage permissions, roles and account security protocols
          </p>
        </div>
      </UCard>

      <UCard
        class="glass-effect group hover:ring-2 hover:ring-emerald-500 transition-all duration-500 cursor-pointer overflow-hidden relative"
        :ui="{ body: { padding: 'p-8' } }"
        @click="navigateTo('/admin/books')"
      >
        <div class="flex flex-col items-center text-center space-y-4">
          <div class="p-4 rounded-2xl bg-emerald-500/10 group-hover:scale-110 transition-transform duration-500">
            <UIcon name="i-heroicons-book-open-solid" class="w-12 h-12 text-emerald-500" />
          </div>
          <h3 class="font-black text-xl text-slate-900 dark:text-white uppercase tracking-tighter">Inventory Control</h3>
          <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest leading-relaxed">
            Update library collections, stock levels and metadata
          </p>
        </div>
      </UCard>

      <UCard
        class="glass-effect group hover:ring-2 hover:ring-amber-500 transition-all duration-500 cursor-pointer overflow-hidden relative"
        :ui="{ body: { padding: 'p-8' } }"
        @click="navigateTo('/admin/logs')"
      >
        <div class="flex flex-col items-center text-center space-y-4">
          <div class="p-4 rounded-2xl bg-amber-500/10 group-hover:scale-110 transition-transform duration-500">
            <UIcon name="i-heroicons-clipboard-document-list-solid" class="w-12 h-12 text-amber-500" />
          </div>
          <h3 class="font-black text-xl text-slate-900 dark:text-white uppercase tracking-tighter">Audit Logs</h3>
          <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest leading-relaxed">
            Full audit trail powered by MongoDB - Monitor global history
          </p>
        </div>
      </UCard>

    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * Admin Dashboard - Jules v5.0
 * 作用：三方管理入口，已对接 MongoDB Logs 页面
 */
definePageMeta({
  layout: 'admin',
  middleware: 'auth'
})
</script>

<style scoped>
.glass-effect {
  @apply bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-white/20 dark:border-white/5;
}

@keyframes spring-in {
  0% { transform: scale(0.95); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
.animate-spring-in {
  animation: spring-in 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}
</style>
