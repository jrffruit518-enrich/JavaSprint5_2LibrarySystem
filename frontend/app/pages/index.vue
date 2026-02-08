<template>
  <div class="flex flex-col items-center justify-center py-20">
    <div class="text-center mb-10">
      <UIcon
        name="i-lucide-library"
        class="w-16 h-16 text-primary mx-auto mb-4"
      />
      <h1 class="text-4xl font-bold text-highlighted">
        Library Management System
      </h1>
      <p class="text-muted mt-2">
        Please select an option to continue
      </p>
    </div>

    <UCard class="w-full max-w-sm bg-elevated">
      <div class="flex flex-col gap-4">
        <UButton
          to="/register"
          size="xl"
          icon="i-lucide-user-plus"
          block
        >
          Create New Account
        </UButton>

        <USeparator label="OR" />

        <UButton
          to="/login"
          size="xl"
          variant="outline"
          icon="i-lucide-log-in"
          block
        >
          User Login
        </UButton>

        <div class="mt-6 pt-6 border-t border-dashed border-gray-300 dark:border-gray-700 space-y-3">
          <p class="text-center text-xs text-muted uppercase tracking-widest font-bold">
            Dev Quick Access
          </p>
          <div class="grid grid-cols-2 gap-2">
            <UButton
              label="Mock Admin"
              color="orange"
              variant="subtle"
              icon="i-lucide-shield-check"
              @click="quickLogin('admin')"
            />
            <UButton
              label="Mock User"
              color="blue"
              variant="subtle"
              icon="i-lucide-user"
              @click="quickLogin('user')"
            />
          </div>
        </div>
      </div>
    </UCard>
  </div>
</template>

<script setup>
/* 设置页面元数据 */
definePageMeta({
  title: 'Home - Library Project'
})

/* Quick Login logic for development */
const quickLogin = (role) => {
  const roleCookie = useCookie('user-role')

  if (role === 'admin') {
    // Simulate Admin Identity
    roleCookie.value = 'admin'
    navigateTo('/admin')
  } else {
    // Simulate Normal User Identity
    roleCookie.value = 'user'
    navigateTo('/user')
  }
}
</script>
