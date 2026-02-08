<template>
  <div class="space-y-8">
    <div>
      <h1 class="text-2xl font-bold">
        Hello, Reader!
      </h1>
      <p class="text-muted">
        Keep track of your reading journey here.
      </p>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <UCard>
        <div class="text-center">
          <p class="text-sm text-muted mb-1">
            Books Borrowed
          </p>
          <p class="text-3xl font-bold">
            3
          </p>
        </div>
      </UCard>
      <UCard>
        <div class="text-center">
          <p class="text-sm text-muted mb-1">
            Active Reservations
          </p>
          <p class="text-3xl font-bold text-primary">
            1
          </p>
        </div>
      </UCard>
      <UCard>
        <div class="text-center">
          <p class="text-sm text-muted mb-1">
            Overdue Notices
          </p>
          <p class="text-3xl font-bold text-red-500">
            0
          </p>
        </div>
      </UCard>
    </div>
  </div>
</template>

<script setup>
/* Apply user layout */
definePageMeta({
  layout: 'user'
})
</script>
