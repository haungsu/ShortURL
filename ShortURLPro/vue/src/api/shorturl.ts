import { httpClient } from './request'

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
  expireTime?: string
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
  expireTime?: string
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
}

export const shortUrlApi = {
  // 生成短链接（公开接口）
  generate(data: ShortUrlGenerateRequest): Promise<ShortUrlGenerateResponse> {
    return httpClient.post<ShortUrlGenerateResponse>('/api/short-url/generate', data)
      .then(response => response.data)
  },

  // 管理员创建短链接
  create(data: ShortUrlCreateRequest): Promise<ShortUrlResponse> {
    return httpClient.post<ShortUrlResponse>('/api/short-url', data)
      .then(response => response.data)
  },

  // 获取短链接列表（管理员）
  getList(params: {
    page?: number
    size?: number
    keyword?: string
    status?: string
    sort?: string
  }): Promise<ShortUrlListResponse> {
    return httpClient.get<ShortUrlListResponse>('/api/short-url/admin/list', params)
      .then(response => response.data)
  },

  // 获取统计信息（管理员）
  getStats(): Promise<ShortUrlStats> {
    return httpClient.get<ShortUrlStats>('/api/short-url/admin/stats')
      .then(response => response.data)
  },

  // 更新短链接
  update(id: number, data: Partial<ShortUrlCreateRequest>): Promise<ShortUrlResponse> {
    return httpClient.put<ShortUrlResponse>(`/api/short-url/${id}`, data)
      .then(response => response.data)
  },

  // 删除短链接
  delete(id: number): Promise<void> {
    return httpClient.delete<void>(`/api/short-url/${id}`)
      .then(response => response.data)
  },

  // 切换短链接状态
  toggleStatus(id: number, status: 'ENABLED' | 'DISABLED'): Promise<ShortUrlResponse> {
    return httpClient.patch<ShortUrlResponse>(`/api/short-url/${id}/status`, { status })
      .then(response => response.data)
  },

  // 导出数据
  export(params: {
    keyword?: string
    status?: string
    startTime?: string
    endTime?: string
  }): Promise<Blob> {
    return httpClient.get<Blob>('/api/short-url/admin/export', params)
      .then(response => new Blob([JSON.stringify(response.data)], { type: 'application/json' }))
  }
}