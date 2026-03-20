import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface UserInfo {
  id: number
  username: string
  role: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ROLE_ADMIN')
  const displayRole = computed(() => {
    switch (userInfo.value?.role) {
      case 'ROLE_ADMIN': return '管理员'
      case 'ROLE_USER': return '普通用户'
      default: return '访客'
    }
  })

  function setAuth(data: { 
    token: string, 
    refreshToken: string, 
    userInfo: UserInfo 
  }) {
    token.value = data.token
    refreshToken.value = data.refreshToken
    userInfo.value = data.userInfo
    
    localStorage.setItem('token', data.token)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
  }

  function clearAuth() {
    token.value = null
    refreshToken.value = null
    userInfo.value = null
    
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
  }

  function loadFromStorage() {
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      try {
        userInfo.value = JSON.parse(storedUserInfo)
      } catch (e) {
        console.error('Failed to parse user info:', e)
        clearAuth()
      }
    }
  }

  function getAuthHeaders() {
    return {
      'Authorization': `Bearer ${token.value}`,
      'Content-Type': 'application/json'
    }
  }

  // 初始化时从localStorage加载数据
  loadFromStorage()

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    isAdmin,
    displayRole,
    setAuth,
    clearAuth,
    loadFromStorage,
    getAuthHeaders
  }
})