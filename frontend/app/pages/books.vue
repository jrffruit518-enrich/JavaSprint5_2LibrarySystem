发现问题了！是因为在 handleBorrow 的 catch 块结束后，缺少了一个闭合的大括号 }，导致后面的 resetFilters 函数嵌套出错，从而引发了语法错误。

我已经修复了括号嵌套问题，并确保代码结构完整。

📄 完整修复后的 pages/user/books.vue
代码段
<template>
  <UContainer class="py-6">
    <div class="space-y-6">
      <div
        v-if="selectedBook.id"
        class="animate-in fade-in slide-in-from-top-4 duration-300"
      >
        <BookDetailPanel
          :book="selectedBook"
          mode="view"
          role="user"
          @borrow="handleBorrow"
        />
      </div>

      <div class="grid grid-cols-12 gap-6">
        <aside class="col-span-12 md:col-span-3 space-y-6">
          <UCard
            class="border-2 border-primary/10 shadow-sm"
            :ui="{ body: 'p-6' }"
          >
            <div class="space-y-4">
              <h3 class="font-bold text-lg border-b pb-2 italic text-primary text-center">
                Refine Search
              </h3>

              <div class="space-y-2">
                <span class="text-xs font-semibold uppercase text-gray-400">Category</span>
                <USelect
                  v-model="filter.category"
                  :items="['All', 'FICTION', 'NON_FICTION', 'SCIENCE', 'HISTORY', 'ART', 'FANTASY']"
                />
              </div>

              <div class="space-y-2">
                <span class="text-xs font-semibold uppercase text-gray-400">Author</span>
                <UInput
                  v-model="filter.author"
                  placeholder="Search author..."
                  icon="i-lucide-user"
                />
              </div>

              <div class="space-y-2">
                <span class="text-xs font-semibold uppercase text-gray-400">Min Rating</span>
                <USelect
                  v-model.number="filter.minRating"
                  :items="[
                    { label: 'Any Rating', value: 0 },
                    { label: '4.0+ Stars', value: 4 },
                    { label: '4.5+ Stars', value: 4.5 }
                  ]"
                />
              </div>

              <UButton
                block
                variant="subtle"
                icon="i-lucide-rotate-ccw"
                color="neutral"
                @click="resetFilters"
              >
                Reset Filters
              </UButton>
            </div>
          </UCard>
        </aside>

        <main class="col-span-12 md:col-span-9">
          <UCard
            class="border-2 border-primary/10 shadow-sm"
            :ui="{ body: 'p-6' }"
          >
            <template #header>
              <div class="flex justify-between items-center px-4 py-2">
                <h2 class="text-xl font-bold">
                  Library Catalog
                </h2>
                <UInput
                  v-model="quickSearch"
                  icon="i-lucide-search"
                  placeholder="Quick search title..."
                />
              </div>
            </template>

            <div
              v-if="status === 'pending'"
              class="py-10 text-center"
            >
              <UIcon
                name="i-lucide-loader-2"
                class="animate-spin w-8 h-8 mx-auto text-primary"
              />
            </div>

            <div v-else>
              <BookTable
                :data="filteredBooks"
                role="guest"
                @view="handleView"
              />
            </div>
          </UCard>
        </main>
      </div>
    </div>
  </UContainer>
</template>

<script setup lang="ts">
/**
 * 图书馆项目 (Library Project) - User Catalog & Borrowing
 */
import type { Book } from '~/types/book'

/* 1. State Management */
const selectedBook = ref<Book>({} as Book)
const quickSearch = ref('')
const filter = reactive({
  category: 'All',
  author: '',
  minRating: 0
})

/* 2. Data Fetching */
const { data: books, status, refresh } = await useApi<Book[]>('/api/books')

/* 3. Computed: Filtering Logic */
const filteredBooks = computed(() => {
  if (!books.value) return []
  return books.value.filter((book: Book) => {
    const matchCategory = filter.category === 'All' || book.bookGenre === filter.category
    const matchAuthor = (book.author || '').toLowerCase().includes(filter.author.toLowerCase())
    const matchRating = (book.rating || 0) >= filter.minRating
    const matchSearch = (book.title || '').toLowerCase().includes(quickSearch.value.toLowerCase())
    return matchCategory && matchAuthor && matchRating && matchSearch
  })
})

/* 4. Action Handlers */
const handleView = (book: Book) => {
  selectedBook.value = { ...book }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleBorrow = async (book: Book) => {
  const token = useCookie('auth_token').value
  // English Comment: Borrowing requires valid user session/token
  if (!token) {
    alert('Please login first to borrow books.')
    return
  }

  try {
    // English Comment: Send borrowing request to Spring Boot backend
    await $fetch(`/api/books/${book.id}/borrow`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` }
    })

    alert(`Successfully borrowed "${book.title}"!`)
    await refresh() // Refresh to update stock count
    selectedBook.value = {} as Book // Close panel
  } catch (err: unknown) {
    // English Comment: Cast error to any to access response data properties
    const fetchError = err as any
    console.error('Borrow failed:', fetchError)
    alert(fetchError.data?.message || 'Failed to borrow the book. It might be out of stock.')
  }
} // 👈 这个闭合括号之前漏掉了

const resetFilters = () => {
  filter.category = 'All'
  filter.author = ''
  filter.minRating = 0
  quickSearch.value = ''
}

definePageMeta({
  layout: 'default'
})
</script>
