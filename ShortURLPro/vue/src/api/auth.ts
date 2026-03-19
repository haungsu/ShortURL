import { api } from './request'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  username: string
  role: string
  nickname: string
}

export interface TokenValidationResponse {
  valid: boolean
  username: string
  role: string
}

export const authApi = {
  login: (data: LoginRequest) => api.post<LoginResponse>('/api/auth/login', data, { skipAuth: true }),
  validate: () => api.get<TokenValidationResponse>('/api/auth/validate')
}
