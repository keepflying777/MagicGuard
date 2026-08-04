<template>
  <div class="ukey-view">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-primary">
        <div class="stat-icon">
          <el-icon :size="28"><Key /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ ukeys.length }}</div>
          <div class="stat-label">U-Key 总数</div>
        </div>
      </div>
      <div class="stat-card stat-success">
        <div class="stat-icon">
          <el-icon :size="28"><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ activeCount }}</div>
          <div class="stat-label">已绑定</div>
        </div>
      </div>
      <div class="stat-card stat-info">
        <div class="stat-icon">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ boundUserCount }}</div>
          <div class="stat-label">使用用户</div>
        </div>
      </div>
      <div class="stat-card stat-warning">
        <div class="stat-icon">
          <el-icon :size="28"><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ expiringSoon }}</div>
          <div class="stat-label">即将过期</div>
        </div>
      </div>
    </div>

    <!-- 主内容卡片 -->
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">U-Key 管理</span>
            <el-tag type="info" size="small">共 {{ ukeys.length }} 个</el-tag>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="showBindDialog">
              <el-icon><Plus /></el-icon> 绑定 U-Key
            </el-button>
            <el-button plain @click="detectUkey">
              <el-icon><RefreshRight /></el-icon> 检测设备
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="ukeys"
        stripe
        class="data-table"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="serialNumber" label="序列号" min-width="150">
          <template #default="{ row }">
            <div class="serial-cell">
              <el-icon><Key /></el-icon>
              <span>{{ row.serialNumber }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="model" label="型号" min-width="120" />
        <el-table-column prop="bindUser" label="绑定用户" width="120">
          <template #default="{ row }">
            <span v-if="row.bindUser">{{ row.bindUser }}</span>
            <el-tag v-else type="info" size="small">未绑定</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : row.status === 'DISABLED' ? 'danger' : 'warning'" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expireDate" label="有效期" width="120" />
        <el-table-column prop="lastUsed" label="最后使用" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" type="primary" plain @click="viewDetail(row)">详情</el-button>
              <el-button size="small" type="warning" plain @click="unbind(row)" :disabled="!row.bindUser">解绑</el-button>
              <el-button size="small" type="danger" plain @click="disable(row)">禁用</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 绑定 U-Key 对话框 -->
    <el-dialog v-model="bindDialogVisible" title="绑定 U-Key" width="500px" class="custom-dialog">
      <el-form :model="bindForm" label-width="100px" class="custom-form">
        <el-form-item label="选择用户">
          <el-select v-model="bindForm.userId" placeholder="请选择要绑定的用户" style="width: 100%">
            <el-option
              v-for="user in availableUsers"
              :key="user.id"
              :label="user.username + ' (' + user.nickname + ')'"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="U-Key 序列号">
          <el-input v-model="bindForm.serialNumber" placeholder="请输入或扫描 U-Key 序列号" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="bindForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBind">确认绑定</el-button>
      </template>
    </el-dialog>

    <!-- U-Key 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="U-Key 详情" width="550px" class="custom-dialog">
      <el-descriptions :column="2" border v-if="currentUkey" class="ukey-details">
        <el-descriptions-item label="序列号" :span="2">
          <span class="serial-highlight">{{ currentUkey.serialNumber }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="型号">{{ currentUkey.model }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentUkey.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
            {{ getStatusText(currentUkey.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="绑定用户">{{ currentUkey.bindUser || '-' }}</el-descriptions-item>
        <el-descriptions-item label="绑定时间">{{ currentUkey.bindTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="有效期">{{ currentUkey.expireDate }}</el-descriptions-item>
        <el-descriptions-item label="最后使用">{{ currentUkey.lastUsed || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentUkey.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentUkey.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const ukeys = ref([
  { id: 1, serialNumber: 'UK12345678', model: 'MagicGuard USB Key V1', bindUser: 'admin', status: 'ACTIVE', bindTime: '2026-01-15 10:30:00', expireDate: '2027-01-15', lastUsed: '2026-08-04 12:30:00', createTime: '2026-01-10 09:00:00', remark: '管理员使用' },
  { id: 2, serialNumber: 'UK87654321', model: 'MagicGuard USB Key V1', bindUser: 'operator', status: 'ACTIVE', bindTime: '2026-03-20 14:20:00', expireDate: '2027-03-20', lastUsed: '2026-08-03 16:45:00', createTime: '2026-03-18 11:00:00', remark: '' },
  { id: 3, serialNumber: 'UK11223344', model: 'MagicGuard USB Key V2', bindUser: null, status: 'UNBIND', bindTime: null, expireDate: '2028-06-01', lastUsed: null, createTime: '2026-06-01 08:00:00', remark: '备用 Key' }
])

const users = ref([
  { id: 1, username: 'admin', nickname: '系统管理员' },
  { id: 2, username: 'operator', nickname: '操作员' },
  { id: 3, username: 'auditor', nickname: '审计员' }
])

const loading = ref(false)
const bindDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentUkey = ref(null)

const bindForm = ref({
  userId: null,
  serialNumber: '',
  remark: ''
})

const activeCount = computed(() => ukeys.value.filter(u => u.status === 'ACTIVE').length)
const boundUserCount = computed(() => new Set(ukeys.value.filter(u => u.bindUser).map(u => u.bindUser)).size)
const expiringSoon = computed(() => {
  const thirtyDaysLater = new Date()
  thirtyDaysLater.setDate(thirtyDaysLater.getDate() + 30)
  return ukeys.value.filter(u => u.expireDate && new Date(u.expireDate) <= thirtyDaysLater).length
})

const availableUsers = computed(() => {
  const boundUserIds = ukeys.value.filter(u => u.bindUser).map(u => u.bindUser)
  return users.value.filter(u => !boundUserIds.includes(u.username))
})

const getStatusText = (status) => {
  const texts = { ACTIVE: '正常', DISABLED: '禁用', UNBIND: '未绑定', EXPIRED: '已过期' }
  return texts[status] || status
}

const detectUkey = () => {
  loading.value = true
  ElMessage.info('正在检测 U-Key 设备...')

  setTimeout(() => {
    loading.value = false
    const detected = Math.random() < 0.7
    if (detected) {
      ElMessage.success('检测到 1 个 U-Key 设备')
    } else {
      ElMessage.warning('未检测到 U-Key 设备，请确认设备已正确插入')
    }
  }, 1500)
}

const showBindDialog = () => {
  bindForm.value = { userId: null, serialNumber: '', remark: '' }
  bindDialogVisible.value = true
}

const confirmBind = () => {
  if (!bindForm.value.userId) {
    ElMessage.warning('请选择要绑定的用户')
    return
  }
  if (!bindForm.value.serialNumber) {
    ElMessage.warning('请输入 U-Key 序列号')
    return
  }

  const user = users.value.find(u => u.id === bindForm.value.userId)
  const newUkey = {
    id: ukeys.value.length + 1,
    serialNumber: bindForm.value.serialNumber,
    model: 'MagicGuard USB Key V2',
    bindUser: user.username,
    status: 'ACTIVE',
    bindTime: new Date().toLocaleString(),
    expireDate: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toLocaleDateString(),
    lastUsed: null,
    createTime: new Date().toLocaleString(),
    remark: bindForm.value.remark
  }

  ukeys.value.push(newUkey)
  ElMessage.success('U-Key 绑定成功')
  bindDialogVisible.value = false
}

const viewDetail = (ukey) => {
  currentUkey.value = ukey
  detailDialogVisible.value = true
}

const unbind = async (ukey) => {
  try {
    await ElMessageBox.confirm('确定要解除该 U-Key 的绑定吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ukey.bindUser = null
    ukey.bindTime = null
    ukey.status = 'UNBIND'
    ElMessage.success('U-Key 已解除绑定')
  } catch (e) {}
}

const disable = async (ukey) => {
  try {
    await ElMessageBox.confirm('确定要禁用该 U-Key 吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ukey.status = 'DISABLED'
    ElMessage.success('U-Key 已禁用')
  } catch (e) {}
}
</script>

<style scoped>
.ukey-view { width: 100%; }

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
.header-actions { display: flex; gap: 10px; }
.title { font-size: 16px; font-weight: 600; color: #1a1a2e; }

.data-table :deep(.el-table__row:hover > td) { background: #f5f7fa !important; }

.serial-cell { display: flex; align-items: center; gap: 8px; color: #409eff; font-weight: 500; }
.action-buttons { display: flex; gap: 8px; }

.custom-form :deep(.el-form-item__label) { font-weight: 500; }
.ukey-details :deep(.el-descriptions-item__label) { font-weight: 500; }
.serial-highlight { font-family: 'SF Mono', Monaco, monospace; font-weight: 600; color: #409eff; font-size: 14px; }
</style>
