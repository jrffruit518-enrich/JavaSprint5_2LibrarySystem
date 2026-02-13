<template>
  <div class="border border-gray-200 dark:border-gray-800 rounded-lg overflow-hidden flex flex-col">
    <div class="overflow-y-auto max-h-[500px] relative">
      <UTable
        :rows="data"
        :columns="tableColumns"
        :loading="loading"
        :ui="{ 
          wrapper: 'relative',
          thead: 'bg-gray-50 dark:bg-gray-900 sticky top-0 z-10 shadow-sm',
          th: { base: 'text-xs font-semibold text-gray-500 uppercase tracking-wider' },
          td: { base: 'text-sm text-gray-600 dark:text-gray-400' }
        }"
      >
        <template #title-data="{ row }">
          <span class="font-medium text-gray-900 dark:text-white truncate max-w-[200px] block">
            {{ row.title }}
          </span>
        </template>

        <template #bookGenre-data="{ row }">
          <UBadge
            size="xs"
            variant="soft"
            :color="getGenreColor(row.bookGenre)"
          >
            {{ row.bookGenre }}
          </UBadge>
        </template>

        <template #stock-data="{ row }">
          <span :class="row.availableStock <= 5 ? 'text-red-500 font-bold' : ''">
            {{ row.availableStock }}
          </span>
        </template>

        <template #rating-data="{ row }">
          <div class="flex items-center gap-1">
            <span class="text-orange-400">★</span>
            <span>{{ row.rating }}</span>
          </div>
        </template>

        <template #actions-data="{ row }">
          <div class="flex items-center gap-1">
            <UButton
              icon="i-heroicons-eye"
              size="xs"
              variant="ghost"
              color="gray"
              @click="$emit('view', row)"
            />

            <template v-if="role === 'admin'">
              <UButton
                icon="i-heroicons-pencil-square"
                size="xs"
                variant="ghost"
                color="primary"
                @click="$emit('view', row)" 
              />
              <UButton
                icon="i-heroicons-trash"
                size="xs"
                variant="ghost"
                color="red"
                @click="$emit('delete', row)"
              />
            </template>
          </div>
        </template>
      </UTable>
    </div>
    
    <div class="p-2 px-4 border-t border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-gray-900 text-xs text-gray-500">
      Showing {{ data?.length || 0 }} entries
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * Jules Component Sync: BookTable (Fixed Viewport Edition)
 * 修复了长列表导致的页面溢出问题。
 */
import type { Book } from '~/types/book'

const props = defineProps<{
  data: Book[]
  loading?: boolean
  role: 'admin' | 'guest' | 'user'
}>()

defineEmits<{
  view: [book: Book]
  delete: [book: Book]
}>()

// 表格列配置
const tableColumns = [
  { key: 'title', label: 'Title' },
  { key: 'author', label: 'Author' },
  { key: 'bookGenre', label: 'Genre' },
  { key: 'stock', label: 'Stock' }, 
  { key: 'rating', label: 'Rating' },
  { key: 'actions', label: 'Actions' }
]

const getGenreColor = (genre: string) => {
  const map: Record<string, string> = {
    'FICTION': 'blue',
    'NON_FICTION': 'green',
    'SCIENCE': 'purple',
    'FANTASY': 'orange',
    'HISTORY': 'yellow'
  }
  return map[genre] || 'gray'
}
</script>