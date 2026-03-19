<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { shortUrlApi, type ShortUrlResponse, type ShortUrlStats } from '@/api/shorturl'
import { authApi } from '@/api/auth'

interface CreateForm {
  name: string
  originalUrl: string
  status: 'ENABLED' | 'DISABLED'
}

const router = useRouter()
const authStore = useAuthStore()

// 统计数据
const stats = ref<ShortUrlStats>({
  totalUrls: 0,
  totalClicks: 0,
  todayClicks: 0,
  activeUrls: 0
})

// 短链接列表
const shortUrls = ref<ShortUrlResponse[]>([])
const loading = ref(false)
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

// 搜索和筛选
const searchKeyword = ref('')
const statusFilter = ref('')
const sortOrder = ref('createdAt,desc')

// 模态框状态
const showCreateModal = ref(false)
const showEditModal = ref(false)
const editingUrl = ref<ShortUrlResponse | null>(null)

// 表单数据
const createForm = ref<CreateForm>({
  name: '',
  originalUrl: '',
  status: 'ENABLED'
})

const editForm = ref<CreateForm>({
  name: '',
  originalUrl: '',
  status: 'ENABLED'
})

onMounted(async () => {
  await checkAdminAuth()
  await loadStats()
  await loadShortUrls()
})

async function checkAdminAuth() {
  try {
    console.log('开始权限验证...');
    
    // 检查是否有token
    const token = localStorage.getItem('token');
    if (!token) {
      console.log('未找到token，跳转到登录页');
      alert('请先登录');
      router.push('/login');
      return;
    }
    
    const response = await authApi.validate();
    console.log('权限验证响应:', response);
    
    // 检查响应是否存在
    if (!response) {
      console.error('权限验证响应为空');
      alert('权限验证失败：服务器无响应');
      router.push('/login');
      return;
    }
    
    // 检查token有效性
    if (!response.valid) {
      console.log('Token无效:', response.errorMessage);
      alert('Token无效: ' + (response.errorMessage || '未知错误'));
      router.push('/login');
      return;
    }
    
    // 检查管理员权限
    if (response.role !== 'ROLE_ADMIN') {
      console.log('权限不足，当前角色:', response.role);
      alert('权限不足，需要管理员权限');
      router.push('/');
      return;
    }
    
    console.log('权限验证通过，角色:', response.role);
    // 更新store中的用户信息
    authStore.loadFromStorage();
    
  } catch (error: any) {
    console.error('权限验证失败:', error);
    console.error('错误详情:', error.response || error.message || error);
    alert('权限验证失败，请重新登录: ' + (error.message || '网络错误'));
    router.push('/login');
  }
}

async function loadStats() {
  try {
    const statsData = await shortUrlApi.getStats()
    // 处理不同的统计字段名称
    stats.value = {
      totalUrls: statsData.totalUrls || statsData.totalCount || 0,
      totalClicks: statsData.totalClicks || 0,
      todayClicks: statsData.todayClicks || 0,
      activeUrls: statsData.activeUrls || statsData.enabledCount || 0
    }
  } catch (error: any) {
    console.error('加载统计失败:', error)
    // 设置默认值
    stats.value = {
      totalUrls: 0,
      totalClicks: 0,
      todayClicks: 0,
      activeUrls: 0
    }
  }
}

