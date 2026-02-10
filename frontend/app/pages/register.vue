<script setup>
/**
 * 图书馆项目 (Library Project) - 首页登录
 * 对接后端认证接口
 */
const isLoginModalOpen = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

// 获取运行时配置中的 apiBase
const config = useRuntimeConfig()
const apiBase = config.public.apiBase

/* 处理真实登录逻辑 */
const handleLogin = async () => {
  try {
    // 1. 调用后端登录接口
    const response = await $fetch(`${apiBase}/api/auth/login`, {
      method: 'POST',
      body: {
        username: loginForm.username,
        password: loginForm.password
      }
    })

    // 2. 假设后端返回的对象里包含 { token: "...", role: "..." }
    // 将 Token 存入 Cookie (与 useApi.ts 里的名称保持一致)
    const tokenCookie = useCookie('auth-token')
    tokenCookie.value = response.token

    // 3. 根据角色跳转
    // 这里的角色判断建议使用 response 里的真实角色
    if (response.role === 'ROLE_ADMIN') {
      navigateTo('/admin')
    } else {
      navigateTo('/user')
    }

    isLoginModalOpen.value = false
    console.log('Login successful!')
  } catch (err) {
    console.error('Login failed:', err)
    alert('Invalid username or password. Please try again.')
  }
}
</script>
