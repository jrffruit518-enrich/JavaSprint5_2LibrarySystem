<template>
  <ClientOnly>
    <div class="space-y-6 animate-spring-in">
      <div class="flex items-center justify-between bg-slate-900 text-white p-6 rounded-xl border border-slate-800 shadow-xl">
        <div class="flex items-center gap-4">
          <div class="bg-emerald-500/20 p-3 rounded-lg">
            <UIcon name="i-heroicons-users-solid" class="w-8 h-8 text-emerald-500" />
          </div>
          <div>
            <h1 class="text-2xl font-black leading-tight tracking-tighter uppercase">
              User <span class="text-emerald-500">Directory</span>
            </h1>
            <p class="text-[10px] text-slate-400 font-black uppercase tracking-[0.2em]">Administrative Control & Staff Access</p>
          </div>
        </div>

        <UButton
          v-if="currentMode === 'view'"
          label="Add New Admin"
          icon="i-heroicons-user-plus-solid"
          size="lg"
          color="emerald"
          class="font-black px-8 rounded-full shadow-lg hover:scale-105 transition-transform"
          @click="currentMode = 'add'"
        />
        <UButton
          v-else
          label="Back to List"
          icon="i-heroicons-arrow-left"
          variant="ghost"
          color="white"
          class="font-bold"
          @click="currentMode = 'view'"
        />
      </div>

      <UCard v-if="currentMode === 'add'" class="glass-effect shadow-2xl max-w-2xl mx-auto ring-1 ring-emerald-500/30 animate-spring-in">
        <template #header>
          <div class="flex items-center gap-2 font-black text-xl uppercase tracking-tighter text-slate-900 dark:text-white">
            <UIcon name="i-heroicons-shield-check-solid" class="text-emerald-500 w-7 h-7" />
            <span>Register New Administrator</span>
          </div>
        </template>
        
        <form class="space-y-5 p-2" @submit.prevent="handleSaveAdmin">
          <UFormGroup label="Admin Username" required class="font-bold">
            <UInput v-model="newAdmin.username" icon="i-heroicons-user" placeholder="Enter admin username" size="md" />
          </UFormGroup>
          <UFormGroup label="Email Address" required class="font-bold">
            <UInput v-model="newAdmin.email" icon="i-heroicons-envelope" type="email" placeholder="admin@example.com" size="md" />
          </UFormGroup>
          <UFormGroup label="Initial Password" required class="font-bold">
            <UInput v-model="newAdmin.password" icon="i-heroicons-lock-closed" type="password" size="md" />
          </UFormGroup>
          
          <div class="flex justify-end gap-3 pt-6 border-t border-gray-100 dark:border-gray-700">
            <UButton label="Cancel" variant="ghost" color="gray" @click="currentMode = 'view'" />
            <UButton type="submit" label="Confirm & Create" color="emerald" class="px-6 font-black" :loading="pending" />
          </div>
        </form>
      </UCard>

      <div v-show="currentMode === 'view'" class="bg-white dark:bg-gray-800 p-5 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm flex flex-wrap items-end gap-4">
        <UFormGroup label="Username" class="flex-1 min-w-[150px]" size="sm">
          <UInput v-model="filter.username" icon="i-heroicons-user" placeholder="Search user..." input-class="font-bold" />
        </UFormGroup>
        
        <UFormGroup label="Email" class="flex-1 min-w-[150px]" size="sm">
          <UInput v-model="filter.email" icon="i-heroicons-envelope" placeholder="Search email..." input-class="font-bold" />
        </UFormGroup>

        <UFormGroup label="Role" class="w-36" size="sm">
          <USelect v-model="filter.role" :options="['All', 'ADMIN', 'USER']" />
        </UFormGroup>

        <UFormGroup label="Status" class="w-32" size="sm">
          <USelect 
            v-model="filter.status" 
            :options="[
              { label: 'All', value: 'all' },
              { label: 'Active', value: 'true' },
              { label: 'Disabled', value: 'false' }
            ]" 
          />
        </UFormGroup>

        <UButton icon="i-heroicons-arrow-path" color="gray" variant="soft" size="sm" class="mb-[2px] font-bold" @click="resetFilters">Reset</UButton>
      </div>

      <UCard v-show="currentMode === 'view'" class="border-none shadow-xl ring-1 ring-gray-200 dark:ring-gray-700 overflow-hidden" :ui="{ body: { padding: 'p-0' } }">
        <div class="h-[calc(100vh-420px)] overflow-y-auto relative custom-scrollbar">
          <UTable
            :rows="filteredUsers"
            :columns="columns"
            :loading="status === 'pending'"
            class="w-full"
            :ui="{ 
              wrapper: 'relative overflow-visible', 
              thead: 'table-header-sticky',
              th: { base: 'text-xs font-black text-slate-900 dark:text-white uppercase py-4 px-4 text-center bg-gray-50 dark:bg-slate-900 tracking-widest' },
              td: { base: 'text-sm py-4 px-4 text-center align-middle font-medium' }
            }"
          >
            <template #userRole-data="{ row }">
              <div class="flex justify-center">
                <UBadge 
                  :color="row.userRole === 'ROLE_ADMIN' ? 'emerald' : 'slate'" 
                  variant="subtle"
                  class="font-black scale-90 px-3 py-1 rounded-md"
                >
                  {{ row.userRole.replace('ROLE_', '') }}
                </UBadge>
              </div>
            </template>

            <template #enabled-data="{ row }">
              <div class="flex justify-center">
                <UBadge :color="row.enabled ? 'emerald' : 'rose'" variant="soft" class="font-bold uppercase text-[10px]">
                  {{ row.enabled ? 'Active' : 'Disabled' }}
                </UBadge>
              </div>
            </template>

            <template #actions-data="{ row }">
              <div class="flex items-center justify-center gap-2">
                <UTooltip :text="row.enabled ? 'Disable Account' : 'Enable Account'">
                  <UButton
                    :icon="row.enabled ? 'i-heroicons-lock-closed' : 'i-heroicons-lock-open'"
                    size="xs"
                    :color="row.enabled ? 'amber' : 'emerald'"
                    variant="soft"
                    class="rounded-lg"
                    @click="triggerConfirm('lock', row)"
                  />
                </UTooltip>
                <UTooltip text="Delete User">
                  <UButton
                    icon="i-heroicons-trash"
                    size="xs"
                    color="rose"
                    variant="ghost"
                    class="rounded-lg hover:bg-rose-50"
                    @click="triggerConfirm('delete', row)"
                  />
                </UTooltip>
              </div>
            </template>
          </UTable>
        </div>
      </UCard>

      <UModal v-model="confirmModal.isOpen">
        <UCard :ui="{ ring: '', divide: 'divide-y divide-gray-100 dark:divide-gray-800' }">
          <template #header>
            <div class="flex items-center gap-3" :class="confirmModal.type === 'delete' ? 'text-rose-500' : 'text-amber-500'">
              <UIcon :name="confirmModal.type === 'delete' ? 'i-heroicons-trash-solid' : 'i-heroicons-shield-exclamation-solid'" class="w-6 h-6" />
              <span class="font-black uppercase tracking-widest">{{ confirmModal.title }}</span>
            </div>
          </template>

          <div class="p-4">
            <p class="text-sm font-bold text-slate-600 dark:text-slate-300">
              {{ confirmModal.message }} <span :class="confirmModal.type === 'delete' ? 'text-rose-600' : 'text-amber-600'">"{{ confirmModal.targetUser?.username }}"</span>?
            </p>
          </div>

          <template #footer>
            <div class="flex justify-end gap-3">
              <UButton color="gray" variant="ghost" label="Cancel" @click="confirmModal.isOpen = false" />
              <UButton 
                :color="confirmModal.type === 'delete' ? 'rose' : 'amber'" 
                :label="confirmModal.confirmLabel" 
                :loading="actionPending" 
                @click="executeConfirmedAction" 
              />
            </div>
          </template>
        </UCard>
      </UModal>
    </div>
  </ClientOnly>
