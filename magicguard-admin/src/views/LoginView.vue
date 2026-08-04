<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo-icon">
          <el-icon :size="40"><Lock /></el-icon>
        </div>
        <h1 class="title">MagicGuard</h1>
        <p class="subtitle">数据安全平台</p>
      </div>

      <!-- 登录方式切换 -->
      <div class="login-type-tabs">
        <div
          class="tab-item"
          :class="{ active: loginType === 'password' }"
          @click="loginType = 'password'"
        >
          <el-icon><Key /></el-icon>
          <span>密码登录</span>
        </div>
        <div
          class="tab-item"
          :class="{ active: loginType === 'ukey' }"
          @click="handleUkeyLogin"
        >
          <el-icon><Shield /></el-icon>
          <span>U-Key登录</span>
        </div>
      </div>

      <!-- 密码登录表单 -->
      <el-form
        v-if="loginType === 'password'"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- U-Key登录表单 -->
      <div v-else class="ukey-login">
        <div class="ukey-status">
          <div class="ukey-icon" :class="{ connected: ukeyConnected }">
            <el-icon :size="48"><Key /></el-icon>
          </div>
          <div class="ukey-info">
            <h3 v-if="ukeyConnected">U-Key 已连接</h3>
            <h3 v-else-if="ukeyChecking">正在检测 U-Key...</h3>
            <h3 v-else>未检测到 U-Key</h3>
            <p v-if="ukeyConnected && ukeySerial">序列号: {{ ukeySerial }}</p>
            <p v-else-if="!ukeyConnected && !ukeyChecking">
              请插入 U-Key 设备，或联系管理员获取
            </p>
          </div>
        </div>

        <!-- 未安装控件提示 -->
        <el-alert
          v-if="!ukeyDriverInstalled"
          title="U-Key 控件未安装"
          type="warning"
          :closable="false"
          show-icon
          class="ukey-alert"
        >
          <template #default>
            请先
            <el-link type="primary" @click="downloadDriver">下载安装 U-Key 控件</el-link>
            ，安装完成后刷新页面重试。
          </template>
        </el-alert>

        <el-button
          v-if="ukeyConnected"
          type="primary"
          size="large"
          :loading="loading"
          class="login-button"
          @click="handleUkeyLoginConfirm"
        >
          使用 U-Key 登录
        </el-button>

        <el-button
          v-if="ukeyConnected"
          type="warning"
          plain
          size="small"
          class="ukey-refresh-btn"
          @click="detectUkey"
        >
          <el-icon><RefreshRight /></el-icon> 重新检测
        </el-button>
      </div>

      <div class="login-footer">
        <span class="version">v1.0.0</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, Key, Shield, RefreshRight } from '@element-plus/icons-vue'

const emit = defineEmits(['login-success'])

const loginFormRef = ref(null)
const loading = ref(false)
const loginType = ref('password')
const ukeyConnected = ref(false)
const ukeyChecking = ref(false)
const ukeyDriverInstalled = ref(true)
const ukeySerial = ref('')

const users = [
  { id: 1, username: 'admin', password: 'admin123', role: 'ADMIN', nickname: '系统管理员', ukeySerial: 'UK12345678' },
  { id: 2, username: 'operator', password: 'operator123', role: 'USER', nickname: '操作员', ukeySerial: 'UK87654321' }
]

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate((valid) => {
    if (!valid) return

    loading.value = true

    setTimeout(() => {
      const user = users.find(
        u => u.username === loginForm.username && u.password === loginForm.password
      )

      if (user) {
        const userInfo = {
          id: user.id,
          username: user.username,
          nickname: user.nickname,
          role: user.role,
          loginType: 'password',
          token: 'mock_token_' + Date.now()
        }
        localStorage.setItem('magicguard_user', JSON.stringify(userInfo))
        if (loginForm.remember) {
          localStorage.setItem('magicguard_remember', JSON.stringify({
            username: loginForm.username,
            password: loginForm.password
          }))
        }

        ElMessage.success('登录成功')
        emit('login-success')
      } else {
        ElMessage.error('用户名或密码错误')
      }
      loading.value = false
    }, 800)
  })
}

const handleUkeyLogin = () => {
  loginType.value = 'ukey'
  detectUkey()
}

const detectUkey = () => {
  ukeyChecking.value = true
  ukeyConnected.value = false

  setTimeout(() => {
    ukeyChecking.value = false
    const detected = Math.random() < 0.7
    if (detected) {
      ukeyConnected.value = true
      ukeySerial.value = 'UK' + Math.random().toString().slice(2, 10)
      ElMessage.success('U-Key 检测成功')
    } else {
      ukeyConnected.value = false
      ukeySerial.value = ''
    }
  }, 1500)
}

const handleUkeyLoginConfirm = () => {
  if (!ukeyConnected.value) {
    ElMessage.warning('请先插入 U-Key 设备')
    return
  }

  loading.value = true

  setTimeout(() => {
    const user = users[Math.floor(Math.random() * users.length)]
    const userInfo = {
      id: user.id,
      username: user.username,
      nickname: user.nickname,
      role: user.role,
      loginType: 'ukey',
      ukeySerial: ukeySerial.value,
      token: 'ukey_token_' + Date.now()
    }
    localStorage.setItem('magicguard_user', JSON.stringify(userInfo))
    ElMessage.success('U-Key 登录成功')
    emit('login-success')
    loading.value = false
  }, 1000)
}

const downloadDriver = () => {
  ElMessage.info('正在跳转到驱动下载页面...')
  window.open('https://example.com/ukey-driver-download', '_blank')
}

const checkRemember = () => {
  const remember = localStorage.getItem('magicguard_remember')
  if (remember) {
    try {
      const { username, password } = JSON.parse(remember)
      loginForm.username = username
      loginForm.password = password
      loginForm.remember = true
    } catch (e) {}
  }
}

onMounted(() => {
  checkRemember()
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin: 0 auto 20px;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-type-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 24px;
  background: #f5f7fa;
  border-radius: 10px;
  padding: 4px;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  transition: all 0.3s;
}

.tab-item:hover {
  color: #409eff;
}

.tab-item.active {
  background: #fff;
  color: #409eff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.login-form {
  margin-top: 20px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 12px 16px;
  border-radius: 10px;
}

.login-form :deep(.el-input__inner) {
  font-size: 15px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.login-button:active {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  margin-top: 30px;
}

.version {
  font-size: 12px;
  color: #c0c4cc;
}

/* U-Key 登录 */
.ukey-login {
  padding: 20px 0;
}

.ukey-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}

.ukey-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  margin-bottom: 16px;
  transition: all 0.3s;
}

.ukey-icon.connected {
  background: linear-gradient(135deg, #52c41a 0%, #237804 100%);
  color: #fff;
  box-shadow: 0 8px 24px rgba(82, 196, 26, 0.4);
}

.ukey-info {
  text-align: center;
}

.ukey-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px;
}

.ukey-info p {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.ukey-refresh-btn {
  margin-top: 16px;
}

.ukey-alert {
  margin-bottom: 20px;
}
</style>
