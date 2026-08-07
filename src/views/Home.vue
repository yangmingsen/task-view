<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import {
  fetchTodos,
  updateTodo,
  getTypeLabel,
  getStatusLabel,
  getPriorityLabel,
} from '../api/task.js'
//} from '@/mock/data.js'
import GlobalSearch from '../components/GlobalSearch.vue'

const router = useRouter()

/* ========== 全局搜索 ========== */
const searchRef = ref(null)

function onGlobalKeydown(e) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    searchRef.value?.open()
  }
}

onMounted(() => {
  window.addEventListener('keydown', onGlobalKeydown)
  loadFilters()
  loadList()
})

let restoring = false

function loadFilters() {
  restoring = true
  try {
    const saved = JSON.parse(localStorage.getItem(FILTER_KEY))
    if (saved) {
      keyword.value = saved.keyword || ''
      filterType.value = saved.filterType || ''
      filterStatus.value = saved.filterStatus || ''
      currentPage.value = saved.currentPage || 1
    }
  } catch { /* ignore */ }
  nextTick(() => { restoring = false })
}

onUnmounted(() => {
  window.removeEventListener('keydown', onGlobalKeydown)
})

/* ========== 搜索 & 筛选 ========== */
const FILTER_KEY = 'home_filters'

function saveFilters() {
  localStorage.setItem(FILTER_KEY, JSON.stringify({
    keyword: keyword.value,
    filterType: filterType.value,
    filterStatus: filterStatus.value,
    currentPage: currentPage.value,
  }))
}

const keyword = ref('')
const filterType = ref('')
const filterStatus = ref('')

const statusTabs = [
  { key: '', label: '全部' },
  { key: 'wait', label: '未开始' },
  { key: 'doing', label: '进行中' },
  { key: 'done', label: '已完成' },
  { key: 'closed', label: '已关闭' },
]

const typeTabs = [
  { key: '', label: '全部类型' },
  { key: 'story', label: '需求' },
  { key: 'bug', label: 'Bug' },
  { key: 'task', label: '任务' },
  { key: 'problem', label: '问题' },
]

/* ========== 列表 & 分页 ========== */
const list = ref([])
const loading = ref(true)
const currentPage = ref(1)
const total = ref(0)
const totalPages = ref(0)
const pageSize = 10

async function loadList() {
  loading.value = true
  try {
    const res = await fetchTodos({
      keyword: keyword.value,
      type: filterType.value,
      status: filterStatus.value,
      page: currentPage.value,
      pageSize,
    })
    list.value = res.items
    total.value = res.total
    totalPages.value = res.totalPages
  } catch (e) {
    console.error('加载失败:', e)
  } finally {
    loading.value = false
  }
}

function onSearch() {
  currentPage.value = 1
  loadList()
}

watch([filterStatus, filterType, keyword, currentPage], () => {
  saveFilters()
})

watch([filterStatus, filterType], () => {
  if (!restoring) currentPage.value = 1
  loadList()
})

/* ========== 导航 ========== */
function goCreate() {
  router.push('/task/new')
}

function goDetail(todo) {
  router.push(`/task/${todo.id}`)
}

function goEdit(todo) {
  router.push(`/task/${todo.id}/edit`)
}

async function handleStart(todo) {
  try {
    // eslint-disable-next-line no-unused-vars
    const { desc, ...data } = todo
    await updateTodo(todo.id, { ...data, status: 'doing' })
    loadList()
  } catch (e) {
    alert('操作失败: ' + e.message)
  }
}

async function handleComplete(todo) {
  try {
    // eslint-disable-next-line no-unused-vars
    const { desc, ...data } = todo
    await updateTodo(todo.id, { ...data, status: 'done', progress: 100 })
    loadList()
  } catch (e) {
    alert('操作失败: ' + e.message)
  }
}

async function handleClose(todo) {
  try {
    // eslint-disable-next-line no-unused-vars
    const { desc, ...data } = todo
    await updateTodo(todo.id, { ...data, status: 'closed' })
    loadList()
  } catch (e) {
    alert('操作失败: ' + e.message)
  }
}

function goPage(page) {
  currentPage.value = page
  loadList()
}

