<template>
  <UContainer class="flex justify-center items-center min-h-[60vh]">
    <UCard class="w-full max-w-md">
      <template #header>
        <h3 class="text-xl font-bold">
          创建新账号 (Register)
        </h3>
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
          />
        </UFormField>

        <UButton
          type="submit"
          block
          color="primary"
          class="mt-6"
        >
          立即注册
        </UButton>
      </form>

      <template #footer>
        <p class="text-xs text-center">
          已有账号？<ULink
            to="/"
            class="text-primary"
          >返回登录</ULink>
        </p>
      </template>
    </UCard>
  </UContainer>
</template>

<script setup>
import { ref, reactive } from 'vue'

/**
 * 图书馆项目 - 注册页面逻辑
 */

const registerForm = reactive({
  username: '',
  email: '',
  password: ''
})

const config = useRuntimeConfig()
const apiBase = config.public.apiBase

const handleRegister = async () => {
  try {
    // 调用后端的 AuthController @PostMapping("/register")
    await $fetch(`${apiBase}/api/auth/register`, {
      method: 'POST',
      body: {
        username: registerForm.username,
        email: registerForm.email,
        password: registerForm.password
      }
    })

    alert('注册成功！正在跳转到登录页面...')
    await navigateTo('/') // 注册完跳回首页去登录
  } catch (err) {
    console.error('注册失败:', err)
    // 这里的提示能帮你快速定位是后端 400 还是 500 错误
    alert('注册失败: ' + (err.data?.message || '请检查后端服务是否开启'))
  }
}
</script>
