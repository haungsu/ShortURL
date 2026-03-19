<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const isLoggedIn = computed(() => authStore.isLoggedIn)
const isAdmin = computed(() => authStore.isAdmin)
const userInfo = computed(() => authStore.userInfo)

function handleLogout() {
  authStore.clearAuth()
  router.push('/')
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
    <!-- 顶部导航栏 -->
    <nav class="bg-white bg-opacity-90 backdrop-blur-md shadow-sm border-b border-gray-100 sticky top-0 z-50">
      <div class="container">
        <div class="flex justify-between items-center h-14">
          <div class="flex items-center space-x-3">
            <div class="flex-shrink-0 flex items-center">
              <div class="w-8 h-8 bg-gradient-to-r from-purple-600 to-indigo-600 rounded-lg flex items-center justify-center shadow-sm">
                <span class="text-lg font-bold text-white">🔗</span>
              </div>
              <span class="ml-2 text-lg font-bold bg-gradient-to-r from-purple-600 to-indigo-600 bg-clip-text text-transparent">ShortURL</span>
            </div>
          </div>
          
          <div class="flex items-center space-x-3">
            <div v-if="isLoggedIn" class="hidden sm:flex items-center space-x-2 bg-gray-100 px-3 py-1 rounded-full">
              <div class="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
              <span class="text-sm text-gray-700">{{ userInfo.username }}</span>
              <span v-if="isAdmin" class="badge badge-success text-xs px-2 py-0.5">ADMIN</span>
            </div>
            <button 
              v-if="isLoggedIn"
              @click="handleLogout"
              class="btn btn-text text-gray-600 hover:text-red-600 text-sm py-1 px-2"
            >
              退出
            </button>
            <router-link 
              v-else
              to="/login" 
              class="btn btn-outline btn-sm"
            >
              登录
            </router-link>
          </div>
        </div>
      </div>
    </nav>
    
    <!-- 主要内容区域 -->
    <main class="py-8">
      <div class="container">
        <RouterView />
      </div>
    </main>
    
    <!-- 页脚 -->
    <footer class="bg-white border-t border-gray-200 mt-16 py-8">
      <div class="container">
        <div class="text-center">
          <div class="flex items-center justify-center space-x-2 mb-2">
            <span class="text-2xl">🔗</span>
            <span class="text-xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">ShortURL Pro</span>
          </div>
          <p class="text-gray-600 text-sm mb-4">现代化短链接服务平台</p>
          <div class="flex flex-wrap justify-center gap-4 text-xs text-gray-500">
            <span>© 2026</span>
            <span>•</span>
            <span>构建于Vue 3</span>
            <span>•</span>
            <span>Spring Boot后端</span>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<style>
/* 全局样式已移至 assets/styles.css */
</style>
