<template>
  <div class="competition-audit">
    <el-card>
      <template #header>
        <span>竞赛管理</span>
      </template>
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="name" label="竞赛名称" />
        <el-table-column prop="publisherName" label="发布者" width="120">
          <template #default="{ row }">
            {{ row.publisherName || '系统' }}
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
        <el-table-column label="操作" width="320">
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

    <!-- 选择评委对话框 -->
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

    <!-- 竞赛详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="竞赛详情" width="800px">
      <el-descriptions v-if="currentCompetition" :column="2" border>
        <el-descriptions-item label="竞赛名称" :span="2">{{ currentCompetition.name }}</el-descriptions-item>
        <el-descriptions-item label="发布者">{{ currentCompetition.publisherName || '系统' }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentCompetition.category || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentCompetition.type === 0 ? '个人赛' : '团队赛' }}</el-descriptions-item>
        <el-descriptions-item label="是否需要作品">{{ currentCompetition.needWork === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="报名费">¥{{ currentCompetition.registrationFee || 0 }}</el-descriptions-item>
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
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCompetitionList, approveCompetition, getCompetitionById, getJudgesByCategory } from '../../api/competition'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '../../utils/dateFormat'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const judgeDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentCompetition = ref(null)
const judgeOptions = ref([])
const selectedJudgeIds = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCompetitionList({ status: 1, isSystem: 0, page: page.value, size: size.value })
    // 处理返回的数据格式
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
    // 获取竞赛详情以便拿到分类
    const detailRes = await getCompetitionById(row.id)
    currentCompetition.value = detailRes.data || row
    
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
    // 按分类加载可选评委
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
    ElMessage.error(error.response?.data?.message || error.message || '操作失败')
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
      ElMessage.error('操作失败')
    }
  }
}

const handleViewDetail = async (id) => {
  try {
    const res = await getCompetitionById(id)
    if (res.data) {
      currentCompetition.value = res.data
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
.competition-audit {
  padding: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>