async function loadShortUrls() {
  loading.value = true
  try {
    console.log('发送请求参数:', {
      page: currentPage.value,
      size: 10,
      keyword: searchKeyword.value,
      status: statusFilter.value,
      sort: sortOrder.value
    })
    
    const response = await shortUrlApi.getList({
      page: currentPage.value,
      size: 10,
      keyword: searchKeyword.value,
      status: statusFilter.value,
      sort: sortOrder.value
    })
    
    console.log('API响应数据:', response)
    
    // 直接使用返回的数据
    shortUrls.value = response.content || []
    totalPages.value = response.totalPages || 0
    totalElements.value = response.totalElements || 0
    
  } catch (error: any) {
    console.error('加载短链接列表失败:', error)
    console.error('错误详情:', error.response || error.message || error)
    shortUrls.value = []
    totalPages.value = 0
    totalElements.value = 0
    alert('加载数据失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

async function searchShortUrls() {
  currentPage.value = 0
  await loadShortUrls()
}

async function exportData() {
  try {
    const blob = await shortUrlApi.export({
      keyword: searchKeyword.value,
      status: statusFilter.value
    })
    
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `shorturls-${new Date().toISOString().split('T')[0]}.json`
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('导出失败:', error)
    alert('导出失败')
  }
}

function showCreateForm() {
  createForm.value = {
    name: '',
    originalUrl: '',
    status: 'ENABLED'
  }
  showCreateModal.value = true
}

async function createShortUrl() {
  try {
    await shortUrlApi.create(createForm.value)
    showCreateModal.value = false
    await loadStats()
    await loadShortUrls()
  } catch (error: any) {
    alert(`创建失败: ${error.message}`)
  }
}

function showEditForm(url: ShortUrlResponse) {
  editingUrl.value = url
  editForm.value = {
    name: url.name,
    originalUrl: url.originalUrl,
    status: url.status
  }
  showEditModal.value = true
}

async function saveChanges() {
  if (!editingUrl.value) return
  
  try {
    await shortUrlApi.update(editingUrl.value.id, editForm.value)
    showEditModal.value = false
    editingUrl.value = null
    await loadShortUrls()
  } catch (error: any) {
    alert(`更新失败: ${error.message}`)
  }
}

async function toggleStatus(url: ShortUrlResponse) {
  try {
    const newStatus = url.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
    await shortUrlApi.toggleStatus(url.id, newStatus)
    await loadShortUrls()
    await loadStats()
  } catch (error: any) {
    alert(`操作失败: ${error.message}`)
  }
}

async function deleteShortUrl(url: ShortUrlResponse) {
  if (!confirm(`确定要删除短链接 "${url.name}" 吗？`)) return
  
  try {
    await shortUrlApi.delete(url.id)
    await loadShortUrls()
    await loadStats()
  } catch (error: any) {
    alert(`删除失败: ${error.message}`)
  }
}

function formatDate(dateString: string): string {
  return new Date(dateString).toLocaleString('zh-CN')
}

function getStatusBadge(status: string) {
  return status === 'ENABLED' ? 'badge-success' : 'badge-error'
}

function getStatusText(status: string) {
  return status === 'ENABLED' ? '启用' : '禁用'
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
    <!-- 页面标题 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-text-primary">管理后台</h1>
      <p class="mt-2 text-text-secondary">管理您的短链接和查看统计数据</p>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="card shadow-card">
        <div class="flex items-center">
          <div class="p-3 rounded-lg bg-blue-100 dark:bg-blue-900/30">
            <svg class="w-6 h-6 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"></path>
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-text-secondary">总链接数</p>
            <p class="text-2xl font-semibold text-text-primary">{{ stats.totalUrls }}</p>
          </div>
        </div>
      </div>
      
      <div class="card shadow-card">
        <div class="flex items-center">
          <div class="p-3 rounded-lg bg-green-100 dark:bg-green-900/30">
            <svg class="w-6 h-6 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-text-secondary">总点击量</p>
            <p class="text-2xl font-semibold text-text-primary">{{ stats.totalClicks }}</p>
          </div>
        </div>
      </div>
      
      <div class="card shadow-card">
        <div class="flex items-center">
          <div class="p-3 rounded-lg bg-purple-100 dark:bg-purple-900/30">
            <svg class="w-6 h-6 text-purple-600 dark:text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-text-secondary">今日点击</p>
            <p class="text-2xl font-semibold text-text-primary">{{ stats.todayClicks }}</p>
          </div>
        </div>
      </div>
      
      <div class="card shadow-card">
        <div class="flex items-center">
          <div class="p-3 rounded-lg bg-yellow-100 dark:bg-yellow-900/30">
            <svg class="w-6 h-6 text-yellow-600 dark:text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-text-secondary">活跃链接</p>
            <p class="text-2xl font-semibold text-text-primary">{{ stats.activeUrls }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="card mb-6">
      <div class="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
        <div class="flex flex-col sm:flex-row gap-4 flex-1">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索链接名称或原始URL"
            class="input w-full sm:w-64"
            @keyup.enter="searchShortUrls"
          />
          <select
            v-model="statusFilter"
            class="input w-full sm:w-32"
            @change="searchShortUrls"
          >
            <option value="">全部状态</option>
            <option value="ENABLED">启用</option>
            <option value="DISABLED">禁用</option>
          </select>
        </div>
        <div class="flex gap-2">
          <button
            @click="exportData"
            class="btn btn-secondary"
          >
            导出数据
          </button>
          <button
            @click="showCreateForm"
            class="btn btn-primary"
          >
            新建链接
          </button>
        </div>
      </div>
    </div>

    <!-- 短链接列表 -->
    <div class="card">
      <div v-if="loading" class="text-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
        <p class="mt-2 text-text-secondary">加载中...</p>
      </div>
      
      <div v-else-if="shortUrls.length === 0" class="text-center py-8">
        <svg class="mx-auto h-12 w-12 text-text-secondary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6-4h6m2 5.291A7.962 7.962 0 0112 15c-2.343 0-4.43.859-6 2.291m0-11.291A7.962 7.962 0 0112 3c2.343 0 4.43.859 6 2.291m0 11.291v-2.5a2.5 2.5 0 00-2.5-2.5h-10A2.5 2.5 0 004 18.5v2.5"></path>
        </svg>
        <h3 class="mt-2 text-sm font-medium text-text-primary">暂无数据</h3>
        <p class="mt-1 text-sm text-text-secondary">开始创建您的第一个短链接吧</p>
      </div>
      
      <div v-else>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-border">
            <thead class="bg-surface">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">名称</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">短链接</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">原始链接</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">状态</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">点击量</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">创建时间</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">操作</th>
              </tr>
            </thead>
            <tbody class="bg-background divide-y divide-border">
              <tr v-for="url in shortUrls" :key="url.id" class="hover:bg-surface/50">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-text-primary">
                  {{ url.name }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-text-secondary">
                  <a :href="url.shortUrl" target="_blank" class="text-primary hover:underline">
                    {{ url.shortUrl }}
                  </a>
                </td>
                <td class="px-6 py-4 text-sm text-text-secondary max-w-xs truncate">
                  {{ url.originalUrl }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span :class="['badge', getStatusBadge(url.status)]">
                    {{ getStatusText(url.status) }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-text-primary">
                  {{ url.clickCount }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-text-secondary">
                  {{ formatDate(url.createdAt) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <button
                    @click="toggleStatus(url)"
                    :class="url.status === 'ENABLED' ? 'text-warning hover:text-orange-600' : 'text-success hover:text-green-600'"
                    class="mr-3"
                  >
                    {{ url.status === 'ENABLED' ? '禁用' : '启用' }}
                  </button>
                  <button
                    @click="showEditForm(url)"
                    class="text-primary hover:text-blue-600 mr-3"
                  >
                    编辑
                  </button>
                  <button
                    @click="deleteShortUrl(url)"
                    class="text-error hover:text-red-600"
                  >
                    删除
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- 分页 -->
        <div v-if="totalPages > 1" class="mt-6 flex items-center justify-between">
          <div class="text-sm text-text-secondary">
            显示第 {{ currentPage * 10 + 1 }} 到 {{ Math.min((currentPage + 1) * 10, totalElements) }} 条，共 {{ totalElements }} 条记录
          </div>
          <div class="flex space-x-2">
            <button
              @click="currentPage--; loadShortUrls()"
              :disabled="currentPage === 0"
              class="px-3 py-1 rounded-md text-sm font-medium bg-white dark:bg-surface border border-border text-text-primary disabled:opacity-50 disabled:cursor-not-allowed hover:bg-surface"
            >
              上一页
            </button>
            <button
              @click="currentPage++; loadShortUrls()"
              :disabled="currentPage >= totalPages - 1"
              class="px-3 py-1 rounded-md text-sm font-medium bg-white dark:bg-surface border border-border text-text-primary disabled:opacity-50 disabled:cursor-not-allowed hover:bg-surface"
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建短链接模态框 -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div class="bg-white dark:bg-surface rounded-lg shadow-xl max-w-md w-full">
        <div class="p-6">
          <h3 class="text-lg font-medium text-text-primary mb-4">创建短链接</h3>
          <form @submit.prevent="createShortUrl" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-text-primary mb-1">链接名称</label>
              <input
                v-model="createForm.name"
                type="text"
                required
                class="input"
                placeholder="例如：官网链接"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-text-primary mb-1">原始链接</label>
              <input
                v-model="createForm.originalUrl"
                type="url"
                required
                class="input"
                placeholder="https://example.com"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-text-primary mb-1">状态</label>
              <select v-model="createForm.status" class="input">
                <option value="ENABLED">启用</option>
                <option value="DISABLED">禁用</option>
              </select>
            </div>
            <div class="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                @click="showCreateModal = false"
                class="btn btn-secondary"
              >
                取消
              </button>
              <button
                type="submit"
                class="btn btn-primary"
              >
                创建
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 编辑短链接模态框 -->
    <div v-if="showEditModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div class="bg-white dark:bg-surface rounded-lg shadow-xl max-w-md w-full">
        <div class="p-6">
          <h3 class="text-lg font-medium text-text-primary mb-4">编辑短链接</h3>
          <form @submit.prevent="saveChanges" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-text-primary mb-1">链接名称</label>
              <input
                v-model="editForm.name"
                type="text"
                required
                class="input"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-text-primary mb-1">原始链接</label>
              <input
                v-model="editForm.originalUrl"
                type="url"
                required
                class="input"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-text-primary mb-1">状态</label>
              <select v-model="editForm.status" class="input">
                <option value="ENABLED">启用</option>
                <option value="DISABLED">禁用</option>
              </select>
            </div>
            <div class="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                @click="showEditModal = false"
                class="btn btn-secondary"
              >
                取消
              </button>
              <button
                type="submit"
                class="btn btn-primary"
              >
                保存
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>