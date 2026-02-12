<template>
  <ClientOnly>
    <div class="space-y-8 p-4">
      <div>
        <h1 class="text-2xl font-bold text-highlighted">
          Admin Dashboard
        </h1>
        <p class="text-muted text-sm">
          Welcome! Manage the library's core infrastructure from here.
        </p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <UCard
          class="hover:border-primary cursor-pointer transition-colors"
          @click="navigateTo('/admin/users')"
        >
          <div class="flex flex-col items-center py-6 space-y-3">
            <UIcon
              name="i-lucide-users"
              class="w-12 h-12 text-blue-500"
            />
            <span class="font-bold text-lg">Manage Users</span>
          </div>
        </UCard>

        <UCard
          class="hover:border-primary cursor-pointer transition-colors"
          @click="navigateTo('/admin/books')"
        >
          <div class="flex flex-col items-center py-6 space-y-3">
            <UIcon
              name="i-lucide-book-open"
              class="w-12 h-12 text-green-500"
            />
            <span class="font-bold text-lg">Manage Books</span>
          </div>
        </UCard>
      </div>
    </div>
  </ClientOnly>
</template>

<script setup>
/**
 * 图书馆项目 - Admin Dashboard
 * 使用最基础的 UCard 和 UIcon，作为安全的登录着陆页
 */
definePageMeta({
  layout: 'admin'
})
</script>
