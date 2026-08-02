<script setup>
import { ref, onMounted, watch } from 'vue'
import { fetchFiles, uploadFiles, deleteFile, downloadFile, formatFileSize } from '@/api/file.js'

const props = defineProps({
  taskId: { type: String, default: '' },
  /** 新建模式下 taskId 还不存在，使用临时 UUID */
  tempId: { type: String, default: '' },
})

const emit = defineEmits(['files-change'])

const files = ref([])
const uploading = ref(false)
const fileInput = ref(null)

const actualTaskId = ref(props.taskId || props.tempId)

watch(() => props.taskId, (val) => {
  if (val) {
    actualTaskId.value = val
    loadRemoteFiles()
  }
})

async function loadRemoteFiles() {
  if (!props.taskId) return
  try {
    files.value = await fetchFiles(props.taskId)
    emitFiles()
  } catch (e) {
    console.error('获取附件失败:', e)
  }
}

function emitFiles() {
  emit('files-change', files.value)
}

function triggerPick() {
  fileInput.value?.click()
}

async function onFilesPicked(e) {
  const selected = Array.from(e.target.files || [])
  if (selected.length === 0) return
  uploading.value = true

  // 如果已有 taskId，直接上传；否则暂存
  if (props.taskId) {
    try {
      const uploaded = await uploadFiles(props.taskId, selected)
      files.value = [...files.value, ...uploaded]
      emitFiles()
    } catch (e) {
      alert('上传失败: ' + e.message)
    }
  } else {
    // 新建模式：暂存文件对象，待保存后再一起上传
    for (const f of selected) {
      files.value.push({
        _local: true,
        _file: f,
        fileName: f.name,
        fileSize: f.size,
        fileType: f.type,
      })
    }
    emitFiles()
  }

  fileInput.value.value = ''
  uploading.value = false
}

async function handleDelete(item, idx) {
  if (!confirm('确定要删除该附件吗？')) return
  if (item._local) {
    files.value.splice(idx, 1)
    emitFiles()
    return
  }
  try {
    await deleteFile(item.id)
    files.value.splice(idx, 1)
    emitFiles()
  } catch (e) {
    alert('删除失败: ' + e.message)
  }
}

function getIcon(type) {
  if (!type) return '📄'
  if (type.includes('image')) return '🖼️'
  if (type.includes('pdf')) return '📕'
  if (type.includes('zip') || type.includes('rar')) return '📦'
  if (type.includes('word') || type.includes('document')) return '📝'
  if (type.includes('excel') || type.includes('sheet')) return '📊'
  if (type.includes('text')) return '📃'
  return '📄'
}

onMounted(() => {
  if (props.taskId) loadRemoteFiles()
})

defineExpose({ files })
</script>

<template>
  <div class="file-upload-card">
    <div class="card-header">
      <h3 class="card-title">📎 附件 ({{ files.length }})</h3>
      <button class="btn-upload" @click="triggerPick" :disabled="uploading">
        {{ uploading ? '⏳ 上传中...' : '➕ 选择文件' }}
      </button>
      <input
        ref="fileInput"
        type="file"
        multiple
        class="file-input-hidden"
        @change="onFilesPicked"
      />
    </div>

    <div class="file-empty" v-if="files.length === 0">暂无附件，点击上方按钮添加</div>

    <ul class="file-list" v-else>
      <li v-for="(f, idx) in files" :key="f.id || ('local-' + idx)" class="file-item">
        <span class="file-icon">{{ getIcon(f.fileType) }}</span>
        <div class="file-info">
          <span class="file-name">{{ f.fileName }}</span>
          <span class="file-meta">
            {{ f._local ? '待上传' : formatFileSize(f.fileSize) }}
          </span>
        </div>
        <template v-if="!f._local && f.id">
          <a class="file-download" @click.prevent="downloadFile(f.id, f.fileName)" href="#" title="下载">⬇️</a>
        </template>
        <button class="file-delete" @click="handleDelete(f, idx)" title="删除">✕</button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.file-upload-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.btn-upload {
  padding: 6px 16px;
  font-size: 13px;
  border: 1px solid #1a73e8;
  border-radius: 6px;
  background: #f0f5ff;
  color: #1a73e8;
  cursor: pointer;
  transition: background 0.15s;
}

.btn-upload:hover:not(:disabled) {
  background: #d6e4ff;
}

.btn-upload:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.file-input-hidden {
  display: none;
}

.file-empty {
  color: #999;
  font-size: 14px;
  padding: 12px 0;
}

.file-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  transition: background 0.15s;
}

.file-item:hover {
  background: #fafafa;
}

.file-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-meta {
  font-size: 12px;
  color: #999;
}

.file-download {
  flex-shrink: 0;
  text-decoration: none;
  font-size: 18px;
  padding: 4px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;
}

.file-download:hover {
  background: #f0f5ff;
}

.file-delete {
  flex-shrink: 0;
  border: none;
  background: transparent;
  color: #999;
  font-size: 14px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.15s;
}

.file-delete:hover {
  background: #fff1f0;
  color: #cf1322;
}
</style>
