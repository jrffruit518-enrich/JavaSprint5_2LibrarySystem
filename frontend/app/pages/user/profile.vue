<template>
  <div class="space-y-6 animate-spring-in">
    <div class="flex justify-between items-center bg-slate-900 text-white p-6 rounded-xl shadow-lg border border-slate-800 mb-8">
      <div class="flex items-center gap-4">
        <div class="bg-emerald-500/20 p-3 rounded-lg">
          <UIcon name="i-heroicons-user-circle-solid" class="w-8 h-8 text-emerald-500" />
        </div>
        <div>
          <h1 class="text-2xl font-black leading-tight uppercase tracking-tighter">Personal Profile</h1>
          <p class="text-sm text-slate-400 font-medium">Identity & Account Preferences</p>
        </div>
      </div>
      <nav class="hidden md:flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-slate-500">
        User <UIcon name="i-heroicons-chevron-right" /> <span class="text-emerald-500">Profile</span>
      </nav>
    </div>

    <div v-if="userProfile" class="grid grid-cols-1 lg:grid-cols-12 gap-8">
      
      <UCard class="lg:col-span-4 h-fit glass-effect !border-none shadow-2xl overflow-hidden" :ui="{ body: { padding: 'p-0' } }">
        <div class="bg-gradient-to-br from-emerald-500/10 to-transparent p-8 flex flex-col items-center">
          <UAvatar
            :src="userProfile.avatarUrl"
            :alt="userProfile.username"
            size="3xl"
            class="ring-4 ring-white dark:ring-slate-800 shadow-2xl text-2xl font-black bg-emerald-100"
            :ui="{ size: { '3xl': 'h-32 w-32' } }"
          />
          <h2 class="mt-6 text-2xl font-black text-slate-900 dark:text-white uppercase tracking-tighter">
            {{ userProfile.username }}
          </h2>
          <div class="mt-2 flex items-center gap-2 bg-slate-100 dark:bg-slate-800 px-3 py-1 rounded-full">
            <span class="text-[10px] font-mono font-bold text-slate-500">
              LIB-{{ String(userProfile.id || 0).padStart(4, '0') }}
            </span>
          </div>
          <UBadge class="mt-4 font-black uppercase tracking-widest text-[10px] px-4" variant="solid" color="emerald">
            {{ userProfile.userRole }}
          </UBadge>
        </div>
        
        <div class="p-6 border-t border-slate-100 dark:border-white/5 space-y-4">
          <div class="flex justify-between items-center">
            <span class="text-[10px] font-black text-slate-400 uppercase">Account Security</span>
            <span class="text-[10px] font-black text-emerald-500 uppercase">High</span>
          </div>
          <UMeter :value="100" color="emerald" size="xs" />
        </div>
      </UCard>

      <UCard class="lg:col-span-8 glass-effect !border-none shadow-2xl" :ui="{ header: { padding: 'p-6' } }">
        <template #header>
          <div class="flex justify-between items-center">
            <div class="flex items-center gap-2 font-black text-slate-900 dark:text-white uppercase tracking-tight">
              <UIcon name="i-heroicons-cog-6-tooth" class="text-emerald-500 w-5 h-5" />
              Account Settings
            </div>
            <UButton
              v-if="!isEditing"
              variant="soft"
              color="emerald"
              icon="i-heroicons-pencil-square"
              label="Edit Details"
              class="font-black uppercase tracking-widest text-[10px]"
              @click="startEditing"
            />
          </div>
        </template>

        <div v-if="!isEditing" class="space-y-8 py-4 px-2">
          <div class="flex flex-col gap-2 border-b border-slate-100 dark:border-white/5 pb-6">
            <span class="text-slate-400 text-[10px] uppercase font-black tracking-[0.2em]">Registered Email</span>
            <span class="text-slate-900 dark:text-white font-black text-lg">{{ userProfile.email }}</span>
          </div>
          
          <div class="flex flex-col gap-2 border-b border-slate-100 dark:border-white/5 pb-6">
            <span class="text-slate-400 text-[10px] uppercase font-black tracking-[0.2em]">Avatar Resource</span>
            <span class="text-slate-500 dark:text-slate-400 font-mono text-xs truncate">
              {{ userProfile.avatarUrl || 'DEFAULT_SYSTEM_AVATAR' }}
            </span>
          </div>

          <div class="flex flex-col gap-2">
            <span class="text-slate-400 text-[10px] uppercase font-black tracking-[0.2em]">Membership Status</span>
            <div class="flex items-center gap-3">
               <div class="h-10 w-10 rounded-xl bg-emerald-500/10 flex items-center justify-center">
                  <UIcon name="i-heroicons-check-badge-solid" class="text-emerald-500 w-6 h-6" />
               </div>
               <div>
                 <p class="font-black text-slate-900 dark:text-white uppercase text-sm">Valid Member</p>
                 <p class="text-[10px] text-slate-400 font-bold uppercase tracking-tight">Access to all library collections</p>
               </div>
            </div>
          </div>
        </div>

        <UForm v-else :state="editForm" class="space-y-6 py-4 px-2" @submit="handleUpdate">
          <UFormGroup label="Email Address" name="email" :ui="{ label: { base: 'font-black uppercase text-[10px] text-slate-500' } }">
            <UInput v-model="editForm.email" icon="i-heroicons-envelope" color="emerald" variant="none" class="bg-slate-100 dark:bg-slate-800 rounded-lg" input-class="font-bold" />
          </UFormGroup>

          <UFormGroup label="Avatar URL" name="avatarUrl" :ui="{ label: { base: 'font-black uppercase text-[10px] text-slate-500' } }">
            <UInput v-model="editForm.avatarUrl" icon="i-heroicons-photo" color="emerald" variant="none" class="bg-slate-100 dark:bg-slate-800 rounded-lg" input-class="font-bold" />
          </UFormGroup>

          <UFormGroup label="Security Password" name="password" help="Keep empty to remain unchanged" :ui="{ label: { base: 'font-black uppercase text-[10px] text-slate-500' }, help: 'text-[10px] font-bold text-slate-400 italic' }">
            <UInput v-model="editForm.password" type="password" icon="i-heroicons-lock-closed" color="emerald" variant="none" class="bg-slate-100 dark:bg-slate-800 rounded-lg" input-class="font-bold" />
          </UFormGroup>

          <div class="flex justify-end gap-3 pt-6 border-t border-slate-100 dark:border-white/5">
            <UButton color="gray" variant="ghost" label="Cancel" class="font-black uppercase text-[10px]" @click="isEditing = false" />
            <UButton 
              type="submit" 
              color="emerald" 
              label="Sync Profile" 
              :loading="loading" 
              icon="i-heroicons-check-circle-solid" 
              class="font-black uppercase text-[10px] px-6 rounded-lg"
            />
          </div>
        </UForm>
      </UCard>
    </div>

    <div v-else class="flex flex-col items-center justify-center py-32 bg-slate-50 dark:bg-slate-900/50 rounded-3xl border-2 border-dashed border-slate-200 dark:border-slate-800">
       <UIcon name="i-heroicons-arrow-path" class="animate-spin text-4xl text-emerald-500 mb-4" />
       <p class="text-slate-500 font-black uppercase tracking-widest text-[10px]">Synchronizing Secure Data...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * User Profile - UI Consistency v3 (Jules Optimized)
 */

