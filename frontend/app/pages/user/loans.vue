<script setup lang="ts">
/**
 * 图书馆项目 - My Loans (Jules v9 - Optimized)
 * 1. 修正 userId 类型转换，解决 MongoDB 查询空结果问题
 * 2. 规范 API 路径拼接，防止 404
 */

definePageMeta({
  layout: 'user',
  middleware: 'auth'
})

// --- 1. 用户 ID 处理 ---
// 获取 Cookie 中的原始值并强制转为数字，以匹配后端 Long 类型
const userIdRaw = useCookie('user_id')
const userId = computed(() => (userIdRaw.value ? Number(userIdRaw.value) : null))

const searchQuery = ref('')

// --- 2. 数据获取 ---
// 路径去掉首斜杠，由 useApi 自动拼接 baseURL (/api)
const { data: loans, refresh, status } = await useApi<any[]>(() => 
  userId.value ? `borrowings/user/${userId.value}` : null
)

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

// --- 3. 数据过滤逻辑 ---
const activeLoans = computed(() => {
  // 如果 status 返回的是小写或对象，请确保此处匹配后端 LogStatus 枚举的字符串
  return (loans.value || []).filter(l => l.status === 'BORROWED')
})

const filteredHistory = computed(() => {
  const history = (loans.value || []).filter(l => l.status === 'RETURNED')
  if (!searchQuery.value) return history
  return history.filter(l => l.bookTitle.toLowerCase().includes(searchQuery.value.toLowerCase()))
})

// --- 4. 交互逻辑 ---
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

    <UCard>
      <UTabs :items="tabs" class="w-full">
        <template #active>
          <div class="py-4 space-y-4">
            <UTable :rows="activeLoans" :columns="activeColumns" :loading="status === 'pending'">
              <template #bookTitle-data="{ row }">
                <div class="flex items-center gap-3">
                  <UIcon name="i-heroicons-book-open" class="text-primary w-5 h-5" />
                  <span class="font-medium text-gray-700">{{ row.bookTitle }}</span>
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