<template>
  <div class="tasks-view">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-primary">
        <div class="stat-icon">
          <el-icon :size="28"><List /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ tasks.length }}</div>
          <div class="stat-label">任务总数</div>
        </div>
      </div>
      <div class="stat-card stat-warning">
        <div class="stat-icon">
          <el-icon :size="28"><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ pendingCount }}</div>
          <div class="stat-label">待执行</div>
        </div>
      </div>
      <div class="stat-card stat-success">
        <div class="stat-icon">
          <el-icon :size="28"><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ successCount }}</div>
          <div class="stat-label">执行成功</div>
        </div>
      </div>
      <div class="stat-card stat-danger">
        <div class="stat-icon">
          <el-icon :size="28"><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ failedCount }}</div>
          <div class="stat-label">执行失败</div>
        </div>
      </div>
    </div>

    <!-- 主内容卡片 -->
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">脱敏任务列表</span>
            <el-tag type="info" size="small">共 {{ tasks.length }} 条</el-tag>
          </div>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon> 创建任务
          </el-button>
        </div>
      </template>

      <el-table :data="tasks" stripe class="data-table" :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="taskName" label="任务名称" min-width="150">
          <template #default="{ row }">
            <div class="task-name-cell">
              <el-icon><List /></el-icon>
              <span>{{ row.taskName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="taskCode" label="任务代码" min-width="150">
          <template #default="{ row }">
            <el-tooltip :content="row.taskCode" placement="top" :show-after="300">
              <span class="code-text">{{ row.taskCode }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="sourceTables" label="源表" min-width="150">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.sourceTables }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="execType" label="执行类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.execType === 'FULL' ? 'primary' : 'warning'" size="small">
              {{ row.execType === 'FULL' ? '全量' : '增量' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scheduleType" label="调度类型" width="100">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ scheduleTypeText[row.scheduleType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType[row.status]" size="small">
              {{ statusText[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="execTime" label="执行时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                v-if="row.status === 'PENDING'"
                size="small"
                type="success"
                plain
                @click="executeTask(row)"
              >
                <el-icon><VideoPlay /></el-icon> 执行
              </el-button>
              <el-button
                v-if="row.status === 'RUNNING'"
                size="small"
                type="warning"
                plain
                @click="cancelTask(row)"
              >
                <el-icon><VideoPause /></el-icon> 取消
              </el-button>
              <el-button size="small" type="primary" plain @click="viewDetail(row)">
                <el-icon><Document /></el-icon> 详情
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建任务对话框 -->
    <el-dialog v-model="dialogVisible" title="创建脱敏任务" width="600px" class="custom-dialog">
      <el-form :model="taskForm" label-width="120px" class="custom-form">
        <el-form-item label="任务名称">
          <el-input v-model="taskForm.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="源数据源">
          <el-select v-model="taskForm.sourceDatasourceCode" placeholder="请选择数据源" style="width: 100%">
            <el-option
              v-for="ds in datasources"
              :key="ds.id"
              :label="ds.datasourceName"
              :value="ds.datasourceCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="源表名">
          <el-input v-model="taskForm.sourceTables" placeholder="多个表用逗号分隔，如: user,order" />
        </el-form-item>
        <el-form-item label="目标类型">
          <el-select v-model="taskForm.targetType" placeholder="请选择目标类型" style="width: 100%">
            <el-option label="数据库" value="DATABASE" />
            <el-option label="文件" value="FILE" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行类型">
          <el-select v-model="taskForm.execType" placeholder="请选择执行类型" style="width: 100%">
            <el-option label="全量" value="FULL" />
            <el-option label="增量" value="INCREMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="调度类型">
          <el-select v-model="taskForm.scheduleType" placeholder="请选择调度类型" style="width: 100%">
            <el-option label="立即执行" value="IMMEDIATE" />
            <el-option label="定时执行" value="SCHEDULED" />
            <el-option label="周期执行" value="PERIODIC" />
          </el-select>
        </el-form-item>
        <el-form-item label="脱敏规则">
          <el-input
            v-model="taskForm.maskRulesText"
            type="textarea"
            :rows="4"
            placeholder='JSON格式，如: [{"tableName": "user", "columns": [{"name": "id_card", "ruleId": 1}]}]'
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createTask">确认创建</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="任务详情" width="650px" class="custom-dialog">
      <el-descriptions :column="2" border v-if="currentTask" class="task-details">
        <el-descriptions-item label="任务名称">{{ currentTask.taskName }}</el-descriptions-item>
        <el-descriptions-item label="任务代码">{{ currentTask.taskCode }}</el-descriptions-item>
        <el-descriptions-item label="源表">{{ currentTask.sourceTables }}</el-descriptions-item>
        <el-descriptions-item label="执行类型">
          <el-tag :type="currentTask.execType === 'FULL' ? 'primary' : 'warning'" size="small">
            {{ currentTask.execType === 'FULL' ? '全量' : '增量' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="调度类型">{{ scheduleTypeText[currentTask.scheduleType] }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType[currentTask.status]" size="small">{{ statusText[currentTask.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="执行时间" :span="2">{{ currentTask.execTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间" :span="2">{{ currentTask.finishTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="执行信息" :span="2">
          <div class="message-box" v-if="currentTask.message">{{ currentTask.message }}</div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="脱敏规则" :span="2">
          <pre class="rules-json" v-if="currentTask.maskRulesJson">{{ currentTask.maskRulesJson }}</pre>
          <span v-else>-</span>
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
import { ElMessage, ElMessageBox } from 'element-plus'

const API_BASE = 'http://localhost:8080/api'

const tasks = ref([])
const datasources = ref([])
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentTask = ref(null)

const statusText = {
  PENDING: '待执行',
  RUNNING: '执行中',
  SUCCESS: '成功',
  FAILED: '失败',
  CANCELLED: '已取消'
}

const statusType = {
  PENDING: 'info',
  RUNNING: 'warning',
  SUCCESS: 'success',
  FAILED: 'danger',
  CANCELLED: 'info'
}

const scheduleTypeText = {
  IMMEDIATE: '立即',
  SCHEDULED: '定时',
  PERIODIC: '周期'
}

const taskForm = ref({
  taskName: '',
  sourceDatasourceCode: '',
  sourceTables: '',
  targetType: 'DATABASE',
  execType: 'FULL',
  scheduleType: 'IMMEDIATE',
  maskRulesText: ''
})

const pendingCount = computed(() => tasks.value.filter(t => t.status === 'PENDING').length)
const successCount = computed(() => tasks.value.filter(t => t.status === 'SUCCESS').length)
const failedCount = computed(() => tasks.value.filter(t => t.status === 'FAILED').length)

const loadTasks = async () => {
  try {
    const response = await fetch(`${API_BASE}/tasks`)
    tasks.value = await response.json()
  } catch (error) {
    ElMessage.error('加载任务列表失败')
  }
}

const loadDatasources = async () => {
  try {
    const response = await fetch(`${API_BASE}/datasources`)
    datasources.value = await response.json()
  } catch (error) {
    console.error('加载数据源失败')
  }
}

const showCreateDialog = () => {
  taskForm.value = {
    taskName: '',
    sourceDatasourceCode: datasources.value[0]?.datasourceCode || '',
    sourceTables: '',
    targetType: 'DATABASE',
    execType: 'FULL',
    scheduleType: 'IMMEDIATE',
    maskRulesText: ''
  }
  dialogVisible.value = true
}

const createTask = async () => {
  if (!taskForm.value.taskName || !taskForm.value.sourceTables) {
    ElMessage.warning('请填写任务名称和源表')
    return
  }
  try {
    let maskRules = []
    if (taskForm.value.maskRulesText) {
      maskRules = JSON.parse(taskForm.value.maskRulesText)
    }
    const response = await fetch(`${API_BASE}/tasks`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        taskName: taskForm.value.taskName,
        sourceDatasourceCode: taskForm.value.sourceDatasourceCode,
        sourceTables: taskForm.value.sourceTables,
        targetType: taskForm.value.targetType,
        execType: taskForm.value.execType,
        scheduleType: taskForm.value.scheduleType,
        maskRules: maskRules
      })
    })
    if (response.ok) {
      ElMessage.success('任务创建成功')
      dialogVisible.value = false
      loadTasks()
    }
  } catch (error) {
    ElMessage.error('任务创建失败: ' + error.message)
  }
}

const executeTask = async (task) => {
  try {
    await ElMessageBox.confirm('确定要执行此任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    const response = await fetch(`${API_BASE}/tasks/${task.id}/execute`, { method: 'POST' })
    if (response.ok) {
      ElMessage.success('任务开始执行')
      loadTasks()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('任务执行失败')
    }
  }
}

const cancelTask = async (task) => {
  try {
    await ElMessageBox.confirm('确定要取消此任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await fetch(`${API_BASE}/tasks/${task.id}/cancel`, { method: 'POST' })
    if (response.ok) {
      ElMessage.success('任务已取消')
      loadTasks()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const viewDetail = (task) => {
  currentTask.value = task
  detailDialogVisible.value = true
}

onMounted(() => {
  loadTasks()
  loadDatasources()
})
</script>

<style scoped>
.tasks-view { width: 100%; }

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
.stat-danger .stat-icon { background: linear-gradient(135deg, #ff4d4f 0%, #a8071a 100%); }

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

.task-name-cell { display: flex; align-items: center; gap: 8px; color: #409eff; }
.code-text { font-family: 'SF Mono', Monaco, monospace; font-size: 12px; color: #909399; }
.action-buttons { display: flex; gap: 8px; }

.custom-form :deep(.el-form-item__label) { font-weight: 500; }

.message-box {
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
  max-height: 100px;
  overflow-y: auto;
}

.rules-json {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
  font-size: 12px;
  font-family: 'SF Mono', Monaco, monospace;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 150px;
  overflow-y: auto;
}
</style>
