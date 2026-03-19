<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { shortUrlApi, type ShortUrlResponse, type ShortUrlStats } from '@/api/shorturl'
import { authApi } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

// 获取后端服务域名
const backendOrigin = computed(() => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL
  if (baseUrl) {
    // 移除可能的尾部斜杠
    return baseUrl.replace(/\/$/, '')
  }
  // 默认使用 localhost:8080
  return 'http://localhost:8080'
})

// 统计数据
const stats = ref<ShortUrlStats>({
  totalCount: 0,
  enabledCount: 0,
  disabledCount: 0,
  totalClicks: 0
})

// 列表数据
const shortUrls = ref<ShortUrlResponse[]>([])
const loading = ref(false)
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = 10

// 搜索和筛选
const searchKeyword = ref('')
const statusFilter = ref('')
const sortOrder = ref('createdAt,desc')

// 防抖定时器
let searchTimeout: number | null = null

// 模态框
const showCreateModal = ref(false)
const showEditModal = ref(false)
const editingUrl = ref<ShortUrlResponse | null>(null)

// 表单数据
const createForm = ref({
  name: '',
  originalUrl: '',
  shortCode: '',
  status: 'ENABLED' as 'ENABLED' | 'DISABLED',
  appId: '',
  expiresAt: ''
})

const editForm = ref({
  id: 0,
  name: '',
  shortCode: '',
  originalUrl: '',
  status: 'ENABLED' as 'ENABLED' | 'DISABLED',
  appId: '',
  expiresAt: ''
})

onMounted(async () => {
  await checkAdminAuth()
  await loadStats()
  await loadShortUrls()
})

async function checkAdminAuth() {
  try {
    const data = await authApi.validate()
    if (!data.valid || data.role !== 'ROLE_ADMIN') {
      alert('权限不足！需要管理员权限。')
      router.push('/')
    }
  } catch {
    alert('请先登录！')
    router.push('/login')
  }
}

