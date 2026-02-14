<template>
  <div class="w-full space-y-6 animate-spring-in">
    <div class="flex justify-between items-center bg-slate-900 text-white p-6 rounded-xl shadow-xl border border-slate-800">
      <div class="flex items-center gap-4">
        <div class="bg-emerald-500/20 p-3 rounded-lg">
          <UIcon name="i-heroicons-building-library-solid" class="w-8 h-8 text-emerald-500" />
        </div>
        <div>
          <h1 class="text-2xl font-black leading-tight tracking-tighter uppercase">
            Library <span class="text-emerald-500">Catalog</span>
          </h1>
          <p class="text-sm text-gray-400 font-medium tracking-wide">Explore our collection and manage your readings.</p>
        </div>
      </div>
      
      <div v-if="userStatus" class="flex items-center gap-4 bg-white/5 p-2 px-4 rounded-xl border border-white/10">
        <div class="text-right">
          <p class="text-[10px] font-black text-slate-400 uppercase tracking-widest">Borrowing Limit</p>
          <p class="font-black text-emerald-400 text-lg leading-none">
            {{ userStatus.borrowCount ?? 0 }} <span class="text-slate-500 text-xs">/ 10</span>
          </p>
        </div>
        <UIcon name="i-heroicons-bookmark-square-solid" class="w-8 h-8 text-emerald-500/50" />
      </div>
    </div>

    <div class="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm flex flex-wrap items-end gap-4">
      <UFormGroup label="Quick Search" class="flex-1 min-w-[200px]" size="sm">
        <UInput 
          v-model="quickSearch" 
          icon="i-heroicons-magnifying-glass" 
          placeholder="Search title or author..." 
          input-class="font-bold"
        />
      </UFormGroup>

      <UFormGroup label="Category" size="sm" class="w-48">
        <USelectMenu 
          v-model="filter.category" 
          :options="['All', 'FICTION', 'NON_FICTION', 'TECH', 'HISTORY', 'ART', 'FANTASY']" 
        />
      </UFormGroup>

      <UButton 
        icon="i-heroicons-arrow-path" 
        color="gray" 
        variant="soft" 
        size="sm" 
        class="mb-[2px] font-bold" 
        @click="resetFilters"
      >
        Reset
      </UButton>
    </div>

    <div class="grid grid-cols-12 gap-6 items-start">
      
      <main class="col-span-12 lg:col-span-9">
        <UCard class="border-none shadow-xl ring-1 ring-gray-200 dark:ring-gray-700 overflow-hidden" :ui="{ body: { padding: 'p-0' } }">
          <div class="h-[calc(100vh-420px)] overflow-hidden">
            <BookTable 
              :data="filteredBooks" 
              :loading="status === 'pending'" 
              role="user"
              :selected-id="selectedBook.id"
              @view="handleView"
            />
          </div>
        </UCard>
      </main>

      <aside class="col-span-12 lg:col-span-3 sticky top-6">
        <div class="h-[calc(100vh-160px)] side-panel-deep-text">
          <BookDetailPanel
            :book="selectedBook"
            :mode="panelMode"
            role="user"
            @borrow="handleBorrow"
            @close="resetSelection"
          />
        </div>
      </aside>

    </div>
  </div>
</template>

<script setup lang="ts">
import { type Book, createEmptyBook } from '~/types/book'
const toast = useToast()

definePageMeta({ layout: 'user', middleware: 'auth' })

const selectedBook = ref<Book>(createEmptyBook())
const panelMode = ref<'view' | 'edit' | 'add'>('view')
const quickSearch = ref('')
const filter = reactive({ category: 'All' })

const userCookie = useCookie<any>('user-data')
const userId = computed(() => userCookie.value?.id)

// --- 数据获取 ---
const { data: books, status, refresh: refreshBooks } = await useApi<Book[]>('/books')
const { data: userStatus, refresh: refreshStatus } = await useApi<any>(
  () => userId.value ? `/borrowings/${userId.value}/status` : null
)

// --- 交互逻辑 ---
const handleView = (book: Book) => {
  selectedBook.value = { ...book }
  panelMode.value = 'view'
}

const resetSelection = () => {
  selectedBook.value = createEmptyBook()
}

const handleBorrow = async (book: Book) => {
  const toastStyle = { position: 'top-0 bottom-auto' as const }

  if (userStatus.value && !userStatus.value.canBorrow) {
    toast.add({
      title: 'Action Denied',
      description: userStatus.value.hasOverdue ? 'Please return overdue books first!' : 'Borrowing limit reached (10).',
      color: 'rose',
      icon: 'i-heroicons-x-circle-solid',
      ui: toastStyle
    })
    return
  }

  try {
    await $fetch(`/api/borrowings/borrow/${book.id}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${useCookie('auth_token').value}` }
    })
    
    toast.add({
      title: 'Success!',
      description: `"${book.title}" added to your collection.`,
      color: 'emerald',
      icon: 'i-heroicons-check-circle-solid',
      ui: toastStyle
    })
    
    await Promise.all([refreshBooks(), refreshStatus()])
  } catch (err: any) {
    const errorMsg = err.data?.message || 'The request could not be completed.'
    toast.add({ title: 'Borrow Failed', description: errorMsg, color: 'rose', ui: toastStyle })
  }
}

// --- 过滤逻辑 ---
const filteredBooks = computed(() => {
  const list = (unref(books) || []) as Book[]
  return list.filter((b) => {
    const s = quickSearch.value.toLowerCase()
    const matchSearch = (b.title || '').toLowerCase().includes(s) || (b.author || '').toLowerCase().includes(s)
    const matchCat = filter.category === 'All' || b.bookGenre === filter.category
    return matchCat && matchSearch
  })
})

const resetFilters = () => { filter.category = 'All'; quickSearch.value = '' }
</script>

<style scoped>
/* 深度字体策略：确保详情面板文字足够清晰 */
.side-panel-deep-text :deep(label) {
  @apply font-black text-slate-900 dark:text-white uppercase text-[11px] tracking-wider !important;
}

.side-panel-deep-text :deep(p),
.side-panel-deep-text :deep(span) {
  @apply font-bold text-slate-800 dark:text-slate-100 !important;
}

/* 移除导致显示不全的局部滚动限制 */
:deep(.table-fixed-header thead th) {
  position: sticky !important;
  top: 0 !important;
  z-index: 20 !important;
  @apply bg-white dark:bg-slate-900 border-b-2 border-emerald-600 font-black text-slate-900 dark:text-white uppercase text-[10px] tracking-widest !important;
}
</style>