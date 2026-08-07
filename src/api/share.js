import request from './request.js'
import axios from 'axios'

/**
 * 创建分享链接（需要登录态）
 * @returns {{ shareUrl, apiUrl, token }}
 */
export function createShareLink(taskId) {
  return request.post(`/share/create/${taskId}`)
}

/**
 * 分享页获取任务详情（含附件列表）—— 不走全局 request，直接请求
 * @param {string} apiHost - 后端地址 如 "10.x.x.x:8009"
 * @param {string} taskId
 * @param {string} token
 */
export function fetchShareTask(apiHost, taskId, token) {
  return axios.get(`http://${apiHost}/api/share/tasks/${taskId}`, {
    params: { token },
  })
}

/**
 * 分享页下载附件
 * @param {string} apiHost - 后端地址 如 "10.x.x.x:8009"
 * @param {string} fileId
 * @param {string} fileName
 * @param {string} token
 */
export function downloadShareFile(apiHost, fileId, fileName, token) {
  const url = `http://${apiHost}/api/share/files/${fileId}/download?token=${encodeURIComponent(token)}`
  const a = document.createElement('a')
  a.href = url
  a.download = fileName || ''
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}
