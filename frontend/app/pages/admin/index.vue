<template>
  <div class="space-y-8">
    <div>
      <h1 class="text-2xl font-bold text-highlighted">
        Admin Dashboard
      </h1>
      <p class="text-muted text-sm">
        Welcome! Manage the library's core infrastructure from here.
      </p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <UCard
        class="hover:border-primary cursor-pointer transition-colors"
        @click="navigateTo('/admin/users')"
      >
        <div class="flex flex-col items-center py-4 space-y-2">
          <UIcon
            name="i-lucide-users"
            class="w-10 h-10 text-blue-500"
          />
          <span class="font-bold">Manage Users</span>
        </div>
      </UCard>

      <UCard
        class="hover:border-primary cursor-pointer transition-colors"
        @click="navigateTo('/admin/books')"
      >
        <div class="flex flex-col items-center py-4 space-y-2">
          <UIcon
            name="i-lucide-book-marked"
            class="w-10 h-10 text-green-500"
          />
          <span class="font-bold">Manage Books</span>
        </div>
      </UCard>
    </div>
  </div>
</template>

<script setup>
definePageMeta({
  layout: 'admin'
})
</script>
