<template>
  <ClientOnly>
    <div class="space-y-6 p-6">
      <AppBreadcrumb current-page-title="User Management" />

      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
            User Management
          </h1>
          <p class="text-sm text-gray-500">
            Administrative Control Panel & Staff Access
          </p>
        </div>

        <UButton
          v-if="currentMode === 'view'"
          label="Add New Admin"
          icon="i-lucide-user-plus"
          color="primary"
          @click="openAddMode"
        />
        <UButton
          v-else
          label="Back to List"
          icon="i-lucide-arrow-left"
          variant="ghost"
          color="neutral"
          @click="currentMode = 'view'"
        />
      </div>

      <UCard
        v-if="currentMode === 'add'"
        class="border-2 border-primary/20 shadow-lg max-w-2xl mx-auto"
      >
        <template #header>
          <div class="flex items-center gap-2 font-bold">
            <UIcon
              name="i-lucide-shield-check"
              class="text-primary"
            />
            Register New Administrator
          </div>
        </template>
        <form
          class="space-y-4"
          @submit.prevent="handleSaveAdmin"
        >
          <UFormGroup
            label="Admin Username"
            required
          >
            <UInput
              v-model="newAdmin.username"
              icon="i-lucide-user"
            />
          </UFormGroup>
          <UFormGroup
            label="Email Address"
            required
          >
            <UInput
              v-model="newAdmin.email"
              icon="i-lucide-mail"
              type="email"
            />
          </UFormGroup>
          <UFormGroup
            label="Initial Password"
            required
          >
            <UInput
              v-model="newAdmin.password"
              icon="i-lucide-lock"
              type="password"
            />
          </UFormGroup>
          <div class="flex justify-end gap-3 pt-4 border-t border-gray-100 dark:border-gray-800">
            <UButton
              label="Cancel"
              color="neutral"
              variant="ghost"
              @click="currentMode = 'view'"
            />
            <UButton
              type="submit"
              label="Confirm & Create"
              color="primary"
              :loading="pending"
            />
          </div>
        </form>
      </UCard>

      <UAlert
        v-if="diagnosticError"
        icon="i-lucide-alert-circle"
        color="error"
        variant="subtle"
        :title="`Error: ${diagnosticError.statusCode}`"
        :description="diagnosticError.message"
      />

      <UCard
        v-show="currentMode === 'view'"
        :ui="{ body: 'p-0' }"
      >
        <UTable
          :key="users.length"
          :data="users"
          :columns="columns"
          :loading="pending"
        >
          <template #userRole-cell="{ row }">
            <UBadge
              :color="row.original.userRole === 'ROLE_ADMIN' ? 'primary' : 'neutral'"
              variant="subtle"
            >
              {{ row.original.userRole }}
            </UBadge>
          </template>

          <template #enabled-cell="{ row }">
            <UBadge
              :color="row.original.enabled ? 'success' : 'error'"
              variant="outline"
            >
              {{ row.original.enabled ? 'Active' : 'Disabled' }}
            </UBadge>
          </template>

          <template #actions-cell="{ row }">
            <div class="flex items-center gap-2">
              <UButton
                :icon="row.original.enabled ? 'i-lucide-user-x' : 'i-lucide-user-check'"
                size="sm"
                :color="row.original.enabled ? 'warning' : 'success'"
                variant="ghost"
                @click="handleToggleStatus(row.original)"
              />
              <UButton
                icon="i-lucide-trash-2"
                size="sm"
                color="error"
                variant="ghost"
                @click="handleDeleteUser(row.original)"
              />
            </div>
          </template>

          <template #empty>
            <div class="py-8 text-center text-gray-500">
              No users found.
            </div>
          </template>
        </UTable>
      </UCard>
    </div>

    <template #fallback>
      <div class="p-10 text-center italic text-muted">
        Initializing...
      </div>
    </template>
  </ClientOnly>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { TableColumn } from '@nuxt/ui'

definePageMeta({
  layout: 'admin',
  middleware: 'auth'
})

/* --- 1. 类型定义 --- */
interface UserRow {
  id: number
  username: string
  email: string
  userRole: string
  enabled: boolean
}

interface DiagnosticError {
  statusCode: number
  message: string
}

/* --- 2. 状态管理 --- */
const currentMode = ref<'view' | 'add'>('view')
const users = ref<UserRow[]>([])
const pending = ref(false)
const diagnosticError = ref<DiagnosticError | null>(null)
const newAdmin = ref({ username: '', email: '', password: '' })

const columns: TableColumn<UserRow>[] = [
  { accessorKey: 'id', header: 'ID' },
  { accessorKey: 'username', header: 'Username' },
  { accessorKey: 'email', header: 'Email' },
  { accessorKey: 'userRole', header: 'Role' },
  { accessorKey: 'enabled', header: 'Status' },
  { accessorKey: 'actions', header: 'Actions' }
]

/* --- 3. 核心逻辑 --- */

const loadUsers = async () => {
  pending.value = true
  diagnosticError.value = null
  const token = useCookie('auth_token').value
  try {
    const response = await $fetch<any>('/api/users', {
      headers: { Authorization: `Bearer ${token}` }
    })
    const extracted = Array.isArray(response) ? response : (response?.content || [])
    users.value = extracted.map((u: any): UserRow => ({
      id: Number(u.id),
      username: u.username ?? '—',
      email: u.email ?? '—',
      userRole: u.userRole ?? 'UNKNOWN',
      enabled: !!u.enabled
    }))
  } catch (err: any) {
    diagnosticError.value = {
      statusCode: err.status || 500,
      message: err.data?.message || err.message || 'Fetch failed'
    }
  } finally {
    pending.value = false
  }
}

const openAddMode = () => {
  newAdmin.value = { username: '', email: '', password: '' }
  currentMode.value = 'add'
}

const handleDeleteUser = async (user: UserRow) => {
  if (!confirm(`Are you sure you want to delete user: ${user.username}?`)) return
  const token = useCookie('auth_token').value
  try {
    await $fetch(`/api/users/${user.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` }
    })
    await loadUsers()
  } catch (err: any) {
    alert(err.data?.message || 'Delete failed')
  }
}

const handleToggleStatus = async (user: UserRow) => {
  const token = useCookie('auth_token').value
  const targetStatus = !user.enabled

  try {
    // 解决方法：将布尔值转化为 JSON 字符串，以符合 BodyInit 类型要求
    await $fetch(`/api/users/${user.id}/status`, {
      method: 'PATCH',
      body: JSON.stringify(targetStatus),
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    await loadUsers()
  } catch (err: any) {
    console.error('Status Update Failed:', err)
    alert(err.data?.message || 'Failed to update user status')
  }
}

const handleSaveAdmin = async () => {
  if (!newAdmin.value.username || !newAdmin.value.password) return
  pending.value = true
  const token = useCookie('auth_token').value
  try {
    await $fetch('/api/users/admin', {
      method: 'POST',
      body: newAdmin.value,
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })
    currentMode.value = 'view'
    await loadUsers()
    alert('Admin created successfully!')
  } catch (err: any) {
    alert(err.data?.message || 'Creation failed')
  } finally {
    pending.value = false
  }
}

// 遵循单行单语句规范
onMounted(loadUsers)
</script>