</template>

<script setup lang="ts">
/**
 * User Directory Management - Nuxt UI Modal Upgrade
 */
import { type UserRow } from '~/types/user'

definePageMeta({ layout: 'admin', middleware: 'auth' })

const toast = useToast()
const currentMode = ref<'view' | 'add'>('view')
const newAdmin = ref({ username: '', email: '', password: '' })
const pending = ref(false)
const actionPending = ref(false)

// Confirm Modal Reactive State
const confirmModal = reactive({
  isOpen: false,
  type: 'delete' as 'delete' | 'lock',
  title: '',
  message: '',
  confirmLabel: '',
  targetUser: null as UserRow | null
})

const filter = reactive({
  username: '',
  email: '',
  role: 'All',
  status: 'all'
})

const columns = [
  { key: 'id', label: 'ID', class: 'w-[10%]' },
  { key: 'username', label: 'Username', class: 'w-[25%]' },
  { key: 'email', label: 'Email Address', class: 'w-[30%]' },
  { key: 'userRole', label: 'Access Level', class: 'w-[15%]' },
  { key: 'enabled', label: 'Security Status', class: 'w-[10%]' },
  { key: 'actions', label: 'Actions', class: 'w-[10%]' }
]

const { data: rawUsers, refresh, status } = await useApi<any>('/users')

