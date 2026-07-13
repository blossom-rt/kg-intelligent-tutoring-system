import request from '../utils/request'

// 角色管理
export function getRoleList() { return request.get('/admin/roles') }
export function createRole(data) { return request.post('/admin/roles', data) }
export function updateRole(id, data) { return request.put(`/admin/roles/${id}`, data) }
export function deleteRole(id) { return request.delete(`/admin/roles/${id}`) }

// 用户管理
export function getUserList(params) { return request.get('/admin/users', { params }) }
export function createUser(data) { return request.post('/admin/users', data) }
export function updateUser(id, data) { return request.put(`/admin/users/${id}`, data) }
export function deleteUser(id) { return request.delete(`/admin/users/${id}`) }
export function toggleUserStatus(id, data) { return request.put(`/admin/users/${id}/status`, data) }
export function exportUsers() { return request.get('/admin/users/export', { responseType: 'blob' }) }
export function importUsers(data) { return request.post('/admin/users/import', data) }

// 公告管理
export function getNoticeList(params) { return request.get('/notices', { params }) }
export function createNotice(data) { return request.post('/notices', data) }
export function updateNotice(id, data) { return request.put(`/notices/${id}`, data) }
export function deleteNotice(id) { return request.delete(`/notices/${id}`) }

// 日志
export function getOperLogs(params) { return request.get('/admin/oper-logs', { params }) }
export function getAiLogs(params) { return request.get('/admin/ai-logs', { params }) }

// 课程管理（管理员）
export function getCourseList(params) { return request.get('/courses', { params }) }
export function createCourse(data) { return request.post('/courses', data) }
export function updateCourse(id, data) { return request.put(`/courses/${id}`, data) }
export function deleteCourse(id) { return request.delete(`/courses/${id}`) }

// 主题管理（教师也可用）
export function getThemeList(params) { return request.get('/themes', { params }) }
export function getThemeDetail(id) { return request.get(`/themes/${id}`) }
export function createTheme(data) { return request.post('/themes', data) }
export function updateTheme(id, data) { return request.put(`/themes/${id}`, data) }
export function deleteTheme(id) { return request.delete(`/themes/${id}`) }
export function toggleThemeStatus(id, data) { return request.put(`/themes/${id}/status`, data) }
