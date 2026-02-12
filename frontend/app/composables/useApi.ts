import type { UseFetchOptions } from 'nuxt/app'

/**
 * app/composables/useApi.ts
 * A type-safe fetch wrapper for the Library Project.
 * Updated: Standardized cookie key to 'auth_token'
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
  userRole: 'MEMBER' | 'ADMIN'
  avatarUrl: string
}

export const useApi = <T>(url: string, options: UseFetchOptions<T> = {}) => {
  /**
   * 1. Core Fix: Changed 'auth-token' to 'auth_token' to match index.vue
   */
  const token = useCookie<string | null>('auth_token')

  /**
   * 2. Get runtime configuration.
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
     * 3. Use dynamic baseURL from runtimeConfig.
     */
    baseURL: config.public.apiBase,

    /* Manually mapping necessary options - ALL ORIGINAL OPTIONS PRESERVED */
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

        // English Comment: Standardize redirect to login on auth failure
        if (import.meta.client) {
          navigateTo('/')
        }
      } else {
        console.error(`[API Error ${status}]:`, errorMsg)
      }
    }
  } as UseFetchOptions<T>)
}

/**
 * 4. Helper function to fetch the current user's profile.
 */
export const useUserProfile = () => {
  return useApi<UserProfileDTO>('/api/users/profile', {
    method: 'GET'
  })
}
