<script setup lang="ts">
/**
 * User Dashboard Index (Jules v3 - Data Connected)
 * 1. 自动从 Cookie 读取 userId。
 * 2. 对接后端 /api/borrowings/{userId}/status 获取真实统计。
 */

definePageMeta({
  layout: 'user',
  middleware: 'auth'
})

// 从 Cookie 获取当前用户 ID
const userId = useCookie('user_id')

// 响应式统计数据
const stats = reactive({
  borrowed: 0,
  reservations: 0, // 预留功能
  overdue: 0
})

/**
 * 获取实时统计数据
 */
const fetchStats = async () => {
  if (!userId.value) return

  try {
    // 路径对齐：后端定义的 @GetMapping("/{userId}/status")
    // 前缀 /api/borrowings 由 useApi 自动处理（如果已封装）或手动拼接
    const { data } = await useApi<any>(`/borrowings/${userId.value}/status`)
    
    if (data.value) {
      // 对应后端 UserStatusResponse 字段: borrowCount, hasOverdue
      stats.borrowed = data.value.borrowCount || 0
      stats.overdue = data.value.hasOverdue ? 1 : 0 
    }
  } catch (err) {
    console.error('[JULES] Dashboard Stats Sync Error:', err)
  }
}

// 挂载时加载数据
onMounted(fetchStats)
</script>

<template>
  <div class="space-y-8">
    <div>
      <h1 class="text-2xl font-bold text-green-500">
        Hello, Reader!
      </h1>
      <p class="text-gray-500">
        Welcome back to your reading dashboard.
      </p>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <UCard class="hover:shadow-md transition-all border-l-4 border-green-500">
        <div class="text-center py-2">
          <UIcon name="i-heroicons-book-open" class="w-8 h-8 text-green-500 mx-auto mb-2" />
          <p class="text-sm text-gray-500 mb-1 font-medium uppercase tracking-wider">Books Borrowed</p>
          <p class="text-4xl font-extrabold text-green-600">
            {{ stats.borrowed }}
          </p>
        </div>
      </UCard>

      <UCard class="hover:shadow-md transition-all border-l-4 border-primary">
        <div class="text-center py-2">
          <UIcon name="i-heroicons-calendar-days" class="w-8 h-8 text-primary mx-auto mb-2" />
          <p class="text-sm text-gray-500 mb-1 font-medium uppercase tracking-wider">Active Reservations</p>
          <p class="text-4xl font-extrabold text-primary">
            {{ stats.reservations }}
          </p>
        </div>
      </UCard>

      <UCard class="hover:shadow-md transition-all border-l-4 border-red-500">
        <div class="text-center py-2">
          <UIcon name="i-heroicons-exclamation-circle" class="w-8 h-8 text-red-500 mx-auto mb-2" />
          <p class="text-sm text-gray-500 mb-1 font-medium uppercase tracking-wider">Overdue Notices</p>
          <p class="text-4xl font-extrabold text-red-500">
            {{ stats.overdue }}
          </p>
        </div>
      </UCard>
    </div>

    <div class="mt-10">
      <h3 class="text-lg font-semibold mb-4 text-gray-700">Quick Actions</h3>
      <div class="flex gap-4">
        <UButton to="/user/books" icon="i-heroicons-magnifying-glass" color="green">Explore Books</UButton>
        <UButton to="/user/loans" icon="i-heroicons-bookmark" variant="outline">My Active Loans</UButton>
      </div>
    </div>
  </div>
</template>
