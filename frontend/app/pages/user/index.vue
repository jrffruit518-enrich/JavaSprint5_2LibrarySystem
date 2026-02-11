<template>
  <div class="space-y-8">
    <div>
      <h1 class="text-2xl font-bold text-highlighted">
        Hello, Reader!
      </h1>
      <p class="text-muted">
        Welcome back to your reading dashboard.
      </p>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <UCard>
        <div class="text-center">
          <p class="text-sm text-muted mb-1">
            Books Borrowed
          </p>
          <p class="text-3xl font-bold text-highlighted">
            {{ stats.borrowed }}
          </p>
        </div>
      </UCard>

      <UCard>
        <div class="text-center">
          <p class="text-sm text-muted mb-1">
            Active Reservations
          </p>
          <p class="text-3xl font-bold text-primary">
            {{ stats.reservations }}
          </p>
        </div>
      </UCard>

      <UCard>
        <div class="text-center">
          <p class="text-sm text-muted mb-1">
            Overdue Notices
          </p>
          <p class="text-3xl font-bold text-red-500">
            {{ stats.overdue }}
          </p>
        </div>
      </UCard>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

/**
 * User Dashboard Page
 * Layout: user
 */
definePageMeta({
  layout: 'user',
  middleware: 'auth'
})

// Mock data for now, fetch from API later
const stats = reactive({
  borrowed: 0,
  reservations: 0,
  overdue: 0
})
</script>
