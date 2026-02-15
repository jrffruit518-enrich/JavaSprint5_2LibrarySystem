<template>
  <UContainer class="flex justify-center items-center min-h-[90vh] animate-spring-in">
    <UCard 
      class="w-full max-w-md glass-effect border-none shadow-2xl"
      :ui="{ 
        base: 'relative overflow-hidden',
        ring: '', 
        divide: 'divide-y divide-black/5 dark:divide-white/5',
        header: { base: 'bg-white/40 dark:bg-white/5' },
        footer: { base: 'bg-white/40 dark:bg-white/5' }
      }"
    >
      <template #header>
        <div class="text-center py-2">
          <div class="inline-flex p-3 bg-emerald-500 rounded-2xl shadow-lg shadow-emerald-500/20 mb-4 relative overflow-hidden group">
             <div class="absolute inset-0 bg-white/20 translate-x-[-100%] group-hover:translate-x-[100%] transition-transform duration-700"></div>
            <UIcon name="i-heroicons-user-plus-solid" class="w-8 h-8 text-white" />
          </div>
          <h3 class="text-2xl font-black text-slate-900 dark:text-white uppercase tracking-tighter">
            Create Account
          </h3>
          <p class="text-[10px] font-black text-emerald-600 dark:text-emerald-400 uppercase tracking-[0.2em] mt-1">
            Join our library community
          </p>
        </div>
      </template>

      <form class="space-y-5 py-4" @submit.prevent="handleRegister">
        <UFormGroup label="Username" name="username" required :ui="{ label: { base: 'text-[10px] font-black uppercase text-slate-400' } }">
          <UInput
            v-model="registerForm.username" 
            placeholder="Enter your username"
            icon="i-heroicons-user"
            size="lg"
            autofocus
          />
        </UFormGroup>

        <UFormGroup label="Email Address" name="email" required :ui="{ label: { base: 'text-[10px] font-black uppercase text-slate-400' } }">
          <UInput
            v-model="registerForm.email"
            type="email"
            placeholder="email@example.com"
            icon="i-heroicons-envelope"
            size="lg"
          />
        </UFormGroup>

        <UFormGroup label="Security Key" name="password" required :ui="{ label: { base: 'text-[10px] font-black uppercase text-slate-400' } }">
          <UInput
            v-model="registerForm.password"
            type="password"
            placeholder="Min. 6 characters"
            icon="i-heroicons-lock-closed"
            size="lg"
          />
        </UFormGroup>

        <UButton
          type="submit"
          block
          color="emerald"
          class="mt-8 btn-glow btn-shimmer font-black py-3 rounded-xl transition-all active:scale-95"
          size="lg"
          :loading="isSubmitting"
        >
          Initialize Account
        </UButton>
      </form>

      <template #footer>
        <div class="text-center space-y-4">
          <p class="text-xs font-bold text-slate-500">
            Already have an account?
            <ULink to="/" class="text-emerald-600 dark:text-emerald-400 font-black hover:underline underline-offset-4">
              Return to Login
            </ULink>
          </p>
          
          <Transition name="fade">
            <p v-if="errorMsg" class="text-[10px] font-black text-rose-500 uppercase tracking-widest animate-pulse">
              {{ errorMsg }}
            </p>
          </Transition>
        </div>
      </template>
    </UCard>
  </UContainer>
</template>

<script setup lang="ts">
/**
 * Register Page (Logic Simplified v4.5 + Visual Polish)
 * Integrated btn-shimmer for enhanced UI interaction.
 */

const toast = useToast()
const registerForm = reactive({
  username: '',
  email: '',
  password: ''
})

const isSubmitting = ref(false)
const errorMsg = ref('')

const handleRegister = async () => {
  if (registerForm.password.length < 6) {
    errorMsg.value = 'Password must be at least 6 characters'
    toast.add({ 
      title: 'Security Alert', 
      description: errorMsg.value, 
      color: 'rose',
      icon: 'i-heroicons-shield-exclamation'
    })
    return
  }

  isSubmitting.value = true
  errorMsg.value = ''

  try {
    await $fetch('/api/auth/register', {
      method: 'POST',
      body: registerForm
    })

    toast.add({
      title: 'Registration Success',
      description: 'Your identity has been verified. Redirecting...',
      color: 'emerald',
      icon: 'i-heroicons-check-badge'
    })

    setTimeout(async () => {
      await navigateTo('/')
    }, 1500)

  } catch (err: any) {
    errorMsg.value = 'Registration failed: The username or email already exists.'
    toast.add({ 
      title: 'Identity Conflict', 
      description: 'This account could not be created. Please use a different username or email.', 
      color: 'rose',
      icon: 'i-heroicons-exclamation-triangle'
    })
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
/* 局部样式保留，确保玻璃拟态效果 */
.glass-effect {
  @apply bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-white/20;
}

.btn-glow {
  @apply shadow-lg shadow-emerald-500/20 hover:shadow-emerald-500/40;
}

@keyframes spring-in {
  0% { transform: scale(0.95); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
.animate-spring-in {
  animation: spring-in 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>