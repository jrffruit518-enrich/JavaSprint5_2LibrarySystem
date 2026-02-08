<template>
  <div class="min-h-screen flex flex-col">
    <header class="h-16 border-b border-default flex items-center justify-between px-6 bg-background sticky top-0 z-10">
      <div class="flex items-center gap-2">
        <UIcon
          name="i-lucide-book-open"
          class="w-6 h-6 text-primary"
        />
        <span class="font-bold text-lg text-highlighted">Reader Center</span>
      </div>

      <div class="flex items-center gap-4">
        <span class="text-sm text-muted hidden sm:inline font-medium">Welcome, {{ userName }}</span>
        <UAvatar
          :alt="userName"
          size="sm"
        />
      </div>
    </header>

    <div class="flex flex-1">
      <aside class="w-64 border-r border-default bg-elevated hidden md:flex flex-col">
        <nav class="p-4 space-y-2 flex-1">
          <UButton
            v-for="link in userLinks"
            :key="link.to"
            :to="link.to"
            :icon="link.icon"
            :label="link.label"
            variant="ghost"
            block
            class="justify-start"
            active-class="bg-primary/10 text-primary font-semibold"
          />
        </nav>

        <div class="p-4 border-t border-default">
          <UButton
            label="Logout"
            color="red"
            variant="ghost"
            block
            icon="i-lucide-log-out"
            class="justify-start"
            @click="handleLogout"
          />
        </div>
      </aside>

      <main class="flex-1 p-6">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
/* Current user's name (Mock data) */
const userName = ref('John Doe')

/* User sidebar menu items */
const userLinks = [
  { label: 'My Dashboard', icon: 'i-lucide-layout-grid', to: '/user' },
  { label: 'Browse & Borrow', icon: 'i-lucide-search', to: '/user/books' },
  { label: 'My Loans', icon: 'i-lucide-library', to: '/user/loans' },
  { label: 'Profile Settings', icon: 'i-lucide-user-cog', to: '/user/profile' }
]

/* Logout logic */
const handleLogout = () => {
  /* Clear the user role cookie */
  const roleCookie = useCookie('user-role')
  roleCookie.value = null

  /* Redirect back to login page */
  navigateTo('/')
}
</script>
