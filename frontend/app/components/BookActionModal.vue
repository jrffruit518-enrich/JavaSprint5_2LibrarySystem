<template>
  <UModal v-model="isOpen">
    <UCard :ui="{ ring: '', divide: 'divide-y' }">
      <template #header>
        <div class="flex items-center justify-between">
          <h3 class="text-base font-bold text-gray-900 capitalize">
            {{ mode === 'view' ? 'Book Details' : mode + ' Book' }}
          </h3>
          <UButton color="gray" variant="ghost" icon="i-heroicons-x-mark" @click="isOpen = false" />
        </div>
      </template>

      <div class="space-y-4 py-2">
        <div class="grid grid-cols-2 gap-4">
          <div class="text-sm font-medium text-gray-500">Title</div>
          <div class="text-sm text-gray-900">{{ book.title }}</div>
          <div class="text-sm font-medium text-gray-500">Author</div>
          <div class="text-sm text-gray-900">{{ book.author }}</div>
          <div class="text-sm font-medium text-gray-500">Genre</div>
          <div class="text-sm"><UBadge size="xs">{{ book.bookGenre }}</UBadge></div>
          <div class="text-sm font-medium text-gray-500">Available Stock</div>
          <div class="text-sm" :class="book.availableStock > 0 ? 'text-green-600' : 'text-red-600'">
            {{ book.availableStock }}
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-3">
          <UButton label="Close" variant="ghost" @click="isOpen = false" />
          
          <UButton
            v-if="role === 'user' && mode === 'view'"
            label="Confirm Borrow"
            icon="i-heroicons-plus-circle"
            color="primary"
            :disabled="book.availableStock <= 0"
            @click="$emit('borrow', book)"
          />

          <UButton
            v-if="role === 'admin' && mode !== 'view'"
            label="Save Changes"
            color="primary"
            @click="$emit('save', book)"
          />
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import type { Book } from '~/types/book'

const props = defineProps<{
  modelValue: boolean
  book: Book
  mode: 'view' | 'add' | 'edit'
  role: 'admin' | 'user'
}>()

const emit = defineEmits(['update:modelValue', 'save', 'borrow'])

const isOpen = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})
</script>
