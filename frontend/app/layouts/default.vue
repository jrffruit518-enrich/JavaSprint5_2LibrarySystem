<template>
  <div class="min-h-screen flex flex-col bg-white dark:bg-gray-900 transition-colors duration-300">
    <header class="border-b border-gray-200 dark:border-gray-800 sticky top-0 bg-white/80 dark:bg-gray-900/80 backdrop-blur-md z-50">
      <UContainer>
        <div class="h-16 flex items-center justify-between">
          <div class="flex items-center gap-8">
            <AppLogo />

            <nav class="hidden md:flex items-center gap-1">
              <UButton
                v-for="item in navItems"
                :key="item.to"
                :to="item.to"
                :icon="item.icon"
                variant="ghost"
                color="gray"
                class="font-medium"
              >
                {{ item.label }}
              </UButton>
            </nav>
          </div>

          <div class="flex items-center gap-3">
            <ClientOnly>
              <UButton
                :icon="isDark ? 'i-heroicons-moon' : 'i-heroicons-sun'"
                color="gray"
                variant="ghost"
                aria-label="Theme"
                @click="isDark = !isDark"
              />
            </ClientOnly>

            <UButton 
              to="/" 
              variant="solid" 
              color="primary"
              label="Sign In" 
            />
          </div>
        </div>
      </UContainer>
    </header>

    <main class="flex-1">
      <UContainer class="py-12">
        <slot />
      </UContainer>
    </main>

    <footer class="border-t border-gray-100 dark:border-gray-800 py-8 bg-gray-50 dark:bg-gray-950">
      <UContainer>
        <div class="flex flex-col md:flex-row justify-between items-center gap-4">
          <p class="text-sm text-gray-500 italic">
            Library Project • Built with Nuxt 4 & Jules Intelligence
          </p>
          <div class="text-sm text-gray-400 font-mono">
            © {{ new Date().getFullYear() }} All Rights Reserved.
          </div>
        </div>
      </UContainer>
    </footer>
  </div>
</template>

<script setup lang="ts">
/**
 * Default Layout - Jules v2 Standard
 * 1. 修复：将 /login 链接统一指向 / (index.vue)
 * 2. 修复：手动实现 ColorMode 切换以移除警告
 * 3. 修复：集成 AppLogo 统一品牌
 */

const colorMode = useColorMode()

// 响应式计算属性：控制黑夜模式
const isDark = computed({
  get () {
    return colorMode.value === 'dark'
  },
  set () {
    colorMode.preference = colorMode.value === 'dark' ? 'light' : 'dark'
  }
})

// 导航配置
const navItems = [
  { label: 'Home', to: '/', icon: 'i-heroicons-home' },
  { label: 'Browse Books', to: '/books', icon: 'i-heroicons-book-open' },
  { label: 'About', to: '/about', icon: 'i-heroicons-information-circle' }
]
</script>

<style scoped>
/* 激活状态的导航链接样式 */
.router-link-active {
  @apply text-primary-600 dark:text-primary-400 bg-primary-50 dark:bg-primary-900/20;
}
</style>
