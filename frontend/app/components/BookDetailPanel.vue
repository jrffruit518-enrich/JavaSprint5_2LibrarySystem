<template>
  <UCard
    class="flex flex-col h-full overflow-hidden glass-effect border-none shadow-none"
    :ui="{ 
      base: 'relative',
      body: { base: 'flex-1 overflow-y-auto custom-scrollbar p-0' },
      header: { base: 'bg-white/40 dark:bg-white/5 border-b border-black/5 dark:border-white/5', padding: 'py-3 px-6' },
      rounded: 'rounded-none'
    }"
  >
    <template #header>
      <div class="flex justify-between items-center">
        <h3 class="text-xs font-black uppercase tracking-[0.2em] flex items-center gap-2 text-slate-500 dark:text-slate-400">
          <UIcon
            :name="mode === 'view' ? 'i-heroicons-book-open' : 'i-heroicons-pencil-square'"
            class="text-emerald-500 w-5 h-5"
          />
          {{ modeTitle }}
        </h3>

        <div class="flex items-center gap-2">
          <div v-if="book.id && role === 'admin'" class="flex gap-2">
            <UButton
              v-if="mode === 'view'"
              icon="i-heroicons-pencil"
              size="xs"
              variant="soft"
              color="indigo"
              label="Edit"
              class="font-bold rounded-lg"
              @click="$emit('change-mode', 'edit')"
            />
            <UButton
              v-else
              icon="i-heroicons-eye"
              size="xs"
              color="white"
              variant="solid"
              label="Cancel"
              class="font-bold rounded-lg shadow-sm"
              @click="$emit('change-mode', 'view')"
            />
          </div>
          <UButton
            icon="i-heroicons-x-mark"
            size="sm"
            color="gray"
            variant="ghost"
            class="rounded-full hover:bg-rose-500/20 hover:text-rose-500 transition-all"
            @click="$emit('close')"
          />
        </div>
      </div>
    </template>

    <div v-if="!book.id && mode !== 'add'" class="py-20 text-center text-slate-400 italic px-6 flex flex-col items-center justify-center h-full animate-pulse">
      <UIcon name="i-heroicons-cursor-arrow-ripple" class="w-16 h-16 mb-4 opacity-10" />
      <p class="font-mono text-[10px] tracking-[0.2em] uppercase">Select a book to view details</p>
    </div>

    <div v-else class="flex flex-col">
      <div class="w-full py-8 flex flex-col items-center border-b border-black/5 dark:border-white/5 bg-gradient-to-b from-white/20 to-transparent">
        <div class="relative group w-56 aspect-[2/3] overflow-hidden rounded-xl shadow-[0_20px_50px_rgba(0,0,0,0.3)] border-4 border-white dark:border-slate-800 transition-transform duration-500 hover:scale-[1.02]">
          <img
            :src="coverUrl"
            class="w-full h-full object-cover transition-opacity duration-700"
            :class="{ 'opacity-0': !isImageLoaded }"
            alt="Book Cover"
            @load="isImageLoaded = true"
            @error="handleImageError"
          >
          <div v-if="!isImageLoaded && !imageHasError" class="absolute inset-0 flex items-center justify-center bg-slate-100 dark:bg-slate-900">
            <UIcon name="i-heroicons-arrow-path" class="animate-spin text-emerald-500 w-8 h-8" />
          </div>
        </div>
        
        <div v-if="mode !== 'view' && role === 'admin'" class="w-full max-w-xs mt-6 px-4">
          <UFormGroup label="Cover URL" size="xs" :ui="{ label: { base: 'text-[10px] uppercase font-black text-slate-400' } }">
            <UInput 
              v-model="localBook.coverImageUrl" 
              placeholder="/covers/example.jpg" 
              variant="none"
              class="bg-black/5 dark:bg-white/5 rounded-lg border border-white/10"
            />
          </UFormGroup>
        </div>
      </div>

      <div class="p-8 space-y-8">
        <div class="space-y-4 text-center">
          <div class="space-y-1">
            <span class="text-[10px] font-black text-emerald-600 dark:text-emerald-400 uppercase tracking-[0.3em]">Title</span>
            <div v-if="mode === 'view'" class="text-2xl font-black text-slate-900 dark:text-white leading-tight tracking-tight px-2">
              {{ book.title }}
            </div>
            <UInput v-else v-model="localBook.title" size="xl" class="text-center" />
          </div>
          
          <div class="space-y-1">
            <span class="text-[10px] font-black text-slate-400 uppercase tracking-[0.3em]">Author</span>
            <div v-if="mode === 'view'" class="text-lg text-slate-700 dark:text-slate-300 font-bold italic">
              {{ book.author }}
            </div>
            <UInput v-else v-model="localBook.author" class="text-center" />
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4 py-6 border-y border-black/5 dark:border-white/5">
          <div class="space-y-4">
            <div class="flex flex-col items-center">
              <span class="text-[9px] font-black text-slate-400 uppercase tracking-widest">ISBN</span>
              <div v-if="mode === 'view'" class="font-mono text-xs font-bold text-slate-600 dark:text-slate-400 mt-1">
                {{ book.isbn }}
              </div>
              <UInput v-else v-model="localBook.isbn" size="xs" />
            </div>
            
            <div class="flex flex-col items-center">
              <span class="text-[9px] font-black text-slate-400 uppercase tracking-widest">Genre</span>
              <UBadge v-if="mode === 'view'" variant="subtle" color="emerald" class="font-black mt-1 uppercase tracking-tighter px-3">
                {{ book.bookGenre }}
              </UBadge>
              <USelect v-else v-model="localBook.bookGenre" :options="['FICTION', 'NON_FICTION', 'SCIENCE', 'HISTORY', 'ART', 'FANTASY']" size="xs" />
            </div>
          </div>

          <div class="space-y-4 border-l border-black/5 dark:border-white/5">
            <div class="flex flex-col items-center">
              <span class="text-[9px] font-black text-slate-400 uppercase tracking-widest">Stock</span>
              <div v-if="mode === 'view'" class="mt-1 flex items-center gap-1">
                <span :class="['text-xl font-black', book.availableStock <= 5 ? 'text-rose-500' : 'text-slate-900 dark:text-white']">
                  {{ book.availableStock }}
                </span>
                <span class="text-[10px] text-slate-400 font-bold uppercase">Units</span>
              </div>
              <UInput v-else v-model.number="localBook.availableStock" type="number" size="xs" />
            </div>

            <div class="flex flex-col items-center">
              <span class="text-[9px] font-black text-slate-400 uppercase tracking-widest">Rating</span>
              <div v-if="mode === 'view'" class="mt-1 flex items-center gap-1.5">
                <UIcon name="i-heroicons-star-solid" class="text-amber-400 w-4 h-4" />
                <span class="text-lg font-black text-slate-900 dark:text-white">{{ book.rating }}</span>
              </div>
              <UInput v-else v-model.number="localBook.rating" type="number" step="0.1" size="xs" />
            </div>
          </div>
        </div>

        <div class="space-y-2">
          <span class="text-[10px] font-black text-slate-400 uppercase tracking-[0.2em]">Description</span>
          <div
            v-if="mode === 'view'"
            class="text-sm text-slate-700 dark:text-slate-400 leading-relaxed text-justify bg-white/20 dark:bg-black/20 p-5 rounded-2xl h-44 overflow-y-auto custom-scrollbar border border-white/10 italic"
          >
            {{ book.description || 'No description available for this record.' }}
          </div>
          <UTextarea
            v-else
            v-model="localBook.description"
            :rows="6"
            class="custom-scrollbar"
            placeholder="Book description..."
          />
        </div>

        <div class="flex justify-center pt-4 pb-10">
          <UButton
            v-if="role === 'user' && mode === 'view' && book.id"
            size="xl"
            color="emerald"
            icon="i-heroicons-bolt"
            label="CONFIRM BORROW"
            :disabled="book.availableStock <= 0"
            class="btn-glow font-black px-12 rounded-2xl transition-all active:scale-95 shadow-lg"
            @click="$emit('borrow', book)"
          />

          <UButton
            v-if="role === 'admin' && mode !== 'view'"
            size="xl"
            color="indigo"
            icon="i-heroicons-cloud-arrow-up"
            :label="mode === 'add' ? 'CREATE BOOK' : 'SAVE CHANGES'"
            class="btn-glow font-black px-12 rounded-2xl transition-all active:scale-95 shadow-lg"
            @click="handleSave"
          />
        </div>
      </div>
    </div>
  </UCard>
