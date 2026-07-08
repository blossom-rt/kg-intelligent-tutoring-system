import request from '../utils/request'

// 学生端图谱数据
export function getStudentGraph(params) { return request.get('/student/graph', { params }) }

// 学习路径
export function generatePath(data) { return request.post('/student/path/generate', data) }
export function generatePathByTheme(data) { return request.post('/student/path/generate-by-theme', data) }
export function getPathList() { return request.get('/student/path/list') }
export function getPathDetail(id) { return request.get(`/student/path/${id}`) }
export function updatePathDetail(id, data) { return request.put(`/student/path/detail/${id}`, data) }
export function deletePath(id) { return request.delete(`/student/path/${id}`) }

// 学习记录
export function updateStudyRecord(data) { return request.put('/student/study-record', data) }

// 练习
export function getPracticeQuestions(params) { return request.get('/student/practice/questions', { params }) }
export function submitPractice(data) { return request.post('/student/practice/submit', data) }

// 测评
export function getStudentExams() { return request.get('/student/exams') }
export function getExamPaper(id) { return request.get(`/student/exam-paper/${id}`) }
export function getExamResult(id) { return request.get(`/student/exam/${id}`) }
export function submitExam(data) { return request.post('/student/exam/submit', data) }

// 错题
export function getWrongQuestions(params) { return request.get('/student/wrong-questions', { params }) }
export function deleteWrongQuestion(id) { return request.delete(`/student/wrong-questions/${id}`) }

// AI
export function aiNodeSummary(data) { return request.post('/student/ai/node-summary', data) }
export function aiWrongExplain(data) { return request.post('/student/ai/wrong-explain', data) }
export function aiExamReport(data) { return request.post('/student/ai/exam-report', data) }
export function aiChat(data) { return request.post('/student/ai/chat', data) }

// 个人分析
export function getPersonalAnalysis() { return request.get('/student/analysis/personal') }
export function getWeakAnalysis() { return request.get('/student/analysis/weak') }

// 学生仪表盘
export function getStudentDashboard() { return request.get('/student/dashboard') }

// 添加错题
export function addWrongQuestion(data) { return request.post('/student/wrong-questions', data) }

// 获取学习记录
export function getStudyRecords(params) { return request.get('/student/study-records', { params }) }

// 收藏夹
export function getFavoriteList(config = {}) { return request.get('/student/favorites', config) }
export function addFavorite(data) { return request.post('/student/favorites', data) }
export function deleteFavorite(id) { return request.delete(`/student/favorites/${id}`) }

// 前置知识点
export function getPrerequisiteNodes(nodeId, config = {}) { return request.get(`/student/prerequisite-nodes/${nodeId}`, config) }
