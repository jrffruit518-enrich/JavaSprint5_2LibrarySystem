<template>
  <div class="grid grid-cols-12 gap-6">
    <aside class="col-span-3 space-y-6">
      <UCard>
        <div class="space-y-4">
          <h3 class="font-bold text-lg border-b pb-2">
            Filters
          </h3>

          <div class="space-y-2">
            <span class="text-sm font-medium">Categories</span>
            <USelect
              v-model="filter.category"
              :options="['All', 'Fiction', 'Science', 'History']"
            />
          </div>

          <div class="space-y-2">
            <span class="text-sm font-medium">Author</span>
            <UInput
              v-model="filter.author"
              placeholder="Search author..."
            />
          </div>

          <div class="space-y-2">
            <span class="text-sm font-medium">Min Rating</span>
            <URange
              v-model="filter.minRating"
              :min="0"
              :max="5"
              :step="0.5"
            />
            <div class="text-xs text-muted text-right">
              {{ filter.minRating }} Stars
            </div>
          </div>

          <UButton
            block
            variant="outline"
            icon="i-lucide-rotate-ccw"
            @click="resetFilters"
          >
            Reset Filters
          </UButton>
        </div>
      </UCard>
    </aside>

    <main class="col-span-9">
      <UCard>
        <template #header>
          <div class="flex justify-between items-center">
            <h2 class="text-xl font-bold">
              Available Books
            </h2>
            <UInput
              icon="i-lucide-search"
              placeholder="Quick search..."
            />
          </div>
        </template>

        <BookTable
          mode="preview"
          :filters="filter"
        />
      </UCard>
    </main>
  </div>
</template>

<script setup>
/* 响应式筛选状态 */
const filter = reactive({
  category: 'All',
  author: '',
  minRating: 0
})

const resetFilters = () => {
  filter.category = 'All'
  filter.author = ''
  filter.minRating = 0
}

definePageMeta({
  title: 'Browse Books - Library System'
})
</script>
