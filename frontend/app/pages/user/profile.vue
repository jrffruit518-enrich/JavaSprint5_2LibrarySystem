这是为你加固后的完整 profile.vue 代码。我统一了请求逻辑，增强了诊断输出，并确保字段与后端的 UserResponse 和 UserProfileRequest 能够完美对齐。

代码段
<template>
  <div class="space-y-6">
    <AppBreadcrumb current-page-title="Personal Profile" />

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <UCard class="lg:col-span-1 h-fit">
        <div class="flex flex-col items-center py-8">
          <UAvatar
            :alt="user.username"
            size="3xl"
            class="ring-4 ring-primary/20 text-2xl font-bold"
          />
          <h2 class="mt-4 text-2xl font-bold text-highlighted text-center">
            {{ user.username || 'Loading...' }}
          </h2>
          <p class="text-sm text-muted font-mono mt-1">
            ID: LIB-2026-{{ String(user.id || 0).padStart(3, '0') }}
          </p>
        </div>
      </UCard>

      <UCard class="lg:col-span-2">
        <template #header>
          <div class="flex justify-between items-center">
            <div class="flex items-center gap-2 font-semibold text-lg text-highlighted">
              <UIcon name="i-lucide-user-cog" />
              Account Information
            </div>
            <UButton
              v-if="!isEditing"
              variant="ghost"
              icon="i-lucide-edit"
              label="Edit Profile"
              @click="startEditing"
            />
          </div>
        </template>

        <div
          v-if="!isEditing"
          class="space-y-6 py-2"
        >
          <div class="grid grid-cols-2 gap-4 border-b border-default pb-4">
            <span class="text-muted text-sm uppercase font-semibold">Username</span>
            <span class="text-highlighted font-medium">{{ user.username || '---' }}</span>
          </div>
          <div class="grid grid-cols-2 gap-4 border-b border-default pb-4">
            <span class="text-muted text-sm uppercase font-semibold">Email Address</span>
            <span class="text-highlighted font-medium">{{ user.email || '---' }}</span>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <span class="text-muted text-sm uppercase font-semibold">Account Status</span>
            <span class="text-green-500 font-medium flex items-center gap-1">
              <span class="h-2 w-2 rounded-full bg-green-500" /> Active
            </span>
          </div>
        </div>

        <UForm
          v-else
          :state="editForm"
          class="space-y-4"
          @submit="handleUpdate"
        >
          <UFormField
            label="New Email"
            name="email"
          >
            <UInput
              v-model="editForm.email"
              icon="i-lucide-mail"
              placeholder="Enter new email"
            />
          </UFormField>

          <UFormField
            label="Change Password"
            name="password"
            help="Leave blank to keep current password"
          >
            <UInput
              v-model="editForm.password"
              type="password"
              icon="i-lucide-lock"
              placeholder="••••••••"
            />
          </UFormField>

          <div class="flex justify-end gap-3 pt-4 border-t border-default">
            <UButton
              color="neutral"
              variant="ghost"
              label="Cancel"
              @click="isEditing = false"
            />
            <UButton
              type="submit"
              label="Save Changes"
              icon="i-lucide-save"
              :loading="loading"
            />
          </div>
        </UForm>
      </UCard>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

definePageMeta({
  layout: 'user',
  middleware: 'auth'
})

const isEditing = ref(false)
const loading = ref(false)

const user = reactive({
  id: null,
  username: '',
  email: ''
})

const editForm = reactive({
  email: '',
  password: ''
})

/**
 * 1. Fetch Profile
 * English Comment: Uses $fetch with explicit headers to ensure proxy compatibility
 */
const fetchProfile = async () => {
  try {
    const token = useCookie('auth-token').value
    console.log('>>> [DEBUG] Profile Request: GET /api/users/me')

    const response = await $fetch('/api/users/me', {
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: 'application/json'
      },
      onResponseError({ response }) {
        console.error('>>> [DEBUG] Server Failed (500/404):', response._data)
      }
    })

    console.log('>>> [DEBUG] Profile Data Received:', response)

    if (response) {
      user.id = response.id
      user.username = response.username
      user.email = response.email
      // Sync form initially
      editForm.email = response.email
    }
  } catch (err) {
    console.error('>>> [DEBUG] Fetch Profile Exception:', err)
  }
}

const startEditing = () => {
  editForm.email = user.email
  editForm.password = ''
  isEditing.value = true
}

/**
 * 2. Update Profile
 * English Comment: PUT request to update current user information
 */
const handleUpdate = async () => {
  loading.value = true
  try {
    const token = useCookie('auth-token').value

    await $fetch('/api/users/me', {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: {
        email: editForm.email,
        // Only send password if the user typed something
        password: editForm.password.trim() || null
      }
    })

    console.log('>>> [DEBUG] Update successful')
    await fetchProfile()
    isEditing.value = false
    alert('Changes saved successfully.')
  } catch (err) {
    console.error('>>> [DEBUG] Update failed:', err.data || err)
    alert('Update failed: ' + (err.data?.message || 'Server Error'))
  } finally {
    loading.value = false
  }
}

onMounted(fetchProfile)
</script>
