<template>
  <div class="space-y-6">
    <AppBreadcrumb current-page-title="Book Management" />

    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-highlighted">
          Book Management
        </h1>
        <p class="text-muted text-sm">
          Add, update or remove books from the library collection.
        </p>
      </div>

      <UButton
        label="Add New Book"
        icon="i-lucide-plus"
        color="primary"
        @click="isAddModalOpen = true"
      />
    </div>

    <UCard>
      <BookTable
        :data="allBooks"
        role="admin"
        @edit="handleEdit"
        @delete="handleDelete"
      />
    </UCard>

    <UModal
      v-model:open="isAddModalOpen"
      title="Manage Book"
    >
      <template #content>
        <div class="p-4 text-center text-muted">
          Book Form will be here (Linked to your Backend later).
        </div>
      </template>
    </UModal>
  </div>
</template>

<script setup>
/* 使用管理员侧导航布局 */
definePageMeta({
  layout: 'admin'
})

/* 状态变量 */
const isAddModalOpen = ref(false)

/* 模拟全量图书数据 (Admin 会看到所有书) */
const allBooks = ref([
  { id: 1, title: 'The Great Gatsby', author: 'F. Scott Fitzgerald', category: 'Fiction', status: 'Available' },
  { id: 2, title: 'Clean Code', author: 'Robert C. Martin', category: 'Technology', status: 'Borrowed' },
  { id: 3, title: '1984', author: 'George Orwell', category: 'Fiction', status: 'Available' }
])

/* 处理编辑逻辑 */
const handleEdit = (book) => {
  console.log('Editing book:', book.title)
  /* 这里以后会弹出编辑表单，并填充当前数据 */
  alert(`Editing: ${book.title}`)
}

/* 处理删除逻辑 */
const handleDelete = (book) => {
  if (confirm(`Are you sure you want to delete "${book.title}"?`)) {
    /* 模拟删除操作 */
    allBooks.value = allBooks.value.filter(b => b.id !== book.id)
    console.log('Deleted book ID:', book.id)
  }
}
</script>
