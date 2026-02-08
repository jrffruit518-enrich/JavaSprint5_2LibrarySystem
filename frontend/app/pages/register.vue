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
              placeholder="e.g. admin or user"
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
/* 页面元数据 */
definePageMeta({
  title: 'Home - Library Project'
})

/* 控制弹窗显示的状态变量 */
const isLoginModalOpen = ref(false)

/* 登录表单数据模型 */
const loginForm = reactive({
  username: '',
  password: ''
})

/* 处理登录模拟逻辑 */
const handleLogin = () => {
  // 简单的路由分流模拟
  if (loginForm.username === 'admin') {
    navigateTo('/admin')
  } else {
    navigateTo('/user')
  }

  // 登录后关闭弹窗
  isLoginModalOpen.value = false
}
</script>
