import request from '../utils/request'

// ===== 课程管理 =====
export function getCourseList(params) { return request.get('/courses', { params }) }
export function getRelatedCourseRecommendations(id, params) { return request.get(`/courses/${id}/recommendations/related`, { params }) }
export function getAlsoLearnedCourseRecommendations(id, params) { return request.get(`/courses/${id}/recommendations/also-learned`, { params }) }
export function createCourse(data) { return request.post('/courses', data) }
export function updateCourse(id, data) { return request.put(`/courses/${id}`, data) }
export function deleteCourse(id) { return request.delete(`/courses/${id}`) }

// ===== 知识点管理 =====
export function getNodeList(params) { return request.get('/nodes', { params }) }
export function getNodeById(id) { return request.get(`/nodes/${id}`) }
export function createNode(data) { return request.post('/nodes', data) }
export function updateNode(id, data) { return request.put(`/nodes/${id}`, data) }
export function deleteNode(id) { return request.delete(`/nodes/${id}`) }

// ===== 知识点学习资源 =====
export function getNodeResources(nodeId, params, config = {}) {
  return request.get(`/nodes/${nodeId}/resources`, { params, ...config })
}
export function createNodeResource(nodeId, data) { return request.post(`/nodes/${nodeId}/resources`, data) }
export function updateNodeResource(id, data) { return request.put(`/resources/${id}`, data) }
export function deleteNodeResource(id) { return request.delete(`/resources/${id}`) }

// ===== 依赖边 =====
export function getEdgeList(params) { return request.get('/edges', { params }) }
export function createEdge(data) { return request.post('/edges', data) }
export function deleteEdge(id) { return request.delete(`/edges/${id}`) }

// ===== 章节管理 =====
export function getChapterList(params) { return request.get('/chapters', { params }) }
export function getChapterById(id) { return request.get(`/chapters/${id}`) }
export function createChapter(data) { return request.post('/chapters', data) }
export function updateChapter(id, data) { return request.put(`/chapters/${id}`, data) }
export function deleteChapter(id) { return request.delete(`/chapters/${id}`) }
export function updateChapterSort(id, data) { return request.put(`/chapters/${id}/sort`, data) }
