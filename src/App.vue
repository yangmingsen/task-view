<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import {
  fetchTodos,
  fetchTodoById,
  createTodo,
  updateTodo,
  deleteTodo,
  getTypeLabel,
  getStatusLabel,
  getPriorityLabel,
} from './mock/data.js'

// ---------- 列表数据 ----------
const list = ref([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = 20

// ---------- 搜索 & 筛选 ----------
const keyword = ref('')
const filterType = ref('')
const filterStatus = ref('')
const searchType = ref('')   // 顶部搜索框的类型下拉

const activeTab = ref('all') // all / wait / doing / done / closed / story / bug / task

// ---------- 弹窗控制 ----------
const detailVisible = ref(false)
const editVisible = ref(false)
const currentTodo = ref(null)
const editForm = ref(null)
const isEdit = ref(false)
const saving = ref(false)

// ---------- 删除确认 ----------
const deleteConfirmVisible = ref(false)
const deleteTargetId = ref(null)

// ---------- 常量 ----------
const tabs = [
  { key: 'all', label: '全部' },
  { key: 'wait', label: '未开始' },
  { key: 'doing', label: '进行中' },
  { key: 'done', label: '已完成' },
  { key: 'closed', label: '已关闭' },
  { key: 'story', label: '需求' },
  { key: 'bug', label: 'Bug' },
  { key: 'task', label: '任务' },
]

const typeOptions = [
  { value: '', label: '所有类型' },
  { value: 'story', label: '需求' },
  { value: 'bug', label: 'Bug' },
  { value: 'task', label: '任务' },
]

const statusOptions = [
  { value: '', label: '所有状态' },
  { value: 'wait', label: '未开始' },
  { value: 'doing', label: '进行中' },
  { value: 'done', label: '已完成' },
  { value: 'closed', label: '已关闭' },
]

const priorityOptions = [
  { value: 1, label: '紧急' },
  { value: 2, label: '高' },
  { value: 3, label: '中' },
  { value: 4, label: '低' },
]

// ---------- 加载列表 ----------
async function loadList() {
  loading.value = true
  try {
    let type = filterType.value
    let status = filterStatus.value

    const tab = activeTab.value
    if (tab === 'story' || tab === 'bug' || tab === 'task') {
      type = tab
    } else if (tab === 'wait' || tab === 'doing' || tab === 'done' || tab === 'closed') {
      status = tab
    }

    const res = await fetchTodos({
      keyword: keyword.value,
      type,
      status,
      page: currentPage.value,
      pageSize,
    })
    list.value = res.items
    total.value = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// ---------- 切换 tab ----------
function switchTab(tab) {
  activeTab.value = tab
  filterType.value = ''
  filterStatus.value = ''
  currentPage.value = 1
  loadList()
}

// ---------- 搜索 ----------
function handleSearch() {
  currentPage.value = 1
  // 重置 tab 避免冲突
  if (searchType.value) {
    activeTab.value = 'all'
    filterType.value = ''
    filterStatus.value = ''
  }
  loadList()
}

function handleKeydown(e) {
  if (e.key === 'Enter') handleSearch()
}

// ---------- 查看详情 ----------
async function viewDetail(todo) {
  try {
    currentTodo.value = await fetchTodoById(todo.id)
    detailVisible.value = true
  } catch (e) {
    alert('获取详情失败: ' + e.message)
  }
}

// ---------- 编辑 ----------
function openEdit(todo) {
  isEdit.value = true
  editForm.value = {
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
  editVisible.value = true
}

// ---------- 新建 ----------
function openCreate() {
  isEdit.value = false
  editForm.value = {
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
  }
  editVisible.value = true
}

// ---------- 保存（新建/编辑） ----------
async function handleSave() {
  const form = editForm.value
  if (!form.title.trim()) {
    alert('请输入标题')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateTodo(form.id, { ...form })
    } else {
      await createTodo({ ...form })
    }
    editVisible.value = false
    loadList()
  } catch (e) {
    alert('保存失败: ' + e.message)
  } finally {
    saving.value = false
  }
}

// ---------- 删除 ----------
function confirmDelete(id) {
  deleteTargetId.value = id
  deleteConfirmVisible.value = true
}

async function handleDelete() {
  try {
    await deleteTodo(deleteTargetId.value)
    deleteConfirmVisible.value = false
    detailVisible.value = false
    loadList()
  } catch (e) {
    alert('删除失败: ' + e.message)
  }
}

// ---------- 工具方法 ----------
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

// ---------- 分页 ----------
const totalPages = computed(() => Math.ceil(total.value / pageSize))

function goPage(p) {
  if (p < 1 || p > totalPages.value) return
  currentPage.value = p
  loadList()
}

onMounted(() => {
  loadList()
})
</script>

<template>
  <div class="app-container">
    <!-- ========== 顶部导航 ========== -->
    <header class="header">
      <div class="header-left">
        <span class="logo">📋 待办系统</span>
      </div>
      <div class="header-right">
        <span class="user-avatar">👤</span>
        <span class="user-name">管理员</span>
      </div>
    </header>

    <!-- ========== 搜索栏 ========== -->
    <div class="search-bar">
      <div class="search-input-group">
        <select v-model="searchType" class="search-type-select">
          <option value="">全部</option>
          <option value="story">需求</option>
          <option value="bug">Bug</option>
          <option value="task">任务</option>
        </select>
        <input
          v-model="keyword"
          type="text"
          class="search-input"
          placeholder="搜索 ID、标题、负责人..."
          @keydown="handleKeydown"
        />
        <button class="btn btn-search" @click="handleSearch">🔍 搜索</button>
      </div>
      <button class="btn btn-primary" @click="openCreate">+ 新建待办</button>
    </div>

    <!-- ========== 筛选 Tab ========== -->
    <div class="filter-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        :class="['tab-btn', { active: activeTab === tab.key }]"
        @click="switchTab(tab.key)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- ========== 待办表格 ========== -->
    <div class="table-container" v-if="!loading">
      <table class="todo-table" v-if="list.length > 0">
        <thead>
          <tr>
            <th style="width: 60px">ID</th>
            <th style="width: 70px">类型</th>
            <th style="width: 60px">优先级</th>
            <th>标题</th>
            <th style="width: 80px">状态</th>
            <th style="width: 80px">负责人</th>
            <th style="width: 110px">截止日期</th>
            <th style="width: 80px">进度</th>
            <th style="width: 130px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="todo in list"
            :key="todo.id"
            class="todo-row"
            @click="viewDetail(todo)"
          >
            <td class="cell-id">{{ todo.id }}</td>
            <td>
              <span :class="['type-tag', getTypeClass(todo.type)]">
                {{ getTypeLabel(todo.type) }}
              </span>
            </td>
            <td>
              <span :class="['priority-tag', getPriorityClass(todo.priority)]">
                {{ getPriorityLabel(todo.priority) }}
              </span>
            </td>
            <td class="cell-title">{{ todo.title }}</td>
            <td>
              <span :class="['status-tag', getStatusClass(todo.status)]">
                {{ getStatusLabel(todo.status) }}
              </span>
            </td>
            <td>{{ todo.assignedTo }}</td>
            <td class="cell-deadline" :class="{ overdue: todo.deadline < '2026-08-02' && todo.status !== 'done' && todo.status !== 'closed' }">
              {{ todo.deadline }}
            </td>
            <td>
              <div class="progress-bar-mini">
                <div class="progress-fill" :style="{ width: todo.progress + '%' }"></div>
              </div>
            </td>
            <td class="cell-actions" @click.stop>
              <button class="btn-sm btn-edit" @click="openEdit(todo)">编辑</button>
              <button class="btn-sm btn-delete" @click="confirmDelete(todo.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 空状态 -->
      <div class="empty-state" v-else>
        <div class="empty-icon">📭</div>
        <p>暂无待办数据</p>
        <button class="btn btn-primary" @click="openCreate">创建第一个待办</button>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <button :disabled="currentPage === 1" @click="goPage(currentPage - 1)">上一页</button>
        <span v-for="p in totalPages" :key="p">
          <button
            :class="{ active: p === currentPage }"
            @click="goPage(p)"
          >{{ p }}</button>
        </span>
        <button :disabled="currentPage === totalPages" @click="goPage(currentPage + 1)">下一页</button>
        <span class="page-info">共 {{ total }} 条</span>
      </div>
    </div>

    <!-- 加载中 -->
    <div class="loading-state" v-else>
      <p>加载中...</p>
    </div>

    <!-- ========== 详情弹窗 ========== -->
    <div class="modal-overlay" v-if="detailVisible" @click.self="detailVisible = false">
      <div class="modal modal-detail">
        <div class="modal-header">
          <h3>待办详情 #{{ currentTodo?.id }}</h3>
          <button class="modal-close" @click="detailVisible = false">✕</button>
        </div>
        <div class="modal-body" v-if="currentTodo">
          <div class="detail-grid">
            <div class="detail-item">
              <label>标题</label>
              <span class="detail-value title-value">{{ currentTodo.title }}</span>
            </div>
            <div class="detail-row">
              <div class="detail-item">
                <label>类型</label>
                <span :class="['type-tag', getTypeClass(currentTodo.type)]">
                  {{ getTypeLabel(currentTodo.type) }}
                </span>
              </div>
              <div class="detail-item">
                <label>优先级</label>
                <span :class="['priority-tag', getPriorityClass(currentTodo.priority)]">
                  {{ getPriorityLabel(currentTodo.priority) }}
                </span>
              </div>
              <div class="detail-item">
                <label>状态</label>
                <span :class="['status-tag', getStatusClass(currentTodo.status)]">
                  {{ getStatusLabel(currentTodo.status) }}
                </span>
              </div>
            </div>
            <div class="detail-row">
              <div class="detail-item">
                <label>负责人</label>
                <span class="detail-value">{{ currentTodo.assignedTo }}</span>
              </div>
              <div class="detail-item">
                <label>截止日期</label>
                <span class="detail-value">{{ currentTodo.deadline }}</span>
              </div>
              <div class="detail-item">
                <label>进度</label>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: currentTodo.progress + '%' }"></div>
                  <span class="progress-text">{{ currentTodo.progress }}%</span>
                </div>
              </div>
            </div>
            <div class="detail-row">
              <div class="detail-item">
                <label>所属项目</label>
                <span class="detail-value">{{ currentTodo.project }}</span>
              </div>
              <div class="detail-item">
                <label>所属模块</label>
                <span class="detail-value">{{ currentTodo.module }}</span>
              </div>
            </div>
            <div class="detail-row">
              <div class="detail-item">
                <label>创建人</label>
                <span class="detail-value">{{ currentTodo.createdBy }}</span>
              </div>
              <div class="detail-item">
                <label>创建日期</label>
                <span class="detail-value">{{ currentTodo.createdDate }}</span>
              </div>
            </div>
            <div class="detail-item">
              <label>描述</label>
              <div class="detail-desc">{{ currentTodo.desc || '暂无描述' }}</div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-edit" @click="openEdit(currentTodo); detailVisible = false">✏️ 编辑</button>
          <button class="btn btn-delete" @click="confirmDelete(currentTodo.id)">🗑️ 删除</button>
          <button class="btn btn-cancel" @click="detailVisible = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- ========== 编辑/新建弹窗 ========== -->
    <div class="modal-overlay" v-if="editVisible" @click.self="editVisible = false">
      <div class="modal modal-edit">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑待办' : '新建待办' }}</h3>
          <button class="modal-close" @click="editVisible = false">✕</button>
        </div>
        <div class="modal-body" v-if="editForm">
          <div class="form-grid">
            <div class="form-item">
              <label>标题 <span class="required">*</span></label>
              <input v-model="editForm.title" type="text" class="form-input" placeholder="请输入标题" />
            </div>
            <div class="form-row">
              <div class="form-item">
                <label>类型</label>
                <select v-model="editForm.type" class="form-select">
                  <option value="story">需求</option>
                  <option value="bug">Bug</option>
                  <option value="task">任务</option>
                </select>
              </div>
              <div class="form-item">
                <label>优先级</label>
                <select v-model.number="editForm.priority" class="form-select">
                  <option v-for="p in priorityOptions" :key="p.value" :value="p.value">{{ p.label }}</option>
                </select>
              </div>
              <div class="form-item">
                <label>状态</label>
                <select v-model="editForm.status" class="form-select">
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
                <input v-model="editForm.assignedTo" type="text" class="form-input" placeholder="请输入负责人" />
              </div>
              <div class="form-item">
                <label>截止日期</label>
                <input v-model="editForm.deadline" type="date" class="form-input" />
              </div>
              <div class="form-item">
                <label>进度 (%)</label>
                <input v-model.number="editForm.progress" type="number" min="0" max="100" class="form-input" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-item">
                <label>所属项目</label>
                <input v-model="editForm.project" type="text" class="form-input" placeholder="请输入项目名称" />
              </div>
              <div class="form-item">
                <label>所属模块</label>
                <input v-model="editForm.module" type="text" class="form-input" placeholder="请输入模块名称" />
              </div>
            </div>
            <div class="form-item">
              <label>描述</label>
              <textarea v-model="editForm.desc" class="form-textarea" rows="4" placeholder="请输入描述"></textarea>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="handleSave" :disabled="saving">
            {{ saving ? '保存中...' : '💾 保存' }}
          </button>
          <button class="btn btn-cancel" @click="editVisible = false">取消</button>
        </div>
      </div>
    </div>

    <!-- ========== 删除确认弹窗 ========== -->
    <div class="modal-overlay" v-if="deleteConfirmVisible" @click.self="deleteConfirmVisible = false">
      <div class="modal modal-confirm">
        <div class="modal-header">
          <h3>确认删除</h3>
          <button class="modal-close" @click="deleteConfirmVisible = false">✕</button>
        </div>
        <div class="modal-body">
          <p>确定要删除该待办吗？此操作不可撤销。</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-delete" @click="handleDelete">确认删除</button>
          <button class="btn btn-cancel" @click="deleteConfirmVisible = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
/* ========== 全局重置 ========== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', sans-serif;
  background: #f0f2f5;
  color: #333;
  line-height: 1.6;
}

#app {
  min-height: 100vh;
}

/* ========== 布局 ========== */
.app-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

/* ========== 顶部导航 ========== */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #e8e8e8;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  color: #1a73e8;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.user-avatar {
  font-size: 20px;
}

/* ========== 搜索栏 ========== */
.search-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.search-input-group {
  display: flex;
  align-items: center;
  flex: 1;
  max-width: 600px;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  overflow: hidden;
  transition: border-color 0.2s;
}

.search-input-group:focus-within {
  border-color: #1a73e8;
  box-shadow: 0 0 0 2px rgba(26, 115, 232, 0.1);
}

.search-type-select {
  border: none;
  outline: none;
  background: #fafafa;
  padding: 10px 12px;
  font-size: 14px;
  color: #555;
  border-right: 1px solid #d9d9d9;
  cursor: pointer;
  min-width: 80px;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 10px 14px;
  font-size: 14px;
}

.btn-search {
  border: none;
  background: #1a73e8;
  color: #fff;
  padding: 10px 20px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-search:hover {
  background: #1557b0;
}

/* ========== 按钮通用 ========== */
.btn {
  padding: 10px 20px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
  color: #333;
  white-space: nowrap;
}

.btn:hover {
  border-color: #1a73e8;
  color: #1a73e8;
}

.btn-primary {
  background: #1a73e8;
  color: #fff;
  border-color: #1a73e8;
}

.btn-primary:hover {
  background: #1557b0;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-edit {
  background: #fff7e6;
  color: #d48806;
  border-color: #ffd591;
}

.btn-edit:hover {
  background: #ffe7ba;
}

.btn-delete {
  background: #fff1f0;
  color: #cf1322;
  border-color: #ffa39e;
}

.btn-delete:hover {
  background: #ffe0de;
}

.btn-cancel {
  background: #fafafa;
  color: #666;
}

.btn-cancel:hover {
  background: #f0f0f0;
}

/* ========== 筛选 Tab ========== */
.filter-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
  background: #fff;
  padding: 6px;
  border-radius: 10px;
  border: 1px solid #e8e8e8;
}

.tab-btn {
  padding: 8px 18px;
  border: none;
  background: transparent;
  color: #666;
  font-size: 14px;
  border-radius: 7px;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  background: #f0f5ff;
  color: #1a73e8;
}

.tab-btn.active {
  background: #1a73e8;
  color: #fff;
}

/* ========== 表格 ========== */
.table-container {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  overflow: hidden;
}

.todo-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.todo-table thead {
  background: #fafafa;
}

.todo-table th {
  padding: 14px 16px;
  text-align: left;
  font-weight: 600;
  color: #555;
  border-bottom: 1px solid #e8e8e8;
  white-space: nowrap;
}

.todo-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  vertical-align: middle;
}

.todo-row {
  cursor: pointer;
  transition: background 0.15s;
}

.todo-row:hover {
  background: #f0f5ff;
}

.cell-id {
  color: #999;
  font-size: 13px;
}

.cell-title {
  font-weight: 500;
}

.cell-deadline.overdue {
  color: #cf1322;
  font-weight: 500;
}

.cell-actions {
  display: flex;
  gap: 6px;
}

.btn-sm {
  padding: 4px 12px;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  background: #fff;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-sm.btn-edit:hover {
  border-color: #1a73e8;
  color: #1a73e8;
}

.btn-sm.btn-delete:hover {
  border-color: #cf1322;
  color: #cf1322;
}

/* ========== 标签 ========== */
.type-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.type-story {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.type-bug {
  background: #fff1f0;
  color: #cf1322;
  border: 1px solid #ffa39e;
}

.type-task {
  background: #f6ffed;
  color: #389e0d;
  border: 1px solid #b7eb8f;
}

.priority-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.pri-urgent {
  background: #fff1f0;
  color: #cf1322;
}

.pri-high {
  background: #fff7e6;
  color: #d48806;
}

.pri-mid {
  background: #e6f7ff;
  color: #1890ff;
}

.pri-low {
  background: #f5f5f5;
  color: #999;
}

.status-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-wait {
  background: #f5f5f5;
  color: #999;
  border: 1px solid #d9d9d9;
}

.status-doing {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.status-done {
  background: #f6ffed;
  color: #389e0d;
  border: 1px solid #b7eb8f;
}

.status-closed {
  background: #f0f0f0;
  color: #8c8c8c;
  border: 1px solid #d9d9d9;
  text-decoration: line-through;
}

/* ========== 进度条 ========== */
.progress-bar-mini {
  width: 60px;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar-mini .progress-fill {
  height: 100%;
  background: #52c41a;
  border-radius: 3px;
  transition: width 0.3s;
}

.progress-bar {
  width: 120px;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  display: inline-flex;
  align-items: center;
}

.progress-bar .progress-fill {
  height: 100%;
  background: #52c41a;
  border-radius: 4px;
  transition: width 0.3s;
}

.progress-text {
  position: absolute;
  right: -45px;
  font-size: 12px;
  color: #666;
}

/* ========== 空状态 ========== */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.empty-icon {
  font-size: 56px;
  margin-bottom: 16px;
}

.empty-state p {
  margin-bottom: 20px;
  font-size: 16px;
}

/* ========== 加载中 ========== */
.loading-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
  font-size: 16px;
}

/* ========== 分页 ========== */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 20px 16px;
}

.pagination button {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}

.pagination button:hover:not(:disabled) {
  border-color: #1a73e8;
  color: #1a73e8;
}

.pagination button.active {
  background: #1a73e8;
  color: #fff;
  border-color: #1a73e8;
}

.pagination button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  margin-left: 12px;
  color: #999;
  font-size: 13px;
}

/* ========== 弹窗 ========== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  width: 100%;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
}

.modal-detail {
  max-width: 720px;
}

.modal-edit {
  max-width: 680px;
}

.modal-confirm {
  max-width: 420px;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  font-size: 17px;
  font-weight: 600;
}

.modal-close {
  border: none;
  background: transparent;
  font-size: 20px;
  color: #999;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.15s;
}

.modal-close:hover {
  background: #f5f5f5;
  color: #333;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.modal-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

/* ========== 详情 ========== */
.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item label {
  font-size: 12px;
  color: #999;
  font-weight: 500;
}

.detail-value {
  font-size: 14px;
  color: #333;
}

.title-value {
  font-size: 16px;
  font-weight: 600;
}

.detail-desc {
  margin-top: 4px;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  font-size: 14px;
  color: #555;
  line-height: 1.7;
  white-space: pre-wrap;
  border: 1px solid #f0f0f0;
}

/* ========== 表单 ========== */
.form-grid {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
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

.required {
  color: #cf1322;
}

.form-input,
.form-select,
.form-textarea {
  padding: 9px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  border-color: #1a73e8;
  box-shadow: 0 0 0 2px rgba(26, 115, 232, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
  }

  .search-input-group {
    max-width: 100%;
  }

  .filter-tabs {
    flex-wrap: wrap;
  }

  .todo-table {
    font-size: 12px;
  }

  .todo-table th,
  .todo-table td {
    padding: 10px 8px;
  }

  .detail-row,
  .form-row {
    grid-template-columns: 1fr;
  }

  .modal {
    max-height: 95vh;
    margin: 10px;
  }
}
</style>
