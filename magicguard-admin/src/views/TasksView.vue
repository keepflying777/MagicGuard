<template>
  <div class="tasks-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>脱敏任务列表</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon> 创建任务
          </el-button>
        </div>
      </template>

      <el-table :data="tasks" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="taskCode" label="任务代码" />
        <el-table-column prop="sourceTables" label="源表" />
        <el-table-column prop="execType" label="执行类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.execType === 'FULL' ? '全量' : '增量' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scheduleType" label="调度类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ scheduleTypeText[row.scheduleType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType[row.status]">{{ statusText[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="execTime" label="执行时间" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="primary"
              @click="executeTask(row)"
            >
              执行
            </el-button>
            <el-button
              v-if="row.status === 'RUNNING'"
              size="small"
              type="warning"
              @click="cancelTask(row)"
            >
              取消
            </el-button>
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建任务对话框 -->
    <el-dialog v-model="dialogVisible" title="创建脱敏任务" width="600px">
      <el-form :model="taskForm" label-width="120px">
        <el-form-item label="任务名称">
          <el-input v-model="taskForm.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="源数据源">
          <el-select v-model="taskForm.sourceDatasourceCode" placeholder="请选择数据源">
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
          <el-select v-model="taskForm.targetType" placeholder="请选择目标类型">
            <el-option label="数据库" value="DATABASE" />
            <el-option label="文件" value="FILE" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行类型">
          <el-select v-model="taskForm.execType" placeholder="请选择执行类型">
            <el-option label="全量" value="FULL" />
            <el-option label="增量" value="INCREMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="调度类型">
          <el-select v-model="taskForm.scheduleType" placeholder="请选择调度类型">
            <el-option label="立即执行" value="IMMEDIATE" />
            <el-option label="定时执行" value="SCHEDULED" />
            <el-option label="周期执行" value="PERIODIC" />
          </el-select>
        </el-form-item>
        <el-form-item label="脱敏规则">
          <el-input
            v-model="taskForm.maskRulesText"
            type="textarea"
            rows="4"
            placeholder='JSON格式，如: [{"tableName": "user", "columns": [{"name": "id_card", "ruleId": 1}]}]'
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createTask">确定</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="任务详情" width="600px">
      <el-descriptions :column="2" border v-if="currentTask">
        <el-descriptions-item label="任务名称">{{ currentTask.taskName }}</el-descriptions-item>
        <el-descriptions-item label="任务代码">{{ currentTask.taskCode }}</el-descriptions-item>
        <el-descriptions-item label="源表">{{ currentTask.sourceTables }}</el-descriptions-item>
        <el-descriptions-item label="执行类型">{{ currentTask.execType }}</el-descriptions-item>
        <el-descriptions-item label="调度类型">{{ scheduleTypeText[currentTask.scheduleType] }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText[currentTask.status] }}</el-descriptions-item>
        <el-descriptions-item label="执行时间" :span="2">{{ currentTask.execTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间" :span="2">{{ currentTask.finishTime }}</el-descriptions-item>
        <el-descriptions-item label="执行信息" :span="2">{{ currentTask.message }}</el-descriptions-item>
        <el-descriptions-item label="脱敏规则" :span="2">
          <pre style="font-size: 12px;">{{ currentTask.maskRulesJson }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const API_BASE = 'http://localhost:8080/api'

const tasks = ref([])
const datasources = ref []
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
.tasks-view {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
