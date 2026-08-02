<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  fetchTodoById,
  createTodo,
  updateTodo,
} from '../api/task.js'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const pageTitle = computed(() => isEdit.value ? '编辑待办' : '新建待办')
const saving = ref(false)
const loading = ref(false)

const form = ref({
  title: '',
  type: 'task',
  priority: 3,
  status: 'wait',
  assignedTo: '',
  deadline: '',
  desc: '',
  project: '',
  module: '',
  progress: 0,
})

const priorityOptions = [
  { value: 1, label: '紧急' },
  { value: 2, label: '高' },
  { value: 3, label: '中' },
  { value: 4, label: '低' },
]

onMounted(async () => {
  if (isEdit.value) {
    loading.value = true
    try {
      const todo = await fetchTodoById(route.params.id)
      form.value = {
        id: todo.id,
        title: todo.title,
        type: todo.type,
        priority: todo.priority,
        status: todo.status,
        assignedTo: todo.assignedTo,
        deadline: todo.deadline,
        desc: todo.desc,
        project: todo.project,
        module: todo.module,
        progress: todo.progress,
      }
    } catch (e) {
      alert('获取待办失败: ' + e.message)
      router.push('/')
    } finally {
      loading.value = false
    }
  }
})

async function handleSave() {
  if (!form.value.title.trim()) {
    alert('请输入标题')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateTodo(form.value.id, { ...form.value })
    } else {
      await createTodo({ ...form.value })
    }
    router.push('/')
  } catch (e) {
    alert('保存失败: ' + e.message)
  } finally {
    saving.value = false
  }
}

function goBack() {
  if (isEdit.value) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>

<template>
  <div class="form-page">
    <!-- 顶栏 -->
    <header class="form-header">
      <button class="btn-back" @click="goBack">← 返回</button>
      <h2>{{ pageTitle }}</h2>
      <div class="header-actions">
        <button class="btn btn-primary" @click="handleSave" :disabled="saving">
          {{ saving ? '保存中...' : '💾 保存' }}
        </button>
        <button class="btn btn-cancel" @click="goBack">取消</button>
      </div>
    </header>

    <!-- 加载中 -->
    <div class="form-loading" v-if="loading">加载中...</div>

    <!-- 表单 -->
    <div class="form-content" v-else>
      <div class="form-card">
        <h3 class="card-title">基本信息</h3>
        <div class="form-grid">
          <div class="form-item form-item-full">
            <label>标题 <span class="required">*</span></label>
            <input v-model="form.title" type="text" class="form-input" placeholder="请输入标题" />
          </div>

          <div class="form-row">
            <div class="form-item">
              <label>类型</label>
              <select v-model="form.type" class="form-select">
                <option value="story">需求</option>
                <option value="bug">Bug</option>
                <option value="task">任务</option>
              </select>
            </div>
            <div class="form-item">
              <label>优先级</label>
              <select v-model.number="form.priority" class="form-select">
                <option v-for="p in priorityOptions" :key="p.value" :value="p.value">{{ p.label }}</option>
              </select>
            </div>
            <div class="form-item">
              <label>状态</label>
              <select v-model="form.status" class="form-select">
                <option value="wait">未开始</option>
                <option value="doing">进行中</option>
                <option value="done">已完成</option>
                <option value="closed">已关闭</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-item">
              <label>负责人</label>
              <input v-model="form.assignedTo" type="text" class="form-input" placeholder="请输入负责人" />
            </div>
            <div class="form-item">
              <label>截止日期</label>
              <input v-model="form.deadline" type="date" class="form-input" />
            </div>
            <div class="form-item">
              <label>进度 (%)</label>
              <input v-model.number="form.progress" type="number" min="0" max="100" class="form-input" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-item">
              <label>所属项目</label>
              <input v-model="form.project" type="text" class="form-input" placeholder="请输入项目名称" />
            </div>
            <div class="form-item">
              <label>所属模块</label>
              <input v-model="form.module" type="text" class="form-input" placeholder="请输入模块名称" />
            </div>
          </div>
        </div>
      </div>

      <div class="form-card desc-card">
        <h3 class="card-title">详细描述（支持 Markdown）</h3>
        <v-md-editor v-model="form.desc" height="500px" placeholder="请输入描述，支持 Markdown 语法"></v-md-editor>
      </div>
    </div>
  </div>
</template>

<style scoped>
.form-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 24px;
  overflow: hidden;
}

/* 顶栏 */
.form-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 0;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.form-header h2 {
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

/* 加载 */
.form-loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 16px;
}

/* 滚动内容区 */
.form-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px 0;
}

/* 表单卡片 */
.form-card {
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

.form-grid {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-item label {
  font-size: 13px;
  color: #555;
  font-weight: 500;
}

.form-item-full {
  grid-column: 1 / -1;
}

.required {
  color: #cf1322;
}

.form-input,
.form-select {
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
}

.form-input:focus,
.form-select:focus {
  border-color: #1a73e8;
  box-shadow: 0 0 0 2px rgba(26, 115, 232, 0.1);
}

/* 响应式 */
@media (max-width: 768px) {
  .form-header {
    flex-wrap: wrap;
    gap: 12px;
  }

  .form-header h2 {
    order: -1;
    width: 100%;
  }

  .header-actions {
    width: 100%;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 14px;
  }
}
</style>
