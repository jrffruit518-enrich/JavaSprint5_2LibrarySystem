<template>
  <div>
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
    
    <UNotifications />
  </div>
</template>

<script setup>
/**
 * Global Configuration for Library System
 * Updated for Nuxt 4 & UI v2
 */
useSeoMeta({
  title: 'Library System',
  description: 'Manage your books with ease.'
})
</script>