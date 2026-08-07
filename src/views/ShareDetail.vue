<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getTypeLabel, getStatusLabel, getPriorityLabel } from '../api/task.js'
import { fetchShareTask } from '../api/share.js'
import FileList from '../components/FileList.vue'

const route = useRoute()
const todo = ref(null)
const shareFiles = ref([])
const loading = ref(true)
const error = ref('')

// 从 URL 获取分享参数
const apiHost = ref('')
const token = ref('')

function getTypeClass(type) {
  if (type === 'bug') return 'type-bug'
  if (type === 'story') return 'type-story'
  if (type === 'task') return 'type-task'
  if (type === 'problem') return 'type-problem'
  return ''
}

function formatDate(str) {
  if (!str) return ''
  return str.substring(0, 10)
}

function formatDateTime(str) {
  if (!str) return ''
  return str.substring(0, 10) + ' ' + str.substring(11, 19)
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

onMounted(async () => {
  const id = route.params.id
  token.value = route.query.token || ''
  apiHost.value = route.query.api || ''

  if (!apiHost.value || !token.value) {
    error.value = '分享链接参数不完整'
    loading.value = false
    return
  }

  try {
    const res = await fetchShareTask(apiHost.value, id, token.value)
    // axios 直接请求，res.data 是 { code, data: { task, files }, message }
    const body = res.data
    if (body.code === 200 && body.data) {
      todo.value = body.data.task
      shareFiles.value = body.data.files || []
    } else {
      error.value = body.message || '获取待办信息失败'
    }
  } catch (e) {
    const msg = e.response?.data?.message || e.message || '网络请求失败'
    error.value = msg
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="share-page">
    <!-- 加载中 -->
    <div class="share-container" v-if="loading">
      <div class="share-loading">加载中...</div>
    </div>

    <!-- 错误 -->
    <div class="share-container" v-else-if="error">
      <div class="share-error">
        <div class="error-icon">📭</div>
        <p>{{ error }}</p>
      </div>
    </div>

    <!-- 详情 -->
    <div class="share-container" v-else-if="todo">
      <div class="share-card">
        <!-- 头部 -->
        <div class="share-header">
          <div class="share-header-top">
            <span :class="['type-tag', getTypeClass(todo.type)]">{{ getTypeLabel(todo.type) }}</span>
            <span :class="['priority-tag', getPriorityClass(todo.priority)]">{{ getPriorityLabel(todo.priority) }}</span>
            <span :class="['status-tag', getStatusClass(todo.status)]">{{ getStatusLabel(todo.status) }}</span>
          </div>
          <h1 class="share-title">#{{ todo.id }} {{ todo.title }}</h1>
        </div>

        <!-- 元信息 -->
        <div class="share-meta">
          <div class="meta-item">
            <span class="meta-label">负责人</span>
            <span class="meta-value">{{ todo.assignedTo }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">截止日期</span>
            <span class="meta-value">{{ formatDate(todo.deadline) }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">所属项目</span>
            <span class="meta-value">{{ todo.project }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">所属模块</span>
            <span class="meta-value">{{ todo.module }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">创建人</span>
            <span class="meta-value">{{ todo.createdBy }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">创建时间</span>
            <span class="meta-value">{{ formatDateTime(todo.createTime) }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">更新时间</span>
            <span class="meta-value">{{ formatDateTime(todo.updateTime) }}</span>
          </div>
        </div>

        <!-- 进度 -->
        <div class="share-progress">
          <span class="progress-label">完成进度</span>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: todo.progress + '%' }"></div>
            <span class="progress-text">{{ todo.progress }}%</span>
          </div>
        </div>

        <!-- 描述 -->
        <div class="share-desc">
          <h3>详细描述</h3>
          <div class="share-desc-content">
            <v-md-preview :text="todo.desc || '暂无描述'" />
          </div>
        </div>

        <!-- 附件 -->
        <div class="share-files">
          <FileList :task-id="todo.id" :api-host="apiHost" :token="token" :files="shareFiles" />
        </div>

        <!-- 底部 -->
        <div class="share-footer">
          <p class="footer-brand">📋 待办系统</p>
          <p class="footer-hint">此页面为公开分享链接，任何人可查看</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.share-page {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 40px 20px;
}

.share-container {
  max-width: 80%;
  margin: 0 auto;
}

.share-loading {
  text-align: center;
  padding: 80px 20px;
  color: #999;
  font-size: 16px;
}

.share-error {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.error-icon {
  font-size: 56px;
  margin-bottom: 16px;
}

.share-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.share-header {
  padding: 32px 32px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.share-header-top {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.share-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.4;
}

.share-meta {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0;
  padding: 0 32px;
}

.meta-item {
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}

.meta-item:nth-last-child(-n+3) {
  border-bottom: none;
}

.meta-label {
  font-size: 12px;
  color: #999;
  display: block;
  margin-bottom: 4px;
}

.meta-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.share-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 32px;
  border-bottom: 1px solid #f0f0f0;
}

.progress-label {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
}

.share-desc {
  padding: 24px 32px;
  border-bottom: 1px solid #f0f0f0;
}

.share-desc h3 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
}

.share-desc-content {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  font-size: 14px;
  color: #555;
  line-height: 1.8;
  white-space: pre-wrap;
  border: 1px solid #f0f0f0;
}

.share-footer {
  text-align: center;
  padding: 24px 32px;
}

.footer-brand {
  font-size: 14px;
  font-weight: 600;
  color: #1a73e8;
}

.footer-hint {
  font-size: 12px;
  color: #bbb;
  margin-top: 6px;
}

@media (max-width: 600px) {
  .share-meta {
    grid-template-columns: repeat(2, 1fr);
  }

  .share-header,
  .share-meta,
  .share-progress,
  .share-desc,
  .share-footer {
    padding-left: 20px;
    padding-right: 20px;
  }

  .share-title {
    font-size: 18px;
  }
}
</style>
