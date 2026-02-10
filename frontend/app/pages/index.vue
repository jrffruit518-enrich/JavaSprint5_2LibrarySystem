没问题，我们先彻底解决 index.vue。

为了适应你之后的 Docker 部署和 SSR 渲染，我将代码改回了 Nuxt 官方推荐的 useCookie 模式，并加入了 path: '/'、sameSite: 'lax' 等关键参数。这能保证 Cookie 在容器化环境下依然有效，同时也解决了你提到的“跳转后 Cookie 丢失”的问题。

🛠️ 完整修正版 index.vue
代码段
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
 * Updated for Docker/SSR compatibility and fixed Cookie persistence.
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
  /* English Comment: Use useCookie with explicit path and sameSite
    to ensure visibility across sub-routes and containers.
  */
  const authCookie = useCookie('auth-token', {
    path: '/',
    maxAge: 60 * 60 * 24,
    sameSite: 'lax'
  })
  const roleCookie = useCookie('user-role', {
    path: '/',
    maxAge: 60 * 60 * 24,
    sameSite: 'lax'
  })

  try {
    // English Comment: Authentication API call
    const response = await $fetch(`${apiBase}/api/auth/login`, {
      method: 'POST',
      body: loginForm
    })

    if (response.token) {
      console.log('Login Success, token received.')

      // Assign values to cookies
      authCookie.value = response.token

      // Role logic: Default to 'user' unless admin conditions are met
      let role = 'user'
      if (loginForm.username === 'admin' || response.role === 'ROLE_ADMIN') {
        role = 'admin'
      }
      roleCookie.value = role

      console.log('Cookies saved. Redirecting to:', role)

      isLoginModalOpen.value = false

      // Use await to ensure navigation starts after cookie state is committed
      await navigateTo(`/${role}`)
    }
  } catch (error) {
    /* English Comment: Error handling for failed connection or credentials */
    console.error('Login Error:', error)
    alert('登录失败：账号密码错误或后端服务未就绪')
  }
}
</script>
