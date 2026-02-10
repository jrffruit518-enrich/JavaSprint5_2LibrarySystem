export default defineNuxtRouteMiddleware((to, _from) => {
  // English Comment: Using a basic check for now. Replace with your actual auth logic later.
  // _from starts with underscore to tell TS it's intentionally unused.

  const isAuthenticated = false // TODO: Replace with your actual auth state (e.g., a cookie check)

  if (!isAuthenticated && to.path.startsWith('/admin')) {
    // English Comment: Prevent infinite redirect if already on login
    return navigateTo('/')
  }
})
