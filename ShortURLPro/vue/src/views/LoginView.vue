<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')
const success = ref(false)

async function handleLogin() {
  if (!username.value || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  error.value = ''
  success.value = false

  try {
    const response = await authApi.login({
      username: username.value,
      password: password.value
    })
    
    authStore.setAuth(response)
    success.value = true
    error.value = ''
    
    // 延迟跳转以显示成功消息
    setTimeout(() => {
      // 根据角色跳转
      if (response.role === 'ROLE_ADMIN') {
        router.push('/admin')
      } else {
        router.push('/')
      }
    }, 1500)
  } catch (err) {
    error.value = '用户名或密码错误'
    success.value = false
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-gradient-to-br from-blue-50 via-white to-purple-50">
    <div class="max-w-md w-full space-y-8">
      <!-- Logo 和标题 -->
      <div class="text-center animate-fade-in">
        <div class="mx-auto h-16 w-16 bg-gradient-to-r from-blue-600 to-purple-600 rounded-2xl flex items-center justify-center mb-4">
          <span class="text-2xl text-white font-bold">🔗</span>
        </div>
        <h2 class="mt-6 text-3xl font-extrabold text-gray-900">
          管理员登录
        </h2>
        <p class="mt-2 text-sm text-gray-600">
          输入您的凭据访问管理后台
        </p>
      </div>
      
      <!-- 登录表单 -->
      <div class="card animate-slide-up">
        <div class="p-8">
          <form class="space-y-6" @submit.prevent="handleLogin">
            <div>
              <label for="username" class="form-label">用户名</label>
              <div class="mt-1 relative rounded-md shadow-sm">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <span class="text-gray-400">👤</span>
                </div>
                <input
                  id="username"
                  v-model="username"
                  name="username"
                  type="text"
                  required
                  class="input-field pl-10"
                  placeholder="请输入用户名"
                  :disabled="loading"
                />
              </div>
            </div>
            
            <div>
              <label for="password" class="form-label">密码</label>
              <div class="mt-1 relative rounded-md shadow-sm">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <span class="text-gray-400">🔒</span>
                </div>
                <input
                  id="password"
                  v-model="password"
                  name="password"
                  type="password"
                  required
                  class="input-field pl-10"
                  placeholder="请输入密码"
                  :disabled="loading"
                />
              </div>
            </div>
            
            <div>
              <button
                type="submit"
                :disabled="loading"
                class="btn-primary w-full py-3 text-base font-medium flex items-center justify-center group"
              >
                <span v-if="loading" class="mr-2 animate-spin">⏳</span>
                <span v-else class="mr-2 group-hover:scale-110 transition-transform">🔐</span>
                {{ loading ? '登录中...' : '安全登录' }}
              </button>
            </div>
          </form>
          
          <!-- 状态消息 -->
          <div class="mt-6 space-y-3">
            <transition name="fade" mode="out-in">
              <div 
                v-if="error" 
                class="alert alert-error flex items-center"
              >
                <span class="mr-2">❌</span>
                {{ error }}
              </div>
            </transition>
            
            <transition name="fade" mode="out-in">
              <div 
                v-if="success && !loading" 
                class="alert alert-success flex items-center"
              >
                <span class="mr-2">✅</span>
                登录成功，正在跳转...
              </div>
            </transition>
          </div>
          
          <!-- 测试账号信息 -->
          <div class="mt-8 pt-6 border-t border-gray-200">
            <h3 class="text-sm font-medium text-gray-900 mb-3">🔑 测试账号</h3>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between items-center p-2 bg-gray-50 rounded">
                <span class="text-gray-700">普通用户</span>
                <div class="text-right">
                  <div class="font-mono text-gray-900">user</div>
                  <div class="font-mono text-gray-500 text-xs">123456</div>
                </div>
              </div>
              <div class="flex justify-between items-center p-2 bg-blue-50 rounded">
                <span class="text-gray-700">管理员</span>
                <div class="text-right">
                  <div class="font-mono text-gray-900">admin</div>
                  <div class="font-mono text-gray-500 text-xs">123456</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 返回首页 -->
      <div class="text-center">
        <router-link 
          to="/" 
          class="text-sm text-blue-600 hover:text-blue-500 font-medium transition-colors"
        >
          ← 返回首页
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
