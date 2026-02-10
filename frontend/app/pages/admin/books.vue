<template>
  <div class="space-y-6">
    <AppBreadcrumb current-page-title="Book Management" />

    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
          Book Management
        </h1>
        <p class="text-sm text-gray-500">
          Direct administrative control over the library inventory.
        </p>
      </div>

      <UButton
        label="Add New Book"
        icon="i-lucide-plus"
        color="primary"
        @click="openAddMode"
      />
    </div>

    <BookDetailPanel
      :book="selectedBook"
      :mode="currentMode"
      role="admin"
      @save="handleSave"
      @change-mode="(val) => currentMode = val"
    />

    <UAlert
      v-if="error"
      icon="i-lucide-circle-alert"
      color="error"
      variant="soft"
      title="Fetch Error"
      :description="error.message || 'Failed to load books from server.'"
    />

    <UCard
      v-else
      class="mb-8 border-2 border-primary/20 shadow-lg"
      :ui="{ body: 'p-6' }"
    >
      <div
        v-if="status === 'pending'"
        class="py-10 text-center"
      >
        <UIcon
          name="i-lucide-loader-2"
          class="animate-spin w-8 h-8 mx-auto text-primary"
        />
      </div>

      <BookTable
        v-else
        :data="books || []"
        role="admin"
        @view="handleView"
        @edit="handleEdit"
        @delete="handleDelete"
      />
    </UCard>
  </div>
</template>

<script setup lang="ts">
/**
 * 图书馆项目 (Library Project) - Admin Management Page
 */
import type { Book } from '~/types/book'

/* 1. Page State */
const currentMode = ref<'view' | 'edit' | 'add'>('view')
const selectedBook = ref<Book>({} as Book)

/* 2. Data Fetching */
const { data: books, refresh, status, error } = await useApi<Book[]>('/api/books')

/* 3. Helper: Smooth Scroll */
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/* 4. Action Handlers */
const handleView = (book: Book) => {
  selectedBook.value = { ...book }
  currentMode.value = 'view'
  scrollToTop()
}

const handleEdit = (book: Book) => {
  selectedBook.value = { ...book }
  currentMode.value = 'edit'
  scrollToTop()
}

const openAddMode = () => {
  // English Comment: Initialize empty book to trigger the 'add' form in the panel
  selectedBook.value = {
    title: '',
    author: '',
    isbn: '',
    bookGenre: 'FICTION',
    rating: 0,
    availableStock: 1,
    description: '',
    coverImageUrl: ''
  } as Book
  currentMode.value = 'add'
  scrollToTop()
}

const handleSave = async (updatedBook: Book) => {
  const token = useCookie('auth_token').value
  try {
    const isEdit = !!updatedBook.id
    const url = isEdit ? `/api/books/${updatedBook.id}` : `/api/books`

    // English Comment: Standard API call for Create/Update
    await $fetch(url, {
      method: isEdit ? 'PUT' : 'POST',
      body: updatedBook,
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    await refresh()

    // English Comment: Reset mode after successful save
    if (isEdit) {
      selectedBook.value = { ...updatedBook }
      currentMode.value = 'view'
    } else {
      selectedBook.value = {} as Book
      currentMode.value = 'view'
    }
  } catch (err) {
    console.error('Save operation failed:', err)
  }
}

const handleDelete = async (book: Book) => {
  const token = useCookie('auth_token').value
  if (confirm(`Are you sure you want to delete "${book.title}"?`)) {
    try {
      await $fetch(`/api/books/${book.id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` }
      })
      if (selectedBook.value.id === book.id) {
        selectedBook.value = {} as Book
      }
      await refresh()
    } catch (err) {
      console.error('Delete operation failed:', err)
    }
  }
}

definePageMeta({
  layout: 'admin'
})
</script>
