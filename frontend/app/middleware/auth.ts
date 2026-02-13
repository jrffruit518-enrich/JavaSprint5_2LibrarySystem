export default defineNuxtRouteMiddleware((to, _from) => {
  // Jules: 从 Cookie 中获取标准化命名的凭证
  const token = useCookie('auth_token').value
  const role = useCookie('user_role').value 

  const isAuthenticated = !!token

  // 1. 如果未登录，且试图进入受保护区域（/user 或 /admin），直接踢回首页
  if (!isAuthenticated && (to.path.startsWith('/admin') || to.path.startsWith('/user'))) {
    console.warn('[JULES AUTH] Unauthenticated access, redirecting to login...')
    return navigateTo('/')
  }

  // 2. 如果已登录，但试图进入管理页却不具备管理员身份，踢回个人中心
  // 考虑到后端可能返回 ROLE_ADMIN 或 ADMIN，这里做了兼容
  if (to.path.startsWith('/admin')) {
    const isAdmin = role === 'ADMIN' || role === 'ROLE_ADMIN'
    if (!isAdmin) {
      console.error('[JULES AUTH] Admin access denied. Role:', role)
      return navigateTo('/user')
    }
  }
})
