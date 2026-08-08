<template>
  <div class="option-manage">
    <div class="page-header">
      <h2 class="page-title">选项管理</h2>
      <span class="page-desc">维护「所属项目」和「所属模块」的下拉数据</span>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <select v-model="filterType" class="filter-select">
          <option value="">全部类型</option>
          <option value="project">所属项目</option>
          <option value="module">所属模块</option>
        </select>
      </div>
      <button class="btn btn-primary" @click="openAddDialog">+ 新增选项</button>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th style="width:80px">序号</th>
            <th>类型</th>
            <th>名称</th>
            <th>所属项目</th>
            <th style="width:100px">排序</th>
            <th style="width:160px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="filteredList.length === 0">
            <td colspan="6" class="empty-row">暂无数据</td>
          </tr>
          <tr v-for="(item, idx) in filteredList" :key="item.id">
            <td>{{ idx + 1 }}</td>
            <td>
              <span class="type-tag" :class="item.type === 'project' ? 'tag-project' : 'tag-module'">
                {{ item.type === 'project' ? '项目' : '模块' }}
              </span>
            </td>
            <td>{{ item.name }}</td>
            <td>{{ item.parentName || '-' }}</td>
            <td>{{ item.sortOrder }}</td>
            <td>
              <button class="btn btn-sm btn-edit" @click="openEditDialog(item)">编辑</button>
              <button class="btn btn-sm btn-danger" @click="handleDelete(item)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="closeDialog">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑选项' : '新增选项' }}</h3>
          <button class="dialog-close" @click="closeDialog">✕</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label class="form-label">类型 <span class="required">*</span></label>
            <select v-model="form.type" class="form-input">
              <option value="project">所属项目</option>
              <option value="module">所属模块</option>
            </select>
          </div>
          <div class="form-item">
            <label class="form-label">名称 <span class="required">*</span></label>
            <input v-model="form.name" class="form-input" placeholder="请输入名称" maxlength="50" />
          </div>
          <div class="form-item" v-if="form.type === 'module'">
            <label class="form-label">所属项目 <span class="required">*</span></label>
            <select v-model="form.parentName" class="form-input">
              <option value="">请选择</option>
              <option v-for="p in projectList" :key="p.name" :value="p.name">{{ p.name }}</option>
            </select>
          </div>
          <div class="form-item">
            <label class="form-label">排序</label>
            <input v-model.number="form.sortOrder" type="number" class="form-input" placeholder="数字越小越靠前" />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="closeDialog">取消</button>
          <button class="btn btn-primary" @click="handleSave" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { fetchAllOptions, createOption, updateOption, deleteOption } from '@/api/option.js'

const filterType = ref('')
const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const editId = ref('')

const form = reactive({
  type: 'project',
  name: '',
  parentName: '',
  sortOrder: 0,
})

// 从列表中提取所有项目（供模块选择父项目用）
const projectList = computed(() => list.value.filter(item => item.type === 'project'))

// 按类型过滤
const filteredList = computed(() => {
  if (!filterType.value) return list.value
  return list.value.filter(item => item.type === filterType.value)
})

async function loadList() {
  const data = await fetchAllOptions()
  list.value = data || []
  if (Array.isArray(list.value)) {
    list.value = list.value
  }
}

function resetForm() {
  form.type = 'project'
  form.name = ''
  form.parentName = ''
  form.sortOrder = 0
  editId.value = ''
}

function openAddDialog() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(item) {
  isEdit.value = true
  editId.value = item.id
  form.type = item.type
  form.name = item.name
  form.parentName = item.parentName || ''
  form.sortOrder = item.sortOrder || 0
  dialogVisible.value = true
}

function closeDialog() {
  dialogVisible.value = false
  resetForm()
}

async function handleSave() {
  if (!form.name.trim()) {
    alert('名称不能为空')
    return
  }
  if (form.type === 'module' && !form.parentName) {
    alert('模块必须选择所属项目')
    return
  }
  saving.value = true
  try {
    const payload = {
      type: form.type,
      name: form.name.trim(),
      parentName: form.type === 'project' ? '' : form.parentName,
      sortOrder: form.sortOrder || 0,
    }
    if (isEdit.value) {
      await updateOption(editId.value, payload)
    } else {
      await createOption(payload)
    }
    closeDialog()
    await loadList()
  } catch (e) {
    alert('保存失败：' + (e.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

async function handleDelete(item) {
  const typeLabel = item.type === 'project' ? '项目' : '模块'
  if (!confirm(`确定删除${typeLabel}「${item.name}」吗？\n${item.type === 'project' ? '删除项目后其下模块不会自动删除。' : ''}`)) return
  try {
    await deleteOption(item.id)
    await loadList()
  } catch (e) {
    alert('删除失败：' + (e.message || '未知错误'))
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.option-manage {
  max-width: 1000px;
}

.page-header {
  margin-bottom: 20px;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 6px;
}
.page-desc {
  font-size: 13px;
  color: #999;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.filter-select {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}
.filter-select:focus {
  border-color: #1890ff;
}

/* 按钮 */
.btn {
  padding: 7px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all .2s;
}
.btn-primary {
  background: #1890ff;
  color: #fff;
}
.btn-primary:hover {
  background: #40a9ff;
}
.btn-primary:disabled {
  opacity: .6;
  cursor: not-allowed;
}
.btn-sm {
  padding: 4px 12px;
  font-size: 13px;
}
.btn-edit {
  background: #f0f5ff;
  color: #1890ff;
  margin-right: 8px;
}
.btn-edit:hover {
  background: #d6e4ff;
}
.btn-danger {
  background: #fff1f0;
  color: #ff4d4f;
}
.btn-danger:hover {
  background: #ffccc7;
}
.btn-cancel {
  background: #f5f5f5;
  color: #666;
}
.btn-cancel:hover {
  background: #e8e8e8;
}

/* 表格 */
.table-wrap {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table th {
  background: #fafafa;
  padding: 12px 16px;
  text-align: left;
  font-weight: 500;
  font-size: 13px;
  color: #666;
  border-bottom: 1px solid #f0f0f0;
}
.data-table td {
  padding: 12px 16px;
  font-size: 14px;
  color: #333;
  border-bottom: 1px solid #f5f5f5;
}
.data-table tr:last-child td {
  border-bottom: none;
}
.empty-row {
  text-align: center;
  color: #999;
  padding: 40px 16px !important;
}
.type-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
}
.tag-project {
  background: #e6f7ff;
  color: #1890ff;
}
.tag-module {
  background: #fff7e6;
  color: #fa8c16;
}

/* 弹窗 */
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.dialog-box {
  background: #fff;
  border-radius: 8px;
  width: 460px;
  box-shadow: 0 4px 24px rgba(0,0,0,.12);
}
.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
}
.dialog-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}
.dialog-close {
  background: none;
  border: none;
  font-size: 18px;
  color: #999;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}
.dialog-close:hover {
  color: #333;
}
.dialog-body {
  padding: 24px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 12px 24px;
  border-top: 1px solid #f0f0f0;
}
.form-item {
  margin-bottom: 16px;
}
.form-item:last-child {
  margin-bottom: 0;
}
.form-label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #333;
}
.required {
  color: #ff4d4f;
}
.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  outline: none;
  transition: border-color .2s;
}
.form-input:focus {
  border-color: #1890ff;
}
</style>
