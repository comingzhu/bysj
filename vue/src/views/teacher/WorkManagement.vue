<template>
  <div class="work-management">
    <el-card>
      <template #header>
        <div class="header">
          <span>作品管理</span>
          <el-select v-model="selectedCompetitionId" placeholder="请选择竞赛" @change="loadData" style="width: 300px; margin-right: 10px;">
            <el-option label="全部竞赛" :value="null" />
            <el-option 
              v-for="comp in competitionList" 
              :key="comp.id" 
              :label="comp.name" 
              :value="comp.id" 
            />
          </el-select>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="competitionName" label="竞赛名称" width="200" />
        <el-table-column prop="submitterName" label="提交者" width="180" />
        <el-table-column prop="title" label="作品标题" width="200" />
        <el-table-column prop="fileName" label="文件名" width="200" />
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'danger' : 'success'">
              {{ row.status === 1 ? '异常' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handlePreview(row)">预览</el-button>
            <el-button type="success" size="small" @click="handleDownload(row)">下载</el-button>
            <el-button 
              v-if="row.status === 0"
              type="warning" 
              size="small" 
              @click="handleMarkAbnormal(row.id)"
            >
              标记异常
            </el-button>
            <el-button 
              v-if="row.status === 1"
              type="info" 
              size="small" 
              @click="handleMarkNormal(row.id)"
            >
              恢复正常
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

    <!-- 预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="作品预览" width="800px">
      <div v-if="currentWork">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="竞赛名称">{{ currentWork.competitionName }}</el-descriptions-item>
          <el-descriptions-item label="提交者">{{ currentWork.submitterName }}</el-descriptions-item>
          <el-descriptions-item label="作品标题" :span="2">{{ currentWork.title }}</el-descriptions-item>
          <el-descriptions-item label="作品描述" :span="2">{{ currentWork.description || '无' }}</el-descriptions-item>
          <el-descriptions-item label="文件名">{{ currentWork.fileName }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ formatFileSize(currentWork.fileSize) }}</el-descriptions-item>
          <el-descriptions-item label="提交时间" :span="2">{{ formatDateTime(currentWork.submitTime) }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 20px; text-align: center;">
          <el-button type="primary" @click="handleDownload(currentWork)">下载作品</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWorkList, updateWorkStatus, downloadWork } from '../../api/work'
import { getMyCompetitions } from '../../api/competition'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '../../utils/dateFormat'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const selectedCompetitionId = ref(null)
const competitionList = ref([])
const previewDialogVisible = ref(false)
const currentWork = ref(null)

const loadCompetitions = async () => {
  try {
    const res = await getMyCompetitions({ page: 1, size: 1000 })
    if (res.data && res.data.records) {
      // 只显示需要提交作品的竞赛
      competitionList.value = res.data.records.filter(c => c.needWork === 1)
    } else if (Array.isArray(res.data)) {
      competitionList.value = res.data.filter(c => c.needWork === 1)
    }
  } catch (error) {
    console.error('加载竞赛列表失败:', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getWorkList({ 
      competitionId: selectedCompetitionId.value, 
      page: page.value, 
      size: size.value 
    })
    if (res.data) {
      if (res.data.records !== undefined) {
        tableData.value = res.data.records || []
        total.value = res.data.total || 0
      } else if (Array.isArray(res.data)) {
        tableData.value = res.data
        total.value = res.data.length
      } else {
        tableData.value = []
        total.value = 0
      }
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const handlePreview = (row) => {
  currentWork.value = row
  previewDialogVisible.value = true
}

const handleDownload = async (row) => {
  try {
    if (!row.filePath) {
      ElMessage.error('文件路径不存在')
      return
    }
    console.log('下载文件路径:', row.filePath)
    const response = await downloadWork(row.filePath)
    console.log('下载响应:', response)
    
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
    link.download = row.fileName || '作品文件'
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

const handleMarkAbnormal = async (id) => {
  try {
    await ElMessageBox.confirm('确认标记该作品为异常？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateWorkStatus(id, 1)
    ElMessage.success('标记成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleMarkNormal = async (id) => {
  try {
    await updateWorkStatus(id, 0)
    ElMessage.success('操作成功')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadCompetitions()
  loadData()
})
</script>

<style scoped>
.work-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>


