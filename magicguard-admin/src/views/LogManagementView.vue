<template>
  <div class="logs-view">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-primary">
        <div class="stat-icon">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ logs.length }}</div>
          <div class="stat-label">日志总数</div>
        </div>
      </div>
      <div class="stat-card stat-success">
        <div class="stat-icon">
          <el-icon :size="28"><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ successCount }}</div>
          <div class="stat-label">成功操作</div>
        </div>
      </div>
      <div class="stat-card stat-danger">
        <div class="stat-icon">
          <el-icon :size="28"><CircleClose /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ failedCount }}</div>
          <div class="stat-label">失败操作</div>
        </div>
      </div>
      <div class="stat-card stat-info">
        <div class="stat-icon">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ operatorCount }}</div>
          <div class="stat-label">操作人员</div>
        </div>
      </div>
    </div>

    <!-- 主内容卡片 -->
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">操作日志</span>
            <el-tag type="info" size="small">共 {{ logs.length }} 条</el-tag>
          </div>
          <div class="header-actions">
            <el-select v-model="filterStatus" placeholder="状态筛选" style="width: 120px; margin-right: 10px;">
              <el-option label="全部" value="" />
              <el-option label="成功" value="SUCCESS" />
              <el-option label="失败" value="FAILED" />
            </el-select>
            <el-select v-model="filterModule" placeholder="模块筛选" style="width: 140px; margin-right: 10px;">
              <el-option label="全部模块" value="" />
              <el-option label="密钥管理" value="KEY_MANAGEMENT" />
              <el-option label="脱敏规则" value="MASK_RULE" />
              <el-option label="数据源" value="DATASOURCE" />
              <el-option label="脱敏任务" value="MASK_TASK" />
            </el-select>
            <el-button type="primary" plain @click="loadLogs">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="filteredLogs"
        stripe
        class="data-table"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operationType" label="操作类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getOperationType(row.operationType)" size="small">
              {{ getOperationTypeText(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationModule" label="模块" width="130">
          <template #default="{ row }">
            <span class="module-text">{{ row.operationModule }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operationDesc" label="操作描述" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.operationDesc || '-'" placement="top" :show-after="300">
              <span class="desc-text">{{ row.operationDesc || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="120">
          <template #default="{ row }">
            <div class="operator-cell">
              <el-icon><User /></el-icon>
              <span>{{ row.operator }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="operatorIp" label="IP地址" width="140">
          <template #default="{ row }">
            <span class="ip-text">{{ row.operatorIp || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="170" />
      </el-table>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="日志详情" width="650px" class="custom-dialog">
      <el-descriptions :column="2" border v-if="currentLog" class="log-details">
        <el-descriptions-item label="操作类型" :span="2">
          <el-tag :type="getOperationType(currentLog.operationType)" size="small">
            {{ getOperationTypeText(currentLog.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog.operationModule }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operator }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.operatorIp || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标类型">{{ currentLog.targetType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标ID">{{ currentLog.targetId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">
          <div class="detail-desc">{{ currentLog.operationDesc || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="detail-json" v-if="currentLog.requestParams">{{ currentLog.requestParams }}</pre>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="响应结果" :span="2">
          <pre class="detail-json" v-if="currentLog.responseResult">{{ currentLog.responseResult }}</pre>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMessage">
          <div class="error-message">{{ currentLog.errorMessage }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const API_BASE = 'http://localhost:8080/api'

const logs = ref([])
const loading = ref(false)
const filterStatus = ref('')
const filterModule = ref('')
const detailDialogVisible = ref(false)
const currentLog = ref(null)

const successCount = computed(() => logs.value.filter(l => l.status === 'SUCCESS').length)
const failedCount = computed(() => logs.value.filter(l => l.status === 'FAILED').length)
const operatorCount = computed(() => new Set(logs.value.map(l => l.operator)).size)

const filteredLogs = computed(() => {
  return logs.value.filter(log => {
    if (filterStatus.value && log.status !== filterStatus.value) return false
    if (filterModule.value && log.operationModule !== filterModule.value) return false
    return true
  })
})

const getOperationType = (type) => {
  const types = {
    CREATE: 'success',
    UPDATE: 'warning',
    DELETE: 'danger',
    QUERY: 'info',
    EXECUTE: 'primary',
    LOGIN: 'success',
    LOGOUT: 'info'
  }
  return types[type] || 'info'
}

const getOperationTypeText = (type) => {
  const texts = {
    CREATE: '创建',
    UPDATE: '更新',
    DELETE: '删除',
    QUERY: '查询',
    EXECUTE: '执行',
    LOGIN: '登录',
    LOGOUT: '登出'
  }
  return texts[type] || type
}

const loadLogs = async () => {
  loading.value = true
  try {
    const response = await fetch(`${API_BASE}/audit-logs`)
    if (response.ok) {
      logs.value = await response.json()
    } else {
      // API 不存在，使用空数组
      logs.value = []
    }
  } catch (error) {
    console.error('加载日志失败:', error)
    logs.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.logs-view { width: 100%; }

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
.stat-danger .stat-icon { background: linear-gradient(135deg, #ff4d4f 0%, #a8071a 100%); }
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
.header-actions { display: flex; align-items: center; }
.title { font-size: 16px; font-weight: 600; color: #1a1a2e; }

.data-table :deep(.el-table__row:hover > td) { background: #f5f7fa !important; }

.module-text { font-weight: 500; color: #409eff; }
.desc-text { color: #606266; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: block; max-width: 300px; }
.operator-cell { display: flex; align-items: center; gap: 6px; }
.ip-text { font-family: 'SF Mono', Monaco, monospace; font-size: 12px; color: #909399; }

.log-details :deep(.el-descriptions-item__label) { font-weight: 500; }

.detail-desc { color: #606266; }
.detail-json {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
  font-size: 12px;
  font-family: 'SF Mono', Monaco, monospace;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 150px;
  overflow-y: auto;
  margin: 0;
}

.error-message {
  color: #f56c6c;
  background: #fef0f0;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
}
</style>
