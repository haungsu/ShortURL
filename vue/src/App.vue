<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const isLoggedIn = computed(() => authStore.isLoggedIn)
const isAdmin = computed(() => authStore.isAdmin)
const userInfo = computed(() => authStore.userInfo)

const handleLogout = () => {
  authStore.clearAuth()
  router.push('/')
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <!-- 导航栏 -->
    <nav class="bg-white dark:bg-surface border-b border-border sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <div class="flex-shrink-0 flex items-center">
              <span class="text-2xl font-bold text-gradient">ShortURL Pro</span>
            </div>
            <div class="hidden sm:ml-6 sm:flex sm:space-x-8">
              <router-link 
                to="/" 
                class="border-transparent text-text-secondary hover:text-text-primary hover:border-primary inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium transition-colors"
                active-class="border-primary text-primary"
              >
                首页
              </router-link>
              <router-link 
                v-if="isAdmin" 
                to="/admin" 
                class="border-transparent text-text-secondary hover:text-text-primary hover:border-primary inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium transition-colors"
                active-class="border-primary text-primary"
              >
                管理后台
              </router-link>
            </div>
          </div>
          
          <div class="flex items-center">
            <div v-if="isLoggedIn" class="flex items-center space-x-4">
              <span class="text-sm text-text-secondary">
                欢迎，{{ userInfo?.username }}
                <span v-if="isAdmin" class="ml-1 badge badge-success">管理员</span>
              </span>
              <button 
                @click="handleLogout"
                class="btn btn-secondary text-sm"
              >
                退出登录
              </button>
            </div>
            <div v-else class="flex items-center space-x-4">
              <router-link 
                to="/login" 
                class="btn btn-primary text-sm"
              >
                管理员登录
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <!-- 主要内容区域 -->
    <main class="py-8">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="bg-surface border-t border-border mt-12">
      <div class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
        <div class="text-center text-text-secondary text-sm">
          <p>&copy; {{ new Date().getFullYear() }} ShortURL Pro. 使用 Vue 3 + TypeScript + TailwindCSS 构建</p>
          <div class="mt-2 flex justify-center space-x-4">
            <span>🚀 高性能</span>
            <span>🔒 安全可靠</span>
            <span>📊 数据驱动</span>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>