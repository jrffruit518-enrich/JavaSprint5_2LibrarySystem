<template>
  <div class="min-h-screen flex flex-col bg-visitor transition-colors duration-300 relative text-white">
    
    <header class="border-b border-white/10 sticky top-0 bg-white/5 dark:bg-black/20 backdrop-blur-2xl z-[100] shadow-2xl">
      <UContainer>
        <div class="h-16 flex items-center justify-between">
          <div class="flex items-center gap-8">
            <div class="flex items-center brightness-0 invert opacity-100">
              <AppLogo />
            </div>

            <nav class="hidden md:flex items-center gap-1">
              <UButton
                v-for="item in navItems"
                :key="item.to"
                :to="item.to"
                :icon="item.icon"
                variant="ghost"
                class="nav-btn font-bold transition-all hover:scale-105 active:scale-95"
              >
                {{ item.label }}
              </UButton>
            </nav>
          </div>

          <div class="flex items-center gap-3">
            <ClientOnly>
              <UButton
                :icon="isDark ? 'i-heroicons-moon' : 'i-heroicons-sun'"
                color="white"
                variant="ghost"
                class="hover:bg-white/10"
                @click="isDark = !isDark"
              />
            </ClientOnly>

            <UButton 
              to="/" 
              variant="solid" 
              color="primary"
              class="btn-glow font-bold px-6 animate-spring-in"
              label="Sign In" 
            />
          </div>
        </div>
      </UContainer>
    </header>

    <main class="flex-1 relative z-10 flex items-start justify-center animate-spring-in">
      <UContainer class="py-10 w-full">
        <slot />
      </UContainer>
    </main>

    <footer class="py-8 bg-black/40 backdrop-blur-xl border-t border-white/5">
      <UContainer>
        <div class="flex flex-col md:flex-row justify-between items-center gap-4 text-white/40">
          <p class="text-sm italic">
            CloudLibrary • Built with Nuxt 4 & Jules Intelligence
          </p>
          <div class="text-sm font-mono opacity-60">
            © {{ new Date().getFullYear() }} All Rights Reserved.
          </div>
        </div>
      </UContainer>
    </footer>
  </div>
</template>

<script setup lang="ts">
/**
 * Default Layout (Visitor Mode) - Jules v3.2
 * Refined for Glassmorphism & Performance.
 */

const colorMode = useColorMode()

const isDark = computed({
  get () {
    return colorMode.value === 'dark'
  },
  set () {
    colorMode.preference = colorMode.value === 'dark' ? 'light' : 'dark'
  }
})

const navItems = [
  { label: 'Home', to: '/', icon: 'i-heroicons-home-modern' },
  { label: 'Books', to: '/books', icon: 'i-heroicons-book-open' },
  { label: 'About', to: '/about', icon: 'i-heroicons-sparkles' }
]
</script>

<style scoped>
/* Cleaned up duplicate card styles to allow app.config.ts to take control.
  Retained only essential button and active state styles.
*/
.nav-btn {
  @apply text-white/80 hover:text-white;
}

.router-link-active {
  @apply bg-white/20 text-emerald-400 border-b-2 border-emerald-400 !important;
}

/* Custom glow handled by tailwind.config pulse but kept here for specific focus */
.btn-glow {
  box-shadow: 0 0 20px rgba(16, 185, 129, 0.4);
}
</style>