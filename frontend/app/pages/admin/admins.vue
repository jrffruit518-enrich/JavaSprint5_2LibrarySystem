<template>
  <div class="space-y-6">
    <AppBreadcrumb current-page-title="Admin Management" />

    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-highlighted italic">
          System Administrators
        </h1>
        <p class="text-muted text-sm">
          Create and manage administrative accounts. This is a restricted area.
        </p>
      </div>

      <UButton
        label="New Admin Account"
        icon="i-lucide-user-plus"
        color="orange"
        variant="solid"
        @click="isModalOpen = true"
      />
    </div>

    <UCard>
      <UTable
        :rows="adminList"
        :columns="columns"
      >
        <template #status-data="{ row }">
          <UBadge
            color="orange"
            variant="subtle"
            size="sm"
          >
            Active Admin
          </UBadge>
        </template>

        <template #actions-data="{ row }">
          <UButton
            v-if="row.username !== 'admin'"
            icon="i-lucide-user-x"
            color="red"
            variant="ghost"
            @click="deleteAdmin(row)"
          />
          <span
            v-else
            class="text-xs text-muted"
          >Root Account</span>
        </template>
      </UTable>
    </UCard>

    <UModal
      v-model:open="isModalOpen"
      title="Register New Administrator"
    >
      <template #content>
        <UForm
          :state="form"
          class="p-4 space-y-4"
          @submit="onCreateAdmin"
        >
          <UFormField
            label="Admin Username"
            name="username"
          >
            <UInput
              v-model="form.username"
              placeholder="Required"
              icon="i-lucide-user"
            />
          </UFormField>

          <UFormField
            label="Temporary Password"
            name="password"
          >
            <UInput
              v-model="form.password"
              type="password"
              icon="i-lucide-key"
            />
          </UFormField>

          <div class="p-3 bg-neutral-100 dark:bg-neutral-800 rounded-lg flex gap-2">
            <UIcon
              name="i-lucide-shield-alert"
              class="w-5 h-5 text-orange-500 shrink-0"
            />
            <p class="text-xs text-muted">
              Note: This account will have full permissions. Ensure the username is unique in the database.
            </p>
          </div>

          <div class="flex justify-end gap-3 mt-6">
            <UButton
              label="Cancel"
              variant="ghost"
              @click="isModalOpen = false"
            />
            <UButton
              type="submit"
              label="Authorize & Create"
              color="orange"
            />
          </div>
        </UForm>
      </template>
    </UModal>
  </div>
</template>

<script setup>
/* Specify Admin Layout */
definePageMeta({
  layout: 'admin'
})

const isModalOpen = ref(false)

/* Form State */
const form = reactive({
  username: '',
  password: ''
})

/* Admin Table Columns */
const columns = [
  { key: 'username', label: 'Admin Name' },
  { key: 'status', label: 'Role Status' },
  { key: 'actions', label: 'Actions' }
]

/* Mock List: Root and Sub-Admins */
const adminList = ref([
  { id: 1, username: 'admin' }, // Root Admin (Hardcoded in DB)
  { id: 2, username: 'admin_assistant' }
])

/* Handle Creation Request */
const onCreateAdmin = async () => {
  // Validation (Simple)
  if (!form.username || !form.password) return

  /* Logic for backend:
     Send form to POST /api/admins/create
     Authorization: Bearer <current_admin_token>
  */
  console.log('Requesting backend to create:', form.username)

  // Simulate success
  adminList.value.push({ id: Date.now(), username: form.username })

  alert(`Success: Admin "${form.username}" created.`)
  isModalOpen.value = false

  // Reset form
  form.username = ''
  form.password = ''
}

/* Handle Delete (Cannot delete root 'admin') */
const deleteAdmin = (admin) => {
  if (confirm(`Revoke admin privileges for ${admin.username}?`)) {
    adminList.value = adminList.value.filter(a => a.id !== admin.id)
  }
}
</script>
