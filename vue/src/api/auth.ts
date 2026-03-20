import { httpClient } from './request'

interface LoginRequest {
  username: string
  password: string
}

interface LoginResponse {
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
  expireAt: number | null
  errorMessage: string | null
}

export const authApi = {
  // 管理员登录
  login(data: LoginRequest): Promise<LoginResponse> {
    console.log('发送登录请求到:', '/api/auth/login', '数据:', data);
    return httpClient.post<LoginResponse>('/api/auth/login', data)
      .then(response => {
        console.log('登录响应数据:', response);
        // 检查响应格式
        if (!response || response.code !== 200) {
          throw new Error(response?.message || '登录失败');
        }
        return response.data;
      })
      .catch(error => {
        console.error('登录请求失败:', error);
        throw error;
      });
  },

  // 验证Token有效性
  validate(): Promise<TokenValidationResponse> {
    console.log('发送Token验证请求');
    return httpClient.get<TokenValidationResponse>('/api/auth/validate')
      .then(response => {
        console.log('Token验证响应完整数据:', response);
        console.log('Token验证响应类型:', typeof response);
        console.log('Token验证响应是否有data属性:', response && 'data' in response);
        
        // 检查响应格式
        if (!response) {
          throw new Error('Token验证无响应');
        }
        
        // 如果响应本身就是TokenValidationResponse格式
        if (response.hasOwnProperty('valid')) {
          console.log('响应已经是TokenValidationResponse格式');
          return response as unknown as TokenValidationResponse;
        }
        
        // 如果响应包含data字段
        if (response.data && response.data.hasOwnProperty('valid')) {
          console.log('从response.data中提取TokenValidationResponse');
          return response.data as TokenValidationResponse;
        }
        
        // 如果都不匹配，抛出错误
        console.error('无法识别的响应格式:', response);
        throw new Error('Token验证响应格式错误: ' + JSON.stringify(response));
      })
      .catch(error => {
        console.error('Token验证失败:', error);
        // 如果是网络错误或401，返回无效状态
        if (error.message.includes('401') || error.message.includes('认证')) {
          return {
            valid: false,
            username: '',
            role: '',
            expireAt: null,
            errorMessage: 'Token无效或已过期'
          } as TokenValidationResponse;
        }
        throw error;
      });
  },

  // 刷新Token
  refreshToken(refreshToken: string): Promise<LoginResponse> {
    return httpClient.post<LoginResponse>('/api/auth/refresh', { refreshToken })
      .then(response => response.data)
  }
}