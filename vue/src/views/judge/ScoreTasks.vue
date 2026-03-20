<template>
  <div class="score-tasks">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评分任务</span>
          <el-button type="primary" @click="loadData" :loading="loading">刷新</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="competitionName" label="竞赛名称" min-width="200" />
        <el-table-column prop="workTitle" label="作品标题" min-width="200" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已评分' : '待评分' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止时间" width="180">
          <template #default="{ row }">
            <span :style="{ color: row.isExpired ? '#f56c6c' : '' }">
              {{ formatDateTime(row.deadline) }}
              <el-tag v-if="row.isExpired" type="danger" size="small" style="margin-left: 8px">已过期</el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewWork(row)"
            >
              查看作品
            </el-button>
            <el-button
              v-if="row.status === 0 && !row.isExpired"
              type="success"
              size="small"
              @click="handleScore(row)"
            >
              评分
            </el-button>
            <el-button
              v-if="row.status === 0 && row.isExpired"
              type="info"
              size="small"
              disabled
            >
              已过期
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="info"
              size="small"
              @click="handleViewScore(row)"
            >
              查看评分
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 作品详情对话框 -->
    <el-dialog v-model="workDialogVisible" title="作品详情" width="800px">
      <div v-if="currentWork" class="work-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="竞赛名称">{{ currentWork.competitionName }}</el-descriptions-item>
          <el-descriptions-item label="作品标题">{{ currentWork.title }}</el-descriptions-item>
          <el-descriptions-item label="提交者" :span="2">{{ currentWork.submitterName || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="作品描述" :span="2">
            <div style="white-space: pre-wrap;">{{ currentWork.description || '无' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="文件名">{{ currentWork.fileName || '无' }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">
            {{ currentWork.fileSize ? formatFileSize(currentWork.fileSize) : '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="提交时间" :span="2">{{ formatDateTime(currentWork.submitTime) }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="currentWork.filePath" style="margin-top: 20px; text-align: center;">
          <el-button type="primary" @click="handleDownloadWork">下载作品文件</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 评分对话框 -->
    <el-dialog v-model="scoreDialogVisible" :title="scoreDialogTitle" width="800px">
      <el-form :model="scoreForm" label-width="120px" v-if="currentWork">
        <el-form-item label="竞赛名称">
          <span>{{ currentWork.competitionName }}</span>
        </el-form-item>
        <el-form-item label="作品标题">
          <span>{{ currentWork.title }}</span>
        </el-form-item>
        <el-form-item label="作品描述">
          <div style="white-space: pre-wrap; color: #606266;">{{ currentWork.description || '无' }}</div>
        </el-form-item>
        <el-divider />
        <el-form-item label="评分" required>
          <el-input-number 
            v-model="scoreForm.totalScore" 
            :min="0" 
            :max="100" 
            :precision="1" 
            :step="0.5"
            style="width: 200px;"
          />
          <span style="margin-left: 10px; color: #409EFF; font-weight: bold;">满分100分</span>
        </el-form-item>
        <el-form-item label="评语">
          <el-input 
            v-model="scoreForm.comment" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入评语..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitScore" :loading="submitting">提交评分</el-button>
      </template>
    </el-dialog>

    <!-- 查看评分对话框 -->
    <el-dialog v-model="viewScoreDialogVisible" title="评分详情" width="800px">
      <div v-if="currentScore" class="score-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="竞赛名称">{{ currentScore.competitionName }}</el-descriptions-item>
          <el-descriptions-item label="作品标题">{{ currentScore.workTitle }}</el-descriptions-item>
          <el-descriptions-item label="总分">
            <span style="font-size: 20px; font-weight: bold; color: #409EFF;">{{ currentScore.totalScore }}</span>
            <span style="margin-left: 10px; color: #909399;">分</span>
          </el-descriptions-item>
          <el-descriptions-item label="评分时间">{{ formatDateTime(currentScore.scoreTime) }}</el-descriptions-item>

          <el-descriptions-item label="评语" :span="2">
            <div style="white-space: pre-wrap;">{{ currentScore.comment || '无' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getScoreTasks, getScoreTaskDetail, getWorkDetail, submitScore, getScoreDetail } from '../../api/score'
import { downloadWork } from '../../api/work'
import { formatDateTime } from '../../utils/dateFormat'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const workDialogVisible = ref(false)
const scoreDialogVisible = ref(false)
const viewScoreDialogVisible = ref(false)
const scoreDialogTitle = ref('评分')
const currentTask = ref(null)
const currentWork = ref(null)
const currentScore = ref(null)
const scoreDetails = ref(null)

const scoreForm = ref({
  scoreTaskId: null,
  totalScore: 0,
  comment: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getScoreTasks({ page: page.value, size: size.value })
    if (res.data && res.data.records) {
      tableData.value = res.data.records
      total.value = res.data.total || 0
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('加载失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

const handleViewWork = async (row) => {
  try {
    const res = await getWorkDetail(row.workId)
    if (res.data) {
      currentWork.value = res.data
      workDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取作品详情失败：' + (error.response?.data?.message || error.message))
  }
}

const handleScore = async (row) => {
  try {
    // 获取任务详情和作品信息
    const taskRes = await getScoreTaskDetail(row.id)
    if (taskRes.data) {
      currentTask.value = taskRes.data.task
      currentWork.value = {
        competitionName: taskRes.data.competition?.name || '未知竞赛',
        title: taskRes.data.work?.title || '',
        description: taskRes.data.work?.description || '',
        filePath: taskRes.data.work?.filePath,
        fileName: taskRes.data.work?.fileName
      }
      
      // 如果已有评分，填充表单
      if (taskRes.data.existingScore) {
        const existing = taskRes.data.existingScore
        scoreForm.value = {
          scoreTaskId: existing.scoreTaskId,
          totalScore: existing.totalScore || 0,
          comment: existing.comment || ''
        }
        scoreDialogTitle.value = '修改评分'
      } else {
        scoreForm.value = {
          scoreTaskId: row.id,
          totalScore: 0,
          comment: ''
        }
        scoreDialogTitle.value = '评分'
      }
      
      scoreDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取任务详情失败：' + (error.response?.data?.message || error.message))
  }
}

const handleViewScore = async (row) => {
  try {
    const res = await getScoreDetail(row.id)
    if (res.data) {
      currentScore.value = {
        competitionName: res.data.competition?.name || '未知竞赛',
        workTitle: res.data.work?.title || '未知作品',
        totalScore: res.data.score?.totalScore,
        scoreTime: res.data.score?.scoreTime,
        comment: res.data.score?.comment
      }
      
      try {
        scoreDetails.value = res.data.score?.scoreDetails ? 
                            JSON.parse(res.data.score.scoreDetails) : null
      } catch (e) {
        scoreDetails.value = null
      }
      
      viewScoreDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取评分详情失败：' + (error.response?.data?.message || error.message))
  }
}

const handleSubmitScore = async () => {
  if (scoreForm.value.totalScore === 0) {
    ElMessage.warning('请填写评分')
    return
  }
  
  try {
    await ElMessageBox.confirm('确认提交评分？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    submitting.value = true
    
    const scoreData = {
      scoreTaskId: scoreForm.value.scoreTaskId,
      totalScore: scoreForm.value.totalScore,
      comment: scoreForm.value.comment
    }
    
    await submitScore(scoreData)
    ElMessage.success('评分提交成功')
    scoreDialogVisible.value = false
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败：' + (error.response?.data?.message || error.message))
    }
  } finally {
    submitting.value = false
  }
}

const handleDownloadWork = async () => {
  try {
    if (!currentWork.value?.filePath) {
      ElMessage.warning('文件路径不存在')
      return
    }
    
    const response = await downloadWork(currentWork.value.filePath)
    
    // 处理响应数据
    let blob
    if (response instanceof Blob) {
      blob = response
    } else if (response.data instanceof Blob) {
      blob = response.data
    } else {
      // 如果不是 Blob，尝试转换
      blob = new Blob([response])
    }
    
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = currentWork.value.fileName || '作品文件'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败：' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.score-tasks {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.work-detail,
.score-detail {
  padding: 10px 0;
}
</style>
