import { api } from './request'

export interface ShortUrlGenerateRequest {
  originalUrl: string
  appId?: string
}

export interface ShortUrlGenerateResponse {
  shortCode: string
  shortUrl: string
}

export interface ShortUrlCreateRequest {
  name: string
  originalUrl: string
  shortCode?: string
  status?: 'ENABLED' | 'DISABLED'
  appId?: string
  expiresAt?: string
}

export interface ShortUrlResponse {
  id: number
  name: string
  originalUrl: string
  shortCode: string
  status: 'ENABLED' | 'DISABLED'
  clickCount: number
  appId?: string
  expiresAt?: string
  createdAt: string
  updatedAt: string
  userId?: number
}

export interface ShortUrlListResponse {
  content: ShortUrlResponse[]
  totalPages: number
  totalElements: number
  size: number
  number: number
}

export interface ShortUrlStats {
  totalCount: number
  enabledCount: number
  disabledCount: number
  totalClicks: number
}

export interface ShortUrlQueryParams {
  page?: number
  size?: number
  search?: string
  status?: string
  sort?: string
}

export const shortUrlApi = {
  generate: (data: ShortUrlGenerateRequest) => 
    api.post<ShortUrlGenerateResponse>('/api/short-url/generate', data, { skipAuth: true }),
  
  create: (data: ShortUrlCreateRequest) => 
    api.post<ShortUrlResponse>('/api/short-url', data),
  
  update: (id: number, data: ShortUrlCreateRequest) => 
    api.put<ShortUrlResponse>(`/api/short-url/admin/${id}`, data),
  
  delete: (id: number) => 
    api.delete<void>(`/api/short-url/admin/${id}`),
  
  toggleStatus: (id: number, status: 'ENABLED' | 'DISABLED') => 
    api.patch<void>(`/api/short-url/status/${id}`, { status }),
  
  getList: (params: ShortUrlQueryParams = {}) => {
    const query = new URLSearchParams()
    if (params.page !== undefined) query.set('page', String(params.page))
    if (params.size) query.set('size', String(params.size))
    if (params.search) query.set('search', params.search)
    if (params.status) query.set('status', params.status)
    if (params.sort) query.set('sort', params.sort)
    return api.get<ShortUrlListResponse>(`/api/short-url/admin/list?${query.toString()}`)
  },
  
  getStats: () => api.get<ShortUrlStats>('/api/short-url/stats'),
  
  export: (params: { search?: string; status?: string }) => {
    const query = new URLSearchParams()
    if (params.search) query.set('search', params.search)
    if (params.status) query.set('status', params.status)
    return api.get<Blob>(`/api/short-url/admin/export?${query.toString()}`)
  }
}
