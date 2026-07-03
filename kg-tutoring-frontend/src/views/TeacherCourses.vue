<template>
  <div class="page">
    <header class="top-bar">
      <div class="top-left">
        <span class="back-btn" @click="$router.push('/teacher')">&#8592; 返回</span>
        <h2>课程管理</h2>
      </div>
      <el-button type="primary" size="large" round @click="openAdd">新增课程</el-button>
    </header>
    <div class="content">
      <el-table :data="courses" stripe style="width:100%" :header-cell-style="{background:'#f8fafc',color:'#555'}">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="subject" label="学科" width="140" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="description" label="简介" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" round @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" round @click="delCourse(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId?'编辑课程':'新增课程'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="学科" prop="subject"><el-input v-model="form.subject" /></el-form-item>
        <el-form-item label="课程名称" prop="courseName"><el-input v-model="form.courseName" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" round @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const courses = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const form = reactive({ subject: '', courseName: '', description: '' })
const rules = { subject: [{ required: true, message: '请输入学科' }], courseName: [{ required: true, message: '请输入课程名称' }] }

const fetchCourses = async () => { try { courses.value = (await request.get('/teacher/courses')) || [] } catch { } }
onMounted(fetchCourses)

const openAdd = () => { editingId.value = null; Object.assign(form, { subject: '', courseName: '', description: '' }); dialogVisible.value = true }
const openEdit = (row) => { editingId.value = row.id; Object.assign(form, { subject: row.subject, courseName: row.courseName, description: row.description || '' }); dialogVisible.value = true }

const save = async () => {
  if (!(await formRef.value.validate().catch(() => false))) return
  try {
    if (editingId.value) { await request.put('/teacher/courses/' + editingId.value, { ...form }); ElMessage.success('更新成功') }
    else { await request.post('/teacher/courses', { ...form }); ElMessage.success('添加成功') }
    dialogVisible.value = false; fetchCourses()
  } catch { ElMessage.error('操作失败') }
}

const delCourse = async (id) => { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await request.delete('/teacher/courses/' + id); ElMessage.success('已删除'); fetchCourses() }
</script>

<style scoped>
.page { min-height: 100vh; background: #faf7f2; }
.top-bar { display: flex; justify-content: space-between; align-items: center; padding: 16px 36px; background: #fffdf9; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.top-left { display: flex; align-items: center; gap: 16px; }
.back-btn { color: #2d8a4e; cursor: pointer; font-size: 14px; }
.top-bar h2 { margin: 0; font-size: 18px; color: #2d2a26; }
.content { padding: 28px 36px; }
</style>
