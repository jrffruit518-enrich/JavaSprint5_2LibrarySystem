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

  // --- Runtime Config (Handles Environment Variables) ---
  runtimeConfig: {
    public: {
      /**
       * Default value for local development.
       * Will be overridden by NUXT_PUBLIC_API_BASE in .env or Docker.
       */
      apiBase: 'http://localhost:8080'
    }
  },

  routeRules: {
    '/': { prerender: true }
  },

  compatibilityDate: '2025-01-15',

  eslint: {
    config: {
      stylistic: {
        commaDangle: 'never',
        braceStyle: '1tbs'
      }
    }
  }
})
