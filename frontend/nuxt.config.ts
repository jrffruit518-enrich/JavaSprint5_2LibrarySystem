// https://nuxt.com/docs/api/configuration/nuxt-config
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

  // --- Runtime Config ---
  runtimeConfig: {
    public: {
      apiBase: 'http://localhost:8080'
    }
  },

  routeRules: {
    '/': { prerender: true }
  },

  compatibilityDate: '2025-01-15',

  // --- NEW: Nitro Proxy Configuration ---
  // English Comment: Standard transparent proxy for local development
  nitro: {
    devProxy: {
      // 这里的 '/api' 是前端请求的匹配前缀
      '/api': {
        // target 只写到端口，不要带路径
        target: 'http://localhost:8080',
        changeOrigin: true,
        // English Comment: Do NOT rewrite the path, keep /api/users/me as is
        prependPath: true
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
  }
})
