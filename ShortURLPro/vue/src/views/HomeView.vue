<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { shortUrlApi } from '@/api/shorturl'
import { authApi } from '@/api/auth'

const authStore = useAuthStore()
const originalUrl = ref('')
const shortUrl = ref('')
const shortCode = ref('')
const loading = ref(false)
const showResult = ref(false)

// 登录表单相关
const loginUsername = ref('')
const loginPassword = ref('')
const loginLoading = ref(false)

async function generateShortUrl() {
  if (!originalUrl.value) return
  
  loading.value = true
  try {
    const response = await shortUrlApi.generate({ originalUrl: originalUrl.value })
    shortUrl.value = response.shortUrl
    shortCode.value = response.shortCode
    showResult.value = true
    
    // 添加成功动画效果
    setTimeout(() => {
      const resultEl = document.querySelector('.result-section')
      if (resultEl) {
        resultEl.classList.add('animate-fade-in')
      }
    }, 100)
  } catch (error) {
    alert('生成短链失败，请重试')
  } finally {
    loading.value = false
  }
}

function copyToClipboard() {
  navigator.clipboard.writeText(shortUrl.value).then(() => {
    alert('已复制到剪贴板')
  }).catch(() => {
    const textArea = document.createElement('textarea')
    textArea.value = shortUrl.value
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    alert('已复制到剪贴板')
  })
}

// 登录相关函数
async function handleLogin() {
  if (!loginUsername.value || !loginPassword.value) return
  
  loginLoading.value = true
  try {
    const response = await authApi.login({
      username: loginUsername.value,
      password: loginPassword.value
    })
    
    authStore.setAuth(response)
    loginUsername.value = ''
    loginPassword.value = ''
    alert('登录成功！')
  } catch (error) {
    alert('用户名或密码错误！')
  } finally {
    loginLoading.value = false
  }
}

function handleLogout() {
  authStore.clearAuth()
  alert('退出登录成功！')
}

// 页面加载时检查登录状态
onMounted(() => {
  // 如果已有token，验证其有效性
  if (authStore.token) {
    authApi.validate().catch(() => {
      authStore.clearAuth()
    })
  }
})
</script>

