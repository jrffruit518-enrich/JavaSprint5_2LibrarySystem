<template>
  <div class="min-h-screen flex">
    <aside class="w-64 bg-gray-50 border-r border-default p-4 hidden md:block">
      <div class="mb-8 px-2">
        <h2 class="text-xl font-bold text-primary">
          Library Project
        </h2>
        <p class="text-xs text-muted">
          User Portal
        </p>
      </div>

      <nav class="space-y-2">
        <UButton
          to="/user"
          variant="ghost"
          icon="i-lucide-layout-dashboard"
          block
          truncate
        >
          Dashboard
        </UButton>

        <UButton
          to="/user/books"
          variant="ghost"
          icon="i-lucide-book"
          block
          truncate
        >
          Book Catalog
        </UButton>

        <UButton
          to="/user/loans"
          variant="ghost"
          icon="i-lucide-library"
          block
          truncate
        >
          My Loans
        </UButton>

        <UButton
          to="/user/profile"
          variant="ghost"
          icon="i-lucide-user"
          block
          truncate
        >
          My Profile
        </UButton>

        <div class="pt-4 mt-4 border-t border-default">
          <UButton
            to="/"
            variant="ghost"
            icon="i-lucide-log-out"
            color="error"
            block
            truncate
          >
            Logout
          </UButton>
        </div>
      </nav>
    </aside>

    <main class="flex-1 overflow-y-auto bg-white">
      <UContainer class="py-8">
        <slot />
      </UContainer>
    </main>
  </div>
</template>

<script setup>
/**
 * User Layout
 * Updated: Added My Loans navigation item.
 */
</script>
