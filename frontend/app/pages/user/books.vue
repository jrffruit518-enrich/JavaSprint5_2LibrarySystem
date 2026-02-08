<template>
  <div class="space-y-6">
    <AppBreadcrumb current-page-title="Browse & Borrow" />

    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-highlighted">
          Browse & Borrow
        </h1>
        <p class="text-muted text-sm">
          Find your next favorite book and borrow it instantly.
        </p>
      </div>

      <UBadge
        variant="subtle"
        color="primary"
        size="lg"
      >
        Quota: 3 / 5 Books
      </UBadge>
    </div>

    <UCard>
      <BookTable
        :data="booksForUser"
        role="user"
        @borrow="handleBorrow"
      />
    </UCard>
  </div>
</template>

<script setup>
/* Apply User sidebar layout */
definePageMeta({
  layout: 'user'
})

/* Mock data for users (Simplified) */
const booksForUser = ref([
  { id: 1, title: 'The Great Gatsby', author: 'F. Scott Fitzgerald', category: 'Fiction', status: 'Available' },
  { id: 2, title: 'Clean Code', author: 'Robert C. Martin', category: 'Technology', status: 'Borrowed' },
  { id: 3, title: 'The Hobbit', author: 'J.R.R. Tolkien', category: 'Fantasy', status: 'Available' },
  { id: 4, title: 'Vue.js Essentials', author: 'Evan You', category: 'Technology', status: 'Available' }
])

/* Handle the Borrowing logic */
const handleBorrow = (book) => {
  /* Check if book is available */
  if (book.status === 'Borrowed') {
    alert('Sorry, this book is already borrowed by someone else.')
    return
  }

  /* Simulate a backend request */
  console.log('Sending borrow request for book ID:', book.id)

  /* Update UI state (This will be handled by backend API in the future) */
  book.status = 'Borrowed'

  alert(`Success! You have borrowed "${book.title}". You can find it in "My Loans".`)
}
</script>