<template>
  <div class="min-h-screen p-4">
    <div class="w-full max-w-7xl mx-auto">
      <!-- 网格布局主区域 -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        <!-- 左侧：短链生成器卡片 -->
        <div class="card col-span-1 lg:col-span-2 animate-fade-in-up">
          <div class="p-8">
            <div class="text-center mb-8">
              <div class="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-blue-500 to-cyan-500 rounded-2xl text-white text-2xl mb-4 shadow-lg">
                🔗
              </div>
              <h1 class="text-2xl font-bold text-gray-900 mb-2">短链接生成器</h1>
              <p class="text-gray-600">快速将长网址转换为简洁的短链接</p>
            </div>
            
            <form @submit.prevent="generateShortUrl" class="space-y-5">
              <div class="form-group">
                <label class="form-label form-label-required">输入长链接</label>
                <div class="input-group">
                  <div class="input-group-addon">
                    🔗
                  </div>
                  <input
                    v-model="originalUrl"
                    type="url"
                    required
                    placeholder="https://example.com/very-long-url"
                    class="input-field"
                    :disabled="loading"
                  />
                </div>
              </div>
              
              <button 
                type="submit" 
                class="btn btn-primary btn-lg w-full flex items-center justify-center"
                :disabled="loading"
              >
                <span v-if="loading" class="mr-2 animate-spin">⏳</span>
                <span v-else class="mr-2">⚡</span>
                {{ loading ? '生成中...' : '生成短链接' }}
              </button>
            </form>
            
            <!-- 结果展示 -->
            <div v-if="showResult" class="mt-6 p-5 bg-gradient-to-r from-green-50 to-emerald-50 rounded-xl border border-green-200 animate-fade-in">
              <div class="flex items-center mb-4">
                <div class="mr-3 p-2 bg-green-100 rounded-lg text-green-600">
                  <span class="text-lg">✅</span>
                </div>
                <h3 class="text-lg font-semibold text-gray-900">生成成功！</h3>
              </div>
              
              <div class="space-y-4">
                <div>
                  <label class="form-label font-medium">短链接</label>
                  <div class="flex gap-2">
                    <input 
                      type="text" 
                      :value="shortUrl" 
                      readonly 
                      class="input-field flex-1 bg-gray-50 cursor-not-allowed text-sm"
                    />
                    <button 
                      @click="copyToClipboard" 
                      class="btn btn-secondary whitespace-nowrap"
                    >
                      <span class="mr-1">📋</span>
                      复制
                    </button>
                  </div>
                </div>
                
                <div>
                  <label class="form-label font-medium">短码</label>
                  <div class="inline-flex items-center px-3 py-1.5 bg-white rounded-lg border border-gray-300 font-mono text-sm font-semibold">
                    {{ shortCode }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧：登录区域卡片 -->
        <div class="card col-span-1 animate-fade-in-up" style="animation-delay: 0.1s">
          <div class="p-6">
            <div class="text-center mb-6">
              <div class="inline-flex items-center justify-center w-12 h-12 bg-gradient-to-r from-blue-500 to-teal-500 rounded-xl text-white text-lg mb-3">
                👤
              </div>
              <h2 class="text-xl font-semibold text-gray-900">用户登录</h2>
              <p class="text-gray-600 text-sm mt-1">登录后可享受更多功能</p>
            </div>
            
            <!-- 登录状态 -->
            <div class="mb-6">
              <div 
                class="rounded-xl p-4 transition-all duration-300"
                :class="authStore.isLoggedIn ? (authStore.isAdmin ? 'bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200' : 'bg-gradient-to-r from-green-50 to-emerald-50 border border-green-200') : 'bg-gray-100 border border-gray-200'"
              >
                <div class="flex items-center justify-between">
                  <div>
                    <div v-if="authStore.isLoggedIn" class="space-y-1">
                      <div class="flex items-center text-sm">
                        <span class="font-medium text-gray-700 mr-2">用户:</span>
                        <span class="text-gray-900 font-semibold">{{ authStore.userInfo.username }}</span>
                      </div>
                      <div class="flex items-center text-sm">
                        <span class="font-medium text-gray-700 mr-2">角色:</span>
                        <span class="badge text-xs" :class="authStore.isAdmin ? 'badge-success' : 'badge-warning'">
                          {{ authStore.displayRole }}
                        </span>
                      </div>
                    </div>
                    <div v-else class="text-gray-600 text-sm flex items-center">
                      <span class="mr-2">⚠️</span>
                      未登录
                    </div>
                  </div>
                  <div v-if="authStore.isLoggedIn" class="flex items-center space-x-2">
                    <div class="w-3 h-3 bg-green-500 rounded-full animate-pulse"></div>
                    <button 
                      @click="handleLogout"
                      class="btn btn-text text-red-600 hover:text-red-700 text-sm py-1 px-2"
                    >
                      退出
                    </button>
                  </div>
                </div>
              </div>
              
              <div v-if="authStore.isAdmin" class="mt-3">
                <router-link 
                  to="/admin" 
                  class="btn btn-primary w-full flex items-center justify-center text-sm"
                >
                  <span class="mr-1">👑</span>
                  管理后台
                </router-link>
              </div>
            </div>
            
            <!-- 登录表单 -->
            <form @submit.prevent="handleLogin" class="space-y-4">
              <div class="form-group mb-0">
                <label class="form-label text-sm">用户名</label>
                <input 
                  v-model="loginUsername" 
                  type="text" 
                  class="input-field text-sm"
                  placeholder="请输入用户名"
                  required
                  :disabled="loginLoading || authStore.isLoggedIn"
                />
              </div>
              
              <div class="form-group mb-0">
                <label class="form-label text-sm">密码</label>
                <input 
                  v-model="loginPassword" 
                  type="password" 
                  class="input-field text-sm"
                  placeholder="请输入密码"
                  required
                  :disabled="loginLoading || authStore.isLoggedIn"
                />
              </div>
              
              <button 
                v-if="!authStore.isLoggedIn"
                type="submit" 
                class="btn btn-secondary w-full text-sm"
                :disabled="loginLoading"
              >
                <span v-if="loginLoading" class="mr-2 animate-spin">⏳</span>
                <span v-else class="mr-2">🔐</span>
                {{ loginLoading ? '登录中...' : '登录' }}
              </button>
            </form>
            
            <!-- 测试账号 -->
            <div class="mt-6 pt-5 border-t border-gray-200">
              <h4 class="font-medium text-gray-900 mb-3 text-sm flex items-center">
                <span class="mr-2">🔑</span>
                测试账号
              </h4>
              <div class="space-y-2">
                <div class="flex items-center justify-between p-2 bg-white rounded-lg border text-xs">
                  <div class="flex items-center">
                    <div class="w-2 h-2 bg-blue-500 rounded-full mr-2"></div>
                    <span class="text-gray-700">普通用户</span>
                  </div>
                  <div class="text-right">
                    <div class="font-mono text-gray-900 font-medium">user</div>
                    <div class="font-mono text-gray-500">123456</div>
                  </div>
                </div>
                <div class="flex items-center justify-between p-2 bg-white rounded-lg border border-blue-200 text-xs">
                  <div class="flex items-center">
                    <div class="w-2 h-2 bg-purple-500 rounded-full mr-2"></div>
                    <span class="text-gray-700">管理员</span>
                  </div>
                  <div class="text-right">
                    <div class="font-mono text-gray-900 font-medium">admin</div>
                    <div class="font-mono text-gray-500">123456</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 功能介绍网格 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="card text-center p-6 animate-fade-in-up" style="animation-delay: 0.2s">
          <div class="text-3xl mb-3">🚀</div>
          <h3 class="font-semibold text-gray-800 mb-2">快速生成</h3>
          <p class="text-gray-700 text-sm">一键转换长链接</p>
        </div>
        <div class="card text-center p-6 animate-fade-in-up" style="animation-delay: 0.3s">
          <div class="text-3xl mb-3">📊</div>
          <h3 class="font-semibold text-gray-800 mb-2">数据统计</h3>
          <p class="text-gray-700 text-sm">实时访问监控</p>
        </div>
        <div class="card text-center p-6 animate-fade-in-up" style="animation-delay: 0.4s">
          <div class="text-3xl mb-3">🔒</div>
          <h3 class="font-semibold text-gray-800 mb-2">安全管理</h3>
          <p class="text-gray-700 text-sm">权限控制保护</p>
        </div>
      </div>
    </div>
  </div>
</template>

<!-- 样式已通过Tailwind CSS和全局样式处理 -->
