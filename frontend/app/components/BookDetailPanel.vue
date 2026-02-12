<template>
  <UCard
    class="mb-8 border-2 border-primary/20 shadow-lg"
    :ui="{ body: 'p-6' }"
  >
    <template #header>
      <div class="flex justify-between items-center">
        <h3 class="text-lg font-bold flex items-center gap-2">
          <UIcon
            :name="mode === 'view' ? 'i-lucide-book-open' : 'i-lucide-edit-3'"
            class="text-primary"
          />
          {{ modeTitle }}
        </h3>

        <div
          v-if="book.id && role === 'admin'"
          class="flex gap-2"
        >
          <UButton
            v-if="mode === 'view'"
            icon="i-lucide-pencil"
            size="xs"
            label="Edit"
            @click="$emit('change-mode', 'edit')"
          />
          <UButton
            v-if="mode !== 'view'"
            icon="i-lucide-eye"
            size="xs"
            color="neutral"
            label="Cancel"
            @click="$emit('change-mode', 'view')"
          />
        </div>
      </div>
    </template>

    <div
      v-if="!book.id && mode !== 'add'"
      class="py-10 text-center text-muted italic"
    >
      Select a book from the inventory to see its full specifications.
    </div>

    <div
      v-else
      class="flex flex-col md:flex-row gap-8"
    >
      <div class="w-full md:w-48 flex-shrink-0">
        <div class="relative group aspect-[2/3] overflow-hidden rounded-lg shadow-md border dark:border-gray-700 bg-gray-100 dark:bg-gray-800">
          <img
            :src="coverUrl"
            class="w-full h-full object-cover transition-opacity duration-300"
            :class="{ 'opacity-0': !isImageLoaded }"
            alt="Book Cover"
            @load="isImageLoaded = true"
            @error="handleImageError"
          >
          <div
            v-if="!isImageLoaded && !imageHasError"
            class="absolute inset-0 flex items-center justify-center"
          >
            <UIcon
              name="i-lucide-loader-2"
              class="animate-spin text-primary w-6 h-6"
            />
          </div>
        </div>

        <UFormField
          v-if="mode !== 'view' && role === 'admin'"
          label="Image Path"
          class="mt-4"
        >
          <UInput
            v-model="localBook.coverImageUrl"
            size="sm"
            placeholder="/covers/example.jpg"
          />
        </UFormField>
      </div>

      <div class="flex-1 space-y-4 text-sm">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4">
          <div class="flex items-start gap-2">
            <span class="font-bold text-gray-500 shrink-0 w-24">Title:</span>
            <div
              v-if="mode === 'view'"
              class="flex-1 text-lg font-bold text-gray-900 dark:text-white"
            >
              {{ book.title }}
            </div>
            <UInput
              v-else
              v-model="localBook.title"
              class="w-full"
            />
          </div>

          <div class="flex items-start gap-2">
            <span class="font-bold text-gray-500 shrink-0 w-24">Author:</span>
            <div
              v-if="mode === 'view'"
              class="flex-1"
            >
              {{ book.author }}
            </div>
            <UInput
              v-else
              v-model="localBook.author"
              class="w-full"
            />
          </div>

          <div class="flex items-start gap-2">
            <span class="font-bold text-gray-500 shrink-0 w-24">ISBN:</span>
            <div
              v-if="mode === 'view'"
              class="flex-1 font-mono text-xs"
            >
              {{ book.isbn }}
            </div>
            <UInput
              v-else
              v-model="localBook.isbn"
              class="w-full"
            />
          </div>

          <div class="flex items-start gap-2">
            <span class="font-bold text-gray-500 shrink-0 w-24">Genre:</span>
            <UBadge
              v-if="mode === 'view'"
              variant="soft"
              color="primary"
            >
              {{ book.bookGenre }}
            </UBadge>
            <USelect
              v-else
              v-model="localBook.bookGenre"
              :options="['FICTION', 'NON_FICTION', 'SCIENCE', 'HISTORY', 'ART', 'FANTASY']"
            />
          </div>

          <div class="flex items-start gap-2">
            <span class="font-bold text-gray-500 shrink-0 w-24">Stock:</span>
            <div
              v-if="mode === 'view'"
              class="flex-1"
            >
              {{ book.availableStock }} units
            </div>
            <UInput
              v-else
              v-model.number="localBook.availableStock"
              type="number"
              class="w-full"
            />
          </div>

          <div class="flex items-start gap-2">
            <span class="font-bold text-gray-500 shrink-0 w-24">Rating:</span>
            <div
              v-if="mode === 'view'"
              class="flex-1 flex items-center gap-1 text-orange-500"
            >
              <span class="font-bold">{{ book.rating }}</span>
              <UIcon
                name="i-lucide-star"
                class="fill-current w-4 h-4"
              />
            </div>
            <UInput
              v-else
              v-model.number="localBook.rating"
              type="number"
              step="0.1"
            />
          </div>
        </div>

        <div class="flex items-start gap-2 pt-2 border-t dark:border-gray-800">
          <span class="font-bold text-gray-500 shrink-0 w-24">Description:</span>
          <div
            v-if="mode === 'view'"
            class="flex-1 text-gray-600 dark:text-gray-400 leading-relaxed"
          >
            {{ book.description || 'No description available.' }}
          </div>
          <UTextarea
            v-else
            v-model="localBook.description"
            autoresize
            class="w-full"
          />
        </div>

        <div class="flex justify-end gap-3 mt-6 pt-4 border-t dark:border-gray-800">
          <UButton
            v-if="role === 'user' && mode === 'view' && book.id"
            color="primary"
            icon="i-lucide-bookmark-plus"
            label="Borrow Book"
            :disabled="book.availableStock <= 0"
            @click="$emit('borrow', book)"
          />

          <UButton
            v-if="role === 'admin' && mode !== 'view'"
            color="primary"
            icon="i-lucide-save"
            :label="mode === 'add' ? 'Create Record' : 'Save Changes'"
            @click="handleSave"
          />
        </div>
      </div>
    </div>
  </UCard>
