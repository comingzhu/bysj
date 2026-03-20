<template>
  <div class="competition-detail">
    <el-card v-loading="loading">
      <template #header>
        <span>{{ competition.name }}</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="竞赛名称">{{ competition.name }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ competition.category }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ competition.type === 0 ? '个人赛' : '团队赛' }}</el-descriptions-item>
        <el-descriptions-item label="报名费">¥{{ competition.registrationFee }}</el-descriptions-item>
        <el-descriptions-item label="报名开始">{{ formatDateTime(competition.registrationStart) }}</el-descriptions-item>
        <el-descriptions-item label="报名结束">{{ formatDateTime(competition.registrationEnd) }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(competition.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(competition.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="地点">{{ competition.location }}</el-descriptions-item>
        <el-descriptions-item label="是否需要作品">{{ competition.needWork === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          <div v-html="competition.description" class="rich-text"></div>
        </el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-tooltip :content="getRegisterTooltip" placement="top">
          <el-button
            type="primary"
            @click="showRegisterDialog = true"
            :disabled="!canRegister"
          >
            {{ getRegisterButtonText }}
          </el-button>
        </el-tooltip>
      </div>
    </el-card>

    <!-- 报名对话框 -->
    <el-dialog v-model="showRegisterDialog" :title="competition.type === 0 ? '个人报名' : '团队报名'" width="600px">
      <template v-if="competition.type === 0">
        <!-- 个人赛报名 -->
        <el-form :model="registerForm" label-width="100px">
          <el-form-item label="竞赛名称">
            <span>{{ competition.name }}</span>
          </el-form-item>
          <el-form-item label="报名费">
            <span style="color: #F56C6C; font-weight: bold;">¥{{ competition.registrationFee }}</span>
          </el-form-item>
        </el-form>
      </template>
      <template v-else>
        <!-- 团队赛报名 -->
        <el-tabs v-model="teamTab">
          <el-tab-pane label="创建团队" name="create">
            <el-form :model="createTeamForm" :rules="teamRules" ref="createTeamFormRef" label-width="100px">
              <el-form-item label="团队名称" prop="name">
                <el-input v-model="createTeamForm.name" placeholder="请输入团队名称" />
              </el-form-item>
              <el-form-item label="最大人数" prop="maxMembers">
                <el-input-number v-model="createTeamForm.maxMembers" :min="2" :max="10" />
                <span style="margin-left: 10px; color: #909399;">人</span>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="加入团队" name="join">
            <el-form :model="joinTeamForm" label-width="100px">
              <el-form-item label="搜索团队">
                <el-input v-model="joinTeamForm.keyword" placeholder="输入团队名称搜索" @keyup.enter="searchTeams">
                  <template #append>
                    <el-button @click="searchTeams">搜索</el-button>
                  </template>
                </el-input>
              </el-form-item>
            </el-form>
            <div v-loading="teamSearchLoading" style="margin-top: 20px;">
              <div v-if="teamList.length === 0" class="empty-state">
                <el-empty description="暂无团队，请先创建团队" />
              </div>
              <div v-else>
                <div 
                  v-for="team in teamList" 
                  :key="team.id"
                  class="team-item"
                  :class="{ 'selected': joinTeamForm.selectedTeamId === team.id }"
                  @click="joinTeamForm.selectedTeamId = team.id"
                >
                  <div class="team-info">
                    <div class="team-name">{{ team.name }}</div>
                    <div class="team-meta">
                      <span>成员：{{ team.currentMembers }}/{{ team.maxMembers }}</span>
                    </div>
                  </div>
                  <el-icon v-if="joinTeamForm.selectedTeamId === team.id" class="check-icon"><Check /></el-icon>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </template>
      <template #footer>
        <el-button @click="showRegisterDialog = false">取消</el-button>
        <el-button type="primary" @click="handleRegister" :loading="registering">确认报名</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCompetitionById } from '../../api/competition'
import { register, getMyRegistrations } from '../../api/registration'
import { createTeam, searchTeams as searchTeamsApi, joinTeam } from '../../api/team'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { formatDateTime } from '../../utils/dateFormat'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const competition = ref({})
const hasRegistered = ref(false)
const registrationStatus = ref(null) // 存储报名状态：0-待审核，1-已通过，2-已驳回
const rejectReason = ref('') // 存储驳回理由
const showRegisterDialog = ref(false)
const registering = ref(false)
const teamTab = ref('create')
const createTeamFormRef = ref(null)
const createTeamForm = ref({
  name: '',
  maxMembers: 5
})
const joinTeamForm = ref({
  keyword: '',
  selectedTeamId: null
})
const teamList = ref([])
const teamSearchLoading = ref(false)

// 是否可以报名：未报名或已被驳回 + 在报名时间和比赛时间允许范围内
const canRegister = computed(() => {
  if (!competition.value || !competition.value.id) return false
  // 已报名但未被驳回的，不能再次报名
  if (hasRegistered.value && registrationStatus.value !== 2) return false

  const now = new Date()
  const regStart = competition.value.registrationStart ? new Date(competition.value.registrationStart) : null
  const regEnd = competition.value.registrationEnd ? new Date(competition.value.registrationEnd) : null
  const endTime = competition.value.endTime ? new Date(competition.value.endTime) : null

  // 报名未开始或已结束，不能报名
  if (regStart && now < regStart) return false
  if (regEnd && now > regEnd) return false

  // 比赛已结束，也不能报名
  if (endTime && now > endTime) return false

  return true
})

// 获取报名按钮文本
const getRegisterButtonText = computed(() => {
  if (!competition.value || !competition.value.id) return '立即报名'
  if (hasRegistered.value) {
    if (registrationStatus.value === 0) return '待审核'
    if (registrationStatus.value === 1) return '已通过'
    if (registrationStatus.value === 2) return '重新报名'
    return '已报名'
  }

  const now = new Date()
  const regStart = competition.value.registrationStart ? new Date(competition.value.registrationStart) : null
  const regEnd = competition.value.registrationEnd ? new Date(competition.value.registrationEnd) : null
  const endTime = competition.value.endTime ? new Date(competition.value.endTime) : null

  // 报名未开始
  if (regStart && now < regStart) return '报名未开始'
  // 报名已结束
  if (regEnd && now > regEnd) return '报名时间已结束'
  // 比赛已结束
  if (endTime && now > endTime) return '比赛已结束'

  return '立即报名'
})

// 获取报名按钮提示信息
const getRegisterTooltip = computed(() => {
  if (!competition.value || !competition.value.id) return ''
  if (hasRegistered.value) {
    if (registrationStatus.value === 0) return '您的报名正在审核中'
    if (registrationStatus.value === 1) return '您的报名已通过审核'
    if (registrationStatus.value === 2) {
      return rejectReason.value ? `您的报名已被驳回，理由：${rejectReason.value}，点击重新报名` : '您的报名已被驳回，点击重新报名'
    }
    return '您已报名该竞赛'
  }

  const now = new Date()
  const regStart = competition.value.registrationStart ? new Date(competition.value.registrationStart) : null
  const regEnd = competition.value.registrationEnd ? new Date(competition.value.registrationEnd) : null
  const endTime = competition.value.endTime ? new Date(competition.value.endTime) : null

  // 报名未开始
  if (regStart && now < regStart) return `报名开始时间：${formatDateTime(competition.value.registrationStart)}`
  // 报名已结束
  if (regEnd && now > regEnd) return '报名时间已结束'
  // 比赛已结束
  if (endTime && now > endTime) return '比赛已结束，无法报名'

  return '点击报名'
})

const teamRules = {
  name: [
    { required: true, message: '请输入团队名称', trigger: 'blur' },
    { min: 2, max: 20, message: '团队名称长度为2-20个字符', trigger: 'blur' },
    {
      validator: async (rule, value, callback) => {
        if (!value) {
          callback()
          return
        }
        try {
          // 调用后端API检查团队名称是否已存在
          const res = await searchTeamsApi(competition.value.id, value)
          const teams = res.data || []
          const exists = teams.some(team => team.name === value)
          if (exists) {
            callback(new Error('该竞赛中已存在同名团队'))
          } else {
            callback()
          }
        } catch (error) {
          console.error('检查团队名称失败:', error)
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  maxMembers: [
    { required: true, message: '请设置最大人数', trigger: 'blur' },
    { type: 'number', min: 2, max: 10, message: '人数范围为2-10人', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCompetitionById(route.params.id)
    competition.value = res.data
    // 检查是否已报名
    await checkRegistrationStatus()
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const checkRegistrationStatus = async () => {
  try {
    const res = await getMyRegistrations({ page: 1, size: 1000 })
    const registrations = res.data?.records || res.data || []
    // 检查是否有该竞赛的报名记录（个人或团队），包括被驳回的记录
    const regRecord = registrations.find(r => {
      if (r.competitionId === competition.value.id) {
        // 个人赛：有报名记录即可
        if (competition.value.type === 0) {
          return true
        }
        // 团队赛：检查是否是团队成员
        if (competition.value.type === 1 && r.teamId) {
          return true
        }
      }
      return false
    })
    
    if (regRecord) {
      hasRegistered.value = true
      registrationStatus.value = regRecord.status
      rejectReason.value = regRecord.rejectReason || ''
    } else {
      hasRegistered.value = false
      registrationStatus.value = null
      rejectReason.value = ''
    }
  } catch (error) {
    console.error('检查报名状态失败:', error)
    hasRegistered.value = false
    registrationStatus.value = null
    rejectReason.value = ''
  }
}

const searchTeams = async () => {
  if (!joinTeamForm.value.keyword.trim()) {
    ElMessage.warning('请输入团队名称')
    return
  }
  teamSearchLoading.value = true
  try {
    const res = await searchTeamsApi(competition.value.id, joinTeamForm.value.keyword)
    teamList.value = res.data || []
  } catch (error) {
    ElMessage.error('搜索失败：' + (error.response?.data?.message || error.message))
  } finally {
    teamSearchLoading.value = false
  }
}

const handleRegister = async () => {
  if (competition.value.type === 1) {
    // 团队赛
    if (teamTab.value === 'create') {
      // 创建团队
      await createTeamFormRef.value.validate(async (valid) => {
        if (valid) {
          registering.value = true
          try {
            const teamRes = await createTeam({
              name: createTeamForm.value.name,
              competitionId: competition.value.id,
              maxMembers: createTeamForm.value.maxMembers
            })
            const teamId = teamRes.data.id
            // 队长创建团队后可以直接报名
            await register({ competitionId: competition.value.id, teamId })
            ElMessage.success('团队创建成功，报名成功，等待审核')
            showRegisterDialog.value = false
            hasRegistered.value = true
            router.push('/my/registrations')
          } catch (error) {
            ElMessage.error(error.response?.data?.message || error.message || '报名失败')
          } finally {
            registering.value = false
          }
        }
      })
    } else {
      // 加入团队
      if (!joinTeamForm.value.selectedTeamId) {
        ElMessage.warning('请选择要加入的团队')
        return
      }
      registering.value = true
      try {
        // 先申请加入团队
        await joinTeam(joinTeamForm.value.selectedTeamId)
        ElMessage.success('申请已提交，等待队长审核。审核通过后，队长会为团队报名。')
        showRegisterDialog.value = false
        router.push('/my/registrations')
      } catch (error) {
        ElMessage.error(error.response?.data?.message || error.message || '申请失败')
      } finally {
        registering.value = false
      }
    }
  } else {
    // 个人赛
    registering.value = true
    try {
      await register({ competitionId: competition.value.id })
      ElMessage.success('报名成功，等待审核')
      showRegisterDialog.value = false
      hasRegistered.value = true
      router.push('/my/registrations')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || error.message || '报名失败')
    } finally {
      registering.value = false
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.competition-detail {
  padding: 20px;
}

.actions {
  margin-top: 20px;
  text-align: center;
}

.team-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  margin-bottom: 12px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.team-item:hover {
  border-color: #409EFF;
  background-color: #f5f7fa;
}

.team-item.selected {
  border-color: #409EFF;
  background-color: #ecf5ff;
}

.team-info {
  flex: 1;
}

.team-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.team-meta {
  font-size: 14px;
  color: #909399;
}

.check-icon {
  color: #409EFF;
  font-size: 20px;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.rich-text {
  line-height: 1.6;
  font-size: 14px;
}

.rich-text h1 {
  font-size: 20px;
  font-weight: bold;
  margin: 16px 0 8px 0;
  color: #303133;
}

.rich-text h2 {
  font-size: 18px;
  font-weight: bold;
  margin: 14px 0 6px 0;
  color: #303133;
}

.rich-text p {
  margin: 8px 0;
  color: #606266;
}

.rich-text ul,
.rich-text ol {
  margin: 8px 0;
  padding-left: 24px;
  color: #606266;
}

.rich-text li {
  margin: 4px 0;
}

.rich-text strong {
  font-weight: bold;
  color: #303133;
}
</style>

