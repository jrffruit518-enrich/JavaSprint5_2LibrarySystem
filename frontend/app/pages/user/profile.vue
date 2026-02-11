<template>
  <div class="space-y-6">
    <AppBreadcrumb current-page-title="Personal Profile" />

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <UCard class="lg:col-span-1 h-fit">
        <div class="flex flex-col items-center py-8">
          <UAvatar
            :src="user.avatarUrl"
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
          <UBadge
            class="mt-2"
            variant="subtle"
            color="primary"
          >
            {{ user.userRole }}
          </UBadge>
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

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useApi, type UserProfileDTO } from '~/composables/useApi'

definePageMeta({
  layout: 'user',
  middleware: 'auth'
})

const isEditing = ref(false)
const loading = ref(false)

// Initialize with UserProfileDTO structure
const user = reactive<UserProfileDTO>({
  id: 0,
  username: '',
  email: '',
  userRole: 'MEMBER' as any,
  avatarUrl: ''
})

const editForm = reactive({
  email: '',
  password: ''
})

/**
 * 1. Fetch Profile
 * English Comment: Uses the centralized useApi composable for consistency and security.
 */
const fetchProfile = async () => {
  try {
    // Note: URL matches backend @GetMapping("/profile")
    const { data, error } = await useApi<UserProfileDTO>('/api/users/profile')

    if (error.value) {
      console.error('>>> [DEBUG] Fetch Profile Error:', error.value)
      return
    }

    if (data.value) {
      Object.assign(user, data.value)
      editForm.email = data.value.email
      console.log('>>> [DEBUG] Profile Data Synced:', user)
    }
  } catch (err) {
    console.error('>>> [DEBUG] Unexpected Exception:', err)
  }
}

const startEditing = () => {
  editForm.email = user.email
  editForm.password = ''
  isEditing.value = true
}

/**
 * 2. Update Profile
 * English Comment: Submit updated email/password using type-safe useApi.
 */
const handleUpdate = async () => {
  loading.value = true
  try {
    const { error } = await useApi('/api/users/profile', {
      method: 'PUT',
      body: {
        email: editForm.email,
        password: editForm.password.trim() || null
      }
    })

    if (error.value) throw error.value

    console.log('>>> [DEBUG] Update successful')
    await fetchProfile()
    isEditing.value = false
    alert('Changes saved successfully.')
  } catch (err: any) {
    console.error('>>> [DEBUG] Update failed:', err)
    alert('Update failed: ' + (err.data?.message || 'Server Error'))
  } finally {
    loading.value = false
  }
}

onMounted(fetchProfile)
</script>
