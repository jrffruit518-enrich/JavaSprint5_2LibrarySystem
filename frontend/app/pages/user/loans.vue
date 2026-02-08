这是读者端的最后一个核心页面 pages/user/loans.vue。

我也在最顶端加入了 <AppBreadcrumb />，并根据你的设计保留了“正在借阅”和“借阅历史”两个区域的逻辑。

修改后的 pages/user/loans.vue
代码段
<template>
  <div class="space-y-10">
    <AppBreadcrumb current-page-title="My Loans" />

    <div>
      <h1 class="text-2xl font-bold text-highlighted">
        My Book Management
      </h1>
      <p class="text-muted text-sm">
        Track your current reading and historical records.
      </p>
    </div>

    <section class="space-y-4">
      <div class="flex items-center gap-2">
        <UIcon
          name="i-lucide-book-open"
          class="text-primary w-5 h-5"
        />
        <h2 class="text-lg font-semibold">
          Active Loans
        </h2>
      </div>

      <UCard>
        <UTable
          :rows="activeLoans"
          :columns="activeColumns"
        >
          <template #actions-data="{ row }">
            <UButton
              label="Return"
              icon="i-lucide-undo-2"
              color="primary"
              variant="subtle"
              size="sm"
              @click="handleReturn(row)"
            />
          </template>

          <template #dueDate-data="{ row }">
            <span :class="row.isOverdue ? 'text-red-500 font-medium' : ''">
              {{ row.dueDate }}
              <UIcon
                v-if="row.isOverdue"
                name="i-lucide-alert-circle"
                class="inline w-4 h-4 ml-1"
              />
            </span>
          </template>
        </UTable>
      </UCard>
    </section>

    <section class="space-y-4">
      <div class="flex items-center gap-2 text-muted">
        <UIcon
          name="i-lucide-history"
          class="w-5 h-5"
        />
        <h2 class="text-lg font-semibold">
          Borrowing History
        </h2>
      </div>

      <UCard class="bg-neutral-50/50 dark:bg-neutral-900/50">
        <UTable
          :rows="historyLoans"
          :columns="historyColumns"
        />
      </UCard>
    </section>
  </div>
</template>

<script setup>
/* 设置页面使用用户布局 */
definePageMeta({
  layout: 'user'
})

/* 表格列定义 (English Labels) */
const activeColumns = [
  { key: 'title', label: 'Book Title' },
  { key: 'borrowDate', label: 'Borrowed Date' },
  { key: 'dueDate', label: 'Due Date' },
  { key: 'actions', label: 'Action' }
]

const historyColumns = [
  { key: 'title', label: 'Book Title' },
  { key: 'borrowDate', label: 'Borrowed Date' },
  { key: 'returnDate', label: 'Returned Date' }
]

/* 模拟数据 (Mock Data) */
/* 正在借阅的书 */
const activeLoans = ref([
  { id: 101, title: 'Clean Code', borrowDate: '2026-02-01', dueDate: '2026-02-15', isOverdue: false },
  { id: 102, title: 'Refactoring', borrowDate: '2026-01-20', dueDate: '2026-02-03', isOverdue: true }
])

/* 借阅历史 */
const historyLoans = ref([
  { id: 50, title: 'The Great Gatsby', borrowDate: '2025-12-10', returnDate: '2025-12-25' },
  { id: 51, title: 'Vue.js Guide', borrowDate: '2026-01-05', returnDate: '2026-01-15' }
])

/* 还书逻辑 */
const handleReturn = (book) => {
  /* 这里以后会对接后端 API */
  console.log('Returning book:', book.title)
  alert(`You have returned: ${book.title}`)
}
</script>
