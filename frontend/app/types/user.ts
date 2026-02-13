export interface UserRow {
  id: number
  username: string
  email: string
  userRole: string
  enabled: boolean
}

// 方便后续使用的初始化函数
export const createEmptyUser = (): UserRow => ({
  id: 0,
  username: '',
  email: '',
  userRole: 'ROLE_USER',
  enabled: true
})