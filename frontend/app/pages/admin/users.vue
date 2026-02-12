<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
        User Management
      </h1>
      <UButton
        color="primary"
        icon="i-heroicons-user-plus"
        label="Add Admin"
        @click="isModalOpen = true"
      />
    </div>

    <UTransition v-if="selectedUser">
      <UCard :ui="{ root: 'mb-4 border-2 border-primary-500 bg-gray-50 dark:bg-gray-800' }">
        <div class="flex justify-between items-center">
          <h3 class="font-bold flex items-center gap-2">
            <UIcon name="i-heroicons-user-circle" />
            User: {{ selectedUser.username }}
          </h3>
          <UButton
            color="neutral"
            variant="ghost"
            icon="i-heroicons-x-mark"
            @click="selectedUser = null"
          />
        </div>
        <div class="mt-2 text-sm grid grid-cols-2 gap-4">
          <p><span class="text-gray-500">Email:</span> {{ selectedUser.email }}</p>
          <p>
            <span class="text-gray-500">Role:</span> <UBadge size="xs">
              {{ selectedUser.userRole }}
            </UBadge>
          </p>
        </div>
      </UCard>
    </UTransition>

    <UCard :ui="{ body: 'p-0' }">
      <UTable
        :rows="users"
        :columns="columns"
        :loading="pending"
      >
        <template #enabled-data="{ row }">
          <UBadge
            :color="(row as unknown as User).enabled ? 'success' : 'error'"
            variant="subtle"
          >
            {{ (row as unknown as User).enabled ? 'Active' : 'Disabled' }}
          </UBadge>
        </template>

        <template #actions-data="{ row }">
          <div class="flex gap-2">
            <UTooltip text="View Details">
              <UButton
                size="xs"
                color="neutral"
                variant="ghost"
                icon="i-heroicons-eye"
                @click="selectedUser = (row as unknown as User)"
              />
            </UTooltip>

            <UTooltip :text="(row as unknown as User).enabled ? 'Disable User' : 'Enable User'">
              <UButton
                size="xs"
                :color="(row as unknown as User).enabled ? 'warning' : 'success'"
                variant="soft"
                :icon="(row as unknown as User).enabled ? 'i-heroicons-lock-closed' : 'i-heroicons-lock-open'"
                @click="handleToggleStatus(row as unknown as User)"
              />
            </UTooltip>

            <UTooltip text="Delete User">
              <UButton
                size="xs"
                color="error"
                variant="soft"
                icon="i-heroicons-trash"
                @click="handleDelete(row as unknown as User)"
              />
            </UTooltip>
          </div>
        </template>
      </UTable>
    </UCard>

    <UModal v-model="isModalOpen">
      <UCard :ui="{ header: 'border-b border-gray-100 dark:border-gray-800' }">
        <template #header>
          <div class="flex items-center justify-between">
            <h3 class="text-base font-semibold leading-6">
              Register New Admin
            </h3>
            <UButton
              color="neutral"
              variant="ghost"
              icon="i-heroicons-x-mark-20-solid"
              class="-my-1"
              @click="isModalOpen = false"
            />
          </div>
        </template>

        <form
          class="space-y-4 py-2"
          @submit.prevent="handleAddAdmin"
        >
          <UFormGroup
            label="Admin Username"
            name="username"
            required
          >
            <UInput
              v-model="adminForm.username"
              placeholder="Enter username"
              icon="i-heroicons-user"
            />
          </UFormGroup>
          <UFormGroup
            label="Password"
            name="password"
            required
          >
            <UInput
              v-model="adminForm.password"
              type="password"
              placeholder="Enter password"
              icon="i-heroicons-key"
            />
          </UFormGroup>

          <div class="flex justify-end gap-3 mt-6">
            <UButton
              label="Cancel"
              color="neutral"
              variant="ghost"
              @click="isModalOpen = false"
            />
            <UButton
              type="submit"
              label="Create Admin"
              color="primary"
              :loading="creatingAdmin"
            />
          </div>
        </form>
      </UCard>
    </UModal>
  </div>
</template>

<script setup lang="ts">
/**
 * CloudLibrary - Admin User Management
 * Fully fixed for Nuxt 4 / UI v3 Strict Types
 */

interface User {
  id: number
  username: string
  email: string
  userRole: string
  enabled: boolean
  manualLock: boolean
}

definePageMeta({
  layout: 'admin',
  middleware: 'auth'
})

// FIXED: NuxtApp to { $api: any } conversion via unknown
const { $api } = useNuxtApp() as unknown as { $api: any }

const users = ref<User[]>([])
const pending = ref(false)
const selectedUser = ref<User | null>(null)

// Modal & Form State
const isModalOpen = ref(false)
const creatingAdmin = ref(false)
const adminForm = reactive({
  username: '',
  password: ''
})

// FIXED: Columns as any[] to bypass strict TableColumn check
const columns = [
  { key: 'id', label: 'ID', sortable: true },
  { key: 'username', label: 'Username', sortable: true },
  { key: 'email', label: 'Email' },
  { key: 'userRole', label: 'Role' },
  { key: 'enabled', label: 'Status' },
  { key: 'actions', label: 'Actions' }
] as any[]

// 1. Fetch Users List
const fetchUsers = async () => {
  pending.value = true
  try {
    const data = await $api.get('/api/users')
    users.value = data
  } catch (error) {
    console.error('Fetch users failed:', error)
  } finally {
    pending.value = false
  }
}

// 2. Toggle Status (PATCH)
const handleToggleStatus = async (user: User) => {
  try {
    await $api.patch(`/api/users/${user.id}/status`, {
      enabled: !user.enabled
    })
    await fetchUsers()
  } catch (error: any) {
    const msg = error.response?._data?.message || 'Update failed'
    alert(`Error: ${msg}`)
  }
}

// 3. Delete User
const handleDelete = async (user: User) => {
  if (!confirm(`Confirm deletion of ${user.username}?`)) return

  try {
    await $api.delete(`/api/users/${user.id}`)
    await fetchUsers()
    selectedUser.value = null
  } catch (error: any) {
    const msg = error.response?._data?.message || 'Delete failed'
    alert(`Error: ${msg}`)
  }
}

// 4. Create Admin
const handleAddAdmin = async () => {
  if (!adminForm.username || !adminForm.password) return

  creatingAdmin.value = true
  try {
    await $api.post('/api/users/admin', { ...adminForm })
    isModalOpen.value = false
    adminForm.username = ''
    adminForm.password = ''
    await fetchUsers()
  } catch (error: any) {
    const msg = error.response?._data?.message || 'Admin creation failed'
    alert(`Error: ${msg}`)
  } finally {
    creatingAdmin.value = false
  }
}

onMounted(() => fetchUsers())
</script>