</template>

<script setup lang="ts">
/**
 * 图书馆项目 - Book Detail Panel
 * Jules Fix: Refactored for Nuxt 4 and Nuxt UI v3 standards.
 */
import type { Book } from '~/types/book'

const props = defineProps<{
  book: Book
  mode: 'view' | 'edit' | 'add'
  role: 'admin' | 'guest' | 'user'
}>()

const emit = defineEmits(['save', 'change-mode', 'borrow'])

const config = useRuntimeConfig()
const apiBase = config.public.apiBase

const localBook = ref<Book>({ ...props.book })
const isImageLoaded = ref(false)
const imageHasError = ref(false)

const PLACEHOLDER = 'https://placehold.co/400x600/e2e8f0/64748b?text=No+Cover'

const coverUrl = computed(() => {
  if (imageHasError.value || !props.book.coverImageUrl) return PLACEHOLDER
  if (props.book.coverImageUrl.startsWith('http')) return props.book.coverImageUrl
  const path = props.book.coverImageUrl.startsWith('/') ? props.book.coverImageUrl : `/${props.book.coverImageUrl}`
  return `${apiBase}${path}`
})

// English Comment: Standard deep watcher for props sync
watch(() => props.book, (newVal) => {
  localBook.value = { ...newVal }
  isImageLoaded.value = false
  imageHasError.value = false
}, { deep: true, immediate: true })

const handleImageError = () => {
  imageHasError.value = true
}

const modeTitle = computed(() => {
  if (props.mode === 'add') return 'New Inventory Entry'
  return props.mode === 'edit' ? 'Modify Book Data' : 'Book Details'
})

const handleSave = () => {
  emit('save', { ...localBook.value })
}
</script>
