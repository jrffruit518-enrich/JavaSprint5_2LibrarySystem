<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center text-left">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">Borrowing History (MongoDB)</h1>
        <p class="text-sm text-gray-500">Full audit trail of library transactions</p>
      </div>
      <UButton 
        icon="i-heroicons-arrow-path" 
        @click="fetchLogs" 
        :loading="pending"
        variant="soft"
      >
        Refresh Data
      </UButton>
    </div>

    <UCard shadow="sm">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <UFormGroup label="Filter by User">
          <UInput 
            v-model="filters.username" 
            placeholder="e.g. RongSapiens" 
            icon="i-heroicons-user" 
          />
        </UFormGroup>

        <UFormGroup label="Filter by Book">
          <UInput 
            v-model="filters.bookTitle" 
            placeholder="e.g. Java Programming" 
            icon="i-heroicons-book-open" 
          />
        </UFormGroup>

        <UFormGroup label="Status">
          <USelect 
            v-model="filters.status" 
            :options="['ALL', 'BORROWED', 'RETURNED', 'OVERDUE']" 
          />
        </UFormGroup>
      </div>
    </UCard>

    <UCard overflow-hidden>
      <UTable :rows="filteredLogs" :columns="columns" :loading="pending">
        <template #status-data="{ row }">
          <UBadge 
            :color="isOverdue(row) ? 'red' : (row.status === 'BORROWED' ? 'green' : 'gray')" 
            variant="subtle"
            :class="{ 'animate-pulse': isOverdue(row) }"
          >
            {{ isOverdue(row) ? 'OVERDUE' : row.status }}
          </UBadge>
        </template>

        <template #borrowDate-data="{ row }">
          <div class="flex items-center gap-2">
            <span class="text-xs font-mono" :class="{ 'text-red-600 font-bold': isOverdue(row) }">
              {{ formatDate(row.borrowDate) }}
            </span>
            <UIcon 
              v-if="isOverdue(row)" 
              name="i-heroicons-exclamation-triangle" 
              class="text-red-500 w-4 h-4 animate-bounce" 
            />
          </div>
        </template>
      </UTable>
    </UCard>
  </div>
</template>

<script setup lang="ts">
/**
 * Admin Logs Page (Jules v4.7 - Overdue Visualized)
 * 1. 增加 isOverdue 计算逻辑
 * 2. 状态列与日期列联动显示红色警告
 */
definePageMeta({ layout: 'admin', middleware: 'auth' })

const filters = reactive({ 
  username: '', 
  bookTitle: '', 
  status: 'ALL' 
})

const columns = [
  { key: 'username', label: 'User', sortable: true },
  { key: 'bookTitle', label: 'Book', sortable: true },
  { key: 'borrowDate', label: 'Borrow Date' },
  { key: 'status', label: 'Status' }
]

const { data: logs, pending, refresh: fetchLogs } = await useApi<any[]>('/borrowings/all-logs')

// --- Overdue 判定逻辑 (14天) ---
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
    
    // 状态过滤逻辑：如果选了 OVERDUE，则显示所有符合 isOverdue 的记录
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
