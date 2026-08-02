import request from './request'

/**
 * 登录
 */
export async function login(username, password) {
  return await request.post('/auth/login', { username, password })
}

/**
 * 获取当前用户
 */
export async function fetchMe() {
  return await request.get('/auth/me')
}
