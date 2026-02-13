<template>
  <div class="flex flex-col items-center justify-center py-20">
    <div class="text-center mb-10">
      <UIcon
        name="i-heroicons-building-library"
        class="w-16 h-16 text-primary mx-auto mb-4"
      />
      <h1 class="text-4xl font-bold text-green-500">
        Library Management System
      </h1>
      <p class="text-gray-500 mt-2">
        Welcome! Please manage your collection or start reading.
      </p>
    </div>

    <UCard class="w-full max-w-sm border-2 border-primary/10 shadow-xl">
      <div class="flex flex-col gap-4">
        <UButton
          to="/register"
          size="xl"
          icon="i-heroicons-user-plus"
          block
        >
          Create New Account
        </UButton>

        <UDivider label="OR" />

        <UButton
          size="xl"
          variant="outline"
          icon="i-heroicons-arrow-left-on-rectangle"
          block
          @click="isLoginModalOpen = true"
        >
          User Login
        </UButton>
      </div>
    </UCard>

    <UModal v-model="isLoginModalOpen">
      <UCard :ui="{ ring: '', divide: 'divide-y divide-gray-100 dark:divide-gray-800' }">
        <template #header>
          <div class="flex items-center gap-2">
            <UIcon name="i-heroicons-lock-closed" class="text-primary" />
            <h3 class="text-base font-semibold">User Login</h3>
          </div>
        </template>

        <UForm
          :state="loginForm"
          class="space-y-4"
          @submit="handleLogin"
        >
          <UFormGroup label="Username" name="username" required>
            <UInput
              v-model="loginForm.username"
              placeholder="Enter your username"
              icon="i-heroicons-user"
              autofocus
            />
          </UFormGroup>

          <UFormGroup label="Password" name="password" required>
            <UInput
              v-model="loginForm.password"
              type="password"
              icon="i-heroicons-key"
            />
          </UFormGroup>

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
              :loading="isPending"
            />
          </div>
        </UForm>
      </UCard>
    </UModal>
  </div>
</template>

<script setup lang="ts">
/**
 * 图书馆项目 - 登录入口 (Standard v3)
 * Jules Fix: 
 * 1. 注入 user_id Cookie 以便后续页面调用 MongoDB。
 * 2. 统一 Cookie 配置。
 */

const isLoginModalOpen = ref(false)
const isPending = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  isPending.value = true
  
  // Cookie 配置统一设置
  const cookieOptions = { path: '/', maxAge: 86400, sameSite: 'lax' as const }
  const authCookie = useCookie('auth_token', cookieOptions)
  const roleCookie = useCookie('user_role', cookieOptions)
  const userIdCookie = useCookie('user_id', cookieOptions) // Jules Fix: 新增 ID Cookie

  try {
    const response = await $fetch<any>('/api/auth/login', {
      method: 'POST',
      body: loginForm
    })

    if (response.token) {
      // 1. 存储 Token
      authCookie.value = response.token
      
      // 2. 存储 User ID (关键修复：从后端响应中提取 id)
      // 如果你的后端返回的字段是 response.id，这里就对了
      userIdCookie.value = response.id 
      
      // 3. 存储角色逻辑
      let role = response.role || (loginForm.username === 'admin' ? 'ROLE_ADMIN' : 'ROLE_MEMBER')
      if (!role.startsWith('ROLE_')) {
        role = `ROLE_${role.toUpperCase()}`
      }
      roleCookie.value = role

      isLoginModalOpen.value = false

      // 4. 跳转逻辑
      if (role === 'ROLE_ADMIN') {
        await navigateTo('/admin')
      } else {
        await navigateTo('/user/books')
      }
    }
  } catch (error: any) {
    console.error('Login Error:', error)
    alert(error.data?.message || 'Login failed. Please check your credentials.')
  } finally {
    isPending.value = false
  }
}
</script>