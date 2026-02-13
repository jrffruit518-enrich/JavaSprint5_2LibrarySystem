<template>
  <UContainer class="flex justify-center items-center min-h-[80vh]">
    <UCard class="w-full max-w-md shadow-xl border-2 border-primary/10">
      <template #header>
        <div class="text-center">
          <UIcon name="i-heroicons-user-plus" class="w-10 h-10 text-primary mx-auto mb-2" />
          <h3 class="text-xl font-bold text-green-500">创建新账号</h3>
          <p class="text-xs text-gray-500 mt-1">Join our library community today</p>
        </div>
      </template>

      <form class="space-y-4" @submit.prevent="handleRegister">
        <UFormGroup label="用户名" name="username" required>
          <UInput
            v-model="registerForm.username"
            placeholder="请输入用户名"
            icon="i-heroicons-user"
            autofocus
          />
        </UFormGroup>

        <UFormGroup label="邮箱" name="email" required>
          <UInput
            v-model="registerForm.email"
            type="email"
            placeholder="email@example.com"
            icon="i-heroicons-envelope"
          />
        </UFormGroup>

        <UFormGroup label="密码" name="password" required>
          <UInput
            v-model="registerForm.password"
            type="password"
            placeholder="至少6位字符"
            icon="i-heroicons-lock-closed"
          />
        </UFormGroup>

        <UButton
          type="submit"
          block
          color="primary"
          class="mt-6"
          size="lg"
          :loading="isSubmitting"
        >
          立即注册
        </UButton>
      </form>

      <template #footer>
        <div class="text-center space-y-3">
          <p class="text-sm text-gray-600">
            已有账号？
            <ULink to="/" class="text-primary font-bold hover:underline">返回登录</ULink>
          </p>
          <UAlert
            v-if="errorMsg"
            icon="i-heroicons-exclamation-triangle"
            color="red"
            variant="soft"
            :title="errorMsg"
            :ui="{ title: 'text-[10px]' }"
          />
        </div>
      </template>
    </UCard>
  </UContainer>
</template>

<script setup lang="ts">
/**
 * 图书馆项目 - 注册页面 (Jules Standard v2)
 */
const registerForm = reactive({
  username: '',
  email: '',
  password: ''
})

const isSubmitting = ref(false)
const errorMsg = ref('')

const handleRegister = async () => {
  // 基础校验
  if (registerForm.password.length < 6) {
    errorMsg.value = '密码长度至少需要 6 位'
    return
  }

  isSubmitting.value = true
  errorMsg.value = ''

  try {
    await $fetch('/api/auth/register', {
      method: 'POST',
      body: registerForm
    })

    // 成功提示并清理
    alert('注册成功！正在跳转到登录页面...')
    await navigateTo('/')
  } catch (err: any) {
    // English Comment: Capture backend validation errors
    errorMsg.value = err.data?.message || '注册失败，请检查网络或用户名是否已存在'
  } finally {
    isSubmitting.value = false
  }
}
</script>
