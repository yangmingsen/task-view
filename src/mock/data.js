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
    desc: `## 需求背景
需要针对移动端和小屏幕设备进行登录页面的响应式适配。

## 适配范围
- 手机端：320px - 480px
- 平板端：481px - 1024px
- 桌面端：1025px - 1920px

## 技术要求
- 使用 CSS Media Query 实现
- 保证在 320px-1920px 宽度下都能正常显示
- 登录表单在移动端需要占满宽度

## 设计稿
> 详见蓝湖设计稿 [链接]

## 验收标准
- [x] 手机端布局正常
- [ ] 平板端布局调整
- [ ] 桌面端测试通过`,
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
    desc: `## Bug 描述
订单列表切换到第3页后，返回第1页数据不刷新，且分页组件的页码显示异常。

## 复现步骤
1. 打开订单列表页面
2. 点击第3页
3. 点击"上一页"返回第1页
4. 发现第1页显示的数据仍是第3页的数据

## 期望结果
- 每次翻页正常刷新列表数据
- 分页组件页码状态正确

## 影响范围
\`\`\`
OrderList.vue
Pagination.vue
\`\`\`

## 截图
![bug截图](示例)`,
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
    desc: `## 任务描述
开发用户角色权限 CRUD 接口，支持 **RBAC** 模型。

## API 接口列表

### 角色管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/roles | 角色列表 |
| POST | /api/roles | 创建角色 |
| PUT | /api/roles/:id | 更新角色 |
| DELETE | /api/roles/:id | 删除角色 |

### 权限分配
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/roles/:id/permissions | 分配权限 |
| GET | /api/roles/:id/permissions | 查询权限 |

## 数据库设计
\`\`\`sql
CREATE TABLE roles (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
\`\`\`

## 注意事项
> ⚠️ 删除角色前需要检查该角色下是否有用户关联`,
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
    desc: `## 问题分析
当前系统中存在以下慢查询（> 500ms）：

1. **订单统计查询** - 平均 1200ms
2. **报表生成查询** - 平均 800ms
3. **用户活跃度统计** - 平均 650ms

## 优化方案

### 1. 订单统计查询
- 添加复合索引 \`(user_id, created_at, status)\`
- 考虑使用覆盖索引避免回表

### 2. 报表查询
- 引入物化视图或定时汇总表
- 非实时报表走缓存

### 3. 用户统计
- 使用 Redis 计数器实时统计
- 定时任务同步到 MySQL

## 期望结果
- 所有查询优化到 **200ms** 以内

## 参考
\`\`\`sql
-- 建议添加的索引
CREATE INDEX idx_order_user_time_status 
ON orders(user_id, created_at, status);
\`\`\``,
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
    desc: `## 设计目标
设计首页数据看板界面，作为系统入口的数据概览。

## 包含模块
- ✅ **关键指标卡片** - 今日订单数、新增用户、活跃用户、转化率
- ✅ **趋势图** - 近30天订单/用户增长趋势
- ✅ **团队任务分布图** - 各成员任务完成情况饼图
- ✅ **最新动态** - 最近10条操作日志

## 设计规范
- 配色：主色 \`#1a73e8\`，辅色 \`#52c41a\`
- 卡片间距：16px
- 圆角：8px

## 设计稿
> 已交付，详见附件

## 状态
已完成 ✅`,
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
    desc: `## Bug 描述
在报告页面点击导出 PDF：
1. **中文内容出现乱码**
2. **表格边框丢失**

## 环境信息
- 浏览器：Chrome 120+
- 操作系统：Windows 11
- 导出库：html2canvas + jspdf

## 原因分析
- 中文乱码：未引入中文字体文件
- 表格边框丢失：html2canvas 对 \`border-collapse: collapse\` 支持有问题

## 修复方向
\`\`\`javascript
// 方案1：引入中文字体
// 方案2：使用 @font-face 加载字体
// 方案3：修改表格边框实现方式
\`\`\``,
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
    desc: `## 重构目标
重构现有消息通知中心，提升用户体验。

## 功能列表
- [ ] 消息分类（系统通知 / 任务提醒 / @我的）
- [ ] 已读 / 未读标记
- [ ] 批量标记已读
- [ ] 消息推送设置（站内信 / 邮件 / 微信）
- [ ] 消息搜索

## 交互设计
1. 顶部展示未读消息数量角标
2. 点击展开消息列表浮窗
3. 支持无限滚动加载

## 技术方案
> 使用 WebSocket 实时推送 + 历史消息 API`,
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
    desc: `## 任务描述
接入 Swagger/OpenAPI，实现 API 接口文档的自动生成和在线预览。

## 技术选型
- **后端**：springdoc-openapi (Spring Boot)
- **前端**：Swagger UI

## 配置示例
\`\`\`java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API 文档")
                .version("1.0.0"));
    }
}
\`\`\`

## 完成情况
✅ 已完成并上线`,
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
    desc: `## 需求说明
实现文件分片上传功能，替代当前的单文件直传方案。

## 核心功能
1. **分片上传** - 将大文件切分为 5MB 的块上传
2. **断点续传** - 记录已上传分片，刷新后继续
3. **并发上传** - 同时上传 3 个分片
4. **文件大小上限** - 提升至 2GB

## 技术流程
\`\`\`
用户选择文件 → 计算文件hash → 分片 → 并发上传
→ 全部完成 → 通知后端合并 → 返回文件URL
\`\`\`

## API
\`\`\`javascript
POST /api/upload/chunk
{
  fileHash: "xxx",
  chunkIndex: 0,
  totalChunks: 10,
  file: FormData
}
\`\`\`

## 进度
- [x] 分片逻辑
- [x] 断点续传
- [ ] 并发控制（进行中）
- [ ] 合并接口`,
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
    desc: `## 背景
当前登录接口无验证码校验，存在被暴力破解的风险。

## 方案
接入第三方图形验证码服务，在登录接口增加校验逻辑。

## 实现步骤
1. 前端集成验证码组件
2. 后端新增验证码校验中间件
3. 登录流程调整：
   - 用户先获取验证码 \`GET /api/captcha\`
   - 登录时携带 \`captchaId\` + \`captchaCode\`

## 安全要求
> ⚠️ 验证码必须具有：
> - 有效期（5分钟）
> - 一次性使用
> - 防机器识别

## 测试用例
- [ ] 正确验证码 → 登录成功
- [ ] 错误验证码 → 提示"验证码错误"
- [ ] 过期验证码 → 提示"验证码已过期"
- [ ] 重复使用验证码 → 提示"验证码已使用"`,
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
