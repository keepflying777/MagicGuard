<template>
  <div class="rules-view">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-primary">
        <div class="stat-icon">
          <el-icon :size="28"><Filter /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ rules.length }}</div>
          <div class="stat-label">规则总数</div>
        </div>
      </div>
      <div class="stat-card stat-success">
        <div class="stat-icon">
          <el-icon :size="28"><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ enabledRulesCount }}</div>
          <div class="stat-label">已启用</div>
        </div>
      </div>
      <div class="stat-card stat-info">
        <div class="stat-icon">
          <el-icon :size="28"><Cpu /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ algorithmTypesCount }}</div>
          <div class="stat-label">算法类型</div>
        </div>
      </div>
      <div class="stat-card stat-warning">
        <div class="stat-icon">
          <el-icon :size="28"><Setting /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ maxPriority }}</div>
          <div class="stat-label">最高优先级</div>
        </div>
      </div>
    </div>

    <!-- 主内容卡片 -->
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">脱敏规则列表</span>
            <el-tag type="info" size="small">共 {{ rules.length }} 条</el-tag>
          </div>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon> 创建规则
          </el-button>
        </div>
      </template>

      <el-table :data="rules" stripe class="data-table" :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="ruleName" label="规则名称" min-width="150">
          <template #default="{ row }">
            <div class="rule-name-cell">
              <el-icon><Filter /></el-icon>
              <span>{{ row.ruleName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="ruleCode" label="规则代码" min-width="150">
          <template #default="{ row }">
            <el-tooltip :content="row.ruleCode" placement="top" :show-after="300">
              <span class="code-text">{{ row.ruleCode }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="algorithmType" label="算法类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getAlgorithmType(row.algorithmType)" size="small">{{ row.algorithmType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag type="warning" size="small">P{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              :active-value="1"
              :inactive-value="0"
              @change="toggleEnabled(row)"
              active-color="#52c41a"
              inactive-color="#dcdfe6"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" type="primary" plain @click="testRule(row)">测试</el-button>
              <el-button size="small" type="warning" plain @click="editRule(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="deleteRule(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建规则对话框 -->
    <el-dialog v-model="dialogVisible" title="创建脱敏规则" width="520px" class="custom-dialog">
      <el-form :model="ruleForm" label-width="100px" class="custom-form">
        <el-form-item label="规则名称">
          <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则代码">
          <el-input v-model="ruleForm.ruleCode" placeholder="请输入规则代码" />
        </el-form-item>
        <el-form-item label="算法类型">
          <el-select v-model="ruleForm.algorithmType" placeholder="请选择算法" style="width: 100%">
            <el-option v-for="alg in algorithms" :key="alg" :label="getAlgorithmLabel(alg)" :value="alg">
              <span>{{ getAlgorithmLabel(alg) }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="参数配置">
          <el-input
            v-model="ruleForm.paramsText"
            type="textarea"
            :rows="3"
            placeholder='如: {"prefixLen": 3, "suffixLen": 4}'
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="ruleForm.priority" :min="1" :max="1000" :step="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createRule">确认创建</el-button>
      </template>
    </el-dialog>

    <!-- 测试规则对话框 -->
    <el-dialog v-model="testDialogVisible" title="测试脱敏规则" width="500px" class="custom-dialog">
      <el-form label-width="100px" class="custom-form">
        <el-form-item label="选择规则">
          <el-select v-model="testForm.ruleId" placeholder="请选择规则" style="width: 100%">
            <el-option v-for="r in rules" :key="r.id" :label="r.ruleName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="测试数据">
          <el-input v-model="testForm.data" placeholder="请输入测试数据" />
        </el-form-item>
        <el-form-item label="脱敏结果" v-if="testForm.result">
          <el-input v-model="testForm.result" readonly class="result-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
        <el-button type="success" @click="doTest" :disabled="!testForm.ruleId || !testForm.data">
          <el-icon><Cpu /></el-icon> 执行测试
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑规则对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑脱敏规则" width="520px" class="custom-dialog">
      <el-form :model="editForm" label-width="100px" class="custom-form">
        <el-form-item label="规则名称">
          <el-input v-model="editForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="算法类型">
          <el-select v-model="editForm.algorithmType" placeholder="请选择算法" style="width: 100%">
            <el-option v-for="alg in algorithms" :key="alg" :label="getAlgorithmLabel(alg)" :value="alg">
              <span>{{ getAlgorithmLabel(alg) }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="参数配置">
          <el-input
            v-model="editForm.paramsText"
            type="textarea"
            :rows="3"
            placeholder='如: {"prefixLen": 3, "suffixLen": 4}'
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="editForm.priority" :min="1" :max="1000" :step="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateRule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const API_BASE = 'http://192.168.27.164:8080/api'

const rules = ref([])
const algorithms = ref(['MASK', 'REPLACE', 'TRUNCATE', 'FPE', 'HASH'])
const dialogVisible = ref(false)
const testDialogVisible = ref(false)
const editDialogVisible = ref(false)

const ruleForm = ref({
  ruleName: '',
  ruleCode: '',
  algorithmType: 'MASK',
  paramsText: '',
  priority: 100
})

const testForm = ref({
  ruleId: null,
  data: '',
  result: ''
})

const enabledRulesCount = computed(() => rules.value.filter(r => r.enabled === 1).length)
const algorithmTypesCount = computed(() => new Set(rules.value.map(r => r.algorithmType)).size)
const maxPriority = computed(() => {
  if (rules.value.length === 0) return 0
  return Math.min(...rules.value.map(r => r.priority))
})

const getAlgorithmType = (alg) => {
  const types = { MASK: 'primary', REPLACE: 'success', TRUNCATE: 'warning', FPE: 'danger', HASH: 'info' }
  return types[alg] || 'info'
}

const getAlgorithmLabel = (alg) => {
  const labels = {
    MASK: 'MASK（掩码）',
    REPLACE: 'REPLACE（替换）',
    TRUNCATE: 'TRUNCATE（截断）',
    FPE: 'FPE（保形加密）',
    HASH: 'HASH（哈希）'
  }
  return labels[alg] || alg
}

const loadRules = async () => {
  try {
    const response = await fetch(`${API_BASE}/rules`)
    if (response.ok) {
      rules.value = await response.json()
    } else {
      rules.value = []
    }
  } catch (error) {
    rules.value = []
  }
}

const showCreateDialog = () => {
  ruleForm.value = {
    ruleName: '',
    ruleCode: '',
    algorithmType: 'MASK',
    paramsText: '',
    priority: 100
  }
  dialogVisible.value = true
}

const createRule = async () => {
  if (!ruleForm.value.ruleName || !ruleForm.value.ruleCode) {
    ElMessage.warning('请填写规则名称和代码')
    return
  }
  try {
    let params = {}
    if (ruleForm.value.paramsText) {
      params = JSON.parse(ruleForm.value.paramsText)
    }
    const response = await fetch(`${API_BASE}/rules`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        ruleName: ruleForm.value.ruleName,
        ruleCode: ruleForm.value.ruleCode,
        algorithmType: ruleForm.value.algorithmType,
        params: params,
        priority: ruleForm.value.priority
      })
    })
    if (response.ok) {
      ElMessage.success('规则创建成功')
      dialogVisible.value = false
      loadRules()
    }
  } catch (error) {
    ElMessage.error('规则创建失败: ' + error.message)
  }
}

const toggleEnabled = async (rule) => {
  try {
    await fetch(`${API_BASE}/rules/${rule.id}/enabled?enabled=${rule.enabled === 1}`, { method: 'PUT' })
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    loadRules()
  }
}

const testRule = (rule) => {
  testForm.value = { ruleId: rule.id, data: '', result: '' }
  testDialogVisible.value = true
}

const doTest = async () => {
  try {
    const rule = rules.value.find(r => r.id === testForm.value.ruleId)
    const response = await fetch(`${API_BASE}/rules/test`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        algorithmType: rule.algorithmType,
        data: testForm.value.data,
        params: JSON.parse(rule.algorithmParams || '{}')
      })
    })
    const result = await response.json()
    testForm.value.result = result.masked
    ElMessage.success('测试完成')
  } catch (error) {
    ElMessage.error('测试失败')
  }
}

const deleteRule = async (rule) => {
  try {
    await ElMessageBox.confirm('确定要删除此规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await fetch(`${API_BASE}/rules/${rule.id}`, { method: 'DELETE' })
    if (response.ok) {
      ElMessage.success('删除成功')
      loadRules()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const editForm = ref({
  id: null,
  ruleName: '',
  algorithmType: 'MASK',
  paramsText: '',
  priority: 100
})

const editRule = (rule) => {
  editForm.value = {
    id: rule.id,
    ruleName: rule.ruleName,
    algorithmType: rule.algorithmType,
    paramsText: rule.algorithmParams || '',
    priority: rule.priority || 100
  }
  editDialogVisible.value = true
}

const updateRule = async () => {
  if (!editForm.value.ruleName) {
    ElMessage.warning('请填写规则名称')
    return
  }
  try {
    let params = {}
    if (editForm.value.paramsText) {
      params = JSON.parse(editForm.value.paramsText)
    }
    const response = await fetch(`${API_BASE}/rules/${editForm.value.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        ruleName: editForm.value.ruleName,
        algorithmType: editForm.value.algorithmType,
        params: params,
        priority: editForm.value.priority
      })
    })
    if (response.ok) {
      ElMessage.success('规则更新成功')
      editDialogVisible.value = false
      loadRules()
    }
  } catch (error) {
    ElMessage.error('规则更新失败: ' + error.message)
  }
}

onMounted(() => {
  loadRules()
})
</script>

<style scoped>
.rules-view { width: 100%; }

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

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

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

.rule-name-cell { display: flex; align-items: center; gap: 8px; color: #409eff; }
.code-text { font-family: 'SF Mono', Monaco, monospace; font-size: 12px; color: #909399; }
.action-buttons { display: flex; gap: 8px; }

.custom-form :deep(.el-form-item__label) { font-weight: 500; }
.result-input :deep(.el-input__inner) { background: #f5f7fa; }
</style>
