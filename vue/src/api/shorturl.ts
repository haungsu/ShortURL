import { httpClient, API_BASE_URL } from './request'

export interface ShortUrlGenerateRequest {
  originalUrl: string
  appId?: string
}

export interface ShortUrlGenerateResponse {
  shortCode: string
  shortUrl: string
  originalUrl: string
  expireTime?: string
}

export interface ShortUrlCreateRequest {
  name: string
  originalUrl: string
  status: 'ENABLED' | 'DISABLED'
  shortCode?: string
  appId?: string
  expiresAt?: string
}

export interface ShortUrlResponse {
  id: number
  name: string
  shortCode: string
  shortUrl: string
  originalUrl: string
  status: 'ENABLED' | 'DISABLED'
  clickCount: number
  createdAt: string
  updatedAt: string
  userId?: number
  appId?: string
  expiresAt?: string
}

interface ShortUrlListResponse {
  content: ShortUrlResponse[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface ShortUrlStats {
  totalUrls: number
  totalClicks: number
  todayClicks: number
  activeUrls: number
  totalCount?: number
  enabledCount?: number
  disabledCount?: number
}

export const shortUrlApi = {
  // 生成短链接（公开接口）
  generate(data: ShortUrlGenerateRequest): Promise<ShortUrlGenerateResponse> {
    return httpClient.post<ShortUrlGenerateResponse>('/api/short-url/generate', data)
      .then(response => {
        console.log('生成短链接API响应:', response);
        // 检查统一响应格式
        if (!response || response.code !== 200) {
          throw new Error(response?.message || '生成短链接失败');
        }
        return response.data;
      })
      .catch(error => {
        console.error('生成短链接失败:', error);
        throw error;
      });
  },

  // 管理员创建短链接
  create(data: ShortUrlCreateRequest): Promise<ShortUrlResponse> {
    return httpClient.post<ShortUrlResponse>('/api/short-url', data)
      .then(response => {
        if (response.code !== 200) {
          throw new Error(response.message || '创建短链接失败');
        }
        return response.data;
      })
  },

  // 获取短链接列表（管理员）
  getList(params: {
    page?: number
    size?: number
    keyword?: string
    status?: string
    sort?: string
  }): Promise<ShortUrlListResponse> {
    return httpClient.get<any>('/api/short-url/admin/list', params)
      .then(response => {
        console.log('getList API响应:', response);
        
        // 检查统一响应格式
        if (!response || response.code !== 200) {
          throw new Error(response?.message || '获取列表失败');
        }
        
        // 从ApiResponse中提取data
        const responseData = response.data;
        console.log('getList 响应数据:', responseData);
        
        // 处理不同格式的响应
        if (responseData && typeof responseData === 'object') {
          // 直接返回正确的格式
          if ('content' in responseData && Array.isArray(responseData.content)) {
            console.log('使用直接格式:', responseData);
            return responseData as ShortUrlListResponse;
          }
          // 如果是包装在data字段中的格式
          if ('data' in responseData && responseData.data && 'content' in responseData.data) {
            console.log('使用嵌套data格式:', responseData.data);
            return responseData.data as ShortUrlListResponse;
          }
        }
        
        // 如果格式不匹配，返回默认空结构
        console.warn('响应格式不匹配，返回空列表');
        return {
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: 0,
          number: 0
        };
      })
      .catch(error => {
        console.error('getList 失败:', error);
        throw error;
      });
  },

  // 获取统计信息（管理员）
  getStats(): Promise<ShortUrlStats> {
    return httpClient.get<ShortUrlStats>('/api/short-url/admin/stats')
      .then(response => {
        if (response.code !== 200) {
          throw new Error(response.message || '获取统计信息失败');
        }
        return response.data;
      })
  },

  // 更新短链接
  update(id: number, data: Partial<ShortUrlCreateRequest>): Promise<ShortUrlResponse> {
    return httpClient.put<ShortUrlResponse>(`/api/short-url/${id}`, data)
      .then(response => {
        if (response.code !== 200) {
          throw new Error(response.message || '更新短链接失败');
        }
        return response.data;
      })
  },

  // 删除短链接
  delete(id: number): Promise<void> {
    return httpClient.delete<void>(`/api/short-url/${id}`)
      .then(response => {
        // 删除操作通常返回204状态码，没有data
        if (response.code !== 200 && response.code !== 204) {
          throw new Error(response.message || '删除短链接失败');
        }
        return response.data;
      })
  },

  // 切换短链接状态
  toggleStatus(id: number, status: 'ENABLED' | 'DISABLED'): Promise<ShortUrlResponse> {
    return httpClient.patch<ShortUrlResponse>(`/api/short-url/status/${id}`, { status })
      .then(response => {
        if (response.code !== 200) {
          throw new Error(response.message || '切换状态失败');
        }
        return response.data;
      })
  },

  // 导出数据
  export(params?: {
    keyword?: string
    status?: string
    startTime?: string
    endTime?: string
  }): Promise<Blob> {
    // 使用原生fetch处理二进制响应
    const searchParams = new URLSearchParams();
    if (params?.keyword) searchParams.append('keyword', params.keyword);
    if (params?.status) searchParams.append('status', params.status);
      
    const queryString = searchParams.toString();
    const url = `${API_BASE_URL}/api/short-url/admin/export${queryString ? '?' + queryString : ''}`;
      
    // 添加认证头
    const token = localStorage.getItem('token');
    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
      
    return fetch(url, { 
      method: 'GET',
      headers 
    }).then(response => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: 导出失败`);
      }
      return response.blob();
    });
  }
}