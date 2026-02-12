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
        Welcome! Please manage your collection or start reading.
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
          size="xl"
          variant="outline"
          icon="i-lucide-log-in"
          block
          @click="isLoginModalOpen = true"
        >
          User Login
        </UButton>
      </div>
    </UCard>

    <UModal
      v-model:open="isLoginModalOpen"
      title="User Login"
    >
      <template #content>
        <UForm
          :state="loginForm"
          class="space-y-4"
          @submit="handleLogin"
        >
          <UFormField
            label="Username"
            name="username"
          >
            <UInput
              v-model="loginForm.username"
              placeholder="e.g. admin"
              autofocus
            />
          </UFormField>

          <UFormField
            label="Password"
            name="password"
          >
            <UInput
              v-model="loginForm.password"
              type="password"
            />
          </UFormField>

          <div class="flex justify-end gap-3 mt-6">
            <UButton
              label="Cancel"
              variant="ghost"
              @click="isLoginModalOpen = false"
            />
            <UButton
              type="submit"
              label="Sign In"
              color="primary"
            />
          </div>
        </UForm>
      </template>
    </UModal>
  </div>
</template>

<script setup>
/**
 * 图书馆项目 (Library Project) - 首页 index.vue
 * Core Fix: Standardized Cookie names and paths to ensure cross-page access.
 */

/* 1. Configuration & State */
const config = useRuntimeConfig()
const apiBase = config.public.apiBase

const isLoginModalOpen = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

/* 2. Login Logic */
const handleLogin = async () => {
  // English Comment: Standardize naming to 'auth_token' and 'user_role' (underscore)
  const authCookie = useCookie('auth_token', {
    path: '/',
    maxAge: 60 * 60 * 24,
    sameSite: 'lax'
  })
  const roleCookie = useCookie('user_role', {
    path: '/',
    maxAge: 60 * 60 * 24,
    sameSite: 'lax'
  })

  try {
    const response = await $fetch(`${apiBase}/api/auth/login`, {
      method: 'POST',
      body: loginForm
    })

    if (response.token) {
      console.log('Login Success, token received.')

      // Assign values to cookies
      authCookie.value = response.token

      /**
       * English Comment: Critical Fix -
       * Standardize role check logic.
       */
      let role = 'MEMBER'
      if (loginForm.username === 'admin' || response.role === 'ROLE_ADMIN' || response.role === 'ADMIN') {
        role = 'ADMIN'
      }
      roleCookie.value = role

      console.log('Cookies saved. Redirecting with Role:', role)
      isLoginModalOpen.value = false

      if (role === 'ADMIN') {
        await navigateTo('/admin')
      } else {
        await navigateTo('/user')
      }
    }
  } catch (error) {
    console.error('Login Error:', error)
    alert('登录失败：账号密码错误或后端服务未就绪')
  }
}
</script>
