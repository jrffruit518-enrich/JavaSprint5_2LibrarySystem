<template>
  <UContainer class="py-6">
    <div class="space-y-6">
      <div class="flex justify-between items-center bg-gray-50 p-4 rounded-lg border border-gray-200 shadow-sm">
        <div>
          <h1 class="text-xl font-bold text-primary">Public Library Catalog</h1>
          <p class="text-sm text-gray-500">Explore our collection. Please sign in to borrow books.</p>
        </div>
        <UButton
          to="/register"
          icon="i-heroicons-arrow-right-on-rectangle"
          label="Sign In to Borrow"
          color="primary"
          variant="solid"
        />
      </div>

      <BookDetailPanel
        :book="selectedBook"
        mode="view"
        role="guest"
      />

      <div class="grid grid-cols-12 gap-6">
        <aside class="col-span-12 md:col-span-3 space-y-6">
          <UCard class="border-2 border-primary/5 shadow-sm">
            <div class="space-y-4">
              <h3 class="font-bold text-lg border-b pb-2 italic text-primary text-center">
                Search Filters
              </h3>

              <div class="space-y-2">
                <span class="text-xs font-semibold uppercase text-gray-400">Category</span>
                <USelect
                  v-model="filter.category"
                  :options="['All', 'FICTION', 'NON_FICTION', 'SCIENCE', 'HISTORY', 'ART', 'FANTASY']"
                />
              </div>

              <div class="space-y-2">
                <span class="text-xs font-semibold uppercase text-gray-400">Author</span>
                <UInput v-model="filter.author" placeholder="Search author..." icon="i-heroicons-user" />
              </div>

              <div class="space-y-2">
                <span class="text-xs font-semibold uppercase text-gray-400">Min Rating</span>
                <USelect
                  v-model.number="filter.minRating"
                  :options="[
                    { label: 'Any Rating', value: 0 },
                    { label: '4.0+ Stars', value: 4 },
                    { label: '4.5+ Stars', value: 4.5 }
                  ]"
                />
              </div>

              <UButton
                block
                variant="ghost"
                icon="i-heroicons-arrow-path"
                color="gray"
                @click="resetFilters"
              >
                Reset Filters
              </UButton>
            </div>
          </UCard>
        </aside>

        <main class="col-span-12 md:col-span-9">
          <UCard class="border-2 border-primary/5 shadow-sm">
            <template #header>
              <div class="flex justify-between items-center px-4 py-2">
                <h2 class="text-lg font-bold text-gray-700">Available Inventory</h2>
                <UInput 
                  v-model="quickSearch" 
                  icon="i-heroicons-magnifying-glass" 
                  placeholder="Filter by title..." 
                  class="w-64"
                />
              </div>
            </template>

            <BookTable 
              :data="filteredBooks" 
              :loading="status === 'pending'" 
              role="user"
              @view="handleView"
            />
          </UCard>
        </main>
      </div>
    </div>
  </UContainer>
</template>

<script setup lang="ts">
/**
 * 图书馆项目 - Guest Books Catalog (Public View)
 * Jules v2.6 - Large Panel Edition
 */
import { type Book, createEmptyBook } from '~/types/book'

// 1. 数据请求 - 访客无需鉴权，直接获取
const { data: books, status } = await useApi<Book[]>('/books')

// 2. 状态管理
const selectedBook = ref<Book>(createEmptyBook())
const quickSearch = ref('')
const filter = reactive({ category: 'All', author: '', minRating: 0 })

// 3. 核心交互：点击查看
const handleView = (book: Book) => {
  selectedBook.value = { ...book }
  
  // English Comment: Scroll to top to focus on the detail panel with the cover
  if (import.meta.client) {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// 4. 筛选逻辑
const filteredBooks = computed(() => {
  const list = (unref(books) || []) as Book[]
  return list.filter((book: Book) => {
    const matchCategory = filter.category === 'All' || book.bookGenre === filter.category
    const matchAuthor = (book.author || '').toLowerCase().includes(filter.author.toLowerCase())
    const matchRating = (book.rating || 0) >= filter.minRating
    const matchSearch = (book.title || '').toLowerCase().includes(quickSearch.value.toLowerCase())
    return matchCategory && matchAuthor && matchRating && matchSearch
  })
})

const resetFilters = () => {
  filter.category = 'All'; filter.author = ''; filter.minRating = 0; quickSearch.value = '';
}
</script>
