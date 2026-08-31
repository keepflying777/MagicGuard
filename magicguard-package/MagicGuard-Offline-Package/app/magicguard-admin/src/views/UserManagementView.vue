<template>
  <div class="users-view">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-primary">
        <div class="stat-icon">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ users.length }}</div>
          <div class="stat-label">用户总数</div>
        </div>
      </div>
      <div class="stat-card stat-success">
        <div class="stat-icon">
          <el-icon :size="28"><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ activeCount }}</div>
          <div class="stat-label">活跃用户</div>
        </div>
      </div>
      <div class="stat-card stat-info">
        <div class="stat-icon">
          <el-icon :size="28"><Avatar /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ adminCount }}</div>
          <div class="stat-label">管理员</div>
        </div>
      </div>
      <div class="stat-card stat-warning">
        <div class="stat-icon">
          <el-icon :size="28"><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ todayLoginCount }}</div>
          <div class="stat-label">今日登录</div>
        </div>
      </div>
    </div>

    <!-- 主内容卡片 -->
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">用户管理</span>
            <el-tag type="info" size="small">共 {{ users.length }} 人</el-tag>
          </div>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon> 添加用户
          </el-button>
        </div>
      </template>

      <el-table
        :data="users"
        stripe
        class="data-table"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="150">
          <template #default="{ row }">
            <div class="user-name-cell">
              <el-avatar :size="32" :style="{ background: getAvatarColor(row.username) }">
                {{ row.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <span>{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180">
          <template #default="{ row }">
            <span class="email-text">{{ row.email || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">
            <span>{{ row.phone || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" type="primary" plain @click="editUser(row)">编辑</el-button>
              <el-button size="small" type="warning" plain @click="resetPassword(row)">重置密码</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '添加用户'"
      width="520px"
      class="custom-dialog"
    >
      <el-form :model="userForm" label-width="80px" class="custom-form">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="userForm.status"
            active-value="ACTIVE"
            inactive-value="DISABLED"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">确认</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="resetDialogVisible" title="重置密码" width="400px" class="custom-dialog">
      <el-form label-width="80px">
        <el-form-item label="新密码">
          <el-input v-model="newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="confirmPassword" type="password" placeholder="请确认新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResetPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

// 模拟用户数据（实际应从API获取）
const users = ref([
  { id: 1, username: 'admin', nickname: '系统管理员', email: 'admin@example.com', phone: '13800138000', role: 'ADMIN', status: 'ACTIVE', lastLoginTime: '2026-08-04 12:30:00' },
  { id: 2, username: 'operator', nickname: '操作员', email: 'operator@example.com', phone: '13800138001', role: 'USER', status: 'ACTIVE', lastLoginTime: '2026-08-04 10:15:00' },
  { id: 3, username: 'auditor', nickname: '审计员', email: 'auditor@example.com', phone: '13800138002', role: 'USER', status: 'DISABLED', lastLoginTime: '2026-08-02 09:00:00' }
])

const loading = ref(false)
const dialogVisible = ref(false)
const resetDialogVisible = ref(false)
const isEdit = ref(false)
const currentUserId = ref(null)
const newPassword = ref('')
const confirmPassword = ref('')

const userForm = ref({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  role: 'USER',
  status: 'ACTIVE'
})

const activeCount = computed(() => users.value.filter(u => u.status === 'ACTIVE').length)
const adminCount = computed(() => users.value.filter(u => u.role === 'ADMIN').length)
const todayLoginCount = computed(() => {
  const today = new Date().toDateString()
  return users.value.filter(u => u.lastLoginTime && new Date(u.lastLoginTime).toDateString() === today).length
})

const getAvatarColor = (name) => {
  const colors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe', '#00f2fe', '#43e97b', '#fa709a']
  let hash = 0
  for (let i = 0; i < name?.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

const showCreateDialog = () => {
  isEdit.value = false
  userForm.value = {
    username: '',
    nickname: '',
    email: '',
    phone: '',
    role: 'USER',
    status: 'ACTIVE'
  }
  dialogVisible.value = true
}

const editUser = (user) => {
  isEdit.value = true
  currentUserId.value = user.id
  userForm.value = { ...user }
  dialogVisible.value = true
}

const saveUser = () => {
  if (!userForm.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (isEdit.value) {
    const index = users.value.findIndex(u => u.id === currentUserId.value)
    if (index !== -1) {
      users.value[index] = { ...users.value[index], ...userForm.value }
    }
    ElMessage.success('用户更新成功')
  } else {
    users.value.push({
      id: users.value.length + 1,
      ...userForm.value,
      lastLoginTime: '-'
    })
    ElMessage.success('用户添加成功')
  }
  dialogVisible.value = false
}

const resetPassword = (user) => {
  currentUserId.value = user.id
  newPassword.value = ''
  confirmPassword.value = ''
  resetDialogVisible.value = true
}

const confirmResetPassword = () => {
  if (!newPassword.value) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    ElMessage.error('两次密码输入不一致')
    return
  }
  ElMessage.success('密码重置成功')
  resetDialogVisible.value = false
}
</script>

<style scoped>
.users-view { width: 100%; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.stat-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1); }

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 18px;
  color: #fff;
}

.stat-primary .stat-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-success .stat-icon { background: linear-gradient(135deg, #52c41a 0%, #237804 100%); }
.stat-warning .stat-icon { background: linear-gradient(135deg, #faad14 0%, #d48806 100%); }
.stat-info .stat-icon { background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%); }

.stat-content { display: flex; flex-direction: column; }
.stat-value { font-size: 28px; font-weight: 700; color: #1a1a2e; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }

.main-card {
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: none;
}

.main-card :deep(.el-card__header) { padding: 20px 24px; border-bottom: 1px solid #f0f2f5; }
.main-card :deep(.el-card__body) { padding: 0; }

.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 12px; }
.title { font-size: 16px; font-weight: 600; color: #1a1a2e; }

.data-table :deep(.el-table__row:hover > td) { background: #f5f7fa !important; }

.user-name-cell { display: flex; align-items: center; gap: 10px; font-weight: 500; }
.email-text { color: #909399; font-size: 13px; }

.action-buttons { display: flex; gap: 8px; }

.custom-form :deep(.el-form-item__label) { font-weight: 500; }
</style>
