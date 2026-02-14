<template>
  <div class="space-y-6 animate-spring-in">
    <div class="flex justify-between items-center bg-slate-900 text-white p-6 rounded-xl border border-slate-800 shadow-xl">
      <div class="flex items-center gap-4">
        <div class="bg-emerald-500/20 p-3 rounded-lg">
          <UIcon name="i-heroicons-clipboard-document-list-solid" class="w-8 h-8 text-emerald-500" />
        </div>
        <div>
          <h1 class="text-2xl font-black leading-tight tracking-tighter uppercase">
            Borrowing <span class="text-emerald-500">History</span>
          </h1>
          <p class="text-[10px] text-slate-400 font-black uppercase tracking-[0.2em]">Full audit trail (Powered by MongoDB)</p>
        </div>
      </div>
      <UButton 
        icon="i-heroicons-arrow-path" 
        @click="fetchLogs" 
        :loading="pending"
        color="emerald"
        variant="solid"
        size="lg"
        class="font-black px-8 rounded-full shadow-lg hover:scale-105 transition-all"
      >
        Refresh Data
      </UButton>
    </div>

    <UCard class="border-none shadow-md ring-1 ring-gray-200 dark:ring-gray-700" :ui="{ body: { padding: 'p-6' } }">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <UFormGroup label="Filter by User" size="sm" class="font-bold">
          <UInput 
            v-model="filters.username" 
            placeholder="e.g. RongSapiens" 
            icon="i-heroicons-user" 
            input-class="font-bold"
          />
        </UFormGroup>

        <UFormGroup label="Filter by Book" size="sm" class="font-bold">
          <UInput 
            v-model="filters.bookTitle" 
            placeholder="e.g. Java Programming" 
            icon="i-heroicons-book-open" 
            input-class="font-bold"
          />
        </UFormGroup>

        <UFormGroup label="Transaction Status" size="sm" class="font-bold">
          <USelect 
            v-model="filters.status" 
            :options="['ALL', 'BORROWED', 'RETURNED', 'OVERDUE']" 
            class="font-bold"
          />
        </UFormGroup>
      </div>
    </UCard>

    <UCard class="border-none shadow-2xl ring-1 ring-gray-200 dark:ring-gray-700 overflow-hidden" :ui="{ body: { padding: 'p-0' } }">
      <div class="h-[calc(100vh-400px)] overflow-y-auto relative custom-scrollbar">
        <UTable 
          :rows="filteredLogs" 
          :columns="columns" 
          :loading="pending"
          class="w-full"
          :ui="{ 
            wrapper: 'relative overflow-visible', 
            thead: 'table-header-sticky',
            th: { 
              base: 'text-xs font-black text-slate-900 dark:text-white uppercase py-4 px-4 text-center bg-gray-50 dark:bg-slate-900 tracking-widest',
              sort: { base: 'flex justify-center items-center' }
            },
            td: { base: 'text-sm py-4 px-4 text-center align-middle font-medium' }
          }"
        >
          <template #borrowDate-data="{ row }">
            <div class="flex items-center justify-center gap-2 font-mono text-[13px]">
              <span :class="isOverdue(row) ? 'text-rose-600 font-black underline decoration-double' : 'text-slate-600 dark:text-slate-300 font-bold'">
                {{ formatDate(row.borrowDate) }}
              </span>
              <UIcon 
                v-if="isOverdue(row)" 
                name="i-heroicons-exclamation-triangle-solid" 
                class="text-rose-500 w-5 h-5 animate-pulse" 
              />
            </div>
          </template>

          <template #status-data="{ row }">
            <div class="flex justify-center">
              <UBadge 
                :color="isOverdue(row) ? 'rose' : (row.status === 'BORROWED' ? 'emerald' : 'slate')" 
                variant="subtle"
                class="font-black scale-95 px-3 py-1 rounded shadow-sm border"
                :class="isOverdue(row) ? 'border-rose-200 animate-bounce' : 'border-transparent'"
              >
                {{ isOverdue(row) ? 'CRITICAL: OVERDUE' : row.status }}
              </UBadge>
            </div>
          </template>
        </UTable>
      </div>
      
      <div class="flex-none py-2 px-4 border-t border-gray-100 bg-slate-50 dark:bg-slate-900/50 text-[11px] font-black text-emerald-600 dark:text-emerald-400 flex justify-between uppercase tracking-widest">
        <div class="flex items-center gap-2">
          <span class="inline-block w-2 h-2 rounded-full bg-emerald-500 animate-ping"></span>
          Live Audit Trail Monitor
        </div>
        <span>System Index: {{ filteredLogs.length }} Entries Loaded</span>
      </div>
    </UCard>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'admin', middleware: 'auth' })

const filters = reactive({ 
  username: '', 
  bookTitle: '', 
  status: 'ALL' 
})

const columns = [
  { key: 'username', label: 'Operator', sortable: true, class: 'w-[20%]' },
  { key: 'bookTitle', label: 'Resource Title', sortable: true, class: 'w-[40%]' },
  { key: 'borrowDate', label: 'Timestamp', sortable: true, class: 'w-[25%]' },
  { key: 'status', label: 'Current State', sortable: true, class: 'w-[15%]' }
]

const { data: logs, pending, refresh: fetchLogs } = await useApi<any[]>('/borrowings/all-logs')

const isOverdue = (log: any) => {
  if (!log.borrowDate || log.status === 'RETURNED') return false
  const borrowDate = new Date(log.borrowDate).getTime()
  const now = new Date().getTime()
  const diffDays = (now - borrowDate) / (1000 * 60 * 60 * 24)
  return diffDays > 14
}

const filteredLogs = computed(() => {
  if (!logs.value) return []
  return logs.value.filter(log => {
    const matchUser = !filters.username || 
      log.username?.toLowerCase().includes(filters.username.toLowerCase())
    const matchBook = !filters.bookTitle || 
      log.bookTitle?.toLowerCase().includes(filters.bookTitle.toLowerCase())
    
    let matchStatus = filters.status === 'ALL' || log.status === filters.status
    if (filters.status === 'OVERDUE') {
      matchStatus = isOverdue(log)
    }
    return matchUser && matchBook && matchStatus
  })
})

const formatDate = (dateStr: string) => {
  if (!dateStr) return '---'
  return new Date(dateStr).toLocaleString('en-GB', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
/* 滚动条颜色修正为绿色 */
.custom-scrollbar::-webkit-scrollbar-thumb { @apply bg-emerald-200 dark:bg-emerald-900 rounded-full hover:bg-emerald-300 transition-colors; }

:deep(.table-header-sticky) {
  position: sticky !important;
  top: 0 !important;
  z-index: 30 !important;
}

:deep(table) {
  border-collapse: separate !important;
  border-spacing: 0 !important;
  table-layout: fixed !important;
}

:deep(thead th) {
  position: sticky !important;
  top: 0 !important;
  /* 表头底边颜色修正为 Emerald-500 (#10b981) */
  border-bottom: 3px solid #10b981 !important; 
  box-shadow: 0 1px 0 0 #10b981;
}

@keyframes spring-in {
  0% { transform: translateY(10px); opacity: 0; }
  100% { transform: translateY(0); opacity: 1; }
}
.animate-spring-in {
  animation: spring-in 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
</style>