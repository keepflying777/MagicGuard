<template>
  <div class="keys-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>密钥列表</span>
          <el-button type="primary" @click="showGenerateDialog">
            <el-icon><Plus /></el-icon> 生成密钥
          </el-button>
        </div>
      </template>

      <el-table :data="keys" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="keyName" label="密钥名称" />
        <el-table-column prop="keyCode" label="密钥代码" />
        <el-table-column prop="algorithm" label="算法" width="100" />
        <el-table-column prop="keyLength" label="密钥长度" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="testEncrypt(row)">测试</el-button>
            <el-button size="small" type="warning" @click="rotateKey(row)">轮换</el-button>
            <el-button size="small" type="danger" @click="destroyKey(row)">销毁</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 生成密钥对话框 -->
    <el-dialog v-model="generateDialogVisible" title="生成密钥" width="500px">
      <el-form :model="generateForm" label-width="100px">
        <el-form-item label="密钥名称">
          <el-input v-model="generateForm.keyName" placeholder="请输入密钥名称" />
        </el-form-item>
        <el-form-item label="算法">
          <el-select v-model="generateForm.algorithm" placeholder="请选择算法">
            <el-option label="SM4" value="SM4" />
            <el-option label="AES" value="AES" />
          </el-select>
        </el-form-item>
        <el-form-item label="密钥长度">
          <el-select v-model="generateForm.keyLength" placeholder="请选择密钥长度">
            <el-option label="128位" :value="128" />
            <el-option label="256位" :value="256" />
          </el-select>
        </el-form-item>
        <el-form-item label="用途">
          <el-select v-model="generateForm.purpose" placeholder="请选择用途">
            <el-option label="字段加密" value="FIELD_ENCRYPT" />
            <el-option label="数据脱敏" value="DATA_MASK" />
            <el-option label="密钥包装" value="KEY_WRAP" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="generateKey">确定</el-button>
      </template>
    </el-dialog>

    <!-- 测试加密对话框 -->
    <el-dialog v-model="testDialogVisible" title="测试加解密" width="500px">
      <el-form :model="testForm" label-width="100px">
        <el-form-item label="选择密钥">
          <el-select v-model="testForm.keyId" placeholder="请选择密钥">
            <el-option
              v-for="key in keys"
              :key="key.id"
              :label="key.keyName"
              :value="key.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="原始数据">
          <el-input v-model="testForm.plainData" type="textarea" rows="3" placeholder="请输入要加密的数据" />
        </el-form-item>
        <el-form-item label="加密结果" v-if="testForm.encryptedData">
          <el-input v-model="testForm.encryptedData" type="textarea" rows="3" readonly />
        </el-form-item>
        <el-form-item label="解密结果" v-if="testForm.decryptedData">
          <el-input v-model="testForm.decryptedData" type="textarea" rows="3" readonly />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="doEncrypt">加密</el-button>
        <el-button type="success" @click="doDecrypt" :disabled="!testForm.encryptedData">解密</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const API_BASE = 'http://localhost:8080/api'

const keys = ref([])
const generateDialogVisible = ref(false)
const testDialogVisible = ref(false)

const generateForm = ref({
  keyName: '',
  algorithm: 'SM4',
  keyLength: 128,
  purpose: 'FIELD_ENCRYPT'
})

const testForm = ref({
  keyId: null,
  plainData: '',
  encryptedData: '',
  decryptedData: ''
})

const loadKeys = async () => {
  try {
    const response = await fetch(`${API_BASE}/keys`)
    keys.value = await response.json()
  } catch (error) {
    ElMessage.error('加载密钥列表失败')
  }
}

const showGenerateDialog = () => {
  generateForm.value.keyName = ''
  generateDialogVisible.value = true
}

const generateKey = async () => {
  try {
    const response = await fetch(`${API_BASE}/keys/generate?keyName=${encodeURIComponent(generateForm.value.keyName)}&algorithm=${generateForm.value.algorithm}&keyLength=${generateForm.value.keyLength}&purpose=${generateForm.value.purpose}`, {
      method: 'POST'
    })
    if (response.ok) {
      ElMessage.success('密钥生成成功')
      generateDialogVisible.value = false
      loadKeys()
    }
  } catch (error) {
    ElMessage.error('密钥生成失败')
  }
}

const testEncrypt = (key) => {
  testForm.value = {
    keyId: key.id,
    plainData: '',
    encryptedData: '',
    decryptedData: ''
  }
  testDialogVisible.value = true
}

const doEncrypt = async () => {
  if (!testForm.value.keyId || !testForm.value.plainData) {
    ElMessage.warning('请选择密钥并输入数据')
    return
  }
  try {
    const response = await fetch(`${API_BASE}/keys/encrypt?data=${encodeURIComponent(testForm.value.plainData)}&keyId=${testForm.value.keyId}`, {
      method: 'POST'
    })
    const result = await response.json()
    testForm.value.encryptedData = result.encryptedData
    ElMessage.success('加密成功')
  } catch (error) {
    ElMessage.error('加密失败')
  }
}

const doDecrypt = async () => {
  if (!testForm.value.keyId || !testForm.value.encryptedData) {
    ElMessage.warning('请选择密钥并输入加密数据')
    return
  }
  try {
    const response = await fetch(`${API_BASE}/keys/decrypt?encryptedData=${encodeURIComponent(testForm.value.encryptedData)}&keyId=${testForm.value.keyId}`, {
      method: 'POST'
    })
    const result = await response.json()
    testForm.value.decryptedData = result.data
    ElMessage.success('解密成功')
  } catch (error) {
    ElMessage.error('解密失败')
  }
}

const rotateKey = async (key) => {
  try {
    await ElMessageBox.confirm('确定要轮换此密钥吗？轮换后旧密钥将标记为 ROTATED 状态。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await fetch(`${API_BASE}/keys/${key.id}/rotate`, { method: 'POST' })
    if (response.ok) {
      ElMessage.success('密钥轮换成功')
      loadKeys()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('密钥轮换失败')
    }
  }
}

const destroyKey = async (key) => {
  try {
    await ElMessageBox.confirm('确定要销毁此密钥吗？销毁后数据将无法解密！', '危险操作', {
      confirmButtonText: '确定销毁',
      cancelButtonText: '取消',
      type: 'error'
    })
    const response = await fetch(`${API_BASE}/keys/${key.id}`, { method: 'DELETE' })
    if (response.ok) {
      ElMessage.success('密钥销毁成功')
      loadKeys()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('密钥销毁失败')
    }
  }
}

onMounted(() => {
  loadKeys()
})
</script>

<style scoped>
.keys-view {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
