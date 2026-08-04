<template>
  <el-config-provider :locale="zhCn">
    <el-layout class="layout">
      <el-aside width="200px" class="aside">
        <div class="logo">MagicGuard</div>
        <el-menu
          :default-active="activeMenu"
          class="menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="keys">
            <el-icon><Key /></el-icon>
            <span>密钥管理</span>
          </el-menu-item>
          <el-menu-item index="rules">
            <el-icon><Filter /></el-icon>
            <span>脱敏规则</span>
          </el-menu-item>
          <el-menu-item index="datasources">
            <el-icon><Connection /></el-icon>
            <span>数据源</span>
          </el-menu-item>
          <el-menu-item index="tasks">
            <el-icon><List /></el-icon>
            <span>脱敏任务</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="header-title">{{ pageTitle }}</div>
        </el-header>
        <el-main class="main">
          <KeysView v-if="activeMenu === 'keys'" />
          <RulesView v-if="activeMenu === 'rules'" />
          <DatasourcesView v-if="activeMenu === 'datasources'" />
          <TasksView v-if="activeMenu === 'tasks'" />
        </el-main>
      </el-container>
    </el-layout>
  </el-config-provider>
</template>

<script setup>
import { ref, computed } from 'vue'
import KeysView from './views/KeysView.vue'
import RulesView from './views/RulesView.vue'
import DatasourcesView from './views/DatasourcesView.vue'
import TasksView from './views/TasksView.vue'

const activeMenu = ref('keys')

const pageTitle = computed(() => {
  const titles = {
    keys: '密钥管理',
    rules: '脱敏规则',
    datasources: '数据源',
    tasks: '脱敏任务'
  }
  return titles[activeMenu.value] || ''
})

const handleMenuSelect = (index) => {
  activeMenu.value = index
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.layout {
  height: 100vh;
}

.aside {
  background-color: #001529;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #002140;
}

.menu {
  border-right: none;
  background-color: #001529;
}

.menu .el-menu-item {
  color: rgba(255, 255, 255, 0.7);
}

.menu .el-menu-item:hover,
.menu .el-menu-item.is-active {
  background-color: #1890ff;
  color: #fff;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
