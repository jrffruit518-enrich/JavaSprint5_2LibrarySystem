<template>
  <div class="w-full space-y-6">
    <div class="flex justify-between items-center bg-slate-900 text-white p-6 rounded-xl shadow-xl border border-slate-800">
      <div class="flex items-center gap-4">
        <div class="bg-emerald-500/20 p-3 rounded-lg">
          <UIcon name="i-heroicons-wrench-screwdriver" class="w-8 h-8 text-emerald-500" />
        </div>
        <div>
          <h1 class="text-2xl font-black leading-tight tracking-tighter uppercase">Inventory <span class="text-emerald-500">Management</span></h1>
          <p class="text-sm text-gray-400 font-medium">Add, update or remove books from the database.</p>
        </div>
      </div>
      <UButton
        color="emerald"
        size="lg"
        icon="i-heroicons-plus-circle"
        label="Add New Book"
        class="font-black px-8 rounded-full shadow-lg"
        @click="prepareAdd"
      />
    </div>

    <div class="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm flex flex-wrap items-end gap-4">
      <UFormGroup label="Search Title/Author" class="flex-1 min-w-[200px]" size="sm">
        <UInput 
          v-model="filters.query" 
          icon="i-heroicons-magnifying-glass" 
          placeholder="Search..." 
          input-class="font-bold"
        />
      </UFormGroup>

      <UFormGroup label="Genre" size="sm" class="w-40">
        <USelect 
          v-model="filters.genre" 
          :options="[
            { label: 'All Genres', value: 'ALL' },
            { label: 'Fiction', value: 'FICTION' },
            { label: 'Tech', value: 'TECH' },
            { label: 'History', value: 'HISTORY' },
            { label: 'Science', value: 'SCIENCE' }
          ]" 
        />
      </UFormGroup>

      <UFormGroup label="Stock Status" size="sm" class="w-40">
        <USelect 
          v-model="filters.stock" 
          :options="[
            { label: 'All Status', value: 'ALL' },
            { label: 'In Stock', value: 'IN_STOCK' },
            { label: 'Out of Stock', value: 'OUT_OF_STOCK' }
          ]" 
        />
      </UFormGroup>

      <UFormGroup label="Min Rating" size="sm" class="w-24">
        <USelect v-model="filters.rating" :options="[0, 1, 2, 3, 4, 5]" />
      </UFormGroup>
      
      <UButton 
        icon="i-lucide-rotate-ccw" 
        color="gray" 
        variant="soft" 
        size="sm" 
        class="mb-[2px]" 
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
              role="admin"
              @view="handleView"
              @delete="handleDelete"
            />
          </div>
        </UCard>
      </main>

      <aside class="col-span-12 lg:col-span-3 sticky top-6">
        <div class="h-[calc(100vh-160px)] side-panel-deep-text">
          <BookDetailPanel 
            :book="selectedBook" 
            :mode="panelMode" 
            role="admin" 
            @save="handleSave"
            @change-mode="(m) => panelMode = m"
            @close="resetSelection" 
          />
        </div>
      </aside>

    </div>
  </div>
</template>

<script setup lang="ts">
import { type Book, createEmptyBook } from '~/types/book'

definePageMeta({
  layout: 'admin',
  middleware: 'auth'
})

// --- 状态与过滤器 ---
const selectedBook = ref<Book>(createEmptyBook())
const panelMode = ref<'view' | 'edit' | 'add'>('view')

const filters = reactive({
  query: '',
  genre: 'ALL',
  stock: 'ALL',
  rating: 0
})

const resetFilters = () => {
  filters.query = ''; filters.genre = 'ALL'; filters.stock = 'ALL'; filters.rating = 0
}

// --- 数据获取 ---
const { data: books, status, refresh } = await useApi<Book[]>('/books')

// --- 交互逻辑 ---

const prepareAdd = () => {
  selectedBook.value = createEmptyBook()
  panelMode.value = 'add'
}

const handleView = (book: Book) => {
  selectedBook.value = { ...book }
  panelMode.value = 'view'
}

const resetSelection = () => {
  selectedBook.value = createEmptyBook()
  panelMode.value = 'view'
}

const handleSave = async (bookData: Book) => {
  const isNew = panelMode.value === 'add'
  const url = isNew ? '/books' : `/books/${bookData.id}`
  const method = isNew ? 'POST' : 'PUT'

  try {
    await $fetch(`/api${url}`, {
      method,
      body: bookData,
      headers: { Authorization: `Bearer ${useCookie('auth_token').value}` }
    })

    alert(isNew ? 'New book created!' : 'Book updated successfully!')
    panelMode.value = 'view'
    await refresh()
  } catch (err: any) {
    alert(err.data?.message || 'Failed to save changes.')
  }
}

const handleDelete = async (book: Book) => {
  if (!confirm(`Are you sure you want to delete "${book.title}"?`)) return

  try {
    await $fetch(`/api/books/${book.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${useCookie('auth_token').value}` }
    })
    alert('Book deleted.')
    if (selectedBook.value.id === book.id) {
      resetSelection()
    }
    await refresh()
  } catch (err: any) {
    alert('Failed to delete book.')
  }
}

// --- 复合过滤逻辑 ---
const filteredBooks = computed(() => {
  const list = (unref(books) || []) as Book[]
  return list.filter(b => {
    // 1. 标题/作者 搜索
    const matchQuery = !filters.query || 
      (b.title || '').toLowerCase().includes(filters.query.toLowerCase()) ||
      (b.author || '').toLowerCase().includes(filters.query.toLowerCase())
    
    // 2. 分类过滤 (对应 bookGenre)
    const matchGenre = filters.genre === 'ALL' || b.bookGenre === filters.genre
    
    // 3. 库存过滤 (对应 availableStock)
    const stockCount = b.availableStock ?? 0
    const matchStock = filters.stock === 'ALL' || 
      (filters.stock === 'IN_STOCK' ? stockCount > 0 : stockCount === 0)
    
    // 4. 评分过滤
    const matchRating = (b.rating ?? 0) >= filters.rating

    return matchQuery && matchGenre && matchStock && matchRating
  })
})
</script>

<style scoped>
:deep(.book-table-root) {
  height: 100%;
}

/* 侧导航字体深度加重策略 */
.side-panel-deep-text :deep(label) {
  @apply font-black text-slate-900 dark:text-white uppercase text-[11px] tracking-wider !important;
}

.side-panel-deep-text :deep(span), 
.side-panel-deep-text :deep(p),
.side-panel-deep-text :deep(div) {
  @apply font-bold text-slate-800 dark:text-slate-100 !important;
}

.side-panel-deep-text :deep(input),
.side-panel-deep-text :deep(textarea),
.side-panel-deep-text :deep(select) {
  @apply font-black border-slate-300 dark:border-slate-600 !important;
}
</style>
