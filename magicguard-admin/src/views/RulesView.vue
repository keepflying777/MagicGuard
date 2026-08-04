<template>
  <div class="rules-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>脱敏规则列表</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon> 创建规则
          </el-button>
        </div>
      </template>

      <el-table :data="rules" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="ruleName" label="规则名称" />
        <el-table-column prop="ruleCode" label="规则代码" />
        <el-table-column prop="algorithmType" label="算法类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.algorithmType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              :active-value="1"
              :inactive-value="0"
              @change="toggleEnabled(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="testRule(row)">测试</el-button>
            <el-button size="small" type="danger" @click="deleteRule(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建规则对话框 -->
    <el-dialog v-model="dialogVisible" title="创建脱敏规则" width="500px">
      <el-form :model="ruleForm" label-width="100px">
        <el-form-item label="规则名称">
          <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则代码">
          <el-input v-model="ruleForm.ruleCode" placeholder="请输入规则代码" />
        </el-form-item>
        <el-form-item label="算法类型">
          <el-select v-model="ruleForm.algorithmType" placeholder="请选择算法">
            <el-option v-for="alg in algorithms" :key="alg" :label="alg" :value="alg" />
          </el-select>
        </el-form-item>
        <el-form-item label="参数">
          <el-input v-model="ruleForm.paramsText" type="textarea" rows="3" placeholder='如: {"prefixLen": 3, "suffixLen": 4}' />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="ruleForm.priority" :min="1" :max="1000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createRule">确定</el-button>
      </template>
    </el-dialog>

    <!-- 测试规则对话框 -->
    <el-dialog v-model="testDialogVisible" title="测试脱敏规则" width="500px">
      <el-form label-width="100px">
        <el-form-item label="规则">
          <el-select v-model="testForm.ruleId" placeholder="请选择规则">
            <el-option v-for="r in rules" :key="r.id" :label="r.ruleName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="测试数据">
          <el-input v-model="testForm.data" placeholder="请输入测试数据" />
        </el-form-item>
        <el-form-item label="脱敏结果" v-if="testForm.result">
          <el-input v-model="testForm.result" readonly />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="doTest">测试</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const API_BASE = 'http://localhost:8080/api'

const rules = ref([])
const algorithms = ref(['MASK', 'REPLACE', 'TRUNCATE', 'FPE', 'HASH'])
const dialogVisible = ref(false)
const testDialogVisible = ref(false)

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

const loadRules = async () => {
  try {
    const response = await fetch(`${API_BASE}/rules`)
    rules.value = await response.json()
  } catch (error) {
    ElMessage.error('加载规则列表失败')
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
    await fetch(`${API_BASE}/rules/${rule.id}/enabled?enabled=${rule.enabled === 1}`, {
      method: 'PUT'
    })
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
  if (!testForm.value.ruleId || !testForm.value.data) {
    ElMessage.warning('请选择规则并输入测试数据')
    return
  }
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

onMounted(() => {
  loadRules()
})
</script>

<style scoped>
.rules-view {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
