import request from './request'

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes) {
  if (!bytes || bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(1) + ' ' + units[i]
}

/**
 * 获取任务附件列表
 */
export async function fetchFiles(taskId) {
  return await request.get(`/tasks/${taskId}/files`)
}

/**
 * 上传附件
 */
export async function uploadFiles(taskId, files) {
  const formData = new FormData()
  files.forEach((f) => formData.append('files', f))
  return await request.post(`/tasks/${taskId}/files`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/**
 * 下载附件
 */
export function downloadFile(fileId, fileName) {
  const token = localStorage.getItem('token')
  const base = import.meta.env.VITE_API_BASE_URL || '/api'
  const url = `${base}/files/${fileId}/download`
  const a = document.createElement('a')
  a.href = url
  a.download = fileName || ''
  if (token) {
    a.href = url + '?token=' + encodeURIComponent(token)
  }
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

/**
 * 删除附件
 */
export async function deleteFile(fileId) {
  return await request.delete(`/files/${fileId}`)
}

/**
 * 上传 Markdown 内嵌图片（Ctrl+V 粘贴时调用）
 * 返回 { url: '/api/markdown/images/xxx.png', name: 'original.png' }
 */
export async function uploadMarkdownImage(file) {
  const formData = new FormData()
  formData.append('image', file)
  return await request.post('/markdown/images/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
