<template>
  <ClientOnly>
    <div class="space-y-8 p-4">
      <div>
        <h1 class="text-2xl font-bold text-green-500">
          Admin Dashboard
        </h1>
        <p class="text-gray-500 text-sm">
          Welcome! Manage the library's core infrastructure from here.
        </p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <UCard
          class="hover:border-primary-500 hover:ring-1 hover:ring-primary-500 cursor-pointer transition-all duration-200"
          @click="navigateTo('/admin/users')"
        >
          <div class="flex flex-col items-center py-6 space-y-3">
            <UIcon
              name="i-heroicons-users"
              class="w-12 h-12 text-blue-500"
            />
            <span class="font-bold text-lg text-gray-700 dark:text-gray-200">Manage Users</span>
            <p class="text-xs text-gray-400 text-center">Update user profiles and permissions</p>
          </div>
        </UCard>

        <UCard
          class="hover:border-primary-500 hover:ring-1 hover:ring-primary-500 cursor-pointer transition-all duration-200"
          @click="navigateTo('/admin/books')"
        >
          <div class="flex flex-col items-center py-6 space-y-3">
            <UIcon
              name="i-heroicons-book-open"
              class="w-12 h-12 text-green-500"
            />
            <span class="font-bold text-lg text-gray-700 dark:text-gray-200">Manage Books</span>
            <p class="text-xs text-gray-400 text-center">Inventory control and book details</p>
          </div>
        </UCard>
      </div>
    </div>
  </ClientOnly>
</template>

<script setup lang="ts">
/**
 * 图书馆项目 - Admin Dashboard (Standard v2)
 * 逻辑：仅作为导航入口，无需 useApi
 */
definePageMeta({
  layout: 'admin',      // 启用 layouts/admin.vue
  middleware: 'auth'    // 启用权限校验
})
</script>