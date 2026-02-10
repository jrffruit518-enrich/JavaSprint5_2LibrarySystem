<template>
  <UCard
    class="mb-8 border-2 border-primary/20 shadow-lg"
    :ui="{ body: { padding: 'p-6' } }"
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
          v-if="book.id"
          class="flex gap-2"
        >
          <UButton
            v-if="mode === 'view' && role === 'admin'"
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
      v-if="!book.id"
      class="py-10 text-center text-muted italic"
    >
      Select a book from the list below to view or edit details.
    </div>

    <div
      v-else
      class="flex flex-col md:flex-row gap-8"
    >
      <div class="w-full md:w-48 flex-shrink-0">
        <img
          :src="book.coverImageUrl || 'https://placehold.co/400x600?text=No+Cover'"
          class="w-full aspect-[2/3] object-cover rounded-lg shadow-md border dark:border-gray-700"
        >
        <UField
          v-if="mode !== 'view'"
          label="Cover URL"
          class="mt-4"
        >
          <UInput
            v-model="localBook.coverImageUrl"
            size="sm"
          />
        </UField>
      </div>

      <div class="flex-1 space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <UField label="Book Title">
            <p
              v-if="mode === 'view'"
              class="text-xl font-bold"
            >
              {{ book.title }}
            </p>
            <UInput
              v-else
              v-model="localBook.title"
            />
          </UField>
          <UField label="Author">
            <p v-if="mode === 'view'">
              {{ book.author }}
            </p>
            <UInput
              v-else
              v-model="localBook.author"
            />
          </UField>
        </div>

        <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
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
          <UField label="Genre">
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
              :items="['FICTION', 'NON_FICTION', 'SCIENCE', 'HISTORY', 'ART', 'FANTASY']"
            />
          </UField>
          <UField label="Stock">
            <p v-if="mode === 'view'">
              {{ book.availableStock }}
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
            class="text-sm text-muted leading-relaxed line-clamp-3"
          >
            {{ book.description }}
          </p>
          <UTextarea
            v-else
            v-model="localBook.description"
            autoresize
          />
        </UField>

        <div
          v-if="mode !== 'view'"
          class="flex justify-end mt-4"
        >
          <UButton
            color="primary"
            icon="i-lucide-check"
            label="Save Changes"
            @click="handleSave"
          />
        </div>
      </div>
    </div>
  </UCard>
</template>

<script setup lang="ts">
import type { Book } from '~/types/book'

const props = defineProps<{
  book: Book
  mode: 'view' | 'edit' | 'add'
  role: 'admin' | 'guest'
}>()

const emit = defineEmits(['save', 'change-mode'])

const localBook = ref<Book>({ ...props.book })

watch(() => props.book, (newVal) => {
  localBook.value = { ...newVal }
}, { deep: true })

const modeTitle = computed(() => {
  if (props.mode === 'add') return 'Adding New Entry'
  return props.mode === 'edit' ? 'Editing Book' : 'Book Details'
})

const handleSave = () => {
  emit('save', { ...localBook.value })
}
</script>
