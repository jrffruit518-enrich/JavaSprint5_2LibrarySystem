// Library Project - Visual Refactor nuxt.config.ts (Jules Standard v2.9)
export default defineNuxtConfig({
  // 1. Core Modules: Added Google Fonts for enhanced typography
  modules: [
    '@nuxt/ui',
    '@nuxtjs/google-fonts'
  ],

  // 2. Dev Tools
  devtools: { enabled: true },

  // 3. Global CSS: Loading refactored styles with role-based logic
  css: ['~/assets/css/main.css'],

  // 4. Typography: Enforcing 'Public Sans' for a modern feel
  googleFonts: {
    families: {
      'Public Sans': [300, 400, 500, 600, 700],
    },
    display: 'swap',
    prefetch: true,
    preconnect: true,
  },

  // 5. UI Refinement: Safelist colors to prevent purging during role switching
  ui: {
    safelistColors: ['emerald', 'indigo', 'rose', 'amber', 'gray'],
    icons: ['heroicons', 'lucide'] // Essential for high-quality iconography
  },

  // 6. Environment: Public API base path
  runtimeConfig: {
    public: {
      apiBase: '/api'
    }
  },

  compatibilityDate: '2026-02-13',

  // 7. Nitro Proxy: REMAINED UNTOUCHED (Fixed 404 Solution)
  nitro: {
    routeRules: {
      '/api/**': { 
        proxy: 'http://localhost:8080/**', 
        cors: true 
      }
    },
    devProxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        prependPath: false 
      }
    }
  },

  // 8. ColorMode: Default to light for "Warm Bookshelf" style
  colorMode: {
    preference: 'light',
    fallback: 'light',
    classSuffix: ''
  },

  // 9. Icon Support: Jules standard fix
  icon: {
    localApiEndpoint: '/_icon'
  }
})
