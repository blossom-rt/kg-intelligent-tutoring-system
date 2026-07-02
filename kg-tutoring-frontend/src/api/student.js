import request from '../utils/request'

export function getStudentDashboard() {
  return request.get('/student/dashboard')
}

export function getStudyPaths() {
  return request.get('/student/study-paths')
}

export function getStudyRecords() {
  return request.get('/student/study-records')
}

export function getWrongQuestions(params) {
  return request.get('/student/wrong-questions', { params })
}

export function getExamRecords() {
  return request.get('/student/exam-records')
}

export function getKnowledgeNodes(params) {
  return request.get('/student/knowledge-nodes', { params })
}

export function getCrossSubjects() {
  return request.get('/student/cross-subjects')
}