definePageMeta({ layout: 'user', middleware: 'auth' })
const toast = useToast()

interface UserProfileDTO {
  id: number;
  username: string;
  email: string;
  userRole: string;
  avatarUrl?: string;
}

const isEditing = ref(false)
const loading = ref(false)

const { data: userProfile, refresh } = await useApi<UserProfileDTO>('/users/profile', {
  lazy: false,
  key: 'user-profile-sync-v3'
})

const editForm = reactive({
  email: '',
  avatarUrl: '',
  password: ''
})

watchEffect(() => {
  if (userProfile.value) {
    editForm.email = userProfile.value.email
    editForm.avatarUrl = userProfile.value.avatarUrl || ''
  }
})

const startEditing = () => {
  if (userProfile.value) {
    editForm.email = userProfile.value.email
    editForm.avatarUrl = userProfile.value.avatarUrl || ''
    editForm.password = ''
    isEditing.value = true
  }
}

const handleUpdate = async () => {
  loading.value = true
  const payload = {
    email: editForm.email,
    avatarUrl: editForm.avatarUrl?.trim() || null,
    password: editForm.password?.trim() || null
  }

  try {
    await $fetch('/api/users/profile', {
      method: 'PUT',
      headers: { Authorization: `Bearer ${useCookie('auth_token').value}` },
      body: payload
    })
    
    await refresh()
    isEditing.value = false
    toast.add({
      title: 'Profile Updated',
      description: 'Your changes have been synchronized successfully.',
      color: 'emerald',
      icon: 'i-heroicons-check-circle-solid'
    })
  } catch (err: any) {
    toast.add({
      title: 'Sync Failed',
      description: err.data?.message || 'Server connection error.',
      color: 'rose',
      icon: 'i-heroicons-x-circle-solid'
    })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.glass-effect {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}
.dark .glass-effect {
  background: rgba(15, 23, 42, 0.8);
}
</style>