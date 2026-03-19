<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'

interface LoginForm {
  username: string
  password: string
}

const router = useRouter()
const authStore = useAuthStore()

const form = ref<LoginForm>({
  username: '',
  password: ''
})

const loading = ref(false)
const error = ref('')
const success = ref(false)

const isFormValid = ref(false)

function validateForm() {
  isFormValid.value = form.value.username.trim() !== '' && 
                     form.value.password.trim() !== ''
}

async function handleLogin() {
  if (!isFormValid.value) return
  
  loading.value = true
  error.value = ''
  success.value = false
  
  try {
    const response = await authApi.login({
      username: form.value.username.trim(),
      password: form.value.password
    });
    
    // 验证响应格式
    if (!response || typeof response !== 'object' || !response.accessToken) {
      console.error('无效的登录响应:', response);
      throw new Error('服务器返回了无效的响应格式');
    }
    
    authStore.setAuth({
      token: response.accessToken,
      refreshToken: response.refreshToken,
      userInfo: {
        id: 0, // 后端暂未返回用户ID，使用默认值
        username: response.username,
        role: response.role
      }
    })
    
    success.value = true
    setTimeout(() => {
      router.push('/admin')
    }, 1000)
  } catch (err: any) {
    error.value = err.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-background flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <div class="mx-auto h-12 w-12 flex items-center justify-center rounded-full bg-primary">
          <svg class="h-6 w-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
          </svg>
        </div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-text-primary">
          管理员登录
        </h2>
        <p class="mt-2 text-center text-sm text-text-secondary">
          请输入您的管理员账户信息
        </p>
      </div>
      
      <form class="mt-8 space-y-6" @submit.prevent="handleLogin">
        <div v-if="error" class="rounded-md bg-red-50 dark:bg-red-900/20 p-4">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-red-800 dark:text-red-200">
                登录失败
              </h3>
              <div class="mt-2 text-sm text-red-700 dark:text-red-300">
                <p>{{ error }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="success" class="rounded-md bg-green-50 dark:bg-green-900/20 p-4">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3">
              <p class="text-sm font-medium text-green-800 dark:text-green-200">
                登录成功！正在跳转...
              </p>
            </div>
          </div>
        </div>

        <div class="rounded-md shadow-sm -space-y-px">
          <div>
            <label for="username" class="sr-only">用户名</label>
            <input
              id="username"
              v-model="form.username"
              @input="validateForm"
              name="username"
              type="text"
              required
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-border placeholder-text-secondary text-text-primary rounded-t-md focus:outline-none focus:ring-primary focus:border-primary focus:z-10 sm:text-sm bg-background"
              placeholder="用户名"
              :disabled="loading"
            />
          </div>
          <div>
            <label for="password" class="sr-only">密码</label>
            <input
              id="password"
              v-model="form.password"
              @input="validateForm"
              name="password"
              type="password"
              required
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-border placeholder-text-secondary text-text-primary rounded-b-md focus:outline-none focus:ring-primary focus:border-primary focus:z-10 sm:text-sm bg-background"
              placeholder="密码"
              :disabled="loading"
            />
          </div>
        </div>

        <div>
          <button
            type="submit"
            :disabled="!isFormValid || loading"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-primary hover:bg-primary-hover focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <span class="absolute left-0 inset-y-0 flex items-center pl-3">
              <svg v-if="loading" class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <svg v-else class="h-5 w-5 text-white group-hover:text-white" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd" />
              </svg>
            </span>
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </div>
        
        <div class="text-center text-sm text-text-secondary">
          <p>默认管理员账户: admin / 123456</p>
        </div>
      </form>
      
      <div class="text-center">
        <router-link 
          to="/" 
          class="text-primary hover:text-primary-hover font-medium"
        >
          ← 返回首页
        </router-link>
      </div>
    </div>
  </div>
</template>