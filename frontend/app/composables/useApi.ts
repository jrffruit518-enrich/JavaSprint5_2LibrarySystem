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

/* Define the User Profile interface matching the Backend Record DTO */
export interface UserProfileDTO {
  id: number
  username: string
  email: string
  userRole: 'MEMBER' | 'ADMIN' // Adjust based on your UserRole enum
  avatarUrl: string
}

export const useApi = <T>(url: string, options: UseFetchOptions<T> = {}) => {
  const token = useCookie<string | null>('auth-token')

  /**
   * 1. Get runtime configuration.
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
     */
    baseURL: config.public.apiBase,

    /* Manually mapping necessary options */
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

/**
 * 3. Helper function to fetch the current user's profile.
 * This can be used directly in profile.vue.
 */
export const useUserProfile = () => {
  return useApi<UserProfileDTO>('/api/users/profile', {
    method: 'GET'
  })
}
