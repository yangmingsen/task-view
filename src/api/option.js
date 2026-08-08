import request from './request.js'

/**
 * 获取下拉选项
 * @param {'project'|'module'} type - 选项类型
 * @param {string} parentName - 父级项目名（查询模块时传入）
 * @returns {Promise<Array<{id, type, name, parentName, sortOrder}>>}
 */
export function fetchOptions(type, parentName) {
  const params = { type }
  if (parentName) params.parentName = parentName
  return request.get('/options', { params })
}

/**
 * 获取全部选项（管理页面使用）
 * @param {string} [type] - 可选过滤类型
 * @returns {Promise<Array>}
 */
export function fetchAllOptions(type) {
  const params = {}
  if (type) params.type = type
  return request.get('/options/all', { params })
}

/**
 * 新增选项
 */
export function createOption(data) {
  return request.post('/options', data)
}

/**
 * 更新选项
 */
export function updateOption(id, data) {
  return request.put(`/options/${id}`, data)
}

/**
 * 删除选项
 */
export function deleteOption(id) {
  return request.delete(`/options/${id}`)
}
