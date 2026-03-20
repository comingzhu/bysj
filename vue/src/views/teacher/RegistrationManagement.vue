<template>
  <div class="registration-management">
    <el-card>
      <template #header>
        <span>报名管理</span>
      </template>
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="competitionName" label="竞赛名称" width="200">
          <template #default="{ row }">
            {{ row.competitionName || '未知竞赛' }}
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="报名人/团队" width="200">
          <template #default="{ row }">
            <div>
              <div>{{ row.userName || '未知用户' }}</div>
              <el-button 
                v-if="row.teamId" 
                type="primary" 
                link 
                size="small" 
                @click="handleViewTeamMembers(row.teamId)"
                style="padding: 0; margin-top: 4px;"
              >
                查看团队成员
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="报名状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="缴费状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.paymentStatus === 1 ? 'success' : 'info'">
              {{ row.paymentStatus === 1 ? '已缴费' : '未缴费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="报名时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="详情" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="handleApprove(row.id)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              size="small"
              @click="handleReject(row.id)"
            >
              驳回
            </el-button>
            <el-button
              v-if="row.status === 1 && row.paymentStatus === 1 && row.needWork === 0"
              :type="row.awardId ? 'info' : 'primary'"
              size="small"
              :disabled="!!row.awardId"
              @click="handleEnterScore(row)"
            >
              {{ row.awardId ? '已录入' : '录入成绩' }}
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

    <!-- 团队成员对话框 -->
    <el-dialog v-model="showTeamMembersDialog" title="团队成员" width="600px">
      <el-table :data="teamMembers" v-loading="teamMembersLoading" stripe>
        <el-table-column prop="realName" label="姓名" width="120">
          <template #default="{ row }">
            {{ row.realName || row.username }}
            <el-tag v-if="row.isLeader" type="warning" size="small" style="margin-left: 8px;">队长</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="college" label="学院" />
        <el-table-column prop="major" label="专业" />
      </el-table>
    </el-dialog>

    <!-- 报名详情（个人报名） -->
    <el-dialog v-model="detailDialogVisible" title="报名详情" width="700px">
      <div v-if="detailRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="竞赛名称" :span="2">{{ detailRow.competitionName || '未知竞赛' }}</el-descriptions-item>
          <el-descriptions-item label="报名人/团队">{{ detailRow.userName || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="报名时间">{{ formatDateTime(detailRow.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="报名状态">{{ getStatusText(detailRow.status) }}</el-descriptions-item>
          <el-descriptions-item label="缴费状态">
            {{ detailRow.paymentStatus === 1 ? '已缴费' : '未缴费' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div v-if="detailUser" style="margin-top: 16px;">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ detailUser.realName || detailUser.username }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ detailUser.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学院">{{ detailUser.college || '-' }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ detailUser.major || '-' }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ detailUser.className || '-' }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ detailUser.grade || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detailUser.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detailUser.email || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else-if="detailRow && !detailRow.teamId" style="margin-top: 16px; color: #909399;">
        未能获取到学生详细信息
      </div>
    </el-dialog>

    <!-- 录入成绩对话框 -->
    <el-dialog v-model="scoreDialogVisible" title="录入成绩" width="500px">
      <el-form :model="scoreForm" :rules="scoreRules" ref="scoreFormRef" label-width="120px">
        <el-form-item label="竞赛名称">
          <span>{{ scoreForm.competitionName || '未知竞赛' }}</span>
        </el-form-item>
        <el-form-item label="报名人/团队">
          <span>{{ scoreForm.userName || '未知' }}</span>
        </el-form-item>
        <el-form-item label="成绩" prop="score">
          <el-input-number
            v-model="scoreForm.score"
            :min="0"
            :max="100"
            :precision="2"
            :step="0.1"
            style="width: 100%"
            placeholder="请输入成绩（0-100）"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="scoreForm.comment"
            type="textarea"
            :rows="3"
            placeholder="可选：录入成绩备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveScore" :loading="savingScore">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRegistrationList, approveRegistration } from '../../api/registration'
import { getMyCompetitions, getCompetitionById } from '../../api/competition'
import { getTeamMembers } from '../../api/team'
import { getUserById } from '../../api/user'
import { createAward, getAwardList, updateAward } from '../../api/award'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '../../utils/dateFormat'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const showTeamMembersDialog = ref(false)
const teamMembers = ref([])
const teamMembersLoading = ref(false)
const detailDialogVisible = ref(false)
const detailRow = ref(null)
const detailUser = ref(null)
const scoreDialogVisible = ref(false)
const scoreFormRef = ref(null)
const savingScore = ref(false)
const scoreForm = ref({
  competitionId: null,
  competitionName: '',
  registrationId: null,
  awardId: null,
  userId: null,
  teamId: null,
  userName: '',
  score: null,
  comment: ''
})
const scoreRules = {
  score: [
    { required: true, message: '请输入成绩', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '成绩必须在0-100之间', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    // 先获取该老师发布的竞赛列表
    const compRes = await getMyCompetitions({ page: 1, size: 1000 })
    let competitionIds = []
    if (compRes.data && compRes.data.records) {
      competitionIds = compRes.data.records.map(c => c.id)
    } else if (Array.isArray(compRes.data)) {
      competitionIds = compRes.data.map(c => c.id)
    }
    
    // 如果没有发布的竞赛，直接返回空
    if (competitionIds.length === 0) {
      tableData.value = []
      total.value = 0
      loading.value = false
      return
    }
    
    // 查询这些竞赛的报名记录（需要分别查询每个竞赛的报名）
    let allRegistrations = []
    for (const compId of competitionIds) {
      try {
        const res = await getRegistrationList({ competitionId: compId, page: 1, size: 1000 })
        if (res.data && res.data.records) {
          allRegistrations.push(...res.data.records)
        } else if (Array.isArray(res.data)) {
          allRegistrations.push(...res.data)
        }
      } catch (error) {
        console.error('加载报名记录失败:', error)
      }
    }
    
    // 查询这些竞赛对应的获奖记录，用于判断是否已录入成绩
    const awardRes = await getAwardList(competitionIds)
    const awards = awardRes.data || []
    const awardMap = {}
    awards.forEach(a => {
      // 后端返回的是Map结构，需要根据registrationId字段匹配
      if (a.registrationId) {
        awardMap[a.registrationId] = a
      }
    })
    
    // 额外检查：打印获奖记录和映射情况
    console.log('获奖记录:', awards)
    console.log('获奖记录映射:', awardMap)

    // 为报名记录挂上 award 信息（是否已录入成绩）
    const enriched = allRegistrations.map(r => {
      const award = awardMap[r.id]
      return {
        ...r,
        awardId: award ? award.id : null,
        awardScore: award ? award.score : null,
        awardLevel: award ? award.awardLevel : null,
        awardRank: award ? award.rank : null
      }
    })
    
    // 前端分页
    total.value = enriched.length
    const start = (page.value - 1) * size.value
    const end = start + size.value
    tableData.value = enriched.slice(start, end)
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const handleApprove = async (id) => {
  try {
    await approveRegistration(id, 1, '')
    ElMessage.success('审核通过')
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
    await approveRegistration(id, 2, value)
    ElMessage.success('已驳回')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
    }
  }
}

const handleViewTeamMembers = async (teamId) => {
  showTeamMembersDialog.value = true
  teamMembersLoading.value = true
  try {
    const res = await getTeamMembers(teamId)
    teamMembers.value = res.data || []
  } catch (error) {
    ElMessage.error('加载团队成员失败：' + (error.response?.data?.message || error.message))
    teamMembers.value = []
  } finally {
    teamMembersLoading.value = false
  }
}

const handleViewDetail = async (row) => {
  // 团队报名：直接复用团队成员弹窗
  if (row.teamId) {
    await handleViewTeamMembers(row.teamId)
    return
  }

  detailRow.value = row
  detailUser.value = null
  detailDialogVisible.value = true

  if (!row.userId) return
  try {
    const res = await getUserById(row.userId)
    detailUser.value = res.data || null
  } catch (error) {
    detailUser.value = null
  }
}

const handleEnterScore = async (row) => {
  // 获取竞赛信息，确认是否需要作品
  try {
    const compRes = await getCompetitionById(row.competitionId)
    const competition = compRes.data
    if (competition && competition.needWork === 1) {
      ElMessage.warning('该竞赛需要提交作品，请通过评分任务进行评分')
      return
    }
  } catch (error) {
    ElMessage.error('获取竞赛信息失败')
    return
  }

  scoreForm.value = {
    competitionId: row.competitionId,
    competitionName: row.competitionName || '未知竞赛',
    registrationId: row.id,
    awardId: row.awardId || null,
    userId: row.userId || null,
    teamId: row.teamId || null,
    userName: row.userName || '未知',
    score: row.awardScore ?? null,
    comment: ''
  }
  scoreDialogVisible.value = true
}

const handleSaveScore = async () => {
  await scoreFormRef.value.validate(async (valid) => {
    if (!valid) return

    savingScore.value = true
    try {
      const awardData = {
        competitionId: scoreForm.value.competitionId,
        registrationId: scoreForm.value.registrationId,
        userId: scoreForm.value.userId,
        teamId: scoreForm.value.teamId,
        score: scoreForm.value.score,
        awardLevel: null, // 暂不设置奖项等级，后续在获奖管理页面可以设置
        rank: null // 暂不设置排名，后续在获奖管理页面可以设置
      }

      if (scoreForm.value.awardId) {
        // 已存在获奖记录，修改成绩
        awardData.id = scoreForm.value.awardId
        await updateAward(awardData)
        ElMessage.success('成绩修改成功')
      } else {
        // 首次录入成绩，创建获奖记录
        await createAward(awardData)
        ElMessage.success('成绩录入成功，可在获奖管理页面设置排名和奖项等级')
      }

      scoreDialogVisible.value = false
      await loadData()
    } catch (error) {
      ElMessage.error('录入失败：' + (error.response?.data?.message || error.message))
    } finally {
      savingScore.value = false
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.registration-management {
  padding: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>

