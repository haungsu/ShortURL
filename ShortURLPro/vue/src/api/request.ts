import { useAuthStore } from '@/stores/auth'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

interface RequestOptions extends RequestInit {
  skipAuth?: boolean
}

export async function request<T>(url: string, options: RequestOptions = {}): Promise<T> {
  const authStore = useAuthStore()
  const { skipAuth, ...fetchOptions } = options

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(fetchOptions.headers as Record<string, string>)
  }

  if (!skipAuth && authStore.token) {
    headers['Authorization'] = `Bearer ${authStore.token}`
  }

  const response = await fetch(`${BASE_URL}${url}`, {
    ...fetchOptions,
    headers
  })

  if (!response.ok) {
    if (response.status === 401) {
      authStore.clearAuth()
      window.location.href = '/login'
    }
    
    // 处理不同类型的响应
    let errorMessage = '请求失败'
    const contentType = response.headers.get('content-type')
    
    if (contentType && contentType.includes('application/json')) {
      try {
        const error = await response.json()
        errorMessage = error.message || `HTTP ${response.status}`
      } catch (e) {
        errorMessage = `HTTP ${response.status}`
      }
    } else {
      errorMessage = `HTTP ${response.status}: ${response.statusText}`
    }
    
    throw new Error(errorMessage)
  }

  if (response.status === 204) {
    return undefined as T
  }

  // 检查响应内容类型
  const contentType = response.headers.get('content-type')
  if (!contentType || !contentType.includes('application/json')) {
    // 如果不是JSON响应，返回文本或其他适当类型
    const text = await response.text()
    return text as T
  }

  try {
    return await response.json()
  } catch (error) {
    console.error('JSON解析失败:', error)
    throw new Error('服务器响应格式错误')
  }
}

export const api = {
  get: <T>(url: string, options?: RequestOptions) => request<T>(url, { ...options, method: 'GET' }),
  post: <T>(url: string, body?: unknown, options?: RequestOptions) => request<T>(url, { ...options, method: 'POST', body: body ? JSON.stringify(body) : undefined }),
  put: <T>(url: string, body?: unknown, options?: RequestOptions) => request<T>(url, { ...options, method: 'PUT', body: body ? JSON.stringify(body) : undefined }),
  patch: <T>(url: string, body?: unknown, options?: RequestOptions) => request<T>(url, { ...options, method: 'PATCH', body: body ? JSON.stringify(body) : undefined }),
  delete: <T>(url: string, options?: RequestOptions) => request<T>(url, { ...options, method: 'DELETE' })
}
