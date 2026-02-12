import type { UseFetchOptions } from 'nuxt/app'

/**
 * app/composables/useApi.ts
 * A type-safe fetch wrapper for the Library Project.
 * Updated by Jules: Guaranteed token injection and enhanced error diagnostics.
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
   * 1. Token Retrieval:
   * Ensure index.vue uses 'auth_token' for consistency.
   */
  const token = useCookie<string | null>('auth_token')

  /**
   * 2. Runtime Configuration
   */
  const config = useRuntimeConfig()

  /**
   * 3. Header Merging Strategy
   * We clone existing headers and inject the Authorization bearer token.
   */
  const customHeaders: Record<string, string> = {
    ...((options.headers as Record<string, string>) || {})
  }

  if (token.value) {
    customHeaders['Authorization'] = `Bearer ${token.value}`
  }

  // Jules: Return useFetch with standardized configuration
  return useFetch(url, {
    /**
     * Use dynamic baseURL from runtimeConfig.
     */
    baseURL: config.public.apiBase,

    /* Merge original options */
    ...options,

    /* Explicitly override headers with our token-injected object */
    headers: customHeaders,

    /* Response error handling and diagnostics */
    onResponseError({ response }) {
      const status = response.status
      const errorData = response._data as unknown as ApiError
      const errorMsg = errorData?.message || 'Access Denied'

      if (status === 401 || status === 403) {
        console.error(`>>> [JULES AUTH ERROR ${status}]:`, errorMsg)
        console.dir(response._data) // Print full body for deep debugging

        // Standardize redirect to login on auth failure (Client-side only)
        if (import.meta.client) {
          // Optional: Only redirect on 401 (Unauthorized)
          // navigateTo('/')
        }
      } else {
        console.error(`>>> [JULES API ERROR ${status}]:`, errorMsg)
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