async function loadStats() {
  try {
    stats.value = await shortUrlApi.getStats()
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

async function loadShortUrls(page = 0) {
  loading.value = true
  try {
    const response = await shortUrlApi.getList({
      page,
      size: pageSize,
      search: searchKeyword.value,
      status: statusFilter.value,
      sort: sortOrder.value
    })
    shortUrls.value = response.content
    totalPages.value = response.totalPages
    currentPage.value = response.number
  } catch (error) {
    console.error('加载短链列表失败:', error)
  } finally {
    loading.value = false
  }
}

function search() {
  // 清除之前的定时器
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  
  // 设置新的防抖定时器
  searchTimeout = window.setTimeout(() => {
    loadShortUrls(0)
  }, 500)
}

// 数据导出功能
async function exportData() {
  try {
    const blob = await shortUrlApi.export({
      search: searchKeyword.value,
      status: statusFilter.value
    })
    
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `shorturls_${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
    alert('导出成功！')
  } catch (error: any) {
    alert('导出失败：' + error.message)
  }
}

function openCreateModal() {
  createForm.value = {
    name: '',
    originalUrl: '',
    shortCode: '',
    status: 'ENABLED',
    appId: '',
    expiresAt: ''
  }
  showCreateModal.value = true
}

async function createShortUrl() {
  try {
    await shortUrlApi.create(createForm.value)
    showCreateModal.value = false
    await loadShortUrls(currentPage.value)
    await loadStats()
    alert('创建成功！')
  } catch (error: any) {
    alert('创建失败：' + error.message)
  }
}

function openEditModal(url: ShortUrlResponse) {
  editingUrl.value = url
  editForm.value = {
    id: url.id,
    name: url.name,
    shortCode: url.shortCode,
    originalUrl: url.originalUrl,
    status: url.status,
    appId: url.appId || '',
    expiresAt: url.expiresAt ? url.expiresAt.slice(0, 16) : ''
  }
  showEditModal.value = true
}

async function updateShortUrl() {
  try {
    await shortUrlApi.update(editForm.value.id, {
      name: editForm.value.name,
      originalUrl: editForm.value.originalUrl,
      shortCode: editForm.value.shortCode,
      status: editForm.value.status,
      appId: editForm.value.appId,
      expiresAt: editForm.value.expiresAt || undefined
    })
    showEditModal.value = false
    await loadShortUrls(currentPage.value)
    await loadStats()
    alert('修改成功！')
  } catch (error: any) {
    alert('修改失败：' + error.message)
  }
}

async function toggleStatus(url: ShortUrlResponse) {
  const newStatus = url.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const action = newStatus === 'ENABLED' ? '启用' : '禁用'
  
  if (!confirm(`确定要${action}这个短链吗？`)) return
  
  try {
    await shortUrlApi.toggleStatus(url.id, newStatus)
    await loadShortUrls(currentPage.value)
    await loadStats()
    alert(`${action}成功！`)
  } catch (error: any) {
    alert(`${action}失败：` + error.message)
  }
}

async function deleteShortUrl(url: ShortUrlResponse) {
  if (!confirm('确定要删除这个短链吗？此操作不可恢复！')) return
  
  try {
    await shortUrlApi.delete(url.id)
    await loadShortUrls(currentPage.value)
    await loadStats()
    alert('删除成功！')
  } catch (error: any) {
    alert('删除失败：' + error.message)
  }
}

function copyToClipboard(text: string) {
  navigator.clipboard.writeText(text).then(() => {
    alert('已复制到剪贴板：' + text)
  })
}

function formatDate(dateString?: string) {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

function logout() {
  authStore.clearAuth()
  router.push('/')
}
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 头部 -->
    <header class="bg-gradient-to-r from-blue-600 to-purple-600 text-white shadow-lg sticky top-0 z-40">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center space-x-3">
            <div class="w-10 h-10 bg-white bg-opacity-20 rounded-lg flex items-center justify-center">
              <span class="text-xl">👑</span>
            </div>
            <div>
              <h1 class="text-xl font-bold">管理员短链管理系统</h1>
              <p class="text-sm text-blue-100">全面掌控所有短链接的状态与数据</p>
            </div>
          </div>
          <div class="flex items-center space-x-4">
 <div class="flex items-center space-x-2 bg-white bg-opacity-20 px-3 py-1 rounded-full">
              <span class="text-sm">🛡️</span>
              <span class="font-medium">管理员: {{ authStore.userInfo.username }}</span>
            </div>
            <button 
              @click="loadShortUrls(currentPage)" 
              class="btn-secondary bg-white bg-opacity-20 hover:bg-opacity-30 text-white border-none flex items-center space-x-1"
            >
              <span>🔄</span>
              <span>刷新</span>
            </button>
            <button 
              @click="logout" 
              class="btn-danger bg-red-500 hover:bg-red-600 text-white flex items-center space-x-1"
            >
              <span>🚪</span>
              <span>退出</span>
            </button>
          </div>
        </div>
      </div>
    </header>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- 统计卡片网格 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div class="card bg-gradient-to-br from-blue-500 to-blue-600 text-white transform hover:scale-105 transition-all duration-300 animate-fade-in">
          <div class="p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-blue-100 text-sm font-medium">🔗 总短链数</p>
                <p class="text-3xl font-bold mt-1">{{ stats.totalCount }}</p>
              </div>
              <div class="w-12 h-12 bg-white bg-opacity-20 rounded-lg flex items-center justify-center">
                <span class="text-2xl">🔗</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="card bg-gradient-to-br from-green-500 to-green-600 text-white transform hover:scale-105 transition-all duration-300 animate-fade-in" style="animation-delay: 0.1s">
          <div class="p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-green-100 text-sm font-medium">✅ 启用中</p>
                <p class="text-3xl font-bold mt-1">{{ stats.enabledCount }}</p>
              </div>
              <div class="w-12 h-12 bg-white bg-opacity-20 rounded-lg flex items-center justify-center">
                <span class="text-2xl">✓</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="card bg-gradient-to-br from-yellow-500 to-yellow-600 text-white transform hover:scale-105 transition-all duration-300 animate-fade-in" style="animation-delay: 0.2s">
          <div class="p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-yellow-100 text-sm font-medium">🚫 已禁用</p>
                <p class="text-3xl font-bold mt-1">{{ stats.disabledCount }}</p>
              </div>
              <div class="w-12 h-12 bg-white bg-opacity-20 rounded-lg flex items-center justify-center">
                <span class="text-2xl">✗</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="card bg-gradient-to-br from-purple-500 to-purple-600 text-white transform hover:scale-105 transition-all duration-300 animate-fade-in" style="animation-delay: 0.3s">
          <div class="p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-purple-100 text-sm font-medium">🖱️ 总点击量</p>
                <p class="text-3xl font-bold mt-1">{{ stats.totalClicks }}</p>
              </div>
              <div class="w-12 h-12 bg-white bg-opacity-20 rounded-lg flex items-center justify-center">
                <span class="text-2xl">🖱️</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索和筛选网格 -->
      <div class="card mb-6 animate-slide-up">
        <div class="p-6 border-b border-gray-200">
          <h2 class="text-lg font-semibold text-gray-900 flex items-center">
            <span class="mr-2">🔍</span>
            搜索与筛选
          </h2>
        </div>
        <div class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div>
              <label class="form-label">搜索短链/原链接</label>
              <div class="relative">
                <input 
                  v-model="searchKeyword" 
                  type="text" 
                  placeholder="输入关键词搜索..." 
                  @input="search" 
                  @keyup.enter="search"
                  class="input-field pl-10"
                />
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <span class="text-gray-400">🔍</span>
                </div>
              </div>
            </div>
            
            <div>
              <label class="form-label">状态筛选</label>
              <select 
                v-model="statusFilter" 
                @change="search"
                class="input-field"
              >
                <option value="">全部状态</option>
                <option value="ENABLED">启用</option>
                <option value="DISABLED">禁用</option>
              </select>
            </div>
            
            <div>
              <label class="form-label">排序方式</label>
              <select 
                v-model="sortOrder" 
                @change="search"
                class="input-field"
              >
                <option value="createdAt,desc">按创建时间倒序</option>
                <option value="createdAt,asc">按创建时间正序</option>
                <option value="clickCount,desc">按点击量倒序</option>
                <option value="clickCount,asc">按点击量正序</option>
              </select>
            </div>
            
            <div class="flex items-end">
              <button 
                @click="search" 
                class="btn-primary w-full py-2 flex items-center justify-center"
              >
                <span class="mr-2">🔍</span>
                搜索
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮网格 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6 animate-slide-up">
        <button 
          @click="openCreateModal" 
          class="btn-success w-full flex items-center justify-center space-x-2"
        >
          <span>➕</span>
          <span>新建短链</span>
        </button>
        <button 
          @click="exportData" 
          class="btn-primary w-full flex items-center justify-center space-x-2"
        >
          <span>📥</span>
          <span>导出数据</span>
        </button>
      </div>

      <!-- 短链列表网格 -->
      <div class="card animate-slide-up">
        <div class="p-6 border-b border-gray-200">
          <h2 class="text-lg font-semibold text-gray-900 flex items-center">
            <span class="mr-2">📋</span>
            短链列表
            <span v-if="!loading && shortUrls.length > 0" class="ml-2 text-sm text-gray-500">
              (共 {{ shortUrls.length }} 条记录)
            </span>
          </h2>
        </div>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">短链名称</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">短码</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">原链接</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">点击量</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">创建者</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">创建时间</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-if="loading">
                <td colspan="9" class="px-6 py-12 text-center">
                  <div class="flex flex-col items-center">
                    <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mb-2"></div>
                    <p class="text-gray-600">正在加载数据...</p>
                  </div>
                </td>
              </tr>
              <tr v-else-if="shortUrls.length === 0">
                <td colspan="9" class="px-6 py-12 text-center">
                  <div class="text-gray-500">
                    <div class="text-4xl mb-2">📭</div>
                    <p class="text-lg font-medium">暂无数据</p>
                    <p class="text-sm mt-1">尝试调整搜索条件或创建新的短链</p>
                  </div>
                </td>
              </tr>
              <tr 
                v-for="url in shortUrls" 
                :key="url.id" 
                class="hover:bg-gray-50 transition-colors duration-150"
              >
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ url.id }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ url.name || '-' }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  <div class="flex items-center space-x-2">
                    <code class="px-2 py-1 bg-gray-100 rounded text-xs font-mono">{{ url.shortCode }}</code>
                    <button 
                      @click="copyToClipboard(`${backendOrigin}/${url.shortCode}`)" 
                      class="text-gray-500 hover:text-blue-600 transition-colors"
                      title="复制短链"
                    >
                      <span>📋</span>
                    </button>
                  </div>
                </td>
                <td class="px-6 py-4 max-w-xs">
                  <div 
                    class="text-sm text-gray-900 truncate" 
                    :title="url.originalUrl"
                  >
                    {{ url.originalUrl }}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span 
                    class="badge px-2.5 py-0.5 text-xs font-medium"
                    :class="url.status === 'ENABLED' ? 'badge-success' : 'badge-danger'"
                  >
                    {{ url.status === 'ENABLED' ? '启用' : '禁用' }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ url.clickCount || 0 }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ url.userId ? `用户${url.userId}` : '匿名' }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ formatDate(url.createdAt) }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <div class="flex space-x-1">
                    <button 
                      @click="openEditModal(url)" 
                      class="text-blue-600 hover:text-blue-900 p-1 rounded hover:bg-blue-50 transition-colors"
                      title="编辑"
                    >
                      <span>✏️</span>
                    </button>
                    <button 
                      @click="toggleStatus(url)" 
                      class="p-1 rounded transition-colors"
                      :class="url.status === 'ENABLED' ? 'text-yellow-600 hover:text-yellow-900 hover:bg-yellow-50' : 'text-green-600 hover:text-green-900 hover:bg-green-50'"
                      :title="url.status === 'ENABLED' ? '禁用' : '启用'"
                    >
                      <span>{{ url.status === 'ENABLED' ? '🚫' : '✅' }}</span>
                    </button>
                    <button 
                      @click="deleteShortUrl(url)" 
                      class="text-red-600 hover:text-red-900 p-1 rounded hover:bg-red-50 transition-colors"
                      title="删除"
                    >
                      <span>🗑️</span>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="px-6 py-4 border-t border-gray-200 bg-gray-50">
          <div class="flex justify-center space-x-1">
            <button 
              v-for="page in totalPages" 
              :key="page"
              @click="loadShortUrls(page - 1)"
              class="px-3 py-1 rounded-md text-sm font-medium transition-colors"
              :class="currentPage === page - 1 
                ? 'bg-blue-600 text-white' 
                : 'bg-white text-gray-700 hover:bg-gray-100 border border-gray-300'"
            >
              {{ page }}
            </button>
          </div>
          <div class="text-center text-sm text-gray-500 mt-2">
            第 {{ currentPage + 1 }} 页，共 {{ totalPages }} 页
          </div>
        </div>
      </div>
    </div>

    <!-- 创建模态框 -->
    <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>创建新短链</h3>
          <button @click="showCreateModal = false" class="btn-close">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label>短链名称 *</label>
              <input v-model="createForm.name" type="text" placeholder="例如：官网首页" required />
            </div>
            <div class="form-group">
              <label>短码</label>
              <input v-model="createForm.shortCode" type="text" placeholder="留空则自动生成" />
            </div>
          </div>
          <div class="form-group">
            <label>原链接 *</label>
            <input v-model="createForm.originalUrl" type="url" placeholder="https://example.com" required />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>状态</label>
              <select v-model="createForm.status">
                <option value="ENABLED">启用</option>
                <option value="DISABLED">禁用</option>
              </select>
            </div>
            <div class="form-group">
              <label>过期时间</label>
              <input v-model="createForm.expiresAt" type="datetime-local" />
            </div>
          </div>
          <div class="form-group">
            <label>应用ID</label>
            <input v-model="createForm.appId" type="text" placeholder="可选" />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showCreateModal = false" class="btn btn-secondary">取消</button>
          <button @click="createShortUrl" class="btn btn-success">创建短链</button>
        </div>
      </div>
    </div>

    <!-- 编辑模态框 -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>编辑短链 - {{ editingUrl?.shortCode }}</h3>
          <button @click="showEditModal = false" class="btn-close">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label>短链名称 *</label>
              <input v-model="editForm.name" type="text" required />
            </div>
            <div class="form-group">
              <label>短码 *</label>
              <input v-model="editForm.shortCode" type="text" required />
            </div>
          </div>
          <div class="form-group">
            <label>原链接 *</label>
            <input v-model="editForm.originalUrl" type="url" required />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>状态</label>
              <select v-model="editForm.status">
                <option value="ENABLED">启用</option>
                <option value="DISABLED">禁用</option>
              </select>
            </div>
            <div class="form-group">
              <label>过期时间</label>
              <input v-model="editForm.expiresAt" type="datetime-local" />
            </div>
          </div>
          <div class="form-group">
            <label>应用ID</label>
            <input v-model="editForm.appId" type="text" />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showEditModal = false" class="btn btn-secondary">取消</button>
          <button @click="updateShortUrl" class="btn btn-primary">保存修改</button>
        </div>
      </div>
    </div>
  </div>
</template>

<!-- 样式已通过Tailwind CSS和全局样式处理 -->
