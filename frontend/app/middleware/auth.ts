// middleware/auth.ts
export default defineNuxtRouteMiddleware((to, _from) => {
  const token = useCookie('auth_token').value
  const role = useCookie('user_role').value // 核心：确保这里也是下划线

  const isAuthenticated = !!token

  // 如果未登录，踢回首页
  if (!isAuthenticated && (to.path.startsWith('/admin') || to.path.startsWith('/user'))) {
    return navigateTo('/')
  }

  // 如果想去管理员页面，但角色不是 ADMIN，踢回用户页
  if (to.path.startsWith('/admin') && role !== 'ADMIN') {
    return navigateTo('/user')
  }
})
