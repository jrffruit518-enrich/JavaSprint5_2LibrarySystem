<template>
  <div class="w-full space-y-6 animate-spring-in">
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
              @delete="openDeleteConfirm" 
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
            :loading="isSaving"
            @save="handleSave"
            @change-mode="(m) => panelMode = m"
            @close="resetSelection" 
          />
        </div>
      </aside>
    </div>

    <UModal v-model="isDeleteModalOpen">
      <UCard :ui="{ ring: '', divide: 'divide-y divide-gray-100 dark:divide-gray-800' }">
        <template #header>
          <div class="flex items-center gap-3 text-rose-500">
            <UIcon name="i-heroicons-exclamation-triangle-solid" class="w-6 h-6" />
            <span class="font-black uppercase tracking-widest">Confirm Deletion</span>
          </div>
        </template>

        <div class="p-4">
          <p class="text-sm font-bold text-slate-600 dark:text-slate-300">
            Are you sure you want to delete <span class="text-rose-600">"{{ bookToDelete?.title }}"</span>? 
            This action will permanently remove the record.
          </p>
        </div>

        <template #footer>
          <div class="flex justify-end gap-3">
            <UButton color="gray" variant="ghost" label="Cancel" @click="isDeleteModalOpen = false" />
            <UButton color="rose" label="Delete Book" :loading="isDeleting" @click="handleDelete" />
          </div>
        </template>
      </UCard>
    </UModal>
  </div>
</template>

<script setup lang="ts">
/**
 * Admin Inventory Management - UI Upgrade v2.3 (No Native Alerts)
 * 1. 彻底消灭原生 confirm 和 alert。
 * 2. 保持完整过滤逻辑与样式策略。
 */
import { type Book, createEmptyBook } from '~/types/book'

definePageMeta({
  layout: 'admin',
  middleware: 'auth'
})

const toast = useToast()
const isSaving = ref(false)
const isDeleting = ref(false)
const isDeleteModalOpen = ref(false)
const bookToDelete = ref<Book | null>(null)

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
  
  isSaving.value = true
  try {
    await $fetch(`/api${url}`, {
      method,
      body: bookData,
      headers: { Authorization: `Bearer ${useCookie('auth_token').value}` }
    })

    toast.add({
      title: isNew ? 'Book Created' : 'Update Successful',
      description: `"${bookData.title}" has been saved.`,
      color: 'emerald',
      icon: 'i-heroicons-check-circle'
    })

    panelMode.value = 'view'
    await refresh()
  } catch (err: any) {
    toast.add({
      title: 'Save Failed',
      description: err.data?.message || 'Database connection error.',
      color: 'rose',
      icon: 'i-heroicons-exclamation-triangle'
    })
  } finally {
    isSaving.value = false
  }
}

// 弹出删除确认
const openDeleteConfirm = (book: Book) => {
  bookToDelete.value = book
  isDeleteModalOpen.value = true
}

// 执行删除逻辑
const handleDelete = async () => {
  if (!bookToDelete.value) return
  
  isDeleting.value = true
  try {
    await $fetch(`/api/books/${bookToDelete.value.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${useCookie('auth_token').value}` }
    })
    
    toast.add({
      title: 'Book Removed',
      description: `"${bookToDelete.value.title}" was deleted.`,
      color: 'rose',
      icon: 'i-heroicons-trash'
    })

    if (selectedBook.value.id === bookToDelete.value.id) {
      resetSelection()
    }
    isDeleteModalOpen.value = false
    await refresh()
  } catch (err: any) {
    toast.add({
      title: 'Deletion Failed',
      color: 'rose',
      icon: 'i-heroicons-x-mark'
    })
  } finally {
    isDeleting.value = false
    bookToDelete.value = null
  }
}

// --- 复合过滤逻辑 (保持完整) ---
const filteredBooks = computed(() => {
  const list = (unref(books) || []) as Book[]
  return list.filter(b => {
    const matchQuery = !filters.query || 
      (b.title || '').toLowerCase().includes(filters.query.toLowerCase()) ||
      (b.author || '').toLowerCase().includes(filters.query.toLowerCase())
    
    const matchGenre = filters.genre === 'ALL' || b.bookGenre === filters.genre
    
    const stockCount = b.availableStock ?? 0
    const matchStock = filters.stock === 'ALL' || 
      (filters.stock === 'IN_STOCK' ? stockCount > 0 : stockCount === 0)
    
    const matchRating = (b.rating ?? 0) >= filters.rating

    return matchQuery && matchGenre && matchStock && matchRating
  })
})
</script>

<style scoped>
.animate-spring-in {
  animation: spring-in 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes spring-in {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

:deep(.book-table-root) {
  height: 100%;
}

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