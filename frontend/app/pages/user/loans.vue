<script setup lang="ts">
/**
 * 图书馆项目 - My Loans (Jules v11.4 - Hydration Stability Fix)
 */

// 1. 核心元数据 (必须置顶)
definePageMeta({
  layout: 'user',
  middleware: 'auth'
})

const searchQuery = ref('')

// 2. 静态数据定义 (优先定义，防止模板访问不到)
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

// 3. 数据获取 (移除 lazy，保证 SSR 稳定性)
const { data: loans, refresh, status } = await useApi<any[]>('borrowings/user/loans')

// 4. 辅助函数
const isOverdue = (dateStr: any) => {
  if (!dateStr || typeof dateStr !== 'string') return false
  const bDate = new Date(dateStr)
  if (isNaN(bDate.getTime())) return false
  const diffDays = (Date.now() - bDate.getTime()) / (1000 * 60 * 60 * 24)
  return diffDays > 30
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('en-GB')
}

// 5. 过滤逻辑
const activeLoans = computed(() => {
  return (loans.value || []).filter(l => l && String(l.status).toUpperCase() === 'BORROWED')
})

const filteredHistory = computed(() => {
  const history = (loans.value || []).filter(l => l && String(l.status).toUpperCase() === 'RETURNED')
  if (!searchQuery.value) return history
  return history.filter(l => l.bookTitle?.toLowerCase().includes(searchQuery.value.toLowerCase()))
})

// 6. 交互逻辑
const handleReturn = async (bookId: number) => {
  if (!confirm('Confirm to return this book?')) return
  try {
    const token = useCookie('auth_token').value
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
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-green-500">My Loans</h1>
        <p class="text-sm text-gray-500">Manage your current readings.</p>
      </div>
      <UButton 
        icon="i-heroicons-arrow-path" 
        variant="ghost" 
        :loading="status === 'pending'" 
        @click="refresh" 
      />
    </div>

    <UCard>
      <UTabs :items="tabs" class="w-full">
        <template #active>
          <div class="py-4">
            <UTable :rows="activeLoans" :columns="activeColumns" :loading="status === 'pending'">
              <template #bookTitle-data="{ row }">
                <div class="flex items-center gap-3">
                  <UIcon name="i-heroicons-book-open" class="text-primary w-5 h-5" />
                  <span class="font-medium">{{ row?.bookTitle }}</span>
                </div>
              </template>

              <template #borrowDate-data="{ row }">
                <div class="flex flex-col">
                  <span :class="{ 'text-red-600 font-bold': isOverdue(row?.borrowDate) }">
                    {{ formatDate(row?.borrowDate) }}
                  </span>
                  <div v-if="isOverdue(row?.borrowDate)" class="text-[10px] bg-red-100 text-red-600 px-1 rounded w-fit mt-1 border border-red-200 font-bold">
                    OVERDUE
                  </div>
                </div>
              </template>

              <template #actions-data="{ row }">
                <UButton label="Return" size="xs" color="primary" variant="soft" @click="handleReturn(row.bookId)" />
              </template>
            </UTable>
            <div v-if="activeLoans.length === 0 && status !== 'pending'" class="text-center py-10 text-gray-400">
              No active loans.
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