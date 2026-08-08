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
