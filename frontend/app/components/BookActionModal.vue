<template>
  <UModal
    :open="modelValue"
    @update:open="$emit('update:modelValue', $event)"
  >
    <UCard :ui="{ root: 'ring-0 divide-y divide-gray-100 dark:divide-gray-800' }">
      <template #header>
        <div class="flex items-center justify-between">
          <h3 class="text-base font-semibold">
            {{ mode === 'add' ? 'Add New Book' : (mode === 'edit' ? 'Edit Book' : 'Book Details') }}
          </h3>
          <UButton
            color="neutral"
            variant="ghost"
            icon="i-lucide-x"
            @click="$emit('update:modelValue', false)"
          />
        </div>
      </template>

      <div class="space-y-6 py-2">
        <div class="flex justify-center bg-gray-50 dark:bg-gray-800 p-4 rounded-lg">
          <img
            :src="book.coverImageUrl || 'https://placehold.co/400x600?text=No+Cover'"
            class="h-48 w-32 object-cover rounded shadow-md border dark:border-gray-700"
            @error="(e: any) => e.target.src = 'https://placehold.co/400x600?text=Image+Error'"
          >
        </div>

        <div class="space-y-4">
          <UField label="Book Title">
            <p
              v-if="mode === 'view'"
              class="text-lg font-bold text-gray-900 dark:text-white"
            >
              {{ book.title }}
            </p>
            <UInput
              v-else
              v-model="localBook.title"
              class="w-full"
            />
          </UField>

          <div class="grid grid-cols-2 gap-4">
            <UField label="Author">
              <p
                v-if="mode === 'view'"
                class="text-gray-700 dark:text-gray-300"
              >
                {{ book.author }}
              </p>
              <UInput
                v-else
                v-model="localBook.author"
              />
            </UField>

            <UField label="ISBN">
              <p
                v-if="mode === 'view'"
                class="font-mono text-sm"
              >
                {{ book.isbn }}
              </p>
              <UInput
                v-else
                v-model="localBook.isbn"
              />
            </UField>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <UField label="Genre">
              <UBadge
                v-if="mode === 'view'"
                variant="soft"
              >
                {{ book.bookGenre }}
              </UBadge>
              <USelect
                v-else
                v-model="localBook.bookGenre"
                :items="['FICTION', 'NON_FICTION', 'SCIENCE', 'HISTORY', 'ART', 'FANTASY']"
              />
            </UField>

            <UField label="Stock">
              <p v-if="mode === 'view'">
                {{ book.availableStock }} copies
              </p>
              <UInput
                v-else
                v-model.number="localBook.availableStock"
                type="number"
              />
            </UField>
          </div>

          <UField label="Description">
            <p
              v-if="mode === 'view'"
              class="text-sm text-gray-600 dark:text-gray-400 leading-relaxed"
            >
              {{ book.description || 'No description available.' }}
            </p>
            <UTextarea
              v-else
              v-model="localBook.description"
              autoresize
            />
          </UField>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-3">
          <UButton
            label="Close"
            color="neutral"
            variant="ghost"
            @click="$emit('update:modelValue', false)"
          />
          <UButton
            v-if="mode === 'view' && role === 'admin'"
            label="Edit Mode"
            color="primary"
            @click="$emit('change-mode', 'edit')"
          />
          <UButton
            v-if="mode !== 'view'"
            label="Save Changes"
            color="primary"
            @click="handleSave"
          />
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
/**
 * 图书馆项目 (Library Project) - BookActionModal (Nuxt UI v3 Standard)
 */
import type { Book } from '~/types/book'

const props = defineProps<{
  modelValue: boolean
  book: Book
  mode: 'view' | 'edit' | 'add'
  role?: string
}>()

const emit = defineEmits(['update:modelValue', 'save', 'change-mode'])

// English Comment: Standard local copy for form handling
const localBook = ref<Book>({ ...props.book })

// English Comment: Sync local copy when props change
watch(() => props.book, (newVal) => {
  localBook.value = { ...newVal }
}, { deep: true })

const handleSave = () => {
  // English Comment: Emit the updated book data back to parent
  emit('save', { ...localBook.value })
}
</script>
