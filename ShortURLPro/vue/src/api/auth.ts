import { httpClient } from './request'

interface LoginRequest {
  username: string
  password: string
}

interface LoginResponse {
  token: string
  refreshToken: string
  userInfo: {
    id: number
    username: string
    role: string
  }
}

interface TokenValidationResponse {
  valid: boolean
  userInfo: {
    id: number
    username: string
    role: string
  }
}

export const authApi = {
  // 管理员登录
  login(data: LoginRequest): Promise<LoginResponse> {
    return httpClient.post<LoginResponse>('/api/auth/login', data)
      .then(response => response.data)
  },

  // 验证Token有效性
  validate(): Promise<TokenValidationResponse> {
    return httpClient.get<TokenValidationResponse>('/api/auth/validate')
      .then(response => response.data)
  },

  // 刷新Token
  refreshToken(refreshToken: string): Promise<LoginResponse> {
    return httpClient.post<LoginResponse>('/api/auth/refresh', { refreshToken })
      .then(response => response.data)
  }
}