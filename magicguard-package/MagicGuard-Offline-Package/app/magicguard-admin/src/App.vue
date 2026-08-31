<template>
  <el-config-provider :locale="zhCn">
    <!-- 登录页面 -->
    <LoginView v-if="!isLoggedIn" @login-success="onLoginSuccess" />

    <!-- 主页面 -->
    <el-layout v-else class="layout">
      <!-- 侧边栏 -->
      <el-aside width="240px" class="aside">
        <div class="logo-container">
          <div class="logo-icon">
            <el-icon :size="32"><Lock /></el-icon>
          </div>
          <div class="logo-text">
            <span class="logo-name">MagicGuard</span>
            <span class="logo-sub">数据安全平台</span>
          </div>
        </div>

        <el-menu
          :default-active="activeMenu"
          class="menu"
          @select="handleMenuSelect"
          :collapse="false"
        >
          <div class="menu-title">安全管理</div>
          <el-menu-item index="keys">
            <el-icon><Key /></el-icon>
            <template #title>密钥管理</template>
          </el-menu-item>
          <el-menu-item index="rules">
            <el-icon><Filter /></el-icon>
            <template #title>脱敏规则</template>
          </el-menu-item>
          <el-menu-item index="datasources">
            <el-icon><Connection /></el-icon>
            <template #title>数据源</template>
          </el-menu-item>
          <el-menu-item index="tasks">
            <el-icon><List /></el-icon>
            <template #title>脱敏任务</template>
          </el-menu-item>

          <div class="menu-title" style="margin-top: 20px;">系统管理</div>
          <el-menu-item index="logs">
            <el-icon><Document /></el-icon>
            <template #title>日志管理</template>
          </el-menu-item>
          <el-menu-item index="users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="ukeys">
            <el-icon><Key /></el-icon>
            <template #title>U-Key管理</template>
          </el-menu-item>
        </el-menu>

        <div class="sidebar-footer">
          <div class="user-info">
            <el-avatar :size="32" :style="{ background: avatarColor }">
              {{ currentUser?.username?.charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="user-detail">
              <span class="username">{{ currentUser?.nickname || currentUser?.username }}</span>
              <span class="role">{{ currentUser?.role === 'ADMIN' ? '管理员' : '用户' }}</span>
            </div>
          </div>
          <div class="footer-bottom">
            <span class="version">v{{ APP_VERSION }}</span>
            <el-button type="text" class="logout-btn" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出
            </el-button>
          </div>
        </div>
      </el-aside>

      <!-- 主内容区 -->
      <el-container class="main-container">
        <!-- 顶部栏 -->
        <el-header class="header">
          <div class="header-left">
            <h2 class="page-title">{{ pageTitle }}</h2>
          </div>
          <div class="header-right">
            <div class="header-time">{{ currentTime }}</div>
            <el-dropdown @command="handleCommand">
              <div class="user-dropdown">
                <el-avatar :size="32" :style="{ background: avatarColor }">
                  {{ currentUser?.username?.charAt(0).toUpperCase() }}
                </el-avatar>
                <span class="dropdown-text">{{ currentUser?.nickname }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon> 个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><Setting /></el-icon> 设置
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon> 退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 页面内容 -->
        <el-main class="main">
          <transition name="fade" mode="out-in">
            <component :is="currentView" :key="activeMenu" />
          </transition>
        </el-main>
      </el-container>
    </el-layout>
  </el-config-provider>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import KeysView from './views/KeysView.vue'
import RulesView from './views/RulesView.vue'
import DatasourcesView from './views/DatasourcesView.vue'
import TasksView from './views/TasksView.vue'
import LogManagementView from './views/LogManagementView.vue'
import UserManagementView from './views/UserManagementView.vue'
import UkeyManagementView from './views/UkeyManagementView.vue'

const APP_VERSION = '5.1.0'
import LoginView from './views/LoginView.vue'

const router = useRouter()
const activeMenu = ref('keys')
const currentTime = ref('')
const isLoggedIn = ref(false)
const currentUser = ref(null)

const currentView = computed(() => {
  const views = {
    keys: KeysView,
    rules: RulesView,
    datasources: DatasourcesView,
    tasks: TasksView,
    logs: LogManagementView,
    users: UserManagementView,
    ukeys: UkeyManagementView
  }
  return views[activeMenu.value] || KeysView
})

const pageTitle = computed(() => {
  const titles = {
    keys: '密钥管理',
    rules: '脱敏规则',
    datasources: '数据源',
    tasks: '脱敏任务',
    logs: '日志管理',
    users: '用户管理',
    ukeys: 'U-Key管理'
  }
  return titles[activeMenu.value] || ''
})

const avatarColor = computed(() => {
  const colors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe', '#00f2fe', '#43e97b', '#fa709a']
  if (!currentUser.value?.username) return colors[0]
  let hash = 0
  for (let i = 0; i < currentUser.value.username.length; i++) {
    hash = currentUser.value.username.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
})

const handleMenuSelect = (index) => {
  activeMenu.value = index
}

const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  }
}

const handleLogout = () => {
  localStorage.removeItem('magicguard_user')
  localStorage.removeItem('magicguard_remember')
  currentUser.value = null
  isLoggedIn.value = false
}

const onLoginSuccess = () => {
  const user = localStorage.getItem('magicguard_user')
  if (user) {
    try {
      currentUser.value = JSON.parse(user)
      isLoggedIn.value = true
    } catch (e) {}
  }
}

const updateTime = () => {
  const now = new Date()
  const options = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }
  currentTime.value = now.toLocaleString('zh-CN', options)
}

const checkLogin = () => {
  const user = localStorage.getItem('magicguard_user')
  if (user) {
    try {
      currentUser.value = JSON.parse(user)
      isLoggedIn.value = true
    } catch (e) {
      isLoggedIn.value = false
    }
  } else {
    isLoggedIn.value = false
  }
}

let timeTimer
onMounted(() => {
  checkLogin()
  updateTime()
  timeTimer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  clearInterval(timeTimer)
})
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f0f2f5;
}

::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

.layout {
  height: 100vh;
  display: flex;
}

/* 侧边栏 */
.aside {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
  z-index: 100;
}

.logo-container {
  display: flex;
  align-items: center;
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 14px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-name {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.5px;
}

.logo-sub {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 2px;
}

/* 菜单 */
.menu {
  flex: 1;
  background: transparent;
  border-right: none;
  padding: 16px 0;
}

.menu-title {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: rgba(255, 255, 255, 0.35);
  padding: 20px 20px 8px;
}

.menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  color: rgba(255, 255, 255, 0.65);
  margin: 4px 12px;
  padding-left: 20px !important;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.menu .el-menu-item.is-active {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.menu .el-menu-item .el-icon {
  font-size: 18px;
  margin-right: 12px;
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.user-detail {
  display: flex;
  flex-direction: column;
  margin-left: 10px;
}

.user-detail .username {
  font-size: 14px;
  font-weight: 500;
  color: #fff;
}

.user-detail .role {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}

.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-bottom .version {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 500;
}

.logout-btn {
  color: rgba(255, 255, 255, 0.65);
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border-radius: 6px;
  transition: all 0.3s;
  font-size: 12px;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

/* 主内容区 */
.main-container {
  flex: 1;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

/* 顶部栏 */
.header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  height: 70px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-time {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.user-dropdown:hover {
  background: #f5f7fa;
}

.dropdown-text {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

/* 主内容 */
.main {
  flex: 1;
  padding: 24px 28px;
  overflow-y: auto;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
