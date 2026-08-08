import request from './request'

/** 后端 TaskEntity 转前端 Todo 格式 */
function toTodo(backend) {
  return {
    ...backend,
    desc: backend.description ?? '',
  }
}

// ======================== 标签映射（纯常量，不依赖接口） ========================

export const typeMap = { story: '需求', bug: 'Bug', task: '任务', problem: '问题' }
export const statusMap = { wait: '未开始', doing: '进行中', done: '已完成', closed: '已关闭' }
export const priorityMap = { 1: '紧急', 2: '高', 3: '中', 4: '低' }

export function getTypeLabel(t) { return typeMap[t] || t }
export function getStatusLabel(s) { return statusMap[s] || s }
export function getPriorityLabel(p) { return priorityMap[p] || p }

// ======================== 分页列表 ========================

export async function fetchTodos({ keyword = '', type = '', status = '', project = '', module = '', page = 1, pageSize = 10 } = {}) {
  const params = { page, pageSize }
  if (keyword) params.keyword = keyword
  if (type) params.type = type
  if (status) params.status = status
  if (project) params.project = project
  if (module) params.module = module

  const res = await request.get('/tasks', { params })
  return {
    items: (res.items || []).map(toTodo),
    total: res.total,
    page: res.page,
    pageSize: res.pageSize,
    totalPages: res.totalPages,
  }
}

// ======================== 详情 ========================

export async function fetchTodoById(id) {
  const backend = await request.get(`/tasks/${id}`)
  return toTodo(backend)
}

// ======================== 新建 ========================

export async function createTodo(data) {
  const backend = { ...data }
  if (backend.desc !== undefined) {
    backend.description = backend.desc
    delete backend.desc
  }
  return await request.post('/tasks', backend)
}

// ======================== 更新 ========================

export async function updateTodo(id, data) {
  const backend = { ...data }
  if (backend.desc !== undefined) {
    backend.description = backend.desc
    delete backend.desc
  }
  return await request.put(`/tasks/${id}`, backend)
}

// ======================== 删除 ========================

export async function deleteTodo(id) {
  return await request.delete(`/tasks/${id}`)
}

// ======================== 全局搜索 ========================

export async function searchTodos(query = '') {
  if (!query.trim()) return []
  const results = await request.get('/tasks/search', { params: { q: query.trim() } })
  return (results || []).map((r) => ({
    ...r,
    desc: r.description ?? '',
  }))
}
