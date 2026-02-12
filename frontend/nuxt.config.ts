// nuxt.config.ts
export default defineNuxtConfig({
  // --- Modules ---
  modules: [
    '@nuxt/eslint',
    '@nuxt/ui'
  ],

  devtools: {
    enabled: true
  },

  css: ['~/assets/css/main.css'],

  runtimeConfig: {
    public: {
      apiBase: 'http://localhost:8080'
    }
  },

  routeRules: {
    '/': { prerender: true },
    '/admin/**': { ssr: false }
  },

  compatibilityDate: '2025-01-15',

  nitro: {
    devProxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },

  eslint: {
    config: {
      stylistic: {
        commaDangle: 'never',
        braceStyle: '1tbs'
      }
    }
  },

  // --- Jules Fix: Icon Configuration ---
  // 改变图标加载路径，避免被 nitro 的 /api 代理拦截导致后端 500
  icon: {
    localApiEndpoint: '/_icon'
  }
})
