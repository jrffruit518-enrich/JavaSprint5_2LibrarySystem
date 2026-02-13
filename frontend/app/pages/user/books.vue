<template>
  <UContainer class="py-6">
    <div class="space-y-6">
      <div class="flex justify-between items-center bg-primary/5 p-4 rounded-lg border border-primary/20">
        <div>
          <h1 class="text-xl font-bold text-green-500">Library Catalog</h1>
          <p class="text-sm text-gray-500">Select a book to view details and manage your collection.</p>
        </div>
        <UBadge
          v-if="userStatus"
          variant="subtle"
          :color="userStatus.canBorrow ? 'primary' : 'orange'"
          size="lg"
          class="px-4 py-2"
        >
          <UIcon name="i-heroicons-bookmark" class="mr-1" />
          Borrowing Quota: {{ userStatus.borrowCount ?? 0 }} / 10 Books
        </UBadge>
      </div>

      <BookDetailPanel
        :book="selectedBook"
        :mode="panelMode"
        role="user"
        @borrow="handleBorrow"
      />

      <div class="grid grid-cols-12 gap-6">
        <aside class="col-span-12 md:col-span-3 space-y-6">
          <UCard class="border-2 border-primary/10 shadow-sm">
            <div class="space-y-4">
              <h3 class="font-bold text-lg border-b pb-2 italic text-primary text-center">Refine Search</h3>
              
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
                  icon="i-heroicons-user"
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
                variant="soft"
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
          <UCard class="border-2 border-primary/10 shadow-sm">
            <template #header>
              <div class="flex justify-between items-center px-4 py-2">
                <h2 class="text-lg font-bold text-gray-700">Inventory List</h2>
                <UInput
                  v-model="quickSearch"
                  icon="i-heroicons-magnifying-glass"
                  placeholder="Quick search title..."
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
 * 图书馆项目 - User Books Catalog (Jules Standard v2.5 - Fixed Layout)
 */
import { type Book, createEmptyBook } from '~/types/book'

definePageMeta({
  layout: 'user',
  middleware: 'auth'
})

// --- 状态控制 ---
const selectedBook = ref<Book>(createEmptyBook()) // 初始状态为空书，Panel 会显示提示
const panelMode = ref<'view' | 'edit' | 'add'>('view')
const quickSearch = ref('')
const filter = reactive({ category: 'All', author: '', minRating: 0 })

// 获取用户 Cookie
const userCookie = useCookie<any>('user-data')
const userId = computed(() => userCookie.value?.id)

// --- 1. 数据获取 ---

// 获取图书列表
const { data: books, status, refresh: refreshBooks } = await useApi<Book[]>('/books')

// 获取借阅状态 (Jules Fix: 使用匿名函数传递动态路径以修复 TS 类型报错)
const { data: userStatus, refresh: refreshStatus } = await useApi<any>(
  () => userId.value ? `/borrowings/${userId.value}/status` : null
)

// --- 2. 交互逻辑 ---

// 当点击表格中的“眼睛”图标或查看按钮时
const handleView = (book: Book) => {
  selectedBook.value = { ...book }
  panelMode.value = 'view'
  
  // 滚动回顶部，让用户立即看到选中的书籍大封面和详情
  if (import.meta.client) {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// 处理借阅动作
const handleBorrow = async (book: Book) => {
  if (userStatus.value && !userStatus.value.canBorrow) {
    const reason = userStatus.value.hasOverdue 
      ? 'Please return overdue books first!' 
      : 'You have reached the maximum borrow limit.'
    alert(reason)
    return
  }

  try {
    await $fetch(`/api/borrowings/borrow/${book.id}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${useCookie('auth_token').value}` }
    })

    alert(`Successfully borrowed: ${book.title}`)
    
    // 串行同步刷新：更新图书库存和用户借阅配额
    await Promise.all([
      refreshBooks(),
      refreshStatus()
    ])
  } catch (err: any) {
    alert(err.data?.message || 'Failed to process borrowing request.')
  }
}

// 筛选逻辑
const filteredBooks = computed(() => {
  const list = (unref(books) || []) as Book[]
  return list.filter((b) => {
    const matchCat = filter.category === 'All' || b.bookGenre === filter.category
    const matchAuth = (b.author || '').toLowerCase().includes(filter.author.toLowerCase())
    const matchRate = (b.rating || 0) >= filter.minRating
    const matchSearch = (b.title || '').toLowerCase().includes(quickSearch.value.toLowerCase())
    return matchCat && matchAuth && matchRate && matchSearch
  })
})

const resetFilters = () => {
  filter.category = 'All'; filter.author = ''; filter.minRating = 0; quickSearch.value = ''
}
</script>
