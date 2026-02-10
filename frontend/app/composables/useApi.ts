import type { UseFetchOptions } from 'nuxt/app'

/**
 * app/composables/useApi.ts
 * A type-safe fetch wrapper for the Library Project.
 */

/* Define the structure of the backend error response */
interface ApiError {
  message: string
  status?: number
}

export const useApi = <T>(url: string, options: UseFetchOptions<T> = {}) => {
  const token = useCookie<string | null>('auth-token')
  /**
   * 1. Get runtime configuration.
   * This allows the app to use the API address from .env or environment variables.
   */
  const config = useRuntimeConfig()

  /* Define headers as a record to avoid "any" and provide type safety */
  const customHeaders: Record<string, string> = {
    ...((options.headers as Record<string, string>) || {})
  }

  if (token.value) {
    customHeaders.Authorization = `Bearer ${token.value}`
  }

  return useFetch(url, {
    /**
     * 2. Use dynamic baseURL from runtimeConfig.
     * It defaults to 'http://localhost:8080' as defined in nuxt.config.ts.
     */
    baseURL: config.public.apiBase,

    /* Manually mapping necessary options to avoid generic overload issues */
    method: options.method,
    body: options.body,
    params: options.params,
    query: options.query,
    watch: options.watch,
    immediate: options.immediate,

    /* Use our safely typed headers */
    headers: customHeaders,

    /* Response error handling */
    onResponseError({ response }) {
      const status = response.status

      /* Use unknown as a bridge for safe casting instead of any */
      const errorData = response._data as unknown as ApiError
      const errorMsg = errorData?.message || 'Access Denied'

      if (status === 401 || status === 403) {
        console.error(`[Auth Error ${status}]:`, errorMsg)
      } else {
        console.error(`[API Error ${status}]:`, errorMsg)
      }
    }
  } as UseFetchOptions<T>)
}
