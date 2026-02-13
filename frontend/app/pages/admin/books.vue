<template>
  <UContainer class="py-6">
    <div class="space-y-6">
      <div class="flex justify-between items-center bg-gray-900 text-white p-4 rounded-lg shadow-md">
        <div>
          <h1 class="text-xl font-bold flex items-center gap-2">
            <UIcon name="i-heroicons-wrench-screwdriver" />
            Inventory Management
          </h1>
          <p class="text-xs text-gray-400">Add, update or remove books from the library system.</p>
        </div>
        <UButton
          color="green"
          icon="i-heroicons-plus-circle"
          label="Add New Book"
          @click="prepareAdd"
        />
      </div>

      <BookDetailPanel
        :book="selectedBook"
        :mode="panelMode"
        role="admin"
        @save="handleSave"
        @change-mode="(m) => panelMode = m"
      />

      <UCard class="border-2 border-primary/10 shadow-sm">
        <template #header>
          <div class="flex justify-between items-center px-4 py-2">
            <h2 class="text-lg font-bold text-gray-700">Database Records</h2>
            <div class="flex gap-4">
              <UInput
                v-model="quickSearch"
                icon="i-heroicons-magnifying-glass"
                placeholder="Search by title or author..."
                class="w-80"
              />
            </div>
          </div>
        </template>

        <BookTable 
          :data="filteredBooks" 
          :loading="status === 'pending'" 
          role="admin"
          @view="handleView"
          @delete="handleDelete"
        />
      </UCard>
    </div>
  </UContainer>
</template>

<script setup lang="ts">
/**
 * 图书馆项目 - Admin Books Management (Jules Standard v2.6)
 * 采用“顶部详情/编辑面板 + 底部数据表”的高效布局
 */
import { type Book, createEmptyBook } from '~/types/book'

definePageMeta({
  layout: 'admin',
  middleware: 'auth'
})

// --- 状态与配置 ---
const selectedBook = ref<Book>(createEmptyBook())
const panelMode = ref<'view' | 'edit' | 'add'>('view')
const quickSearch = ref('')

// --- 1. 数据获取 ---
const { data: books, status, refresh } = await useApi<Book[]>('/books')

// --- 2. 交互逻辑 ---

// 准备新增书籍
const prepareAdd = () => {
  selectedBook.value = createEmptyBook()
  panelMode.value = 'add'
  if (import.meta.client) window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 查看/编辑书籍详情
const handleView = (book: Book) => {
  selectedBook.value = { ...book }
  panelMode.value = 'view'
  if (import.meta.client) window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 保存逻辑 (新增或更新)
const handleSave = async (bookData: Book) => {
  const isNew = panelMode.value === 'add'
  const url = isNew ? '/books' : `/books/${bookData.id}`
  const method = isNew ? 'POST' : 'PUT'

  try {
    // English Comment: Call API with token from useApi/fetch
    await $fetch(`/api${url}`, {
      method,
      body: bookData,
      headers: { Authorization: `Bearer ${useCookie('auth_token').value}` }
    })

    alert(isNew ? 'New book created!' : 'Book updated successfully!')
    panelMode.value = 'view'
    await refresh() // 刷新列表
  } catch (err: any) {
    alert(err.data?.message || 'Failed to save changes.')
  }
}

// 删除逻辑
const handleDelete = async (book: Book) => {
  if (!confirm(`Are you sure you want to delete "${book.title}"?`)) return

  try {
    await $fetch(`/api/books/${book.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${useCookie('auth_token').value}` }
    })
    alert('Book deleted.')
    if (selectedBook.value.id === book.id) {
      selectedBook.value = createEmptyBook()
    }
    await refresh()
  } catch (err: any) {
    alert('Failed to delete book.')
  }
}

// 搜索过滤
const filteredBooks = computed(() => {
  const list = (unref(books) || []) as Book[]
  if (!quickSearch.value) return list
  return list.filter(b => 
    b.title.toLowerCase().includes(quickSearch.value.toLowerCase()) ||
    b.author.toLowerCase().includes(quickSearch.value.toLowerCase())
  )
})
</script>
