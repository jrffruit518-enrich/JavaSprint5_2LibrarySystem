<template>
  <div class="space-y-6">
    <nav class="text-sm font-medium text-gray-500 mb-4">
      User / <span class="text-primary">Personal Profile</span>
    </nav>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <UCard class="lg:col-span-1 h-fit">
        <div class="flex flex-col items-center py-8">
          <UAvatar
            :src="user.avatarUrl"
            :alt="user.username"
            size="3xl"
            class="ring-4 ring-primary/20 text-2xl font-bold"
          />
          <h2 class="mt-4 text-2xl font-bold text-green-600 text-center">
            {{ user.username || 'Loading...' }}
          </h2>
          <p class="text-sm text-gray-500 font-mono mt-1">
            ID: LIB-2026-{{ String(user.id || 0).padStart(3, '0') }}
          </p>
          <UBadge class="mt-2" variant="subtle" color="primary">
            {{ user.userRole }}
          </UBadge>
        </div>
      </UCard>

      <UCard class="lg:col-span-2">
        <template #header>
          <div class="flex justify-between items-center">
            <div class="flex items-center gap-2 font-semibold text-lg text-gray-800">
              <UIcon name="i-heroicons-cog-6-tooth" />
              Account Information
            </div>
            <UButton
              v-if="!isEditing"
              variant="ghost"
              icon="i-heroicons-pencil-square"
              label="Edit Profile"
              @click="startEditing"
            />
          </div>
        </template>

        <div v-if="!isEditing" class="space-y-6 py-2">
          <div class="grid grid-cols-2 gap-4 border-b border-gray-100 pb-4">
            <span class="text-gray-400 text-sm uppercase font-semibold">Username</span>
            <span class="text-gray-800 font-medium">{{ user.username || '---' }}</span>
          </div>
          <div class="grid grid-cols-2 gap-4 border-b border-gray-100 pb-4">
            <span class="text-gray-400 text-sm uppercase font-semibold">Email Address</span>
            <span class="text-gray-800 font-medium">{{ user.email || '---' }}</span>
          </div>
          <div class="grid grid-cols-2 gap-4 border-b border-gray-100 pb-4">
            <span class="text-gray-400 text-sm uppercase font-semibold">Avatar URL</span>
            <span class="text-gray-800 font-medium truncate">{{ user.avatarUrl || 'No avatar set' }}</span>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <span class="text-gray-400 text-sm uppercase font-semibold">Account Status</span>
            <span class="text-green-500 font-medium flex items-center gap-1">
              <span class="h-2 w-2 rounded-full bg-green-500" /> Active
            </span>
          </div>
        </div>

        <UForm v-else :state="editForm" class="space-y-4" @submit="handleUpdate">
          <UFormGroup label="New Email" name="email">
            <UInput v-model="editForm.email" icon="i-heroicons-envelope" />
          </UFormGroup>

          <UFormGroup label="Avatar URL" name="avatarUrl" help="Must start with http:// or https://">
            <UInput v-model="editForm.avatarUrl" icon="i-heroicons-photo" />
          </UFormGroup>

          <UFormGroup label="Change Password" name="password" help="Leave blank to keep current">
            <UInput v-model="editForm.password" type="password" icon="i-heroicons-lock-closed" />
          </UFormGroup>

          <div class="flex justify-end gap-3 pt-4 border-t border-gray-100">
            <UButton color="gray" variant="ghost" label="Cancel" @click="isEditing = false" />
            <UButton type="submit" label="Save Changes" :loading="loading" icon="i-heroicons-check-circle" />
          </div>
        </UForm>
      </UCard>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * User Profile Page (Jules v4 - Aligned Version)
 * 1. 使用 useApi 保证调用一致性
 * 2. 完整保留 Template 结构
 */

definePageMeta({
  layout: 'user',
  middleware: 'auth'
})

interface UserProfileDTO {
  id: number;
  username: string;
  email: string;
  userRole: string;
  avatarUrl?: string;
}

const isEditing = ref(false)
const loading = ref(false)

const user = reactive<UserProfileDTO>({
  id: 0,
  username: '',
  email: '',
  userRole: 'USER',
  avatarUrl: ''
})

const editForm = reactive({
  email: '',
  avatarUrl: '',
  password: ''
})

// 使用封装好的 useApi
const fetchProfile = async () => {
  try {
    const { data } = await useApi<UserProfileDTO>('/users/profile')
    if (data.value) {
      Object.assign(user, data.value)
      editForm.email = data.value.email
      editForm.avatarUrl = data.value.avatarUrl || ''
    }
  } catch (err) {
    console.error('[JULES] Profile Sync Error:', err)
  }
}

const startEditing = () => {
  editForm.email = user.email
  editForm.avatarUrl = user.avatarUrl || ''
  editForm.password = ''
  isEditing.value = true
}

const handleUpdate = async () => {
  loading.value = true
  const cleanedPayload = {
    email: editForm.email,
    avatarUrl: editForm.avatarUrl.trim() === '' ? null : editForm.avatarUrl.trim(),
    password: editForm.password.trim() === '' ? null : editForm.password.trim()
  }

  try {
    await $fetch('/api/users/profile', {
      method: 'PUT',
      headers: { Authorization: `Bearer ${useCookie('auth_token').value}` },
      body: cleanedPayload
    })
    await fetchProfile()
    isEditing.value = false
    alert('Changes saved successfully!')
  } catch (err: any) {
    alert('Update failed: ' + (err.data?.message || 'Error'))
  } finally {
    loading.value = false
  }
}

onMounted(fetchProfile)
</script>