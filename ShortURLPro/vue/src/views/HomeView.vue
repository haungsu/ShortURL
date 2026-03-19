<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { shortUrlApi } from '@/api/shorturl'

interface GenerateForm {
  originalUrl: string
}

const router = useRouter()
const authStore = useAuthStore()

const form = ref<GenerateForm>({
  originalUrl: ''
})

const loading = ref(false)
const showResult = ref(false)
const generatedUrl = ref('')
const shortCode = ref('')

const isFormValid = computed(() => {
  return form.value.originalUrl.trim() !== '' && 
         isValidUrl(form.value.originalUrl.trim())
})

function isValidUrl(string: string): boolean {
  try {
    new URL(string)
    return true
  } catch (_) {
    return false
  }
}

async function generateShortUrl() {
  if (!isFormValid.value) return
  
  loading.value = true
  try {
    console.log('发送生成请求:', form.value.originalUrl.trim());
    const response = await shortUrlApi.generate({
      originalUrl: form.value.originalUrl.trim()
    })
    
    console.log('API响应:', response);
    
    // 检查响应是否存在
    if (!response) {
      throw new Error('API无响应');
    }
    
    // 检查必需字段
    if (!response.shortUrl) {
      throw new Error('响应缺少shortUrl字段');
    }
    if (!response.shortCode) {
      throw new Error('响应缺少shortCode字段');
    }
    
    generatedUrl.value = response.shortUrl
    shortCode.value = response.shortCode
    showResult.value = true
    
    // 清空表单
    form.value.originalUrl = ''
  } catch (error: any) {
    console.error('生成短链接失败:', error);
    alert(`生成失败: ${error.message || '未知错误'}`)
  } finally {
    loading.value = false
  }
}

function copyToClipboard() {
  navigator.clipboard.writeText(generatedUrl.value)
    .then(() => {
      alert('已复制到剪贴板!')
    })
    .catch(() => {
      // 降级方案
      const textArea = document.createElement('textarea')
      textArea.value = generatedUrl.value
      document.body.appendChild(textArea)
      textArea.select()
      document.execCommand('copy')
      document.body.removeChild(textArea)
      alert('已复制到剪贴板!')
    })
}

function handleLogin() {
  router.push('/login')
}

function handleLogout() {
  authStore.clearAuth()
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
    <!-- Hero Section -->
    <div class="text-center py-12">
      <h1 class="text-4xl md:text-5xl font-bold text-text-primary mb-4">
        <span class="text-gradient">ShortURL Pro</span>
      </h1>
      <p class="text-xl text-text-secondary mb-8 max-w-2xl mx-auto">
        专业的短链接生成和管理系统，让长链接变得简洁易记，
        同时提供强大的统计分析功能
      </p>
      
      <!-- 认证状态显示 -->
      <div v-if="authStore.isLoggedIn" class="mb-8 p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
        <p class="text-green-800 dark:text-green-200">
          您已登录为 {{ authStore.userInfo?.username }}
          <span v-if="authStore.isAdmin" class="ml-2 badge badge-success">管理员</span>
          <button 
            @click="handleLogout"
            class="ml-4 text-sm text-green-600 hover:text-green-800 dark:text-green-300 dark:hover:text-green-100"
          >
            退出登录
          </button>
          <router-link 
            v-if="authStore.isAdmin"
            to="/admin"
            class="ml-4 text-sm text-green-600 hover:text-green-800 dark:text-green-300 dark:hover:text-green-100 underline"
          >
            进入管理后台
          </router-link>
        </p>
      </div>
      <div v-else class="mb-8">
        <button 
          @click="handleLogin"
          class="btn btn-primary"
        >
          管理员登录
        </button>
      </div>
    </div>

    <!-- 生成器卡片 -->
    <div class="card max-w-2xl mx-auto shadow-card">
      <div class="text-center mb-6">
        <h2 class="text-2xl font-semibold text-text-primary mb-2">生成短链接</h2>
        <p class="text-text-secondary">输入长链接，立即获得简洁的短链接</p>
      </div>
      
      <form @submit.prevent="generateShortUrl" class="space-y-4">
        <div>
          <label for="originalUrl" class="block text-sm font-medium text-text-primary mb-2">
            原始链接
          </label>
          <input
            id="originalUrl"
            v-model="form.originalUrl"
            type="url"
            placeholder="https://example.com/very-long-url-that-needs-shortening"
            class="input"
            :disabled="loading"
          />
        </div>
        
        <button
          type="submit"
          :disabled="!isFormValid || loading"
          class="w-full btn btn-primary"
          :class="{ 'opacity-50 cursor-not-allowed': !isFormValid || loading }"
        >
          <span v-if="loading">生成中...</span>
          <span v-else>生成短链接</span>
        </button>
      </form>

      <!-- 结果展示 -->
      <div v-if="showResult" class="mt-6 p-4 bg-green-50 dark:bg-green-900/20 rounded-lg border border-green-200 dark:border-green-800">
        <div class="flex items-center justify-between">
          <div class="flex-1 min-w-0">
            <p class="text-sm text-green-800 dark:text-green-200 mb-1">生成成功!</p>
            <p class="text-green-700 dark:text-green-300 font-mono truncate">
              {{ generatedUrl }}
            </p>
            <p class="text-xs text-green-600 dark:text-green-400 mt-1">
              短码: {{ shortCode }}
            </p>
          </div>
          <button
            @click="copyToClipboard"
            class="ml-4 px-3 py-1 bg-green-600 hover:bg-green-700 text-white rounded-md text-sm transition-colors"
          >
            复制
          </button>
        </div>
      </div>
    </div>

    <!-- 功能特色 -->
    <div class="mt-16 grid grid-cols-1 md:grid-cols-3 gap-8">
      <div class="text-center">
        <div class="w-12 h-12 bg-primary rounded-lg flex items-center justify-center mx-auto mb-4">
          <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"></path>
          </svg>
        </div>
        <h3 class="text-lg font-semibold text-text-primary mb-2">简洁高效</h3>
        <p class="text-text-secondary">
          将冗长复杂的URL转换为简洁易记的短链接
        </p>
      </div>
      
      <div class="text-center">
        <div class="w-12 h-12 bg-success rounded-lg flex items-center justify-center mx-auto mb-4">
          <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
          </svg>
        </div>
        <h3 class="text-lg font-semibold text-text-primary mb-2">数据统计</h3>
        <p class="text-text-secondary">
          实时追踪点击量、访问来源和地理位置等详细数据
        </p>
      </div>
      
      <div class="text-center">
        <div class="w-12 h-12 bg-warning rounded-lg flex items-center justify-center mx-auto mb-4">
          <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
          </svg>
        </div>
        <h3 class="text-lg font-semibold text-text-primary mb-2">安全可靠</h3>
        <p class="text-text-secondary">
          完善的安全机制和权限控制，保障数据安全
        </p>
      </div>
    </div>
  </div>
</template>