/* ========== 辅助 ========== */
function getTypeClass(type) {
  if (type === 'bug') return 'type-bug'
  if (type === 'story') return 'type-story'
  if (type === 'task') return 'type-task'
  if (type === 'problem') return 'type-problem'
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

function handleLogout() {
  localStorage.clear()
  router.push('/login')
}

function formatDeadline(dateStr) {
  if (!dateStr) return ''
  return dateStr.substring(0, 10)
}


</script>

<template>
  <div class="app-container">
    <!-- ========== 顶部导航 ========== -->
    <header class="header">
      <h1 class="logo">📋 待办系统</h1>
      <button class="btn-logout" @click="handleLogout">退出</button>
    </header>

    <!-- ========== 搜索栏 ========== -->
    <div class="search-bar">
      <div class="search-left">
        <div class="search-input-wrap">
          <input
            v-model="keyword"
            type="text"
            class="search-input"
            placeholder="搜索 ID、标题、负责人..."
            @keyup.enter="onSearch"
          />
          <button class="search-btn" @click="onSearch">🔍</button>
        </div>
        <select v-model="filterType" class="filter-select">
          <option v-for="t in typeTabs" :key="t.key" :value="t.key">{{ t.label }}</option>
        </select>
      </div>
      <button class="btn btn-primary" @click="goCreate">+ 新建待办</button>
    </div>

    <!-- ========== 筛选 Tab ========== -->
    <div class="filter-tabs">
      <button
        v-for="tab in statusTabs"
        :key="tab.key"
        :class="['tab-btn', { active: filterStatus === tab.key }]"
        @click="filterStatus = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- ========== 待办表格 ========== -->
    <div class="table-wrapper" v-if="!loading">
      <template v-if="list.length > 0">
        <div class="table-scroll">
          <table class="todo-table">
            <thead>
              <tr>
                <th style="width: 60px">ID</th>
<th style="width: 80px">类型</th>
<th style="width: 80px">优先级</th>
<th>标题</th>
<th style="width: 90px">状态</th>
                <th style="width: 80px">负责人</th>
                <th style="width: 120px">截止日期</th>
                <th style="width: 80px">进度</th>
                <th style="width: 260px">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="todo in list"
                :key="todo.id"
                class="todo-row"
                @click="goDetail(todo)"
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
                <td class="cell-deadline" :class="{ overdue: formatDeadline(todo.deadline) < new Date().toISOString().substring(0, 10) && todo.status !== 'done' && todo.status !== 'closed' }">
                  {{ formatDeadline(todo.deadline) }}
                </td>
                <td>
                  <div class="progress-bar-mini">
                    <div class="progress-fill" :style="{ width: todo.progress + '%' }"></div>
                  </div>
                </td>
                <td class="cell-actions" @click.stop>
                  <button class="btn-sm btn-edit" @click="goEdit(todo)">编辑</button>
                  <button v-if="todo.status === 'wait'" class="btn-sm btn-start" @click="handleStart(todo)">开始</button>
                  <button v-if="todo.status === 'wait' || todo.status === 'doing'" class="btn-sm btn-done" @click="handleComplete(todo)">完成</button>
                  <button v-if="todo.status === 'wait' || todo.status === 'doing' || todo.status === 'done'" class="btn-sm btn-close" @click="handleClose(todo)">关闭</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination" v-if="totalPages > 1">
          <button :disabled="currentPage === 1" @click="goPage(currentPage - 1)">上一页</button>
          <span v-for="p in totalPages" :key="p">
            <button :class="{ active: p === currentPage }" @click="goPage(p)">{{ p }}</button>
          </span>
          <button :disabled="currentPage === totalPages" @click="goPage(currentPage + 1)">下一页</button>
          <span class="page-info">共 {{ total }} 条</span>
        </div>
      </template>

      <div class="empty-state" v-else>
        <div class="empty-icon">📭</div>
        <p>暂无待办数据</p>
        <button class="btn btn-primary" @click="goCreate">创建第一个待办</button>
      </div>
    </div>

    <div class="loading-state" v-else>加载中...</div>

    <!-- 全局搜索 -->
    <GlobalSearch ref="searchRef" />
  </div>
</template>

<style scoped>
/* ========== 布局 ========== */
.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px;
  overflow: hidden;
}

/* ========== 顶部导航 ========== */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  color: #1a73e8;
}

.btn-logout {
  border: 1px solid #d9d9d9;
  background: #fff;
  color: #666;
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-logout:hover {
  border-color: #cf1322;
  color: #cf1322;
}

/* ========== 搜索栏 ========== */
.search-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 0;
  flex-shrink: 0;
}

.search-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  overflow: hidden;
  transition: border-color 0.2s;
}

.search-input-wrap:focus-within {
  border-color: #1a73e8;
}

.search-input {
  border: none;
  outline: none;
  padding: 8px 12px;
  width: 260px;
  font-size: 14px;
}

.search-btn {
  border: none;
  background: #f0f0f0;
  padding: 8px 14px;
  cursor: pointer;
  font-size: 14px;
}

.search-btn:hover {
  background: #e0e0e0;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 13px;
  background: #fff;
  cursor: pointer;
  outline: none;
}

/* ========== 筛选 Tab ========== */
.filter-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 12px;
  background: #fff;
  padding: 6px;
  border-radius: 10px;
  border: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.tab-btn {
  border: none;
  background: transparent;
  padding: 7px 16px;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  color: #1a73e8;
  background: #f0f5ff;
}

.tab-btn.active {
  background: #1a73e8;
  color: #fff;
}

/* ========== 表格整体容器 ========== */
.table-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  overflow: hidden;
  min-height: 0;
}

/* 表格数据滚动区 */
.table-scroll {
  flex: 1;
  overflow-y: auto;
  overflow-x: auto;
  min-height: 0;
}

.todo-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  table-layout: fixed;
}

.todo-table thead {
  background: #fafafa;
  position: sticky;
  top: 0;
  z-index: 2;
}

.todo-table thead::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  border-bottom: 1px solid #e8e8e8;
}

.todo-table th {
  padding: 14px 16px;
  text-align: left;
  font-weight: 600;
  color: #555;
  white-space: nowrap;
  background: #fafafa;
}

.todo-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  vertical-align: middle;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cell-deadline.overdue {
  color: #cf1322;
  font-weight: 500;
}

.cell-actions {
  display: flex;
  gap: 6px;
}

.btn-start {
  color: #fa8c16;
  border-color: #ffd591;
}
.btn-start:hover { border-color: #fa8c16; color: #fa8c16; background: #fff7e6; }

.btn-done {
  color: #52c41a;
  border-color: #b7eb8f;
}
.btn-done:hover { border-color: #52c41a; color: #52c41a; background: #f6ffed; }

.btn-close {
  color: #999;
  border-color: #d9d9d9;
}
.btn-close:hover { border-color: #999; color: #666; background: #fafafa; }

/* ========== 空状态 & 加载 ========== */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 40px 20px;
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

.loading-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 16px;
}

/* ========== 分页 ========== */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 14px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
  flex-shrink: 0;
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
</style>
