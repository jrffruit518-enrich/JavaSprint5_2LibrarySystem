<script setup lang="ts">
/**
 * 图书馆项目 - My Loans (Jules v10 - Path Synchronization)
 * 1. 核心修复：API 路径修正为 'borrowings/user/loans'
 * 2. 移除 URL 传参：后端现在通过 JWT Token 自动识别用户，不再依赖 URL 中的 userId
 */

definePageMeta({
  layout: 'user',
  middleware: 'auth'
})

const searchQuery = ref('')

// --- 1. 数据获取 ---
// Jules Fix: 路径必须严格匹配后端的 @GetMapping("/user/loans")
// 后端逻辑：Controller 会提取 Token 里的 ID，然后去 MongoDB 查记录
const { data: loans, refresh, status } = await useApi<any[]>('borrowings/user/loans')

const tabs = [
  { label: 'Active Loans', slot: 'active', icon: 'i-heroicons-bookmark' },
  { label: 'History', slot: 'history', icon: 'i-heroicons-clock' }
]

const activeColumns = [
  { key: 'bookTitle', label: 'Book Title' },
  { key: 'borrowDate', label: 'Borrowed On' },
  { key: 'actions', label: 'Action' }
]

const historyColumns = [
  { key: 'bookTitle', label: 'Book Title' },
  { key: 'borrowDate', label: 'Borrowed On' },
  { key: 'returnDate', label: 'Returned On' },
  { key: 'status', label: 'Status' }
]

// --- 2. 数据过滤逻辑 ---
const activeLoans = computed(() => {
  // 后端返回的 status 通常是大写的 'BORROWED'
  return (loans.value || []).filter(l => String(l.status).toUpperCase() === 'BORROWED')
})

const filteredHistory = computed(() => {
  const history = (loans.value || []).filter(l => String(l.status).toUpperCase() === 'RETURNED')
  if (!searchQuery.value) return history
  return history.filter(l => l.bookTitle.toLowerCase().includes(searchQuery.value.toLowerCase()))
})

// --- 3. 交互逻辑 ---
const handleReturn = async (bookId: number) => {
  if (!confirm('Confirm to return this book?')) return
  
  try {
    const token = useCookie('auth_token').value
    // 路径对齐后端 @PostMapping("/return/{bookId}")
    await $fetch(`/api/borrowings/return/${bookId}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` }
    })
    
    alert('Book returned successfully!')
    await refresh() 
  } catch (err: any) {
    alert('Return failed: ' + (err.data?.message || 'Server Error'))
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString()
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-green-500">My Loans</h1>
        <p class="text-sm text-gray-500">Manage your current readings and history.</p>
      </div>
      <UButton 
        icon="i-heroicons-arrow-path" 
        variant="ghost" 
        :loading="status === 'pending'" 
        @click="refresh" 
      />
    </div>

    <div v-if="loans && loans.length > 0" class="p-2 bg-gray-900 text-green-400 text-[10px] font-mono rounded">
      DEBUG: Received {{ loans.length }} records. Example: {{ loans[0].bookTitle }}
    </div>

    <UCard>
      <UTabs :items="tabs" class="w-full">
        <template #active>
          <div class="py-4 space-y-4">
            <UTable :rows="activeLoans" :columns="activeColumns" :loading="status === 'pending'">
              <template #bookTitle-data="{ row }">
                <div class="flex items-center gap-3">
                  <UIcon name="i-heroicons-book-open" class="text-primary w-5 h-5" />
                  <span class="font-medium text-gray-700 dark:text-gray-200">{{ row.bookTitle }}</span>
                </div>
              </template>
              <template #borrowDate-data="{ row }">
                {{ formatDate(row.borrowDate) }}
              </template>
              <template #actions-data="{ row }">
                <UButton 
                  label="Return" 
                  size="xs" 
                  color="primary" 
                  variant="soft" 
                  icon="i-heroicons-arrow-uturn-left"
                  @click="handleReturn(row.bookId)" 
                />
              </template>
            </UTable>
            <div v-if="activeLoans.length === 0 && status !== 'pending'" class="text-center py-10 text-gray-400">
              No active loans found.
            </div>
          </div>
        </template>

        <template #history>
          <div class="py-4 space-y-4">
            <UInput v-model="searchQuery" icon="i-heroicons-magnifying-glass" placeholder="Search history..." />
            <UTable :rows="filteredHistory" :columns="historyColumns" :loading="status === 'pending'">
              <template #borrowDate-data="{ row }">
                {{ formatDate(row.borrowDate) }}
              </template>
              <template #returnDate-data="{ row }">
                {{ formatDate(row.returnDate) }}
              </template>
              <template #status-data="{ row }">
                <UBadge size="xs" color="gray" variant="soft">
                  {{ row.status }}
                </UBadge>
              </template>
            </UTable>
          </div>
        </template>
      </UTabs>
    </UCard>
  </div>
</template>
