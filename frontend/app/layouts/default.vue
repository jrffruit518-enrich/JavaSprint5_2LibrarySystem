<script setup>
/* Navigation items for the library project */
const navItems = [
  [
    { label: 'Home', to: '/', icon: 'i-lucide-house' },
    { label: 'Browse Books', to: '/books', icon: 'i-lucide-book-open' },
    { label: 'About', to: '/about', icon: 'i-lucide-info' }
  ]
]
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <UHeader>
      <template #left>
        <NuxtLink
          to="/"
          class="flex items-center gap-2"
        >
          <UIcon
            name="i-lucide-library"
            class="w-6 h-6 text-primary"
          />
          <span class="font-bold text-xl">Library System</span>
        </NuxtLink>
      </template>

      <UNavigationMenu
        :items="navItems"
        variant="link"
      />

      <template #right>
        <UColorModeButton />
      </template>
    </UHeader>

    <UMain class="flex-1">
      <UContainer class="py-8">
        <slot />
      </UContainer>
    </UMain>

    <USeparator />

    <UFooter>
      <template #left>
        <p class="text-sm text-muted">
          Library Project • © {{ new Date().getFullYear() }}
        </p>
      </template>
    </UFooter>
  </div>
</template>

<style scoped>
/* Ensure the layout takes full height */
</style>
