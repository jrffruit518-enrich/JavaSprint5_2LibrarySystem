<template>
  <div class="flex flex-col items-center justify-center min-h-[80vh] px-4">
    <div class="text-center mb-12 animate-spring-in">
      <div class="relative inline-block mb-4">
        <div class="absolute inset-0 bg-emerald-500/20 blur-2xl rounded-full"></div>
        <UIcon
          name="i-heroicons-building-library-solid"
          class="w-20 h-20 text-emerald-600 dark:text-emerald-400 relative z-10"
        />
      </div>
      <h1 class="text-5xl font-black tracking-tighter bg-gradient-to-r from-slate-900 to-slate-600 dark:from-white dark:to-slate-400 bg-clip-text text-transparent">
        Cloud<span class="text-emerald-600 dark:text-emerald-400">Library</span>
      </h1>
      <p class="text-xs font-black uppercase tracking-[0.4em] text-slate-400 mt-2">
        Integrated Management System
      </p>
    </div>

    <UCard 
      class="w-full max-w-sm glass-effect border-none shadow-2xl"
      :ui="{ base: 'overflow-hidden', body: { padding: 'p-8' } }"
    >
      <div class="flex flex-col gap-5">
        <UButton
          to="/register"
          size="xl"
          icon="i-heroicons-user-plus-solid"
          block
          color="emerald"
          class="btn-glow btn-shimmer font-black py-4 rounded-xl transition-all active:scale-95"
        >
          Create New Account
        </UButton>

        <div class="relative py-2">
          <UDivider label="SECURE ACCESS" :ui="{ label: 'text-[10px] font-black text-slate-400' }" />
        </div>

        <UButton
          size="xl"
          variant="soft"
          color="gray"
          icon="i-heroicons-arrow-left-on-rectangle-solid"
          block
          class="font-black py-4 rounded-xl hover:bg-emerald-500/10 hover:text-emerald-600 transition-all active:scale-95"
          @click="isLoginModalOpen = true"
        >
          User Login
        </UButton>
      </div>
    </UCard>

    <UModal v-model="isLoginModalOpen">
      <UCard 
        class="glass-effect border-none shadow-2xl"
        :ui="{ 
          ring: '', 
          divide: 'divide-y divide-white/5',
          header: { base: 'bg-white/40 dark:bg-white/5' }
        }"
      >
        <template #header>
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <UIcon name="i-heroicons-lock-closed-solid" class="text-emerald-500" />
              <h3 class="text-sm font-black uppercase tracking-widest text-slate-700 dark:text-white">Authentication</h3>
            </div>
            <UButton color="gray" variant="ghost" icon="i-heroicons-x-mark" @click="isLoginModalOpen = false" />
          </div>
        </template>

        <UForm
          :state="loginForm"
          class="space-y-6 py-4"
          @submit="handleLogin"
        >
          <UFormGroup label="Identity" name="username" :ui="{ label: { base: 'text-[10px] font-black uppercase text-slate-400' } }">
            <UInput
              v-model="loginForm.username"
              placeholder="Username"
              icon="i-heroicons-user-solid"
              size="lg"
              autofocus
            />
          </UFormGroup>

          <UFormGroup label="Security Key" name="password" :ui="{ label: { base: 'text-[10px] font-black uppercase text-slate-400' } }">
            <UInput
              v-model="loginForm.password"
              type="password"
              placeholder="••••••••"
              icon="i-heroicons-key-solid"
              size="lg"
            />
          </UFormGroup>

          <div class="flex justify-end gap-3 pt-4">
            <UButton
              label="Cancel"
              variant="ghost"
              class="font-bold"
              @click="isLoginModalOpen = false"
            />
            <UButton
              type="submit"
              label="Authorize"
              color="emerald"
              class="btn-glow btn-shimmer font-black px-8 rounded-lg"
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
 * Index Page (Jules v4.2 - Immersive Login + Shimmer Effect)
 */

const toast = useToast()
const isLoginModalOpen = ref(false)
const isPending = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  isPending.value = true
  
  const cookieOptions = { path: '/', maxAge: 86400, sameSite: 'lax' as const }
  const authCookie = useCookie('auth_token', cookieOptions)
  const roleCookie = useCookie('user_role', cookieOptions)
  const userIdCookie = useCookie('user_id', cookieOptions)

  try {
    const response = await $fetch<any>('/api/auth/login', {
      method: 'POST',
      body: loginForm
    })

    if (response.token) {
      authCookie.value = response.token
      userIdCookie.value = response.id 
      
      let role = response.role || (loginForm.username === 'admin' ? 'ROLE_ADMIN' : 'ROLE_MEMBER')
      if (!role.startsWith('ROLE_')) {
        role = `ROLE_${role.toUpperCase()}`
      }
      roleCookie.value = role

      toast.add({
        title: 'Access Granted',
        description: `Welcome back, ${loginForm.username}.`,
        color: 'emerald',
        icon: 'i-heroicons-check-circle'
      })

      isLoginModalOpen.value = false

      setTimeout(async () => {
        if (role === 'ROLE_ADMIN') {
          await navigateTo('/admin')
        } else {
          await navigateTo('/user')
        }
      }, 500)
    }
  } catch (error: any) {
    console.error('Login Error:', error)
    toast.add({
      title: 'Access Denied',
      description: error.data?.message || 'Invalid credentials. Please verify your identity.',
      color: 'rose',
      icon: 'i-heroicons-exclamation-triangle'
    })
  } finally {
    isPending.value = false
  }
}
</script>

<style scoped>
.animate-spring-in {
  animation: spring-in 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes spring-in {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>