<template>
  <div class="school-competition-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>所有校赛管理</span>
          <div>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索竞赛名称"
              style="width: 300px; margin-right: 10px;"
              clearable
              @keyup.enter="loadData"
            >
              <template #append>
                <el-button @click="loadData">搜索</el-button>
              </template>
            </el-input>
            <el-select v-model="filterStatus" placeholder="筛选状态" style="width: 150px; margin-right: 10px;" clearable @change="loadData">
              <el-option label="全部" :value="undefined" />
              <el-option label="草稿" :value="0" />
              <el-option label="待审核" :value="1" />
              <el-option label="已通过" :value="2" />
              <el-option label="已驳回" :value="3" />
              <el-option label="已发布" :value="4" />
              <el-option label="已结束" :value="5" />
              <el-option label="已暂停" :value="6" />
            </el-select>
          </div>
        </div>
      </template>
      
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="竞赛名称" width="200" />
        <el-table-column prop="publisherName" label="发布者" width="120">
          <template #default="{ row }">
            {{ row.publisherName || '系统' }}
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 0 ? 'info' : 'success'">
              {{ row.type === 0 ? '个人赛' : '团队赛' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registrationFee" label="报名费" width="100">
          <template #default="{ row }">
            ¥{{ row.registrationFee }}
          </template>
        </el-table-column>
        <el-table-column prop="registrationStart" label="报名开始" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.registrationStart) }}
          </template>
        </el-table-column>
        <el-table-column prop="registrationEnd" label="报名结束" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.registrationEnd) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="success"
              size="small"
              @click="openJudgeDialog(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="danger"
              size="small"
              @click="handleReject(row.id)"
            >
              驳回
            </el-button>
            <el-button
              v-if="row.status === 2"
              type="primary"
              size="small"
              @click="handlePublish(row.id)"
            >
              发布
            </el-button>
            <el-button
              v-if="row.status === 4"
              type="warning"
              size="small"
              @click="handlePause(row.id)"
            >
              暂停
            </el-button>
            <el-button
              v-if="row.status === 6"
              type="success"
              size="small"
              @click="handleResume(row.id)"
            >
              恢复
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="handleViewDetail(row.id)"
            >
              详情
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="竞赛详情" width="800px">
      <el-descriptions v-if="currentCompetition" :column="2" border>
        <el-descriptions-item label="竞赛名称" :span="2">{{ currentCompetition.name }}</el-descriptions-item>
        <el-descriptions-item label="发布者">{{ currentCompetition.publisherName || '系统' }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentCompetition.category }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentCompetition.type === 0 ? '个人赛' : '团队赛' }}</el-descriptions-item>
        <el-descriptions-item label="是否需要作品">{{ currentCompetition.needWork === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="报名费">¥{{ currentCompetition.registrationFee }}</el-descriptions-item>
        <el-descriptions-item label="报名开始时间" :span="2">{{ formatDateTime(currentCompetition.registrationStart) }}</el-descriptions-item>
        <el-descriptions-item label="报名结束时间" :span="2">{{ formatDateTime(currentCompetition.registrationEnd) }}</el-descriptions-item>
        <el-descriptions-item label="开始时间" :span="2">{{ formatDateTime(currentCompetition.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间" :span="2">{{ formatDateTime(currentCompetition.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="地点" :span="2">{{ currentCompetition.location || '无' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          <div style="white-space: pre-wrap;">{{ currentCompetition.description || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag :type="getStatusType(currentCompetition.status)">
            {{ getStatusText(currentCompetition.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="已分配评委" :span="2">
          <template v-if="currentCompetition.judges && currentCompetition.judges.length">
            <el-tag
              v-for="j in currentCompetition.judges"
              :key="j.id"
              style="margin-right: 8px; margin-bottom: 4px;"
            >
              {{ j.realName || j.username }}（{{ j.college || '未知学院' }}）
            </el-tag>
          </template>
          <span v-else>暂无评委配置</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 审核通过时选择评委 -->
    <el-dialog v-model="judgeDialogVisible" title="选择评委" width="600px">
      <div v-if="currentCompetition">
        <p style="margin-bottom: 10px;">
          竞赛：<strong>{{ currentCompetition.name }}</strong>
        </p>
        <p style="margin-bottom: 10px;">
          分类：<strong>{{ currentCompetition.category || '未设置' }}</strong>
        </p>
        <el-alert
          title="请根据竞赛分类选择对应的评分员，最少选择3人"
          type="info"
          show-icon
          :closable="false"
          style="margin-bottom: 10px;"
        />
        <el-checkbox-group v-model="selectedJudgeIds">
          <el-checkbox
            v-for="judge in judgeOptions"
            :key="judge.id"
            :label="judge.id"
          >
            {{ judge.realName || judge.username }}（{{ judge.college || '未知学院' }}）
          </el-checkbox>
        </el-checkbox-group>
        <div v-if="judgeOptions.length === 0" style="color: #f56c6c; margin-top: 10px;">
          当前分类下暂无已配置的评分员，请先在数据库中维护 judge_category。
        </div>
      </div>
      <template #footer>
        <el-button @click="judgeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleApproveWithJudges">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCompetitionList, getCompetitionById, approveCompetition, publishCompetition, pauseCompetition, resumeCompetition, getJudgesByCategory, getJudgesByCompetition } from '../../api/competition'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '../../utils/dateFormat'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const filterStatus = ref(undefined)

const detailDialogVisible = ref(false)
const currentCompetition = ref(null)
const judgeDialogVisible = ref(false)
const judgeOptions = ref([])
const selectedJudgeIds = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      isSystem: 0, // 只显示校赛
      page: page.value,
      size: size.value
    }
    if (filterStatus.value !== undefined) {
      params.status = filterStatus.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    const res = await getCompetitionList(params)
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
    ElMessage.error('加载失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => {
  const map = { 0: '草稿', 1: '待审核', 2: '已通过', 3: '已驳回', 4: '已发布', 5: '已结束', 6: '已暂停' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'success', 5: 'info', 6: 'warning' }
  return map[status] || 'info'
}

const openJudgeDialog = async (row) => {
  try {
    const res = await getCompetitionById(row.id)
    currentCompetition.value = res.data || row
    
    // 如果是不需要作品的校赛，直接通过审核，不需要选择评委
    if (currentCompetition.value.needWork === 0) {
      await ElMessageBox.confirm(
        '该竞赛不需要提交作品，审核通过后由发布老师手动录入成绩。确认通过审核？',
        '确认审核',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }
      )
      await approveCompetition(currentCompetition.value.id, 2, '', '')
      ElMessage.success('审核通过')
      loadData()
      return
    }
    
    // 需要作品的校赛，需要选择评委
    selectedJudgeIds.value = []
    judgeOptions.value = []

    if (!currentCompetition.value.category) {
      ElMessage.error('该竞赛未设置分类，无法筛选评委')
      return
    }
    const judgeRes = await getJudgesByCategory(currentCompetition.value.category)
    const list = judgeRes.data || []
    // 去重，避免同一评委被返回多次
    const map = new Map()
    list.forEach(j => {
      if (j && j.id != null && !map.has(j.id)) {
        map.set(j.id, j)
      }
    })
    judgeOptions.value = Array.from(map.values())
    judgeDialogVisible.value = true
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
    }
  }
}

const handleApproveWithJudges = async () => {
  if (!currentCompetition.value) {
    ElMessage.error('无效的竞赛信息')
    return
  }
  if (!selectedJudgeIds.value || selectedJudgeIds.value.length < 3) {
    ElMessage.warning('请至少选择3名评委')
    return
  }
  try {
    await approveCompetition(currentCompetition.value.id, 2, '', selectedJudgeIds.value)
    ElMessage.success('审核通过并已配置评委')
    judgeDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  }
}

const handleReject = async (id) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await approveCompetition(id, 3, value)
    ElMessage.success('已驳回')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
    }
  }
}

const handlePublish = async (id) => {
  try {
    await publishCompetition(id)
    ElMessage.success('发布成功')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  }
}

const handlePause = async (id) => {
  try {
    await pauseCompetition(id)
    ElMessage.success('已暂停')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  }
}

const handleResume = async (id) => {
  try {
    await resumeCompetition(id)
    ElMessage.success('已恢复')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  }
}

const handleViewDetail = async (id) => {
  try {
    const res = await getCompetitionById(id)
    if (res.data) {
      currentCompetition.value = res.data
      try {
        const judgeRes = await getJudgesByCompetition(id)
        currentCompetition.value.judges = judgeRes.data || []
      } catch (e) {
        currentCompetition.value.judges = []
      }
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败：' + (error.response?.data?.message || error.message))
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.school-competition-management {
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
</style>




