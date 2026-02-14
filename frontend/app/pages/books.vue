<template>
  <div class="w-full space-y-6 animate-spring-in">
    
    <div class="flex justify-between items-center glass-effect p-8 rounded-2xl border-none relative overflow-hidden group">
      <div class="absolute -right-20 -top-20 w-64 h-64 bg-emerald-500/10 blur-[100px] rounded-full"></div>
      
      <div class="flex items-center gap-6 relative z-10">
        <div class="bg-emerald-500 p-4 rounded-2xl shadow-lg shadow-emerald-500/20">
          <UIcon name="i-heroicons-building-library-solid" class="w-8 h-8 text-white" />
        </div>
        <div>
          <h1 class="text-3xl font-black text-slate-900 dark:text-white leading-tight tracking-tighter uppercase">
            Public <span class="text-emerald-600 dark:text-emerald-400">Catalog</span>
          </h1>
          <p class="text-[11px] font-black text-slate-600 dark:text-slate-300 uppercase tracking-[0.2em] mt-1">
            Browse our entire collection. Sign in to unlock borrowing.
          </p>
        </div>
      </div>
      <UButton 
        to="/register" 
        size="xl" 
        icon="i-heroicons-arrow-right-on-rectangle-solid" 
        label="Sign In to Borrow" 
        class="btn-glow font-black px-10 rounded-xl shadow-lg transition-all active:scale-95" 
        color="emerald"
      />
    </div>

    <div class="glass-effect p-6 rounded-2xl border-none flex flex-wrap items-end gap-6 shadow-xl">
      <UFormGroup 
        label="Book Title" 
        class="flex-1 min-w-[200px]" 
        :ui="{ label: { base: 'text-[10px] font-black uppercase text-slate-700 dark:text-slate-200 tracking-widest' } }"
      >
        <UInput v-model="quickSearch" icon="i-heroicons-magnifying-glass" placeholder="Search title..." />
      </UFormGroup>
      
      <UFormGroup 
        label="Author" 
        class="w-44" 
        :ui="{ label: { base: 'text-[10px] font-black uppercase text-slate-700 dark:text-slate-200 tracking-widest' } }"
      >
        <UInput v-model="filter.author" icon="i-heroicons-user-solid" placeholder="Author..." />
      </UFormGroup>

      <UFormGroup 
        label="Genre" 
        class="w-40" 
        :ui="{ label: { base: 'text-[10px] font-black uppercase text-slate-700 dark:text-slate-200 tracking-widest' } }"
      >
        <USelect v-model="filter.category" :options="['All', 'FICTION', 'NON_FICTION', 'SCIENCE', 'HISTORY', 'ART', 'FANTASY']" />
      </UFormGroup>

      <UFormGroup 
        label="Min Rating" 
        class="w-32" 
        :ui="{ label: { base: 'text-[10px] font-black uppercase text-slate-700 dark:text-slate-200 tracking-widest' } }"
      >
        <USelect 
          v-model.number="filter.minRating" 
          icon="i-heroicons-star-solid"
          :options="[
            { label: 'All', value: 0 },
            { label: '1.0+', value: 1 },
            { label: '2.0+', value: 2 },
            { label: '3.0+', value: 3 },
            { label: '4.0+', value: 4 }
          ]" 
        />
      </UFormGroup>

      <UButton 
        icon="i-heroicons-arrow-path-solid" 
        color="gray" 
        variant="ghost" 
        class="mb-[2px] hover:bg-emerald-500/10 hover:text-emerald-500 font-bold" 
        @click="resetFilters"
      >
        Reset
      </UButton>
    </div>

    <div class="grid grid-cols-12 gap-8 items-start">
      
      <main class="col-span-12 lg:col-span-9">
        <UCard 
          class="glass-effect border-none shadow-none overflow-hidden" 
          :ui="{ body: { padding: 'p-0' }, rounded: 'rounded-2xl' }"
        >
          <div class="h-[calc(100vh-420px)] overflow-hidden">
            <BookTable 
              :data="filteredBooks" 
              :loading="status === 'pending'" 
              role="guest"
              @view="handleView"
            />
          </div>
        </UCard>
      </main>

      <aside class="col-span-12 lg:col-span-3 sticky top-6">
        <div class="h-[calc(100vh-180px)] glass-effect rounded-2xl overflow-hidden shadow-2xl">
          <BookDetailPanel :book="selectedBook" mode="view" role="guest" @close="resetSelection" />
        </div>
      </aside>

    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * Public Books Page (Jules v4.3 - High Contrast Fix)
 * Logic: 100% Intact.
 * UI: Enhanced visibility for text on glass backgrounds.
 */
import { type Book, createEmptyBook } from '~/types/book'

const { data: books, status } = await useApi<Book[]>('/books')
const selectedBook = ref<Book>(createEmptyBook())
const quickSearch = ref('')
const filter = reactive({ category: 'All', author: '', minRating: 0 })

// 事件处理逻辑
const handleView = (book: Book) => { selectedBook.value = { ...book } }
const resetSelection = () => { selectedBook.value = createEmptyBook() }
const resetFilters = () => {
  filter.category = 'All'; filter.author = ''; filter.minRating = 0; quickSearch.value = '';
}

// 筛选逻辑核对
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
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { 
  background: rgba(16, 185, 129, 0.2);
  border-radius: 10px;
}

.animate-spring-in {
  animation: spring-in 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes spring-in {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
