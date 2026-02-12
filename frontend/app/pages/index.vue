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
 * Jules Fix:
 * 1. Fixed UDivider to USeparator (Nuxt UI v3).
 * 2. Standardized proxy path /api/auth/login.
 */

const isLoginModalOpen = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  // Jules: Cookie 命名与 users.vue 诊断逻辑保持高度一致
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
    // Jules Fix: 请求路径保持 /api 前缀，后端 AuthController 已配置支持
    const response = await $fetch('/api/auth/login', {
      method: 'POST',
      body: loginForm
    })

    if (response.token) {
      console.log('>>> [JULES LOGIN] Success, token saved.')
      authCookie.value = response.token

      let role = 'MEMBER'
      // Jules: 自动匹配 Spring Security 角色前缀或原始名
      if (loginForm.username === 'admin' || response.role === 'ROLE_ADMIN' || response.role === 'ADMIN') {
        role = 'ADMIN'
      }
      roleCookie.value = role

      isLoginModalOpen.value = false

      if (role === 'ADMIN') {
        // 跳转到我们加了监控条的诊断页面
        await navigateTo('/admin/users')
      } else {
        await navigateTo('/user')
      }
    }
  } catch (error) {
    console.error('>>> [JULES LOGIN ERROR]:', error)
    alert('登录失败：账号密码错误或后端服务未就绪')
  }
}
</script>
