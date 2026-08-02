<script setup>
import { ref, onMounted } from 'vue'
import { fetchFiles, downloadFile, deleteFile, formatFileSize } from '@/api/file.js'

const props = defineProps({
  taskId: { type: String, required: true },
})

const files = ref([])
const loading = ref(false)

async function loadFiles() {
  loading.value = true
  try {
    files.value = await fetchFiles(props.taskId)
  } catch (e) {
    console.error('获取附件列表失败:', e)
    files.value = []
  } finally {
    loading.value = false
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

onMounted(loadFiles)
</script>

<template>
  <div class="file-attach-card">
    <h3 class="card-title">📎 附件 ({{ files.length }})</h3>
    <div class="file-loading" v-if="loading">加载中...</div>
    <div class="file-empty" v-else-if="files.length === 0">暂无附件</div>
    <ul class="file-list" v-else>
      <li v-for="f in files" :key="f.id" class="file-item">
        <span class="file-icon">{{ getIcon(f.fileType) }}</span>
        <div class="file-info">
          <span class="file-name">{{ f.fileName }}</span>
          <span class="file-meta">{{ formatFileSize(f.fileSize) }} · {{ f.createdBy }} · {{ (f.createTime || '').slice(0, 10) }}</span>
        </div>
        <a class="file-download" @click.prevent="downloadFile(f.id, f.fileName)" href="#" title="下载">⬇️</a>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.file-attach-card {
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
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.file-loading,
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
  transition: background 0.15s;
  cursor: pointer;
}

.file-download:hover {
  background: #f0f5ff;
}
</style>
