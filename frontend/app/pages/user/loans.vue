<template>
  <div class="w-full space-y-6 animate-spring-in">
    <div class="flex justify-between items-center bg-slate-900 text-white p-6 rounded-xl shadow-lg border border-slate-800">
      <div class="flex items-center gap-4">
        <div class="bg-emerald-500/20 p-3 rounded-lg">
          <UIcon name="i-heroicons-bookmark-square-solid" class="w-8 h-8 text-emerald-500" />
        </div>
        <div>
          <h1 class="text-2xl font-black leading-tight uppercase tracking-tighter">My Loans</h1>
          <p class="text-sm text-slate-400 font-medium">Manage your reading journey and history.</p>
        </div>
      </div>
      <UButton 
        icon="i-heroicons-arrow-path" 
        variant="soft" 
        color="emerald"
        class="rounded-full"
        :loading="status === 'pending'" 
        @click="refresh" 
      />
    </div>

    <UCard class="glass-effect !border-none shadow-2xl" :ui="{ body: { padding: 'p-0' } }">
      <UTabs :items="tabs" class="w-full" :ui="{ wrapper: 'flex flex-col', list: { background: 'bg-slate-100 dark:bg-slate-800/50', marker: { shadow: 'shadow-sm' }, tab: { font: 'font-black uppercase tracking-widest text-[10px]' } } }">
        
        <template #active>
          <div class="p-4 min-h-[400px]">
            <div class="overflow-hidden border border-slate-100 dark:border-white/5 rounded-xl">
              <UTable :rows="activeLoans" :columns="activeColumns" :loading="status === 'pending'"
                :ui="{ th: { base: 'bg-slate-50/50 dark:bg-slate-900/50 font-black text-slate-500 uppercase text-[10px] tracking-widest' }, td: { base: 'font-bold text-slate-700 dark:text-slate-200' } }">
                
                <template #bookTitle-data="{ row }">
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-10 bg-slate-100 dark:bg-slate-800 rounded flex items-center justify-center">
                      <UIcon name="i-heroicons-book-open" class="text-emerald-500 w-5 h-5" />
                    </div>
                    <span class="font-black text-slate-900 dark:text-white">{{ row?.bookTitle }}</span>
                  </div>
                </template>

                <template #borrowDate-data="{ row }">
                  <div class="flex flex-col">
                    <span class="text-xs" :class="isOverdue(row?.borrowDate) ? 'text-rose-500 font-black' : 'text-slate-500'">
                      {{ formatDate(row?.borrowDate) }}
                    </span>
                    <UBadge v-if="isOverdue(row?.borrowDate)" color="rose" variant="flat" size="xs" class="mt-1 w-fit font-black animate-pulse">
                      OVERDUE
                    </UBadge>
                  </div>
                </template>

                <template #actions-data="{ row }">
                  <UButton 
                    label="Return Book" 
                    size="xs" 
                    color="emerald" 
                    variant="solid" 
                    icon="i-heroicons-arrow-uturn-left"
                    class="font-black rounded-lg shadow-sm hover:shadow-emerald-500/20"
                    @click="handleReturn(row.bookId)" 
                  />
                </template>
              </UTable>
            </div>
            <div v-if="activeLoans.length === 0 && status !== 'pending'" class="flex flex-col items-center justify-center py-20 text-slate-400">
              <UIcon name="i-heroicons-inbox-stack" class="w-12 h-12 opacity-20 mb-2" />
              <p class="text-xs font-black uppercase tracking-widest">No active loans found</p>
            </div>
          </div>
        </template>

        <template #history>
          <div class="p-4 space-y-4 min-h-[400px]">
            <div class="flex justify-end px-2">
              <UInput v-model="searchQuery" icon="i-heroicons-magnifying-glass" placeholder="Filter history..." color="emerald" variant="none" class="bg-slate-100 dark:bg-slate-800 rounded-lg w-64" input-class="font-bold text-slate-900 dark:text-white" />
            </div>
            
            <div class="overflow-hidden border border-slate-100 dark:border-white/5 rounded-xl">
              <UTable :rows="filteredHistory" :columns="historyColumns" :loading="status === 'pending'"
                :ui="{ 
                  th: { base: 'bg-slate-100 dark:bg-slate-900 font-black text-slate-900 dark:text-white uppercase text-[10px] tracking-widest border-b border-slate-200 dark:border-slate-800' },
                  td: { base: 'font-bold text-slate-900 dark:text-slate-100' } 
                }">
                
                <template #bookTitle-data="{ row }">
                  <span class="font-black text-slate-900 dark:text-white">{{ row.bookTitle }}</span>
                </template>

                <template #borrowDate-data="{ row }">
                  <span class="text-slate-700 dark:text-slate-300 font-bold text-xs">{{ formatDate(row.borrowDate) }}</span>
                </template>

                <template #returnDate-data="{ row }">
                  <span class="text-emerald-700 dark:text-emerald-400 font-black text-xs">{{ formatDate(row.returnDate) }}</span>
                </template>

                <template #status-data="{ row }">
                  <UBadge size="xs" color="emerald" variant="subtle" class="font-black uppercase tracking-tighter px-2">
                    {{ row.status }}
                  </UBadge>
                </template>
              </UTable>
            </div>
          </div>
        </template>
      </UTabs>
    </UCard>
  </div>
</template>

<script setup lang="ts">
/**
 * My Loans - UI Upgrade v2 (Enhanced Visibility)
 */

definePageMeta({ layout: 'user', middleware: 'auth' })

const searchQuery = ref('')
const tabs = [
  { label: 'Active Readings', slot: 'active', icon: 'i-heroicons-fire' },
  { label: 'Archive', slot: 'history', icon: 'i-heroicons-archive-box' }
]

const activeColumns = [
  { key: 'bookTitle', label: 'Book Details' },
  { key: 'borrowDate', label: 'Timeline' },
  { key: 'actions', label: 'Management' }
]

const historyColumns = [
  { key: 'bookTitle', label: 'Book' },
  { key: 'borrowDate', label: 'Borrowed' },
  { key: 'returnDate', label: 'Returned' },
  { key: 'status', label: 'Status' }
]

const { data: loans, refresh, status } = await useApi<any[]>('borrowings/user/loans')

const isOverdue = (dateStr: any) => {
  if (!dateStr) return false
  const diffDays = (Date.now() - new Date(dateStr).getTime()) / (1000 * 60 * 60 * 24)
  return diffDays > 30
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
}

const activeLoans = computed(() => (loans.value || []).filter(l => String(l.status).toUpperCase() === 'BORROWED'))

const filteredHistory = computed(() => {
  const history = (loans.value || []).filter(l => String(l.status).toUpperCase() === 'RETURNED')
  if (!searchQuery.value) return history
  return history.filter(l => l.bookTitle?.toLowerCase().includes(searchQuery.value.toLowerCase()))
})

const handleReturn = async (bookId: number) => {
  if (!confirm('Return this book to the library?')) return
  try {
    await $fetch(`/api/borrowings/return/${bookId}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${useCookie('auth_token').value}` }
    })
    await refresh() 
  } catch (err: any) {
    alert(err.data?.message || 'Server Error')
  }
}
</script>

<style scoped>
.glass-effect {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}
.dark .glass-effect {
  background: rgba(15, 23, 42, 0.8);
}

:deep(.u-table) {
  @apply text-sm !important;
}

/* 强制提升历史记录表格中书名的字重 */
:deep(.u-table td) {
  @apply py-3 !important;
}
</style>
