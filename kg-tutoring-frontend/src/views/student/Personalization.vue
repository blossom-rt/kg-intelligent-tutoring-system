<template>
  <div class="personalization-page">
    <StudentHeader title="个性化推荐" subtitle="选择一门课程，发现更适合继续学习的方向" />

    <section class="selector-band">
      <div class="selector-copy">
        <span class="eyebrow">Course Discovery</span>
        <h2>从当前课程出发</h2>
      </div>
      <el-select
        v-model="selectedCourseId"
        class="course-select"
        filterable
        placeholder="请选择课程"
        :loading="courseLoading"
        @change="fetchRecommendations"
      >
        <el-option
          v-for="course in courses"
          :key="course.id"
          :label="course.courseName"
          :value="course.id"
        >
          <span>{{ course.courseName }}</span>
          <span class="option-subject">{{ course.subject }}</span>
        </el-option>
      </el-select>
    </section>

    <div class="recommendation-layout" v-loading="recommendLoading">
      <section class="recommend-section">
        <div class="section-heading">
          <div>
            <h3>相关度高的其他课程</h3>
            <p>综合同学共学、同学科关联和课程知识点规模排序</p>
          </div>
          <el-tag type="warning" round>相关推荐</el-tag>
        </div>

        <el-empty v-if="!recommendLoading && relatedCourses.length === 0" description="暂无相关课程" :image-size="64" />

        <article
          v-for="course in relatedCourses"
          :key="course.id"
          class="course-card"
          role="button"
          tabindex="0"
          @click="goCourse(course)"
          @keyup.enter="goCourse(course)"
        >
          <div class="course-main">
            <div>
              <div class="course-title-row">
                <h4>{{ course.courseName }}</h4>
                <el-tag size="small">{{ course.subject }}</el-tag>
              </div>
              <p>{{ course.description || '暂无课程简介' }}</p>
            </div>
            <div class="score-pill">
              <strong>{{ formatNumber(course.score) }}</strong>
              <span>推荐分</span>
            </div>
          </div>
          <div class="metric-row">
            <span>共同学习 {{ course.sharedLearnerCount || 0 }} 人</span>
            <span>知识点 {{ course.nodeCount || 0 }} 个</span>
            <span>累计 {{ course.sharedStudyMinutes || 0 }} 分钟</span>
          </div>
        </article>
      </section>

      <section class="recommend-section">
        <div class="section-heading">
          <div>
            <h3>学这门课的同学还学</h3>
            <p>基于学习过所选课程的同学，在其他课程中的真实学习记录</p>
          </div>
          <el-tag type="success" round>同伴共学</el-tag>
        </div>

        <el-empty v-if="!recommendLoading && alsoLearnedCourses.length === 0" description="暂无同伴共学数据" :image-size="64" />

        <article
          v-for="course in alsoLearnedCourses"
          :key="course.id"
          class="course-card"
          role="button"
          tabindex="0"
          @click="goCourse(course)"
          @keyup.enter="goCourse(course)"
        >
          <div class="course-main">
            <div>
              <div class="course-title-row">
                <h4>{{ course.courseName }}</h4>
                <el-tag size="small" type="success">{{ course.subject }}</el-tag>
              </div>
              <p>{{ course.description || '暂无课程简介' }}</p>
            </div>
            <div class="score-pill green">
              <strong>{{ course.learnerCount || 0 }}</strong>
              <span>同学</span>
            </div>
          </div>
          <div class="metric-row">
            <span>累计 {{ course.totalStudyMinutes || 0 }} 分钟</span>
            <span>平均正确率 {{ course.avgCorrectRate || 0 }}%</span>
          </div>
        </article>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StudentHeader from '../../components/StudentHeader.vue'
import {
  getAlsoLearnedCourseRecommendations,
  getCourseList,
  getRelatedCourseRecommendations
} from '../../api/knowledge'

const router = useRouter()
const courses = ref([])
const selectedCourseId = ref(null)
const relatedCourses = ref([])
const alsoLearnedCourses = ref([])
const courseLoading = ref(false)
const recommendLoading = ref(false)

const formatNumber = (value) => {
  const num = Math.min(Number(value || 0), 100)
  return Number.isInteger(num) ? num : num.toFixed(1)
}

const normalizeList = (res) => {
  if (Array.isArray(res)) return res
  return res?.records || res?.data || []
}

const fetchCourses = async () => {
  courseLoading.value = true
  try {
    const res = await getCourseList({ page: 1, size: 999 })
    courses.value = normalizeList(res)
    if (!selectedCourseId.value && courses.value.length) {
      selectedCourseId.value = courses.value[0].id
      await fetchRecommendations()
    }
  } catch {
    ElMessage.error('加载课程列表失败')
  } finally {
    courseLoading.value = false
  }
}

const fetchRecommendations = async () => {
  if (!selectedCourseId.value) return
  recommendLoading.value = true
  try {
    const [relatedRes, alsoRes] = await Promise.all([
      getRelatedCourseRecommendations(selectedCourseId.value, { limit: 6 }),
      getAlsoLearnedCourseRecommendations(selectedCourseId.value, { limit: 6 })
    ])
    relatedCourses.value = normalizeList(relatedRes)
    alsoLearnedCourses.value = normalizeList(alsoRes)
  } catch {
    ElMessage.error('加载个性化推荐失败')
  } finally {
    recommendLoading.value = false
  }
}

const goCourse = (course) => {
  if (!course?.id) return
  router.push({ path: '/student/knowledge', query: { courseId: course.id } })
}

onMounted(fetchCourses)
</script>

<style scoped>
.personalization-page {
  min-height: 100vh;
  background: var(--bg-root);
}

.selector-band {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 20px 24px;
  margin-bottom: 20px;
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.selector-copy h2 {
  margin: 4px 0 0;
  font-size: 20px;
  color: var(--text-primary);
}

.eyebrow {
  font-size: 12px;
  color: var(--accent);
  font-weight: 700;
}

.course-select {
  width: min(420px, 100%);
}

.option-subject {
  float: right;
  color: var(--text-muted);
  font-size: 12px;
  margin-left: 16px;
}

.recommendation-layout {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  min-height: 320px;
}

.recommend-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.section-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.section-heading h3 {
  margin: 0;
  font-size: 18px;
  color: var(--text-primary);
}

.section-heading p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.6;
}

.course-card {
  padding: 18px;
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.course-card:hover,
.course-card:focus-visible {
  transform: translateY(-2px);
  border-color: var(--accent);
  box-shadow: var(--shadow-md);
  outline: none;
}

.course-main {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.course-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.course-title-row h4 {
  margin: 0;
  font-size: 16px;
  color: var(--text-primary);
}

.course-card p {
  margin: 10px 0 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.score-pill {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--accent-soft);
  color: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  flex-shrink: 0;
}

.score-pill.green {
  background: rgba(61, 138, 94, 0.1);
  color: var(--accent-green);
}

.score-pill strong {
  font-size: 20px;
  line-height: 1;
}

.score-pill span {
  margin-top: 6px;
  font-size: 12px;
}

.metric-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.metric-row span {
  padding: 5px 9px;
  border-radius: var(--radius-sm);
  background: var(--bg-input);
  color: var(--text-muted);
  font-size: 12px;
}

@media (max-width: 900px) {
  .selector-band,
  .recommendation-layout {
    grid-template-columns: 1fr;
  }

  .selector-band {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
