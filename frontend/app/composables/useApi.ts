/**
 * 图书馆项目 - 统一 API 请求 Composable (Jules v3.0 生产级稳定版)
 * 核心修复：
 * 1. 移除了外部 computed key，直接在 useAsyncData 内部生成动态 Key，解决 "Already mounted" 警告。
 * 2. 增强了 lazy 配置的可控性，允许页面级覆盖。
 * 3. 严格路径处理，确保 baseURL 与 path 拼接不会出现 // 或重复 /api。
 */
export const useApi = <T>(
  url: string | (() => string | null),
  options: any = {}
) => {
  const token = useCookie<string | null>('auth_token')
  const config = useRuntimeConfig()

  // 1. 路径清洗函数
  const getCleanPath = (inputUrl: string | null) => {
    if (!inputUrl) return ''
    // 确保去掉重复的 /api 前缀
    let path = inputUrl.startsWith('/api') ? inputUrl.replace('/api', '') : inputUrl
    // 确保路径以 / 开头
    return path.startsWith('/') ? path : `/${path}`
  }

  // 2. 解析当前 URL 值 (支持函数或字符串)
  const currentUrl = typeof url === 'function' ? url() : url
  const cleanPath = getCleanPath(currentUrl)

  // 3. 核心请求逻辑
  // 使用 path 作为 Key 的一部分，确保每个不同接口都有独立缓存
  return useAsyncData<T>(
    `api-fetch:${cleanPath}`, 
    async () => {
      if (!cleanPath) return null as any

      const headers: Record<string, string> = {
        ...((options.headers as Record<string, string>) ?? {})
      }

      // 自动注入认证令牌
      if (token.value) {
        headers.Authorization = `Bearer ${token.value}`
      }

      // 使用 $fetch 执行请求
      return $fetch<T>(cleanPath, {
        baseURL: config.public.apiBase || '/api',
        ...options,
        headers,
        onResponseError({ response }) {
          console.error(`[API ERROR ${response.status}]`, response._data?.message || 'Request Failed')
        }
      })
    },
    {
      server: true,
      // 默认 lazy: true 提升 Layout 加载速度，但允许在调用处通过 options.lazy 覆盖
      lazy: options.lazy !== undefined ? options.lazy : true,
      immediate: true,
      watch: typeof url === 'function' ? [url] : [],
      ...options // 允许传递其他 useAsyncData 的原生配置
    }
  )
}