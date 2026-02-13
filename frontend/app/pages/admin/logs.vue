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
            :color="row.status === 'BORROWED' ? 'green' : (row.status === 'OVERDUE' ? 'red' : 'gray')" 
            variant="subtle"
          >
            {{ row.status }}
          </UBadge>
        </template>

        <template #borrowDate-data="{ row }">
          <span class="text-xs font-mono">{{ formatDate(row.borrowDate) }}</span>
        </template>
      </UTable>
    </UCard>
  </div>
</template>

<script setup lang="ts">
/**
 * Admin Logs Page (Jules v4.5 - Precision Filtering)
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

// 核心：三个维度同时生效的独立过滤
const filteredLogs = computed(() => {
  if (!logs.value) return []
  return logs.value.filter(log => {
    // 1. 用户名精准/模糊匹配
    const matchUser = !filters.username || 
      log.username?.toLowerCase().includes(filters.username.toLowerCase())
    
    // 2. 书名精准/模糊匹配
    const matchBook = !filters.bookTitle || 
      log.bookTitle?.toLowerCase().includes(filters.bookTitle.toLowerCase())
    
    // 3. 状态匹配
    const matchStatus = filters.status === 'ALL' || log.status === filters.status
    
    // 只有当三个条件同时满足时才显示
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
