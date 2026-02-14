<script setup lang="ts">
/**
 * User Dashboard Index (Jules v6 - Final Stability Fix)
 * 1. 使用 ClientOnly 包装动态数据区，杜绝 parentNode 报错。
 * 2. 复用 borrowings 接口，确保逻辑统一。
 */

definePageMeta({
  layout: 'user',
  middleware: 'auth'
})

// 直接获取借阅数据
const { data: loans, status } = await useApi<any[]>('borrowings/user/loans')

// 统计逻辑
const stats = computed(() => {
  const list = loans.value || []
  const activeList = list.filter(l => l && String(l.status).toUpperCase() === 'BORROWED')
  
  const overdueCount = activeList.filter(l => {
    if (!l.borrowDate) return false
    const bDate = new Date(l.borrowDate)
    const diffDays = (Date.now() - bDate.getTime()) / (1000 * 60 * 60 * 24)
    return diffDays > 30
  }).length

  return {
    borrowed: activeList.length,
    overdue: overdueCount
  }
})
</script>

<template>
  <div class="space-y-8">
    <div>
      <h1 class="text-2xl font-bold text-green-500">Hello, Reader!</h1>
      <p class="text-gray-500">Welcome back to your reading dashboard.</p>
    </div>

    <ClientOnly>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <UCard class="hover:shadow-md transition-all border-l-4 border-green-500">
          <div class="text-center py-2">
            <UIcon name="i-heroicons-book-open" class="w-8 h-8 text-green-500 mx-auto mb-2" />
            <p class="text-sm text-gray-500 mb-1 font-medium uppercase tracking-wider">Books Borrowed</p>
            <p class="text-4xl font-extrabold text-green-600">
              {{ status === 'pending' ? '...' : stats.borrowed }}
            </p>
          </div>
        </UCard>

        <UCard class="hover:shadow-md transition-all border-l-4 border-primary">
          <div class="text-center py-2">
            <UIcon name="i-heroicons-calendar-days" class="w-8 h-8 text-primary mx-auto mb-2" />
            <p class="text-sm text-gray-500 mb-1 font-medium uppercase tracking-wider">Active Reservations</p>
            <p class="text-4xl font-extrabold text-primary">0</p>
          </div>
        </UCard>

        <UCard class="hover:shadow-md transition-all border-l-4 border-red-500">
          <div class="text-center py-2">
            <UIcon name="i-heroicons-exclamation-circle" class="w-8 h-8 text-red-500 mx-auto mb-2" />
            <p class="text-sm text-gray-500 mb-1 font-medium uppercase tracking-wider">Overdue Notices</p>
            <p class="text-4xl font-extrabold text-red-500">
              {{ status === 'pending' ? '...' : stats.overdue }}
            </p>
          </div>
        </UCard>
      </div>

      <template #fallback>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          <UCard v-for="i in 3" :key="i" class="h-32 animate-pulse bg-gray-50" />
        </div>
      </template>
    </ClientOnly>

    <div class="mt-10">
      <h3 class="text-lg font-semibold mb-4 text-gray-700">Quick Actions</h3>
      <div class="flex gap-4">
        <UButton to="/user/books" icon="i-heroicons-magnifying-glass" color="green">Explore Books</UButton>
        <UButton to="/user/loans" icon="i-heroicons-bookmark" variant="outline">My Active Loans</UButton>
      </div>
    </div>
  </div>
</template>
