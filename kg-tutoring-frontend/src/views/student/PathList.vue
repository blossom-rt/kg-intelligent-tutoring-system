<template>
  <div class="path-list-page">
    <StudentHeader title="我的学习路径" subtitle="系统生成的个性化学习计划">
      <template #actions>
        <el-button type="primary" @click="openGenerateDialog" round>生成新路径</el-button>
      </template>
    </StudentHeader>

    <el-table
      v-loading="loading"
      :data="pathList"
      style="width: 100%"
      stripe
      empty-text="暂无学习路径"
    >
      <el-table-column prop="pathName" label="路径名称" min-width="160">
        <template #default="{ row }">
          <span class="path-name-cell">{{ row.pathName || row.name || '未命名路径' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="targetNodeId" label="目标知识点ID" min-width="140">
        <template #default="{ row }">
          {{ row.targetNodeId || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="进度" width="180">
        <template #default="{ row }">
          <el-progress
            :percentage="row.progress || 0"
            :stroke-width="8"
            :status="row.progress === 100 ? 'success' : undefined"
          />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.createTime || row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="goDetail(row)">
            继续学习
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 生成新路径对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="生成新学习路径"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="genForm" :rules="genRules" label-width="90px">
        <el-form-item label="路径名称" prop="pathName">
          <el-input v-model="genForm.pathName" placeholder="请输入路径名称" />
        </el-form-item>
        <el-form-item label="所属课程" prop="courseId">
          <el-select
            v-model="genForm.courseId"
            placeholder="请选择课程"
            style="width: 100%"
            @change="onCourseChange"
          >
            <el-option
              v-for="c in courses"
              :key="c.id"
              :label="c.courseName || c.name"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标节点" prop="targetNodeId">
          <el-select
            v-model="genForm.targetNodeId"
            placeholder="请选择目标知识点"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="n in courseNodes"
              :key="n.id"
              :label="n.nodeName || n.name"
              :value="n.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="genLoading" @click="submitGenerate">
          开始生成
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import StudentHeader from '../../components/StudentHeader.vue'
import { getPathList, generatePath, deletePath } from '../../api/student'
import { getCourseList, getNodeList } from '../../api/knowledge'

const router = useRouter()
const loading = ref(false)
const pathList = ref([])
const courses = ref([])
const courseNodes = ref([])

const dialogVisible = ref(false)
const genLoading = ref(false)
const formRef = ref(null)

const genForm = reactive({
  pathName: '',
  courseId: '',
  targetNodeId: ''
})

const genRules = {
  pathName: [{ required: true, message: '请输入路径名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  targetNodeId: [{ required: true, message: '请选择目标知识点', trigger: 'change' }]
}

const statusTagType = (status) => {
  const map = { active: 'success', completed: 'info', paused: 'warning', abandoned: 'danger' }
  return map[status] || 'info'
}

const statusLabel = (status) => {
  const map = { active: '学习中', completed: '已完成', paused: '已暂停', abandoned: '已放弃' }
  return map[status] || '未知'
}

const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const goDetail = (row) => {
  router.push('/student/path/' + row.id)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该学习路径吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePath(row.id)
    ElMessage.success('删除成功')
    fetchPaths()
  } catch {
    // 取消删除
  }
}

const openGenerateDialog = () => {
  genForm.pathName = ''
  genForm.courseId = ''
  genForm.targetNodeId = ''
  courseNodes.value = []
  dialogVisible.value = true
}

const onCourseChange = async (courseId) => {
  genForm.targetNodeId = ''
  if (!courseId) {
    courseNodes.value = []
    return
  }
  try {
    const res = await getNodeList({ courseId })
    courseNodes.value = Array.isArray(res) ? res : (res.records || [])
  } catch {
    ElMessage.error('加载知识点列表失败')
  }
}

const submitGenerate = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  genLoading.value = true
  try {
    const res = await generatePath({
      pathName: genForm.pathName,
      courseId: genForm.courseId,
      targetNodeId: genForm.targetNodeId
    })
    ElMessage.success('学习路径生成成功')
    dialogVisible.value = false
    fetchPaths()
    if (res && res.id) {
      router.push('/student/path/' + res.id)
    }
  } catch {
    // 失败由拦截器处理
  } finally {
    genLoading.value = false
  }
}

const fetchPaths = async () => {
  loading.value = true
  try {
    const res = await getPathList()
    pathList.value = Array.isArray(res) ? res : (res.records || [])
  } catch {
    ElMessage.error('加载学习路径失败')
  } finally {
    loading.value = false
  }
}

const fetchCourses = async () => {
  try {
    const res = await getCourseList()
    courses.value = Array.isArray(res) ? res : (res.records || [])
  } catch {
    // 静默失败
  }
}

onMounted(() => {
  fetchPaths()
  fetchCourses()
})
</script>

<style scoped>
.path-list-page {
  min-height: 100vh;
  background: #faf7f2;
  
}

.path-name-cell {
  font-weight: 500;
  color: #2d2a26;
}
</style>
