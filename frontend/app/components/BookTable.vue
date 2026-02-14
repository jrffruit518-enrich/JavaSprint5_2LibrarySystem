<template>
  <div class="book-table-root flex flex-col h-full rounded-2xl overflow-hidden border border-black/5 dark:border-white/10 shadow-2xl glass-effect animate-spring-in">
    <div class="flex-none py-2 px-4 bg-white/40 dark:bg-white/5 border-b border-black/5 dark:border-white/5 text-[10px] font-black text-emerald-600 dark:text-emerald-400 flex justify-between uppercase tracking-[0.2em]">
      <span class="flex items-center gap-2">
        <UIcon name="i-heroicons-queue-list" />
        Library Catalog
      </span>
      <span>Total: {{ data?.length || 0 }} Entries</span>
    </div>

    <div class="flex-1 relative min-h-0 bg-white/20 dark:bg-transparent">
      <UTable
        :rows="data"
        :columns="tableColumns"
        :loading="loading"
        class="w-full"
        :ui="{ 
          wrapper: 'absolute inset-0 overflow-y-auto custom-scrollbar table-wrapper-fix', 
          thead: 'sticky-header-z',
          th: { 
            base: 'text-[11px] font-black text-slate-700 dark:text-slate-300 uppercase tracking-widest py-4 px-3 text-center' 
          },
          td: { 
            base: 'text-sm py-4 px-3 align-middle text-center border-b border-black/5 dark:border-white/5 text-slate-900 dark:text-slate-200 transition-colors duration-200' 
          },
          tr: {
            base: 'hover:bg-emerald-500/10 dark:hover:bg-emerald-500/5 transition-colors duration-200 group'
          }
        }"
      >
        <template #title-data="{ row }">
          <div class="font-bold text-slate-900 dark:text-slate-100 leading-tight block py-1 break-words text-center group-hover:text-emerald-600 dark:group-hover:text-emerald-400 transition-colors">
            {{ row.title }}
          </div>
        </template>

        <template #author-data="{ row }">
          <div class="break-words leading-tight text-center text-slate-600 dark:text-slate-400 italic font-medium">{{ row.author }}</div>
        </template>

        <template #bookGenre-data="{ row }">
          <div class="flex justify-center">
            <UBadge 
              size="xs" 
              variant="subtle" 
              :color="getGenreColor(row.bookGenre)" 
              class="font-black uppercase tracking-tighter px-2 shadow-sm"
            >
              {{ row.bookGenre }}
            </UBadge>
          </div>
        </template>

        <template #stock-data="{ row }">
          <div class="flex flex-col items-center">
            <span :class="row.availableStock <= 5 ? 'text-rose-600 dark:text-rose-500 font-black animate-pulse' : 'font-bold text-slate-800 dark:text-slate-200'">
              {{ row.availableStock }}
            </span>
            <div class="w-8 h-1 bg-black/10 dark:bg-white/10 rounded-full mt-1 overflow-hidden">
              <div 
                class="h-full rounded-full transition-all duration-500 shadow-[0_0_8px_rgba(16,185,129,0.3)]"
                :class="row.availableStock <= 5 ? 'bg-rose-500' : 'bg-emerald-500'"
                :style="{ width: `${Math.min(row.availableStock * 10, 100)}%` }"
              ></div>
            </div>
          </div>
        </template>

        <template #rating-data="{ row }">
          <div class="flex items-center justify-center gap-1 font-bold">
            <UIcon name="i-heroicons-star-20-solid" class="text-amber-500 text-sm" />
            <span class="font-mono text-slate-800 dark:text-slate-200">{{ row.rating.toFixed(1) }}</span>
          </div>
        </template>

        <template #actions-data="{ row }">
          <div class="flex items-center justify-center gap-2">
            <UButton 
              icon="i-heroicons-arrow-right-circle" 
              size="xs" 
              variant="solid" 
              color="emerald" 
              label="Explore" 
              class="btn-glow font-black rounded-full px-3 shadow-md"
              @click="$emit('view', row)" 
            />
            
            <template v-if="role === 'admin' || role === 'ROLE_ADMIN'">
              <UButton
                icon="i-heroicons-trash"
                size="xs"
                variant="ghost"
                color="rose"
                class="hover:bg-rose-500/20 rounded-full"
                @click="$emit('delete', row)"
              />
            </template>
          </div>
        </template>
      </UTable>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Book } from '~/types/book'

/**
 * Jules v4.2 - Final Fix Edition
 * Fix: Transferred scroll responsibility to the internal UTable wrapper to lock the header.
 */

const props = defineProps<{
  data: Book[]
  loading?: boolean
  role: 'admin' | 'guest' | 'user' | string
}>()

defineEmits<{
  view: [book: Book]
  delete: [book: Book]
}>()

const tableColumns = [
  { key: 'title', label: 'Title', class: 'w-[30%] text-center' },
  { key: 'author', label: 'Author', class: 'w-[20%] text-center' },
  { key: 'bookGenre', label: 'Genre', class: 'w-[15%] text-center' },
  { key: 'stock', label: 'Stock', class: 'w-[15%] text-center' }, 
  { key: 'rating', label: 'Rating', class: 'w-[10%] text-center' },
  { key: 'actions', label: 'Actions', class: 'w-[10%] text-center' }
]

const getGenreColor = (genre: string) => {
  const map: Record<string, any> = {
    'FICTION': 'blue', 'NON_FICTION': 'emerald', 'SCIENCE': 'purple',
    'FANTASY': 'orange', 'HISTORY': 'yellow', 'ART': 'pink'
  }
  return map[genre] || 'gray'
}
</script>

<style scoped>
/* 终极强制锁定逻辑 */
:deep(.table-wrapper-fix) {
  /* 确保这个包装器是 sticky 的参考系 */
  position: absolute !important;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

:deep(table) {
  border-collapse: separate !important; /* 正确属性 */
  border-spacing: 0 !important;
}

:deep(thead) {
  position: sticky !important;
  top: 0 !important;
  z-index: 50 !important;
}

:deep(thead th) {
  position: sticky !important;
  top: 0 !important;
  /* 必须使用不透明背景色，否则滚动时文字会重叠在一起 */
  background-color: #f8fafc !important; 
  border-bottom: 2px solid #10b981 !important;
}

.dark :deep(thead th) {
  background-color: #0f172a !important;
  border-bottom: 2px solid #059669 !important;
}
</style>
