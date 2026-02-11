<template>
  <UContainer class="py-6">
    <div class="space-y-6">
      <div class="flex justify-between items-center bg-primary/5 p-4 rounded-lg border border-primary/20">
        <div>
          <h1 class="text-xl font-bold text-highlighted">
            Library Catalog
          </h1>
          <p class="text-sm text-muted">
            Browse books and manage your personal collection.
          </p>
        </div>
        <UBadge
          variant="subtle"
          color="primary"
          size="lg"
          class="px-4 py-2"
        >
          Borrowing Quota: {{ borrowedCount }} / 10 Books
        </UBadge>
      </div>

      <div
        v-if="selectedBook && selectedBook.id"
        class="animate-in fade-in slide-in-from-top-4 duration-300"
      >
        <BookDetailPanel
          :book="selectedBook"
          mode="view"
          role="user"
          @close="selectedBook = {}"
          @action="handleBorrow"
        />
      </div>

      <div class="grid grid-cols-12 gap-6">
        <aside class="col-span-12 md:col-span-3 space-y-6">
          <UCard class="border-2 border-primary/10 shadow-sm">
            <div class="space-y-4">
              <h3 class="font-bold text-lg border-b pb-2 italic text-primary text-center">
                Refine Search
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
                  :options="[
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
          <UCard class="border-2 border-primary/10 shadow-sm">
            <template #header>
              <div class="flex justify-between items-center px-4 py-2">
                <h2 class="text-lg font-bold">
                  Available Books
                </h2>
                <UInput
                  v-model="quickSearch"
                  icon="i-lucide-search"
                  placeholder="Quick search title..."
                  class="w-64"
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
                role="user"
                @view="handleView"
                @borrow="handleBorrow"
              />
            </div>
          </UCard>
        </main>
      </div>
    </div>
  </UContainer>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'

/**
 * Library Project - User Books Page
 * Business Rule: Max 10 books per user
 */

// Define Layout and Auth
definePageMeta({
  layout: 'user',
  middleware: 'auth'
})

/* 1. State Management */
const selectedBook = ref<any>({})
const quickSearch = ref('')
const filter = reactive({
  category: 'All',
  author: '',
  minRating: 0
})

/* 2. Data Fetching */
const { data: books, status, refresh } = await useApi<any[]>('/api/books')

/* 3. Computed Properties */
const filteredBooks = computed(() => {
  if (!books.value) return []
  return books.value.filter((book) => {
    // Aligned with backend field: bookGenre
    const matchCategory = filter.category === 'All' || book.bookGenre === filter.category
    const matchAuthor = (book.author || '').toLowerCase().includes(filter.author.toLowerCase())
    const matchRating = (book.rating || 0) >= filter.minRating
    const matchSearch = (book.title || '').toLowerCase().includes(quickSearch.value.toLowerCase())
    return matchCategory && matchAuthor && matchRating && matchSearch
  })
})

const borrowedCount = computed(() => {
  // English Comment: Calculate borrowed books based on status
  return books.value?.filter(b => b.status === 'BORROWED').length || 0
})

/* 4. Action Handlers */
const handleView = (book: any) => {
  selectedBook.value = { ...book }
  // Scroll to detail panel at the top
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleBorrow = async (book: any) => {
  // English Comment: Check quota before sending request
  if (borrowedCount.value >= 10) {
    alert('Borrowing limit reached (Max 10 books).')
    return
  }

  try {
    await useApi(`/api/books/${book.id}/borrow`, { method: 'POST' })
    alert(`Successfully borrowed "${book.title}"!`)
    selectedBook.value = {}
    await refresh()
  } catch (err: any) {
    console.error('Borrow operation failed:', err)
    alert(err.data?.message || 'Failed to borrow. Stock might be empty.')
  }
}

const resetFilters = () => {
  filter.category = 'All'
  filter.author = ''
  filter.minRating = 0
  quickSearch.value = ''
}
</script>
