<template>
  <div class="datasources-view">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-primary">
        <div class="stat-icon">
          <el-icon :size="28"><Connection /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ datasources.length }}</div>
          <div class="stat-label">数据源总数</div>
        </div>
      </div>
      <div class="stat-card stat-success">
        <div class="stat-icon">
          <el-icon :size="28"><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ enabledDsCount }}</div>
          <div class="stat-label">已启用</div>
        </div>
      </div>
      <div class="stat-card stat-info">
        <div class="stat-icon">
          <el-icon :size="28"><Monitor /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ mysqlCount }}</div>
          <div class="stat-label">MySQL</div>
        </div>
      </div>
      <div class="stat-card stat-warning">
        <div class="stat-icon">
          <el-icon :size="28"><Grid /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ groupCount }}</div>
          <div class="stat-label">数据源分组</div>
        </div>
      </div>
    </div>

    <!-- 主内容卡片 -->
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">数据源列表</span>
            <el-tag type="info" size="small">共 {{ datasources.length }} 条</el-tag>
          </div>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon> 添加数据源
          </el-button>
        </div>
      </template>

      <el-table :data="datasources" stripe class="data-table" :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="datasourceName" label="数据源名称" min-width="150">
          <template #default="{ row }">
            <div class="ds-name-cell">
              <el-icon><Connection /></el-icon>
              <span>{{ row.datasourceName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="datasourceCode" label="数据源代码" min-width="120">
          <template #default="{ row }">
            <el-tooltip :content="row.datasourceCode" placement="top" :show-after="300">
              <span class="code-text">{{ row.datasourceCode }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="datasourceType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.datasourceType)" size="small">{{ row.datasourceType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="host" label="连接地址" min-width="180">
          <template #default="{ row }">
            <span class="host-text">{{ row.host }}:{{ row.port }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="databaseName" label="数据库" min-width="120" />
        <el-table-column prop="groupName" label="分组" width="120">
          <template #default="{ row }">
            <el-tag type="info" size="small" v-if="row.groupName">{{ row.groupName }}</el-tag>
            <span v-else class="no-group">未分组</span>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">
              {{ row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" type="success" plain @click="testConnection(row)">测试</el-button>
              <el-button size="small" type="danger" plain @click="deleteDatasource(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加数据源对话框 -->
    <el-dialog v-model="dialogVisible" title="添加数据源" width="560px" class="custom-dialog">
      <el-form :model="dsForm" label-width="100px" class="custom-form">
        <el-form-item label="数据源名称">
          <el-input v-model="dsForm.name" placeholder="请输入数据源名称" />
        </el-form-item>
        <el-form-item label="数据源代码">
          <el-input v-model="dsForm.code" placeholder="请输入数据源代码" />
        </el-form-item>
        <el-form-item label="数据库类型">
          <el-select v-model="dsForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="MySQL" value="MYSQL" />
            <el-option label="Oracle" value="ORACLE" />
            <el-option label="PostgreSQL" value="POSTGRESQL" />
            <el-option label="SQL Server" value="SQLSERVER" />
            <el-option label="达梦 (DM)" value="DM" />
            <el-option label="人大金仓 (Kingbase)" value="KINGBASE" />
          </el-select>
        </el-form-item>
        <el-form-item label="主机地址">
          <el-input v-model="dsForm.host" placeholder="如: localhost" />
        </el-form-item>
        <el-form-item label="端口">
          <el-input-number v-model="dsForm.port" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="数据库名">
          <el-input v-model="dsForm.database" placeholder="请输入数据库名称" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="dsForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="dsForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="分组">
          <el-input v-model="dsForm.groupName" placeholder="如: 生产环境" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createDatasource">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const API_BASE = 'http://192.168.27.164:8080/api'

const datasources = ref([])
const dialogVisible = ref(false)

const dsForm = ref({
  name: '',
  code: '',
  type: 'MYSQL',
  host: 'localhost',
  port: 3306,
  database: '',
  username: '',
  password: '',
  groupName: ''
})

const enabledDsCount = computed(() => datasources.value.filter(ds => ds.enabled === 1).length)
const mysqlCount = computed(() => datasources.value.filter(ds => ds.datasourceType === 'MYSQL').length)
const groupCount = computed(() => new Set(datasources.value.filter(ds => ds.groupName).map(ds => ds.groupName)).size)

const getTypeTag = (type) => {
  const types = { MYSQL: 'primary', ORACLE: 'warning', POSTGRESQL: 'success', SQLSERVER: 'danger', DM: 'info', KINGBASE: 'info' }
  return types[type] || 'info'
}

const loadDatasources = async () => {
  try {
    const response = await fetch(`${API_BASE}/datasources`)
    if (response.ok) {
      datasources.value = await response.json()
    } else {
      datasources.value = []
    }
  } catch (error) {
    datasources.value = []
  }
}

const showCreateDialog = () => {
  dsForm.value = {
    name: '',
    code: '',
    type: 'MYSQL',
    host: 'localhost',
    port: 3306,
    database: '',
    username: '',
    password: '',
    groupName: ''
  }
  dialogVisible.value = true
}

const createDatasource = async () => {
  if (!dsForm.value.name || !dsForm.value.code) {
    ElMessage.warning('请填写数据源名称和代码')
    return
  }
  try {
    const response = await fetch(`${API_BASE}/datasources`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: dsForm.value.name,
        code: dsForm.value.code,
        type: dsForm.value.type,
        host: dsForm.value.host,
        port: dsForm.value.port,
        database: dsForm.value.database,
        username: dsForm.value.username,
        password: dsForm.value.password,
        groupName: dsForm.value.groupName
      })
    })
    if (response.ok) {
      ElMessage.success('数据源添加成功')
      dialogVisible.value = false
      loadDatasources()
    }
  } catch (error) {
    ElMessage.error('数据源添加失败')
  }
}

const testConnection = async (ds) => {
  try {
    const response = await fetch(`${API_BASE}/datasources/${ds.id}/test`, { method: 'POST' })
    const result = await response.json()
    if (result.success) {
      ElMessage.success(result.message || '连接成功')
    } else {
      ElMessage.error(result.message || '连接失败')
    }
  } catch (error) {
    ElMessage.error('连接测试失败')
  }
}

const deleteDatasource = async (ds) => {
  try {
    await ElMessageBox.confirm('确定要删除此数据源吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await fetch(`${API_BASE}/datasources/${ds.id}`, { method: 'DELETE' })
    if (response.ok) {
      ElMessage.success('删除成功')
      loadDatasources()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadDatasources()
})
</script>

<style scoped>
.datasources-view { width: 100%; }

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

.ds-name-cell { display: flex; align-items: center; gap: 8px; color: #409eff; }
.code-text { font-family: 'SF Mono', Monaco, monospace; font-size: 12px; color: #909399; }
.host-text { font-family: 'SF Mono', Monaco, monospace; font-size: 13px; }
.no-group { color: #c0c4cc; font-size: 13px; }
.action-buttons { display: flex; gap: 8px; }

.custom-form :deep(.el-form-item__label) { font-weight: 500; }
</style>
