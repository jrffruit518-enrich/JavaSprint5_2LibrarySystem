没问题，这是管理员端的最后一个页面 pages/admin/users.vue。

我同样在最顶端加入了 <AppBreadcrumb />，并确保保留了你所有的 Mock 数据逻辑和注释。

修改后的 pages/admin/users.vue
代码段
<template>
  <div class="space-y-6">
    <AppBreadcrumb current-page-title="User Management" />

    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-highlighted">
          User Management
        </h1>
        <p class="text-muted text-sm">
          Monitor reader accounts, manage borrowing quotas, and account status.
        </p>
      </div>

      <div class="flex gap-4">
        <UBadge
          color="neutral"
          variant="subtle"
        >
          Total Readers: {{ readerList.length }}
        </UBadge>
      </div>
    </div>

    <UCard>
      <UTable
        :rows="readerList"
        :columns="columns"
      >
        <template #status-data="{ row }">
          <UBadge
            :color="row.status === 'Active' ? 'green' : 'red'"
            variant="subtle"
          >
            {{ row.status }}
          </UBadge>
        </template>

        <template #quota-data="{ row }">
          <span class="text-sm">{{ row.borrowed }}/{{ row.limit }} Books</span>
        </template>

        <template #actions-data="{ row }">
          <div class="flex gap-2">
            <UButton
              icon="i-lucide-user-cog"
              variant="ghost"
              color="neutral"
              @click="editUser(row)"
            />
            <UButton
              :icon="row.status === 'Active' ? 'i-lucide-user-minus' : 'i-lucide-user-check'"
              :color="row.status === 'Active' ? 'red' : 'green'"
              variant="ghost"
              @click="toggleUserStatus(row)"
            />
          </div>
        </template>
      </UTable>
    </UCard>
  </div>
</template>

<script setup>
/* Apply Admin Layout */
definePageMeta({
  layout: 'admin'
})

/* Table Structure for Readers */
const columns = [
  { key: 'username', label: 'Reader Account' },
  { key: 'email', label: 'Email Address' },
  { key: 'quota', label: 'Borrowing Quota' },
  { key: 'status', label: 'Status' },
  { key: 'actions', label: 'Actions' }
]

/* Mock Data: Users registered via the register page */
const readerList = ref([
  { id: 1, username: 'bookworm_99', email: 'worm@example.com', borrowed: 2, limit: 5, status: 'Active' },
  { id: 2, username: 'nucter_pro', email: 'nuxt@example.com', borrowed: 0, limit: 5, status: 'Active' },
  { id: 3, username: 'overdue_guy', email: 'late@example.com', borrowed: 5, limit: 5, status: 'Banned' }
])

/* Logic: Toggle Account Status (Ban/Unban) */
const toggleUserStatus = (user) => {
  const action = user.status === 'Active' ? 'ban' : 'activate'
  if (confirm(`Are you sure you want to ${action} user "${user.username}"?`)) {
    /* Update status locally */
    user.status = user.status === 'Active' ? 'Banned' : 'Active'
    console.log(`User ${user.username} status changed to ${user.status}`)
  }
}

/* Logic: Open Edit Form (For quota or profile updates) */
const editUser = (user) => {
  console.log('Editing user profile:', user.username)
  /* Feature: Update quota or info for the specific user */
  alert(`Feature: Update quota or info for ${user.username}`)
}
</script>
