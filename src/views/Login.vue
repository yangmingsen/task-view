<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const username = ref('')
const password = ref('')
const errorMsg = ref('')
const loading = ref(false)

async function handleLogin() {
  errorMsg.value = ''

  if (!username.value.trim()) {
    errorMsg.value = '请输入用户名'
    return
  }
  if (!password.value.trim()) {
    errorMsg.value = '请输入密码'
    return
  }

  loading.value = true
  // 模拟登录请求
  await new Promise((resolve) => setTimeout(resolve, 800))

  // 简单模拟：用户名和密码非空即可登录
  if (username.value.trim() && password.value.trim()) {
    localStorage.setItem('token', 'mock-token-' + Date.now())
    localStorage.setItem('user', JSON.stringify({ name: username.value.trim() }))
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } else {
    errorMsg.value = '用户名或密码错误'
  }
  loading.value = false
}

function handleKeydown(e) {
  if (e.key === 'Enter') handleLogin()
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h1 class="login-logo">📋 待办系统</h1>
        <p class="login-desc">登录以管理您的待办事项</p>
      </div>

      <div class="login-form">
        <div class="form-item">
          <label>用户名</label>
          <input
            v-model="username"
            type="text"
            class="form-input login-input"
            placeholder="请输入用户名"
            @keydown="handleKeydown"
            autofocus
          />
        </div>

        <div class="form-item">
          <label>密码</label>
          <input
            v-model="password"
            type="password"
            class="form-input login-input"
            placeholder="请输入密码"
            @keydown="handleKeydown"
          />
        </div>

        <div v-if="errorMsg" class="login-error">{{ errorMsg }}</div>

        <button
          class="btn btn-primary login-btn"
          :disabled="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </button>

        <p class="login-hint">提示：输入任意用户名和密码即可登录（模拟）</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.login-header {
  text-align: center;
  padding: 40px 32px 24px;
}

.login-logo {
  font-size: 28px;
  font-weight: 700;
  color: #1a73e8;
  margin-bottom: 8px;
}

.login-desc {
  color: #999;
  font-size: 14px;
}

.login-form {
  padding: 0 32px 40px;
}

.login-input {
  width: 100%;
  padding: 12px 14px;
  font-size: 15px;
}

.login-error {
  color: #cf1322;
  font-size: 13px;
  margin-top: -6px;
  padding: 8px 12px;
  background: #fff1f0;
  border-radius: 6px;
  border: 1px solid #ffa39e;
}

.login-btn {
  width: 100%;
  padding: 13px;
  font-size: 16px;
  margin-top: 4px;
}

.login-hint {
  text-align: center;
  color: #bbb;
  font-size: 12px;
  margin-top: 16px;
}
</style>
