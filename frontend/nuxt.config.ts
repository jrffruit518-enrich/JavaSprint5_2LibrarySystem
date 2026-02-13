export default defineNuxtConfig({
  // 保持你 GitHub 上的模块配置
  modules: [
    '@nuxt/ui' 
    // 先不加 eslint，等跑通后再加，避免干扰
  ],

  devtools: { enabled: true },

  css: ['~/assets/css/main.css'],

  runtimeConfig: {
    public: {
      // 在开发环境，我们通过 Nitro Proxy 访问，所以直接写 /api
      apiBase: '/api'
    }
  },

  // 这里的日期改为你今天的日期，解决之前的报错
  compatibilityDate: '2026-02-13',

  nitro: {
    devProxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },

  // 必须保留这个 Jules Fix，防止图标加载失败
  icon: {
    localApiEndpoint: '/_icon'
  }
})