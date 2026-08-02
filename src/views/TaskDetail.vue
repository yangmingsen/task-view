<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  fetchTodoById,
  deleteTodo,
  getTypeLabel,
  getStatusLabel,
  getPriorityLabel,
} from '../api/task.js'

const route = useRoute()
const router = useRouter()

const todo = ref(null)
const loading = ref(true)
const shareCopied = ref(false)

onMounted(async () => {
  try {
    todo.value = await fetchTodoById(route.params.id)
  } catch (e) {
    alert('获取详情失败: ' + e.message)
    router.push('/')
  } finally {
    loading.value = false
  }
})

function goBack() {
  router.push('/')
}

function goEdit() {
  router.push(`/task/${todo.value.id}/edit`)
}

async function handleDelete() {
  if (!confirm('确定要删除该待办吗？此操作不可撤销。')) return
  try {
    await deleteTodo(todo.value.id)
    router.push('/')
  } catch (e) {
    alert('删除失败: ' + e.message)
  }
}

function copyShareLink() {
  const url = window.location.origin + '/share/' + todo.value.id
  navigator.clipboard.writeText(url).then(() => {
    shareCopied.value = true
    setTimeout(() => { shareCopied.value = false }, 2000)
  }).catch(() => {
    const input = document.createElement('input')
    input.value = url
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    shareCopied.value = true
    setTimeout(() => { shareCopied.value = false }, 2000)
  })
}

function getTypeClass(type) {
  if (type === 'bug') return 'type-bug'
  if (type === 'story') return 'type-story'
  if (type === 'task') return 'type-task'
  return ''
}

function getPriorityClass(p) {
  if (p === 1) return 'pri-urgent'
  if (p === 2) return 'pri-high'
  if (p === 3) return 'pri-mid'
  return 'pri-low'
}

function getStatusClass(s) {
  if (s === 'wait') return 'status-wait'
  if (s === 'doing') return 'status-doing'
  if (s === 'done') return 'status-done'
  if (s === 'closed') return 'status-closed'
  return ''
}
</script>

<template>
  <div class="detail-page">
    <!-- 顶栏 -->
    <header class="detail-header">
      <button class="btn-back" @click="goBack">← 返回列表</button>
      <h2 v-if="todo">待办详情 #{{ todo.id }}</h2>
      <div class="header-actions">
        <button class="btn btn-share" @click="copyShareLink">
          {{ shareCopied ? '✅ 已复制' : '🔗 复制分享链接' }}
        </button>
        <button class="btn btn-edit" @click="goEdit">✏️ 编辑</button>
        <button class="btn btn-delete" @click="handleDelete">🗑️ 删除</button>
      </div>
    </header>

    <!-- 加载中 -->
    <div class="detail-loading" v-if="loading">加载中...</div>

    <!-- 内容 -->
    <div class="detail-content" v-else-if="todo">
      <!-- 基本信息 -->
      <div class="info-card">
        <h3 class="card-title">基本信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>标题</label>
            <span class="info-title">{{ todo.title }}</span>
          </div>
          <div class="info-row">
            <div class="info-item">
              <label>类型</label>
              <span :class="['type-tag', getTypeClass(todo.type)]">{{ getTypeLabel(todo.type) }}</span>
            </div>
            <div class="info-item">
              <label>优先级</label>
              <span :class="['priority-tag', getPriorityClass(todo.priority)]">{{ getPriorityLabel(todo.priority) }}</span>
            </div>
            <div class="info-item">
              <label>状态</label>
              <span :class="['status-tag', getStatusClass(todo.status)]">{{ getStatusLabel(todo.status) }}</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <label>负责人</label>
              <span class="info-value">{{ todo.assignedTo }}</span>
            </div>
            <div class="info-item">
              <label>截止日期</label>
              <span class="info-value" :class="{ overdue: todo.deadline < '2026-08-02' && todo.status !== 'done' && todo.status !== 'closed' }">
                {{ todo.deadline }}
              </span>
            </div>
            <div class="info-item">
              <label>进度</label>
              <div class="progress-bar-lg">
                <div class="progress-fill" :style="{ width: todo.progress + '%' }"></div>
              </div>
              <span class="progress-text-lg">{{ todo.progress }}%</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <label>所属项目</label>
              <span class="info-value">{{ todo.project }}</span>
            </div>
            <div class="info-item">
              <label>所属模块</label>
              <span class="info-value">{{ todo.module }}</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <label>创建人</label>
              <span class="info-value">{{ todo.createdBy }}</span>
            </div>
            <div class="info-item">
              <label>创建日期</label>
              <span class="info-value">{{ todo.createdDate }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 描述 -->
      <div class="info-card desc-card">
        <h3 class="card-title">详细描述</h3>
        <v-md-preview :text="todo.desc || '暂无描述'" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 24px;
  overflow: hidden;
}

/* 顶栏 */
.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 0;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.detail-header h2 {
  flex: 1;
  font-size: 18px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.btn-back {
  border: none;
  background: transparent;
  color: #1a73e8;
  font-size: 14px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 6px;
  transition: background 0.15s;
}

.btn-back:hover {
  background: #f0f5ff;
}

.btn-share {
  background: #f0f5ff;
  color: #1a73e8;
  border-color: #91bff9;
}

.btn-share:hover {
  background: #d6e4ff;
}

/* 加载 */
.detail-loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 16px;
}

/* 滚动内容区 */
.detail-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px 0;
}

/* 信息卡片 */
.info-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item label {
  font-size: 12px;
  color: #999;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #333;
}

.info-value.overdue {
  color: #cf1322;
  font-weight: 500;
}

.info-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

/* 进度条 */
.progress-bar-lg {
  width: 100%;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  margin-top: 2px;
}

.progress-bar-lg .progress-fill {
  height: 100%;
  background: #52c41a;
  border-radius: 4px;
  transition: width 0.3s;
}

.progress-text-lg {
  font-size: 12px;
  color: #666;
}

/* 描述卡片 */
.desc-card :deep(.v-md-editor-preview) {
  background: transparent !important;
  padding: 0 !important;
}

/* 响应式 */
@media (max-width: 768px) {
  .detail-header {
    flex-wrap: wrap;
    gap: 12px;
  }

  .detail-header h2 {
    order: -1;
    width: 100%;
  }

  .header-actions {
    width: 100%;
  }

  .info-row {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>
