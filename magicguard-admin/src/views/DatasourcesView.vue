<template>
  <div class="datasources-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据源列表</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon> 添加数据源
          </el-button>
        </div>
      </template>

      <el-table :data="datasources" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="datasourceName" label="数据源名称" />
        <el-table-column prop="datasourceCode" label="数据源代码" />
        <el-table-column prop="datasourceType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.datasourceType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="host" label="主机" />
        <el-table-column prop="port" label="端口" width="80" />
        <el-table-column prop="databaseName" label="数据库" />
        <el-table-column prop="groupName" label="分组" width="100" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'">
              {{ row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="testConnection(row)">测试</el-button>
            <el-button size="small" type="danger" @click="deleteDatasource(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加数据源对话框 -->
    <el-dialog v-model="dialogVisible" title="添加数据源" width="500px">
      <el-form :model="dsForm" label-width="100px">
        <el-form-item label="数据源名称">
          <el-input v-model="dsForm.name" placeholder="请输入数据源名称" />
        </el-form-item>
        <el-form-item label="数据源代码">
          <el-input v-model="dsForm.code" placeholder="请输入数据源代码" />
        </el-form-item>
        <el-form-item label="数据库类型">
          <el-select v-model="dsForm.type" placeholder="请选择类型">
            <el-option label="MySQL" value="MYSQL" />
            <el-option label="Oracle" value="ORACLE" />
            <el-option label="PostgreSQL" value="POSTGRESQL" />
            <el-option label="SQL Server" value="SQLSERVER" />
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
        <el-button type="primary" @click="createDatasource">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const API_BASE = 'http://localhost:8080/api'

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

const loadDatasources = async () => {
  try {
    const response = await fetch(`${API_BASE}/datasources`)
    datasources.value = await response.json()
  } catch (error) {
    ElMessage.error('加载数据源列表失败')
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
      ElMessage.success(result.message)
    } else {
      ElMessage.error(result.message)
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
.datasources-view {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
