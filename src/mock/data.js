// 模拟待办数据
let todos = [
  {
    id: 1,
    title: '用户登录页面响应式适配',
    type: 'story',
    priority: 2,
    status: 'doing',
    assignedTo: '张三',
    deadline: '2026-08-15',
    desc: '需要针对移动端和小屏幕设备进行登录页面的响应式适配，确保在 320px-1920px 宽度下都能正常显示。',
    project: '前端项目',
    module: '用户模块',
    createdBy: '李四',
    createdDate: '2026-07-20',
    progress: 60,
  },
  {
    id: 2,
    title: '修复订单列表分页 BUG',
    type: 'bug',
    priority: 1,
    status: 'wait',
    assignedTo: '王五',
    deadline: '2026-08-10',
    desc: '订单列表切换到第3页后，返回第1页数据不刷新，且分页组件的页码显示异常。',
    project: '后端项目',
    module: '订单模块',
    createdBy: '赵六',
    createdDate: '2026-07-28',
    progress: 0,
  },
  {
    id: 3,
    title: '用户权限管理接口开发',
    type: 'task',
    priority: 3,
    status: 'doing',
    assignedTo: '张三',
    deadline: '2026-08-20',
    desc: '开发用户角色权限 CRUD 接口，包括角色创建、权限分配、角色列表查询等功能。需要支持 RBAC 模型。',
    project: '后端项目',
    module: '权限模块',
    createdBy: '李四',
    createdDate: '2026-07-25',
    progress: 35,
  },
  {
    id: 4,
    title: '数据库慢查询优化',
    type: 'task',
    priority: 1,
    status: 'wait',
    assignedTo: '赵六',
    deadline: '2026-08-08',
    desc: '针对当前系统中超过 500ms 的慢查询进行分析和优化，主要涉及订单统计和报表查询。期望优化到 200ms 以内。',
    project: '后端项目',
    module: '数据库',
    createdBy: '李四',
    createdDate: '2026-07-30',
    progress: 0,
  },
  {
    id: 5,
    title: '首页数据看板 UI 设计',
    type: 'story',
    priority: 2,
    status: 'done',
    assignedTo: '钱七',
    deadline: '2026-07-31',
    desc: '设计首页数据看板界面，包含关键指标卡片、趋势图、团队任务分布图等。',
    project: '前端项目',
    module: '首页',
    createdBy: '李四',
    createdDate: '2026-07-15',
    progress: 100,
  },
  {
    id: 6,
    title: '导出 PDF 功能异常',
    type: 'bug',
    priority: 2,
    status: 'doing',
    assignedTo: '孙八',
    deadline: '2026-08-12',
    desc: '在报告页面点击导出 PDF，中文内容出现乱码，且表格边框丢失。',
    project: '前端项目',
    module: '报告模块',
    createdBy: '周九',
    createdDate: '2026-08-01',
    progress: 40,
  },
  {
    id: 7,
    title: '消息通知中心重构',
    type: 'story',
    priority: 3,
    status: 'wait',
    assignedTo: '吴十',
    deadline: '2026-08-25',
    desc: '重构现有消息通知中心，支持消息分类、已读未读标记、消息推送设置等功能。',
    project: '前端项目',
    module: '消息模块',
    createdBy: '李四',
    createdDate: '2026-07-28',
    progress: 0,
  },
  {
    id: 8,
    title: 'API 接口文档自动生成',
    type: 'task',
    priority: 3,
    status: 'closed',
    assignedTo: '张三',
    deadline: '2026-07-30',
    desc: '接入 Swagger/OpenAPI，实现 API 接口文档的自动生成和在线预览。',
    project: '后端项目',
    module: '基础设施',
    createdBy: '李四',
    createdDate: '2026-07-10',
    progress: 100,
  },
  {
    id: 9,
    title: '上传组件大文件分片上传',
    type: 'story',
    priority: 2,
    status: 'doing',
    assignedTo: '孙八',
    deadline: '2026-08-18',
    desc: '实现文件分片上传功能，支持断点续传和并发上传，单文件大小上限提升至 2GB。',
    project: '前端项目',
    module: '组件库',
    createdBy: '李四',
    createdDate: '2026-07-22',
    progress: 50,
  },
  {
    id: 10,
    title: '登录接口增加验证码校验',
    type: 'task',
    priority: 1,
    status: 'wait',
    assignedTo: '赵六',
    deadline: '2026-08-05',
    desc: '在登录接口增加图形验证码校验逻辑，防止暴力破解。需对接第三方验证码服务。',
    project: '后端项目',
    module: '用户模块',
    createdBy: '李四',
    createdDate: '2026-08-01',
    progress: 0,
  },
]

// 类型映射
const typeMap = {
  story: '需求',
  bug: 'Bug',
  task: '任务',
}

// 状态映射
const statusMap = {
  wait: '未开始',
  doing: '进行中',
  done: '已完成',
  closed: '已关闭',
}

// 优先级映射
const priorityMap = {
  1: '紧急',
  2: '高',
  3: '中',
  4: '低',
}

// 模拟 API 延迟
function delay(ms = 300) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

let nextId = todos.length + 1

// ---------- Mock API ----------

// 获取待办列表（支持搜索、类型筛选、状态筛选）
export async function fetchTodos({ keyword = '', type = '', status = '', page = 1, pageSize = 20 } = {}) {
  await delay()
  let list = [...todos]

  if (keyword) {
    const kw = keyword.toLowerCase()
    list = list.filter(
      (t) =>
        t.title.toLowerCase().includes(kw) ||
        t.assignedTo.toLowerCase().includes(kw) ||
        t.id.toString().includes(kw),
    )
  }

  if (type) {
    list = list.filter((t) => t.type === type)
  }

  if (status) {
    list = list.filter((t) => t.status === status)
  }

  const total = list.length
  const start = (page - 1) * pageSize
  const items = list.slice(start, start + pageSize)

  return {
    items,
    total,
    page,
    pageSize,
    totalPages: Math.ceil(total / pageSize),
  }
}

// 获取单个待办详情
export async function fetchTodoById(id) {
  await delay()
  const todo = todos.find((t) => t.id === id)
  if (!todo) {
    throw new Error('待办不存在')
  }
  return { ...todo }
}

// 创建待办
export async function createTodo(data) {
  await delay()
  const newTodo = {
    id: nextId++,
    ...data,
    createdDate: new Date().toISOString().split('T')[0],
    progress: 0,
  }
  todos.unshift(newTodo)
  return { ...newTodo }
}

// 更新待办
export async function updateTodo(id, data) {
  await delay()
  const index = todos.findIndex((t) => t.id === id)
  if (index === -1) throw new Error('待办不存在')
  todos[index] = { ...todos[index], ...data }
  return { ...todos[index] }
}

// 删除待办
export async function deleteTodo(id) {
  await delay()
  const index = todos.findIndex((t) => t.id === id)
  if (index === -1) throw new Error('待办不存在')
  todos.splice(index, 1)
}

// 获取类型、状态等字典值
export function getTypeLabel(type) {
  return typeMap[type] || type
}

export function getStatusLabel(status) {
  return statusMap[status] || status
}

export function getPriorityLabel(priority) {
  return priorityMap[priority] || priority
}

export { typeMap, statusMap, priorityMap }