</template>

<script setup lang="ts">
import type { Book } from '~/types/book'

/**
 * Jules v4.0 - Full Redesign
 * Focus: Visual hierarchy, glassmorphism, and action alignment.
 */

const props = defineProps<{
  book: Book
  mode: 'view' | 'edit' | 'add'
  role: 'admin' | 'guest' | 'user' | string
}>()

const emit = defineEmits(['save', 'change-mode', 'borrow', 'close'])

const config = useRuntimeConfig()
const apiBase = config.public.apiBase

const localBook = ref<Book>({ ...props.book })
const isImageLoaded = ref(false)
const imageHasError = ref(false)

const PLACEHOLDER = 'https://placehold.co/400x600/1e293b/475569?text=NO+COVER'

const coverUrl = computed(() => {
  if (imageHasError.value || !props.book.coverImageUrl) return PLACEHOLDER
  if (props.book.coverImageUrl.startsWith('http')) return props.book.coverImageUrl
  const path = props.book.coverImageUrl.startsWith('/') ? props.book.coverImageUrl : `/${props.book.coverImageUrl}`
  return `${apiBase}${path}`
})

watch(() => props.book, (newVal) => {
  localBook.value = { ...newVal }
  imageHasError.value = false
  isImageLoaded.value = false
}, { deep: true, immediate: true })

const handleImageError = () => {
  imageHasError.value = true
}

const modeTitle = computed(() => {
  if (props.mode === 'add') return 'New Entry'
  return props.mode === 'edit' ? 'Editing Record' : 'Book Details'
})

const handleSave = () => {
  emit('save', { ...localBook.value })
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { 
  background: rgba(16, 185, 129, 0.2); 
  border-radius: 20px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover { 
  background: rgba(16, 185, 129, 0.5); 
}
</style>
