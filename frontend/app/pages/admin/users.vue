<template>
  <ClientOnly>
    <div class="p-6 space-y-6">
      <div class="flex justify-between items-center">
        <div>
          <h1 class="text-2xl font-bold text-highlighted">
            User Management
          </h1>
          <p class="text-sm text-muted">
            Manage system users and permissions.
          </p>
        </div>
        <UButton
          label="Refresh"
          icon="i-lucide-rotate-cw"
          :loading="pending"
          @click="loadUsers"
        />
      </div>

      <UCard :ui="{ body: 'p-0' }">
        <UTable
          :rows="users"
          :columns="columns"
          :loading="pending"
        >
          <template #userRole-data="scope">
            <UBadge
              :color="(scope.row as any).userRole === 'ADMIN' ? 'primary' : 'neutral'"
              variant="subtle"
            >
              {{ (scope.row as any).userRole }}
            </UBadge>
          </template>

          <template #enabled-data="scope">
            <UBadge
              :color="(scope.row as any).enabled ? 'success' : 'error'"
              variant="outline"
            >
              {{ (scope.row as any).enabled ? 'Active' : 'Disabled' }}
            </UBadge>
          </template>
        </UTable>
      </UCard>
    </div>
  </ClientOnly>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useApi } from '~/composables/useApi'

/**
 * 图书馆项目 - User Management Page
 * Final Correct Syntax for Nuxt 4 / Nuxt UI v3 Table Slots
 */
definePageMeta({
  layout: 'admin',
  middleware: 'auth'
})

// English Comment: Standard UserRow interface
interface UserRow {
  id: number
  username: string
  email: string
  userRole: string
  enabled: boolean
}

const users = ref<UserRow[]>([])
const pending = ref(false)

// English Comment: Using any[] for columns to avoid complex TableColumn generic issues
const columns: any[] = [
  { key: 'id', label: 'ID' },
  { key: 'username', label: 'Username' },
  { key: 'email', label: 'Email' },
  { key: 'userRole', label: 'Role' },
  { key: 'enabled', label: 'Status' }
]

const loadUsers = async () => {
  pending.value = true
  try {
    // English Comment: Trail slash to prevent backend 500 error
    const { data, error } = await useApi<UserRow[]>('/api/users/')

    if (error.value) {
      console.error('>>> [DEBUG] Fetch Users Error:', error.value)
      return
    }

    if (data.value) {
      // English Comment: Ensure data is extracted correctly from Ref
      const rawData = data.value
      users.value = Array.isArray(rawData) ? rawData : (rawData as any).content || []
      console.log('>>> [DEBUG] Users data assigned to table')
    }
  } catch (err) {
    console.error('>>> [DEBUG] Unexpected Error:', err)
  } finally {
    pending.value = false
  }
}

onMounted(loadUsers)
</script>