const resetFilters = () => {
  filter.username = ''; filter.email = ''; filter.role = 'All'; filter.status = 'all'
}

const filteredUsers = computed<UserRow[]>(() => {
  const data = unref(rawUsers)
  const content = (Array.isArray(data) ? data : (data?.content || [])) as any[]
  
  return content
    .map(u => ({
      id: u.id,
      username: u.username ?? '—',
      email: u.email ?? '—',
      userRole: u.userRole ?? 'UNKNOWN',
      enabled: !!u.enabled
    }))
    .filter(user => {
      const matchName = user.username.toLowerCase().includes(filter.username.toLowerCase())
      const matchEmail = user.email.toLowerCase().includes(filter.email.toLowerCase())
      const matchRole = filter.role === 'All' || user.userRole.includes(filter.role)
      const matchStatus = filter.status === 'all' || user.enabled.toString() === filter.status
      return matchName && matchEmail && matchRole && matchStatus
    })
})

// Trigger logic for different actions
const triggerConfirm = (type: 'delete' | 'lock', user: UserRow) => {
  confirmModal.type = type
  confirmModal.targetUser = user
  confirmModal.isOpen = true
  
  if (type === 'delete') {
    confirmModal.title = 'Confirm Deletion'
    confirmModal.message = 'Are you sure you want to permanently delete'
    confirmModal.confirmLabel = 'Delete User'
  } else {
    const action = user.enabled ? 'Disable' : 'Enable'
    confirmModal.title = `${action} Account`
    confirmModal.message = `Are you sure you want to ${action.toLowerCase()}`
    confirmModal.confirmLabel = `${action} Now`
  }
}

// Execute the confirmed action
const executeConfirmedAction = async () => {
  if (!confirmModal.targetUser) return
  const user = confirmModal.targetUser
  actionPending.value = true

  try {
    if (confirmModal.type === 'delete') {
      await $fetch(`/api/users/${user.id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${useCookie('auth_token').value}` }
      })
      toast.add({ title: 'User Deleted', color: 'emerald', icon: 'i-heroicons-trash' })
    } else {
      await $fetch(`/api/users/${user.id}/status`, {
        method: 'PATCH',
        body: { enabled: !user.enabled },
        headers: { Authorization: `Bearer ${useCookie('auth_token').value}` }
      })
      toast.add({ title: `User Status Updated`, color: 'emerald', icon: 'i-heroicons-check-badge' })
    }
    await refresh()
    confirmModal.isOpen = false
  } catch (err: any) {
    if (confirmModal.type === 'delete') {
      // Hardcoded English message for deletion failure due to unreturned books
      toast.add({ 
        title: 'Deletion Failed', 
        description: 'This account cannot be deleted because the user has unreturned books.', 
        color: 'rose',
        icon: 'i-heroicons-exclamation-circle'
      })
    } else {
      toast.add({ title: 'Operation Failed', description: err.data?.message, color: 'rose' })
    }
  } finally {
    actionPending.value = false
    confirmModal.targetUser = null
  }
}

const handleSaveAdmin = async () => {
  pending.value = true
  try {
    await $fetch('/api/users/admin', {
      method: 'POST',
      body: newAdmin.value,
      headers: { Authorization: `Bearer ${useCookie('auth_token').value}` }
    })
    toast.add({ title: 'Admin Created Successfully', color: 'emerald' })
    currentMode.value = 'view'
    newAdmin.value = { username: '', email: '', password: '' } 
    await refresh()
  } catch (err: any) {
    toast.add({ title: 'Creation Failed', description: err.data?.message, color: 'rose' })
  } finally {
    pending.value = false
  }
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { @apply bg-emerald-200 rounded-full hover:bg-emerald-300 transition-colors; }

.glass-effect {
  @apply bg-white/90 dark:bg-slate-900/90 backdrop-blur-md border border-white/20;
}

:deep(.table-header-sticky) {
  position: sticky !important;
  top: 0 !important;
  z-index: 30 !important;
}

:deep(table) {
  border-collapse: separate !important;
  border-spacing: 0 !important;
  table-layout: fixed !important;
}

:deep(thead th) {
  position: sticky !important;
  top: 0 !important;
  border-bottom: 3px solid #10b981 !important; 
  box-shadow: 0 1px 0 0 #10b981;
}

@keyframes spring-in {
  0% { transform: scale(0.98); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
.animate-spring-in {
  animation: spring-in 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
</style>