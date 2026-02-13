<template>
  <ClientOnly>
    <div class="space-y-6 p-6">
      <nav class="text-sm text-gray-500">Admin > User Management</nav>

      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-green-500">User Management</h1>
          <p class="text-sm text-gray-500">Administrative Control Panel & Staff Access</p>
        </div>

        <UButton
          v-if="currentMode === 'view'"
          label="Add New Admin"
          icon="i-heroicons-user-plus"
          color="primary"
          @click="currentMode = 'add'"
        />
        <UButton
          v-else
          label="Back to List"
          icon="i-heroicons-arrow-left"
          variant="ghost"
          @click="currentMode = 'view'"
        />
      </div>

      <UCard v-if="currentMode === 'add'" class="border-2 border-primary/20 shadow-lg max-w-2xl mx-auto">
        <template #header>
          <div class="flex items-center gap-2 font-bold">
            <UIcon name="i-heroicons-shield-check" class="text-primary" />
            Register New Administrator
          </div>
        </template>
        
        <form class="space-y-4" @submit.prevent="handleSaveAdmin">
          <UFormGroup label="Admin Username" required>
            <UInput v-model="newAdmin.username" icon="i-heroicons-user" placeholder="Enter admin username" />
          </UFormGroup>
          <UFormGroup label="Email Address" required>
            <UInput v-model="newAdmin.email" icon="i-heroicons-envelope" type="email" placeholder="admin@example.com" />
          </UFormGroup>
          <UFormGroup label="Initial Password" required>
            <UInput v-model="newAdmin.password" icon="i-heroicons-lock-closed" type="password" />
          </UFormGroup>
          
          <div class="flex justify-end gap-3 pt-4 border-t">
            <UButton label="Cancel" variant="ghost" @click="currentMode = 'view'" />
            <UButton type="submit" label="Confirm & Create" color="primary" :loading="pending" />
          </div>
        </form>
      </UCard>

      <UAlert
        v-if="apiError"
        icon="i-heroicons-exclamation-circle"
        color="red"
        variant="soft"
        title="Fetch Error"
        :description="apiError.message || 'Failed to sync with user database.'"
      />

      <UCard v-show="currentMode === 'view'" :ui="{ body: 'p-0' }" class="overflow-hidden">
        <UTable
          :rows="users"
          :columns="columns"
          :loading="status === 'pending'"
        >
          <template #userRole-data="{ row }">
            <UBadge 
              :color="row.userRole === 'ROLE_ADMIN' ? 'primary' : 'gray'" 
              variant="subtle"
              class="capitalize"
            >
              {{ row.userRole.replace('ROLE_', '') }}
            </UBadge>
          </template>

          <template #enabled-data="{ row }">
            <UBadge :color="row.enabled ? 'green' : 'red'" variant="outline">
              {{ row.enabled ? 'Active' : 'Disabled' }}
            </UBadge>
          </template>

          <template #actions-data="{ row }">
            <div class="flex items-center gap-2">
              <UTooltip :text="row.enabled ? 'Disable User' : 'Enable User'">
                <UButton
                  :icon="row.enabled ? 'i-heroicons-user-minus' : 'i-heroicons-user-plus'"
                  size="sm"
                  :color="row.enabled ? 'orange' : 'green'"
                  variant="ghost"
                  @click="handleToggleStatus(row)"
                />
              </UTooltip>
              <UTooltip text="Delete User">
                <UButton
                  icon="i-heroicons-trash"
                  size="sm"
                  color="red"
                  variant="ghost"
                  @click="handleDeleteUser(row)"
                />
              </UTooltip>
            </div>
          </template>
        </UTable>
      </UCard>
    </div>
  </ClientOnly>
</template>

<script setup lang="ts">
import { type UserRow } from '~/types/user'

definePageMeta({
  layout: 'admin',
  middleware: 'auth'
})

// --- 状态与配置 ---
const currentMode = ref<'view' | 'add'>('view')
const newAdmin = ref({ username: '', email: '', password: '' })
const pending = ref(false)

const columns = [
  { key: 'id', label: 'ID' },
  { key: 'username', label: 'Username' },
  { key: 'email', label: 'Email' },
  { key: 'userRole', label: 'Role' },
  { key: 'enabled', label: 'Status' },
  { key: 'actions', label: 'Actions' }
]

// --- 1. 数据获取逻辑 ---
const { data: rawUsers, refresh, status, error: apiError } = await useApi<any>('/users')

const users = computed<UserRow[]>(() => {
  const data = unref(rawUsers)
  const content = Array.isArray(data) ? data : (data?.content || [])
  return content.map((u: any) => ({
    id: u.id,
    username: u.username ?? '—',
    email: u.email ?? '—',
    userRole: u.userRole ?? 'UNKNOWN',
    enabled: !!u.enabled
  }))
})

// --- 2. 交互操作逻辑 ---

/**
 * Jules Fix: 匹配后端 @RequestBody Map<String, Boolean> payload
 * 1. 使用 PATCH 方法
 * 2. Body 结构必须为 { enabled: boolean }
 */
const handleToggleStatus = async (user: UserRow) => {
  try {
    await $fetch(`/api/users/${user.id}/status`, {
      method: 'PATCH',
      body: { enabled: !user.enabled }, // 精确匹配后端 Map 取值
      headers: { 
        Authorization: `Bearer ${useCookie('auth_token').value}` 
      }
    })
    
    // 成功后刷新列表
    await refresh()
  } catch (err: any) {
    // 捕获 Root Admin 保护 (ForbiddenException) 或其他错误
    const errorMsg = err.data?.message || 'Status update failed'
    alert(`Error: ${errorMsg}`)
    console.error('Update failed:', err.data)
  }
}

// 删除用户
const handleDeleteUser = async (user: UserRow) => {
  if (!confirm(`Are you sure you want to delete user: ${user.username}?`)) return
  try {
    await $fetch(`/api/users/${user.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${useCookie('auth_token').value}` }
    })
    await refresh()
  } catch (err: any) {
    alert('Delete failed')
  }
}

// 保存新管理员
const handleSaveAdmin = async () => {
  pending.value = true
  try {
    await $fetch('/api/users/admin', {
      method: 'POST',
      body: newAdmin.value,
      headers: { Authorization: `Bearer ${useCookie('auth_token').value}` }
    })
    currentMode.value = 'view'
    newAdmin.value = { username: '', email: '', password: '' } 
    await refresh()
  } catch (err: any) {
    alert('Failed to create admin: ' + (err.data?.message || 'Check inputs'))
  } finally {
    pending.value = false
  }
}
</script>
