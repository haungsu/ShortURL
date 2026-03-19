import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export interface UserInfo {
  username: string
  role: string
  nickname: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('jwtToken'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
  const userInfo = ref<UserInfo>({
    username: localStorage.getItem('username') || '',
    role: localStorage.getItem('role') || '',
    nickname: localStorage.getItem('nickname') || ''
  })

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value.role === 'ROLE_ADMIN')
  const displayRole = computed(() => userInfo.value.role.replace('ROLE_', ''))

  function setAuth(data: { accessToken: string; refreshToken: string; username: string; role: string; nickname: string }) {
    token.value = data.accessToken
    refreshToken.value = data.refreshToken
    userInfo.value = {
      username: data.username,
      role: data.role,
      nickname: data.nickname
    }
    localStorage.setItem('jwtToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('username', data.username)
    localStorage.setItem('role', data.role)
    localStorage.setItem('nickname', data.nickname)
  }

  function clearAuth() {
    token.value = null
    refreshToken.value = null
    userInfo.value = { username: '', role: '', nickname: '' }
    localStorage.removeItem('jwtToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    localStorage.removeItem('nickname')
  }

  function getAuthHeaders(): Record<string, string> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json'
    }
    if (token.value && token.value !== 'undefined') {
      headers['Authorization'] = `Bearer ${token.value}`
    }
    return headers
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    isAdmin,
    displayRole,
    setAuth,
    clearAuth,
    getAuthHeaders
  }
})
