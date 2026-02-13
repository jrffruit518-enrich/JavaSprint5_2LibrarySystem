/**
 * 图书馆项目 - 统一 API 请求 Composable (Nuxt 4 适配版)
 * 功能：自动注入 JWT Token, 错误日志拦截, 响应式参数监听
 */
export const useApi = <T>(
  url: string | (() => string | null),
  options: any = {}
) => {
  const token = useCookie<string | null>('auth_token')
  const config = useRuntimeConfig()

  // 1. 确定缓存 Key：如果是函数，使用其返回值作为动态 Key 的一部分
  const key = typeof url === 'string' ? url : `dynamic-${url.toString().length}`

  return useAsyncData<T>(
    key,
    async () => {
      const targetUrl = typeof url === 'function' ? url() : url
      if (!targetUrl) return null as any

      const headers: Record<string, string> = {
        ...((options.headers as Record<string, string>) ?? {})
      }

      // 2. 自动注入 JWT Token
      if (token.value) {
        headers.Authorization = `Bearer ${token.value}`
      }

      // 3. 执行核心 $fetch
      return $fetch<T>(targetUrl, {
        baseURL: config.public.apiBase || '/api',
        ...options,
        headers,
        // 错误处理：可在此扩展 401 自动跳转登录逻辑
        onResponseError({ response }) {
          console.error(`[API ERROR ${response.status}]`, response._data?.message || 'Request Failed')
        }
      })
    },
    {
      server: true,
      // 4. 核心优化：如果是函数式 URL，自动将其加入 watch，URL 变了就自动 fetch
      watch: typeof url === 'function' ? [url] : []
    }
  )
}
