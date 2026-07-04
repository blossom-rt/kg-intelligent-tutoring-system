<template>
  <div class="page">
    <header class="top-bar">
      <div class="top-left">
        <span class="back-btn" @click="$router.push('/teacher')">&#8592; 返回</span>
        <h2>知识点管理</h2>
      </div>
      <div class="top-actions">
        <el-select v-model="filterCourseId" placeholder="全部课程" clearable @change="fetchNodes" style="width:180px" size="large">
          <el-option v-for="c in courses" :key="c.id" :label="c.courseName" :value="c.id" />
        </el-select>
        <el-button type="primary" size="large" round @click="openAdd">新增知识点</el-button>
      </div>
    </header>
    <div class="content">
      <el-table :data="nodes" stripe style="width:100%" :header-cell-style="{background:'#f8fafc',color:'#555'}">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="知识点名称" />
        <el-table-column prop="courseName" label="所属课程" width="140" />
        <el-table-column label="难度" width="90">
          <template #default="{ row }">
            <el-tag size="small" round :type="row.difficulty===3?'danger':row.difficulty===2?'warning':'info'">
              {{ ['','基础','进阶','困难'][row.difficulty] || '基础' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chapter" label="章节" width="100" />
        <el-table-column prop="expectedMinutes" label="预计时长" width="90" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" round @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" round @click="delNode(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑知识点' : '新增知识点'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="知识点名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="form.courseId" style="width:100%">
            <el-option v-for="c in courses" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-radio-group v-model="form.difficulty">
            <el-radio-button :value="1">基础</el-radio-button>
            <el-radio-button :value="2">进阶</el-radio-button>
            <el-radio-button :value="3">困难</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="章节"><el-input v-model="form.chapter" /></el-form-item>
        <el-form-item label="预计时长(分)"><el-input-number v-model="form.expectedMinutes" :min="1" :max="999" /></el-form-item>
        <el-form-item label="内容描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
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

const nodes = ref([])
const courses = ref([])
const filterCourseId = ref(null)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const form = reactive({ name: '', courseId: null, difficulty: 1, chapter: '', expectedMinutes: 30, description: '' })
const rules = {
  name: [{ required: true, message: '请输入知识点名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
}

onMounted(async () => {
  try { courses.value = (await request.get('/teacher/courses')) || [] } catch { }
  fetchNodes()
})

const fetchNodes = async () => {
  try {
    const params = filterCourseId.value ? { courseId: filterCourseId.value } : {}
    nodes.value = (await request.get('/teacher/knowledge-nodes', { params })) || []
    nodes.value.forEach(n => { const c = courses.value.find(c => c.id === n.courseId); n.courseName = c ? c.courseName : '-' })
  } catch { }
}

const openAdd = () => { editingId.value = null; Object.assign(form, { name: '', courseId: null, difficulty: 1, chapter: '', expectedMinutes: 30, description: '' }); dialogVisible.value = true }
const openEdit = (row) => { editingId.value = row.id; Object.assign(form, { name: row.name, courseId: row.courseId, difficulty: row.difficulty || 1, chapter: row.chapter || '', expectedMinutes: row.expectedMinutes || 30, description: row.description || '' }); dialogVisible.value = true }

const save = async () => {
  if (!(await formRef.value.validate().catch(() => false))) return
  try {
    if (editingId.value) { await request.put('/teacher/knowledge-nodes/' + editingId.value, { ...form }); ElMessage.success('更新成功') }
    else { await request.post('/teacher/knowledge-nodes', { ...form }); ElMessage.success('添加成功') }
    dialogVisible.value = false; fetchNodes()
  } catch { ElMessage.error('操作失败') }
}

const delNode = async (id) => { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await request.delete('/teacher/knowledge-nodes/' + id); ElMessage.success('已删除'); fetchNodes() }
</script>

<style scoped>
.page { min-height: 100vh; background: #faf7f2; }
.top-bar { display: flex; justify-content: space-between; align-items: center; padding: 16px 36px; background: #fffdf9; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.top-left { display: flex; align-items: center; gap: 16px; }
.back-btn { color: #2d8a4e; cursor: pointer; font-size: 14px; }
.top-bar h2 { margin: 0; font-size: 18px; color: #2d2a26; }
.top-actions { display: flex; gap: 12px; align-items: center; }
.content { padding: 28px 36px; }
</style>
