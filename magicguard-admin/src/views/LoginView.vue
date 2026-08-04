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

      <el-form
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

      <div class="login-footer">
        <span class="version">v1.0.0</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const emit = defineEmits(['login-success'])

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 模拟用户数据
const users = [
  { id: 1, username: 'admin', password: 'admin123', role: 'ADMIN', nickname: '系统管理员' },
  { id: 2, username: 'operator', password: 'operator123', role: 'USER', nickname: '操作员' }
]

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate((valid) => {
    if (!valid) return

    loading.value = true

    // 模拟登录验证
    setTimeout(() => {
      const user = users.find(
        u => u.username === loginForm.username && u.password === loginForm.password
      )

      if (user) {
        // 存储用户信息到 localStorage
        const userInfo = {
          id: user.id,
          username: user.username,
          nickname: user.nickname,
          role: user.role,
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

// 检查是否记住密码
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

checkRemember()
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
</style>
