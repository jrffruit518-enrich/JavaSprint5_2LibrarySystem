<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-highlighted">
          My Loans
        </h1>
        <p class="text-sm text-muted">
          Manage your current readings and history.
        </p>
      </div>
    </div>

    <UCard>
      <UTabs
        :items="tabs"
        class="w-full"
      >
        <template #active>
          <div class="py-4 space-y-4">
            <UTable
              :rows="activeLoans"
              :columns="activeColumns"
            >
              <template #book-data="props">
                <div
                  v-if="props.row"
                  class="flex items-center gap-3"
                >
                  <UIcon
                    name="i-lucide-book-open"
                    class="text-primary"
                  />
                  <span class="font-medium">{{ props.row.original.bookTitle }}</span>
                </div>
              </template>

              <template #dueDate-data="props">
                <span
                  v-if="props.row"
                  :class="isOverdue(props.row.original.dueDate) ? 'text-red-500 font-bold' : 'text-muted'"
                >
                  {{ props.row.original.dueDate }}
                </span>
              </template>

              <template #actions-data="props">
                <UButton
                  v-if="props.row"
                  label="Return"
                  size="xs"
                  color="neutral"
                  variant="solid"
                  icon="i-lucide-rotate-ccw"
                  @click="handleReturn(props.row.original.id)"
                />
              </template>
            </UTable>
          </div>
        </template>

        <template #history>
          <div class="py-4 space-y-4">
            <UInput
              v-model="searchQuery"
              icon="i-lucide-search"
              placeholder="Search history..."
            />
            <UTable
              :rows="filteredHistory"
              :columns="historyColumns"
            >
              <template #status-data>
                <UBadge
                  size="xs"
                  color="neutral"
                  variant="soft"
                >
                  Returned
                </UBadge>
              </template>
            </UTable>
          </div>
        </template>
      </UTabs>
    </UCard>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

definePageMeta({
  layout: 'user',
  middleware: 'auth'
})

const tabs = [
  { label: 'Active Loans', slot: 'active', icon: 'i-lucide-book-check' },
  { label: 'History', slot: 'history', icon: 'i-lucide-history' }
]

// 核心修正：确保每个 column 都有明确的 id
const activeColumns = [
  { id: 'book', accessorKey: 'bookTitle', header: 'Book Title' },
  { id: 'borrowDate', accessorKey: 'borrowDate', header: 'Borrowed On' },
  { id: 'dueDate', accessorKey: 'dueDate', header: 'Due Date' },
  { id: 'actions', header: 'Action' }
]

const historyColumns = [
  { id: 'bookTitle', accessorKey: 'bookTitle', header: 'Book Title' },
  { id: 'borrowDate', accessorKey: 'borrowDate', header: 'Borrowed On' },
  { id: 'returnDate', accessorKey: 'returnDate', header: 'Returned On' },
  { id: 'status', header: 'Status' }
]

const activeLoans = ref([
  { id: 1, bookTitle: 'Effective Java', borrowDate: '2026-02-01', dueDate: '2026-02-15' },
  { id: 2, bookTitle: 'Clean Code', borrowDate: '2026-02-05', dueDate: '2026-02-12' }
])

const historyData = ref([
  { id: 101, bookTitle: 'Vue 3 Guide', borrowDate: '2026-01-10', returnDate: '2026-01-20' },
  { id: 102, bookTitle: 'Spring Boot in Action', borrowDate: '2026-01-15', returnDate: '2026-01-25' }
])

const searchQuery = ref('')
const filteredHistory = computed(() => {
  return historyData.value.filter(item =>
    item.bookTitle.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const isOverdue = (dateStr: any) => {
  if (!dateStr) return false
  return new Date(dateStr).getTime() < new Date().getTime()
}

const handleReturn = (id: any) => {
  console.log('Returning:', id)
}
</script>
