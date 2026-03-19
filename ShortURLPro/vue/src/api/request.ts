const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

class HttpClient {
  private baseUrl: string

  constructor(baseUrl: string = API_BASE_URL) {
    this.baseUrl = baseUrl
  }

  private async request<T>(
    url: string,
    options: RequestInit = {}
  ): Promise<ApiResponse<T>> {
    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      },
      ...options
    }

    // 添加认证头
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      (config.headers as Record<string, string>)['Authorization'] = `Bearer ${token}`
    }

    try {
      const response = await fetch(`${this.baseUrl}${url}`, config)
      
      if (!response.ok) {
        if (response.status === 401) {
          // Token过期，清除认证信息
          localStorage.removeItem('token')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
          throw new Error('认证已过期，请重新登录')
        }
        
        // 尝试解析统一的错误响应格式
        const errorData = await response.json().catch(() => ({ 
          code: response.status, 
          message: '请求失败' 
        }))
        
        // 如果是统一的ApiResponse格式
        if (errorData && typeof errorData === 'object' && 'message' in errorData) {
          throw new Error(errorData.message || `HTTP ${response.status}`)
        } else {
          throw new Error(`HTTP ${response.status}: ${errorData.message || '请求失败'}`)
        }
      }

      const data = await response.json()
      return data
    } catch (error) {
      console.error('API请求失败:', error)
      throw error
    }
  }

  public get<T>(url: string, params?: Record<string, any>): Promise<ApiResponse<T>> {
    let queryString = ''
    if (params) {
      const searchParams = new URLSearchParams()
      Object.keys(params).forEach(key => {
        if (params[key] !== undefined && params[key] !== null) {
          searchParams.append(key, String(params[key]))
        }
      })
      queryString = `?${searchParams.toString()}`
    }
    return this.request<T>(`${url}${queryString}`, { method: 'GET' })
  }

  public post<T>(url: string, data?: any): Promise<ApiResponse<T>> {
    return this.request<T>(url, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined
    })
  }

  public put<T>(url: string, data?: any): Promise<ApiResponse<T>> {
    return this.request<T>(url, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined
    })
  }

  public patch<T>(url: string, data?: any): Promise<ApiResponse<T>> {
    return this.request<T>(url, {
      method: 'PATCH',
      body: data ? JSON.stringify(data) : undefined
    })
  }

  public delete<T>(url: string): Promise<ApiResponse<T>> {
    return this.request<T>(url, { method: 'DELETE' })
  }
}

export const httpClient = new HttpClient()