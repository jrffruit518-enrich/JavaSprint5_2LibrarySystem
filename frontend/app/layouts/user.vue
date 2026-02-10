<template>
  <UContainer class="py-6">
    <UCard shadow="none">
      <template #header>
        <div class="flex justify-between items-center">
          <div>
            <h2 class="text-xl font-bold text-highlighted">
              User Management
            </h2>
            <p class="text-sm text-muted">
              Manage library staff and member access
            </p>
          </div>
          <UInput
            v-model="searchQuery"
            icon="i-lucide-search"
            placeholder="Search username or email..."
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

      <div
        v-else
        class="overflow-x-auto border border-default rounded-lg"
      >
        <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-800">
          <thead class="bg-gray-50 dark:bg-gray-900">
            <tr>
              <th
                v-for="label in ['User', 'Email', 'Role', 'Status', 'Actions']"
                :key="label"
                class="px-4 py-3 text-left text-xs font-semibold text-muted uppercase tracking-wider"
              >
                {{ label }}
              </th>
            </tr>
          </thead>
          <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-100 dark:divide-gray-800">
            <tr
              v-for="user in filteredUsers"
              :key="user.id"
              class="hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
            >
              <td class="px-4 py-3 text-sm font-medium text-highlighted">
                {{ user.username }}
              </td>
              <td class="px-4 py-3 text-sm text-gray-500">
                {{ user.email }}
              </td>
              <td class="px-4 py-3 text-sm">
                <UBadge
                  :color="user.role === 'ADMIN' ? 'primary' : 'neutral'"
                  variant="soft"
                >
                  {{ user.role }}
                </UBadge>
              </td>
              <td class="px-4 py-3 text-sm">
                <span class="flex items-center gap-1.5">
                  <span class="h-2 w-2 rounded-full bg-green-500" />
                  Active
                </span>
              </td>
              <td class="px-4 py-3 text-sm">
                <div class="flex gap-2">
                  <UButton
                    size="xs"
                    variant="outline"
                    :label="user.role === 'ADMIN' ? 'Demote' : 'Promote'"
                    :color="user.role === 'ADMIN' ? 'error' : 'primary'"
                    @click="toggleRole(user)"
                  />
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </UCard>
  </UContainer>
</template>

<script setup lang="ts">
/**
 * 图书馆项目 (Library Project) - User Management
 * Uses the proven stable table pattern for Nuxt 4.3.1
 */

interface User {
  id: number
  username: string
  email: string
  role: 'ADMIN' | 'USER'
}

const searchQuery = ref('')

/* 1. Fetch Users */
const { data: users, status, refresh } = await useApi<User[]>('/api/admin/users')

/* 2. Filter Logic */
const filteredUsers = computed(() => {
  if (!users.value) return []
  return users.value.filter(u =>
    u.username.toLowerCase().includes(searchQuery.value.toLowerCase())
    || u.email.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

/* 3. Actions */
const toggleRole = async (user: User) => {
  const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN'
  try {
    // English Comment: Optimistic UI update or full refresh
    await useApi(`/api/admin/users/${user.id}/role`, {
      method: 'PUT',
      params: { role: newRole }
    })
    await refresh() // Refresh data from server
  } catch (err) {
    console.error('Failed to update role', err)
  }
}

definePageMeta({
  layout: 'default',
  middleware: 'auth' // Ensure only admins can access
})
</script>
