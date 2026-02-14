<template>
  <UModal v-model="isOpen" :ui="{ shadow: 'shadow-2xl' }">
    <UCard :ui="{ 
      base: 'glass-effect border-white/20 overflow-hidden',
      ring: '', 
      divide: 'divide-y divide-black/5 dark:divide-white/5',
      header: { base: 'bg-white/40 dark:bg-white/5' },
      body: { base: 'bg-transparent' },
      footer: { base: 'bg-white/40 dark:bg-white/5' }
    }">
      <template #header>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <div class="p-1.5 bg-emerald-500 rounded-lg shadow-lg shadow-emerald-500/20">
              <UIcon :name="mode === 'view' ? 'i-heroicons-information-circle' : 'i-heroicons-pencil-square'" class="text-white text-lg" />
            </div>
            <h3 class="text-lg font-black text-slate-900 dark:text-white uppercase tracking-tight">
              {{ mode === 'view' ? 'Book Intel' : mode + ' Book' }}
            </h3>
          </div>
          <UButton color="gray" variant="ghost" icon="i-heroicons-x-mark" class="rounded-full hover:bg-rose-500/20 hover:text-rose-500 transition-colors" @click="isOpen = false" />
        </div>
      </template>

      <div class="space-y-6 py-4 px-2">
        <div class="grid grid-cols-1 gap-5">
          <div class="space-y-1">
            <span class="text-[10px] font-black text-emerald-600 dark:text-emerald-400 uppercase tracking-widest">Title / Author</span>
            <p class="text-xl font-black text-slate-900 dark:text-white leading-tight">{{ book.title }}</p>
            <p class="text-sm text-slate-500 dark:text-slate-400 italic">by {{ book.author }}</p>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="p-3 rounded-2xl bg-black/5 dark:bg-white/5 border border-white/10">
              <span class="text-[10px] font-bold text-slate-500 uppercase block mb-1">Genre</span>
              <UBadge size="xs" variant="subtle" color="emerald" class="font-black uppercase tracking-tighter">{{ book.bookGenre }}</UBadge>
            </div>
            <div class="p-3 rounded-2xl bg-black/5 dark:bg-white/5 border border-white/10">
              <span class="text-[10px] font-bold text-slate-500 uppercase block mb-1">Status</span>
              <div class="flex items-center gap-1.5">
                <div :class="['w-2 h-2 rounded-full animate-pulse', book.availableStock > 0 ? 'bg-emerald-500' : 'bg-rose-500']"></div>
                <span :class="['text-xs font-black uppercase', book.availableStock > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-rose-600 dark:text-rose-500']">
                  {{ book.availableStock > 0 ? 'In Stock' : 'Out of Stock' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end items-center gap-3">
          <UButton 
            label="Cancel" 
            variant="ghost" 
            class="font-bold text-slate-500 hover:text-slate-900 dark:hover:text-white"
            @click="isOpen = false" 
          />
          
          <UButton
            v-if="role === 'user' && mode === 'view'"
            label="Initialize Borrowing"
            icon="i-heroicons-bolt"
            class="btn-glow font-black rounded-xl px-6 py-2.5 shadow-emerald-500/20"
            color="emerald"
            :disabled="book.availableStock <= 0"
            :loading="isActionLoading"
            @click="handleBorrow"
          />

          <UButton
            v-if="role === 'admin' && mode !== 'view'"
            label="Update Inventory"
            icon="i-heroicons-check-circle"
            color="indigo"
            class="btn-glow font-black rounded-xl px-6 py-2.5 shadow-indigo-500/20"
            :loading="isActionLoading"
            @click="handleSave"
          />
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import type { Book } from '~/types/book'

/**
 * BookActionModal (Jules v4.0 - Toast & Glass)
 * Final Refactor: Switched from alerts to useToast for modern interaction.
 */

const props = defineProps<{
  modelValue: boolean
  book: Book
  mode: 'view' | 'add' | 'edit'
  role: 'admin' | 'user' | string
}>()

const emit = defineEmits(['update:modelValue', 'save', 'borrow'])
const toast = useToast()
const isActionLoading = ref(false)

const isOpen = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 统一动作处理：消灭 Alert
const handleBorrow = async () => {
  isActionLoading.value = true
  try {
    // 向上抛出事件，具体的 API 调用将在 pages 层处理
    emit('borrow', props.book)
    // 注意：这里的成功提示建议放在 Pages 层的 API 回调中，但先写好结构
  } finally {
    isActionLoading.value = false
  }
}

const handleSave = () => {
  emit('save', props.book)
}
</script>
