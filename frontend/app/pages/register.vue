<template>
  <UContainer class="flex justify-center items-center min-h-[60vh]">
    <UCard class="w-full max-w-md shadow-lg">
      <template #header>
        <div class="text-center">
          <h3 class="text-xl font-bold text-highlighted">
            创建新账号
          </h3>
          <p class="text-xs text-muted mt-1">
            Join the Library Management System
          </p>
        </div>
      </template>

      <form
        class="space-y-4"
        @submit.prevent="handleRegister"
      >
        <UFormField
          label="用户名"
          name="username"
        >
          <UInput
            v-model="registerForm.username"
            placeholder="请输入用户名"
            icon="i-lucide-user"
          />
        </UFormField>

        <UFormField
          label="邮箱"
          name="email"
        >
          <UInput
            v-model="registerForm.email"
            type="email"
            placeholder="email@example.com"
            icon="i-lucide-mail"
          />
        </UFormField>

        <UFormField
          label="密码"
          name="password"
        >
          <UInput
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            icon="i-lucide-lock"
          />
        </UFormField>

        <UButton
          type="submit"
          block
          color="primary"
          class="mt-6"
          :loading="isSubmitting"
        >
          立即注册
        </UButton>
      </form>

      <template #footer>
        <div class="text-center space-y-2">
          <p class="text-xs">
            已有账号？
            <ULink
              to="/"
              class="text-primary font-bold hover:underline"
            >返回登录</ULink>
          </p>
          <p
            v-if="errorMsg"
            class="text-[10px] text-red-500 bg-red-50 p-1 rounded"
          >
            [JULES DIAGNOSTIC]: {{ errorMsg }}
          </p>
        </div>
      </template>
    </UCard>
  </UContainer>
</template>

<script setup>
import { reactive, ref } from 'vue'

/**
 * 图书馆项目 - 注册页面 (Jules 修正版)
 * English Comment: Using proxy-based routing to ensure CORS compliance and consistent session handling.
 */

const registerForm = reactive({
  username: '',
  email: '',
  password: ''
})

const isSubmitting = ref(false)
const errorMsg = ref('')

const handleRegister = async () => {
  isSubmitting.value = true
  errorMsg.value = ''

  console.log('>>> [JULES REGISTER] Outgoing Request: /api/auth/register')

  try {
    // Jules Fix: 移除 ${apiBase}，直接使用 '/api' 触发 Nitro 代理
    // 这与 nuxt.config.ts 中的 devProxy 完美联动
    await $fetch('/api/auth/register', {
      method: 'POST',
      body: registerForm
    })

    console.log('>>> [JULES REGISTER] Success')
    alert('注册成功！正在跳转到登录页面...')
    await navigateTo('/')
  } catch (err) {
    console.error('>>> [JULES REGISTER ERROR]:', err)

    // Jules: 捕获后端 GlobalExceptionHandler 返回的详细信息
    const serverMessage = err.data?.message || '服务器连接异常'
    errorMsg.value = serverMessage
    alert('注册失败: ' + serverMessage)
  } finally {
    isSubmitting.value = false
  }
}
</script>
