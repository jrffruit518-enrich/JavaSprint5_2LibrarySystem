<template>
  <div class="space-y-6">
    <AppBreadcrumb current-page-title="Profile Settings" />

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <UCard class="lg:col-span-1 h-fit">
        <div class="flex flex-col items-center py-4">
          <UAvatar
            src="https://avatars.githubusercontent.com/u/1?v=4"
            size="xl"
          />
          <h2 class="mt-4 text-xl font-bold text-highlighted">
            {{ user.name }}
          </h2>
          <p class="text-sm text-muted">
            ID: LIB-2026-001
          </p>
          <div class="mt-4 flex gap-2">
            <UBadge
              color="primary"
              variant="subtle"
            >
              Silver Member
            </UBadge>
          </div>
        </div>
      </UCard>

      <UCard class="lg:col-span-2">
        <template #header>
          <div class="flex items-center gap-2 font-semibold">
            <UIcon name="i-lucide-settings-2" />
            Account Security
          </div>
        </template>

        <UForm
          :state="user"
          class="space-y-4"
        >
          <UFormField label="Login Email">
            <UInput
              v-model="user.email"
              icon="i-lucide-mail"
            />
          </UFormField>

          <UFormField label="New Password">
            <UInput
              type="password"
              icon="i-lucide-lock"
              placeholder="••••••••"
            />
          </UFormField>

          <div class="flex justify-end pt-4 border-t border-default">
            <UButton
              label="Save Changes"
              icon="i-lucide-save"
              @click="handleUpdate"
            />
          </div>
        </UForm>
      </UCard>
    </div>
  </div>
</template>

<script setup>
/* User profile logic for the library project */
definePageMeta({ layout: 'user' })

const user = reactive({
  name: 'John Doe',
  email: 'john@example.com'
})

const handleUpdate = () => {
  /* Simple validation feedback */
  alert('Profile update request sent to server.')
}
</script>
