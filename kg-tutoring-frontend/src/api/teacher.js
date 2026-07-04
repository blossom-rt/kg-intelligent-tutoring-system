import request from '../utils/request'

// 题库管理
export function getQuestionList(params) { return request.get('/questions', { params }) }
export function createQuestion(data) { return request.post('/questions', data) }
export function updateQuestion(id, data) { return request.put(`/questions/${id}`, data) }
export function deleteQuestion(id) { return request.delete(`/questions/${id}`) }

// 测评管理
export function getExamList(params) { return request.get('/teacher/exams', { params }) }
export function createExam(data) { return request.post('/teacher/exams', data) }
export function updateExam(id, data) { return request.put(`/teacher/exams/${id}`, data) }
export function deleteExam(id) { return request.delete(`/teacher/exams/${id}`) }

// 学情统计
export function getClassAnalysis(params) { return request.get('/teacher/analysis/class', { params }) }

// 学习路径督导
export function getStudentPaths(studentId) { return request.get(`/teacher/analysis/student-paths?studentId=${studentId}`) }
