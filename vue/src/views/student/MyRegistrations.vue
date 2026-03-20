<template>
  <div class="my-registrations">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的报名</span>
          <div class="balance-info">
            <span class="balance-label">账户余额：</span>
            <span class="balance-value">¥{{ balance.toFixed(2) }}</span>
            <el-button type="primary" size="small" @click="showRechargeDialog = true" style="margin-left: 12px;">
              充值
            </el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="competitionName" label="竞赛名称" width="200">
          <template #default="{ row }">
            {{ row.competitionName || '未知竞赛' }}
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
            <el-tag v-if="row.paymentStatus === 1" type="success">已缴费</el-tag>
            <el-tag v-else-if="row.paymentStatus === 2" type="warning">已退款</el-tag>
            <el-tag v-else type="info">未缴费</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentAmount" label="报名费" width="100">
          <template #default="{ row }">
            <!-- 优先显示当前竞赛的报名费，其次显示报名记录中的金额，保证修改收费后列表展示正确 -->
            ¥{{ row.registrationFee ?? row.paymentAmount ?? 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="报名时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="驳回理由" width="180">
          <template #default="{ row }">
            <el-tooltip v-if="row.status === 2 && row.rejectReason" :content="row.rejectReason" placement="top">
              <span class="reject-reason">{{ row.rejectReason.length > 20 ? row.rejectReason.substring(0, 20) + '...' : row.rejectReason }}</span>
            </el-tooltip>
            <span v-else-if="row.status === 2 && !row.rejectReason">无</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="作品状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.needWork === 1 && row.workSubmitted" type="success">已提交</el-tag>
            <el-tag
              v-else-if="row.needWork === 1
                && row.status === 1
                && (row.paymentStatus === 1 || (row.registrationFee === 0 || row.registrationFee == null))
                && isSubmissionOpen(row)"
              type="warning"
            >
              待提交
            </el-tag>
            <span v-else-if="row.needWork === 0" style="color: #909399;">不需要</span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <!-- 需要缴费且未缴费或已退款时显示缴费按钮 -->
            <el-button
              v-if="row.status === 1 && (row.paymentStatus === 0 || row.paymentStatus === 2) && (row.registrationFee > 0 || row.paymentAmount > 0)"
              type="primary"
              size="small"
              @click="handlePay(row.id)"
            >
              缴费
            </el-button>
            <!-- 提交作品按钮：已通过审核 && (已缴费 || 报名费为0) && 需要作品 && 未提交 && 在允许的提交时间内 -->
            <el-tooltip
              v-if="row.status === 1
                && (row.paymentStatus === 1 || (row.registrationFee === 0 || row.registrationFee == null))
                && row.needWork === 1
                && !row.workSubmitted
                && isSubmissionOpen(row)"
              :content="getSubmitWorkTooltip(row)"
              placement="top"
            >
              <span>
                <el-button
                  type="success"
                  size="small"
                  :disabled="!canSubmitWork(row)"
                  @click="handleSubmitWork(row)"
                >
                  提交作品
                </el-button>
              </span>
            </el-tooltip>
            <!-- 查看作品按钮：只要已提交作品就可以查看，与时间无关 -->
            <el-button
              v-if="row.status === 1 && (row.paymentStatus === 1 || (row.registrationFee === 0 || row.registrationFee == null)) && row.needWork === 1 && row.workSubmitted"
              type="info"
              size="small"
              @click="handleViewWork(row)"
            >
              查看作品
            </el-button>
            <!-- 修改作品按钮：已通过审核 && (已缴费 || 报名费为0) && 需要作品 && 已提交 && 在允许的提交时间内 -->
            <el-button
              v-if="row.status === 1
                && (row.paymentStatus === 1 || (row.registrationFee === 0 || row.registrationFee == null))
                && row.needWork === 1
                && row.workSubmitted
                && isSubmissionOpen(row)"
              type="warning"
              size="small"
              @click="handleSubmitWork(row)"
            >
              修改作品
            </el-button>
            <el-button
              v-if="row.teamId"
              type="primary"
              size="small"
              @click="handleManageTeam(row)"
            >
              团队管理
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

    <!-- 作品提交对话框 -->
    <el-dialog 
      v-model="showWorkDialog" 
      :title="currentRegistration?.workSubmitted ? '修改作品' : '提交作品'" 
      width="700px"
    >
      <el-form :model="workForm" :rules="workRules" ref="workFormRef" label-width="100px">
        <el-form-item label="竞赛名称">
          <span>{{ currentRegistration?.competitionName }}</span>
        </el-form-item>
        <el-form-item label="作品标题" prop="title">
          <el-input v-model="workForm.title" placeholder="请输入作品标题" />
        </el-form-item>
        <el-form-item label="作品描述" prop="description">
          <el-input 
            v-model="workForm.description" 
            type="textarea" 
            :rows="4"
            placeholder="请输入作品描述"
          />
        </el-form-item>
        <el-form-item label="作品文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="fileList"
            :limit="1"
            accept=".pdf,.doc,.docx,.zip,.rar,.jpg,.jpeg,.png"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持格式：PDF、Word、压缩包、图片（单个文件不超过50MB）
              </div>
            </template>
          </el-upload>
          <div v-if="workForm.fileName && !workForm.file" style="margin-top: 8px; color: #909399;">
            当前文件：{{ workForm.fileName }} ({{ formatFileSize(workForm.fileSize) }})
            <span style="margin-left: 8px; color: #E6A23C;">重新选择文件将替换当前文件</span>
          </div>
          <div v-else-if="workForm.fileName && workForm.file" style="margin-top: 8px; color: #67C23A;">
            已选择：{{ workForm.fileName }} ({{ formatFileSize(workForm.fileSize) }})
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showWorkDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitWorkConfirm" :loading="submittingWork">
          {{ currentRegistration?.workSubmitted ? '确认修改' : '确认提交' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看作品对话框 -->
    <el-dialog v-model="showWorkViewDialog" title="查看作品" width="700px">
      <div v-if="currentWork">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="竞赛名称" :span="2">{{ currentWork.competitionName }}</el-descriptions-item>
          <el-descriptions-item label="作品标题" :span="2">{{ currentWork.title }}</el-descriptions-item>
          <el-descriptions-item label="作品描述" :span="2">{{ currentWork.description || '无' }}</el-descriptions-item>
          <el-descriptions-item label="文件名">{{ currentWork.fileName }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ formatFileSize(currentWork.fileSize) }}</el-descriptions-item>
          <el-descriptions-item label="提交时间" :span="2">{{ formatDateTime(currentWork.submitTime) }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 20px; text-align: center;">
          <el-button type="primary" @click="handleDownloadWork(currentWork)">下载作品</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 团队管理对话框 -->
    <el-dialog v-model="showTeamManageDialog" title="团队管理" width="800px">
      <el-tabs v-model="teamManageTab">
        <el-tab-pane label="团队成员" name="members">
          <div v-loading="teamMembersLoading">
            <el-table :data="teamMembers" border>
              <el-table-column prop="realName" label="姓名" width="120" />
              <el-table-column prop="studentNo" label="学号" width="150" />
              <el-table-column prop="college" label="学院" width="150" />
              <el-table-column prop="major" label="专业" width="150" />
              <el-table-column label="角色" width="100">
                <template #default="{ row }">
                  <el-tag v-if="row.isLeader" type="warning">队长</el-tag>
                  <el-tag v-else type="info">成员</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        <el-tab-pane label="待审核申请" name="pending">
          <div v-if="!isTeamLeader(currentTeamId)" style="margin-top: 20px;">
            <el-alert type="warning" :closable="false">
              只有队长可以审核申请
            </el-alert>
          </div>
          <div v-else v-loading="pendingMembersLoading">
            <el-empty v-if="pendingMembers.length === 0" description="暂无待审核申请" />
            <el-table v-else :data="pendingMembers" border>
              <el-table-column prop="realName" label="姓名" width="120" />
              <el-table-column prop="studentNo" label="学号" width="150" />
              <el-table-column prop="college" label="学院" width="150" />
              <el-table-column prop="major" label="专业" width="150" />
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button type="success" size="small" @click="handleApproveMember(row.id, 1)">通过</el-button>
                  <el-button type="danger" size="small" @click="handleApproveMember(row.id, 2)">拒绝</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        <el-tab-pane label="邀请成员" name="invite">
          <div v-if="!isTeamLeader(currentTeamId)">
            <el-alert type="warning" :closable="false" style="margin-top: 20px;">
              只有队长可以邀请成员
            </el-alert>
          </div>
          <el-form v-else :model="inviteForm" label-width="100px" style="margin-top: 20px;">
            <el-form-item label="学号">
              <el-input v-model="inviteForm.studentNo" placeholder="请输入要邀请的学生学号" style="width: 300px;">
                <template #append>
                  <el-button @click="handleInviteMember" :loading="inviting">邀请</el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-text type="info" size="small">输入学生的学号，邀请后该学生将自动加入团队</el-text>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 充值对话框 -->
    <el-dialog v-model="showRechargeDialog" title="账户充值" width="500px">
      <el-form :model="rechargeForm" label-width="100px">
        <el-form-item label="当前余额">
          <span style="font-size: 18px; font-weight: bold; color: #409EFF;">¥{{ balance.toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="充值金额" required>
          <el-input-number 
            v-model="rechargeForm.amount" 
            :min="1" 
            :max="10000" 
            :precision="2"
            :step="10"
            style="width: 100%;"
            placeholder="请输入充值金额"
          />
        </el-form-item>
        <el-form-item label="充值后余额">
          <span style="font-size: 16px; color: #67C23A;">
            ¥{{ ((balance + (rechargeForm.amount || 0)).toFixed(2)) }}
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRechargeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleRecharge" :loading="recharging">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getMyRegistrations, pay } from '../../api/registration'
import { getBalance, rechargeBalance } from '../../api/user'
import { submitWork, uploadFile, getMyWorks, downloadWork, getWorkDetail } from '../../api/work'
import { getTeamMembers, getPendingMembers, approveTeamMember, inviteMember, getMyTeams } from '../../api/team'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '../../utils/dateFormat'
import { useUserStore } from '../../stores/user'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const balance = ref(0)
const showRechargeDialog = ref(false)
const recharging = ref(false)
const rechargeForm = ref({
  amount: 0
})
const showWorkDialog = ref(false)
const submittingWork = ref(false)
const workFormRef = ref(null)
const uploadRef = ref(null)
const fileList = ref([])
const currentRegistration = ref(null)
const showWorkViewDialog = ref(false)
const currentWork = ref(null)
const showTeamManageDialog = ref(false)
const teamManageTab = ref('members')
const currentTeamId = ref(null)
const currentCompetitionId = ref(null)
const teamMembers = ref([])
const teamMembersLoading = ref(false)
const pendingMembers = ref([])
const pendingMembersLoading = ref(false)
const inviteForm = ref({
  studentNo: ''
})
const inviting = ref(false)
const myTeams = ref([])
const workForm = ref({
  title: '',
  description: '',
  file: null,
  fileName: '',
  fileSize: 0
})

const workRules = {
  title: [
    { required: true, message: '请输入作品标题', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入作品描述', trigger: 'blur' }
  ],
  file: [
    { 
      validator: (rule, value, callback) => {
        // 如果是修改作品且没有选择新文件，允许通过（使用原有文件）
        if (currentRegistration.value?.workSubmitted && !workForm.value.file) {
          callback()
        } else if (!workForm.value.file) {
          callback(new Error('请选择作品文件'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    // 加载余额
    const balanceRes = await getBalance()
    if (balanceRes.data) {
      balance.value = balanceRes.data.balance || 0
    }
    
    // 加载报名记录（需要包含竞赛信息，特别是needWork字段）
    const res = await getMyRegistrations({ page: page.value, size: size.value })
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
    ElMessage.error('加载失败：' + (error.response?.data?.message || error.message))
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

// 是否仍在允许的作品提交时间内：
//  1）如果有开始时间，未到开始时间不能提交/修改
//  2）如果有竞赛结束时间，结束后不能提交/修改
//  3）如果有报名结束时间，报名截止后也不能提交/修改
const isSubmissionOpen = (row) => {
  const now = new Date()
  if (row.startTime) {
    const start = new Date(row.startTime)
    if (now < start) return false
  }
  if (row.endTime) {
    const end = new Date(row.endTime)
    if (now > end) return false
  }
  if (row.registrationEnd) {
    const regEnd = new Date(row.registrationEnd)
    if (now > regEnd) return false
  }
  return true
}

// 是否可以提交作品：已通过 + 已缴费/免费 + 需要作品 + 未提交 + 在允许时间内
const canSubmitWork = (row) => {
  if (!(row.status === 1 && row.needWork === 1 && !row.workSubmitted)) {
    return false
  }
  // 必须已缴费或为0元竞赛
  if (!(row.paymentStatus === 1 || row.registrationFee === 0 || row.registrationFee == null)) {
    return false
  }
  // 必须在允许提交的时间窗口内
  return isSubmissionOpen(row)
}

const getSubmitWorkTooltip = (row) => {
  const now = new Date()
  // 比赛已结束
  if (row.endTime && now > new Date(row.endTime)) {
    return '比赛已结束，不能再提交或修改作品'
  }
  // 报名已截止
  if (row.registrationEnd && now > new Date(row.registrationEnd)) {
    return '报名已截止，不能再提交或修改作品'
  }
  // 比赛未开始
  if (row.startTime && now < new Date(row.startTime)) {
    const startText = formatDateTime(row.startTime)
    return `比赛开始时间：${startText}，开始后才能提交作品`
  }
  return '点击提交作品'
}

const handlePay = async (id) => {
  try {
    // 先检查余额
    const balanceRes = await getBalance()
    const currentBalance = balanceRes.data?.balance || 0
    const registration = tableData.value.find(r => r.id === id)
    // 优先使用当前竞赛的报名费，其次使用报名记录里的金额，避免出现“0元”再突然变成有价格的体验
    const paymentAmount = (registration?.registrationFee ?? registration?.paymentAmount ?? 0)
    
    if (currentBalance < paymentAmount) {
      ElMessageBox.confirm(
        `余额不足！当前余额：¥${currentBalance.toFixed(2)}，需要：¥${paymentAmount.toFixed(2)}。是否前往充值？`,
        '余额不足',
        {
          confirmButtonText: '前往充值',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        showRechargeDialog.value = true
        rechargeForm.value.amount = paymentAmount - currentBalance
      }).catch(() => {})
      return
    }
    
    await ElMessageBox.confirm(
      `确认使用余额支付 ¥${paymentAmount.toFixed(2)}？当前余额：¥${currentBalance.toFixed(2)}`,
      '确认缴费',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await pay(id)
    ElMessage.success('缴费成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || error.message || '缴费失败')
    }
  }
}

const handleRecharge = async () => {
  if (!rechargeForm.value.amount || rechargeForm.value.amount <= 0) {
    ElMessage.warning('请输入有效的充值金额')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认充值 ¥${rechargeForm.value.amount.toFixed(2)}？`,
      '确认充值',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    recharging.value = true
    await rechargeBalance(rechargeForm.value.amount)
    ElMessage.success('充值成功')
    showRechargeDialog.value = false
    rechargeForm.value.amount = 0
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || error.message || '充值失败')
    }
  } finally {
    recharging.value = false
  }
}

const handleSubmitWork = async (row) => {
  currentRegistration.value = row
  
  // 如果已提交作品，尝试加载已有作品信息
  if (row.workSubmitted) {
    try {
      const worksRes = await getMyWorks(row.competitionId)
      console.log('获取作品列表:', worksRes)
      const existingWork = worksRes.data?.find(w => w.registrationId === row.id)
      console.log('找到的作品:', existingWork)
      if (existingWork) {
        workForm.value = {
          title: existingWork.title || '',
          description: existingWork.description || '',
          file: null,
          fileName: existingWork.fileName || '',
          fileSize: existingWork.fileSize || 0
        }
        fileList.value = existingWork.fileName ? [{
          name: existingWork.fileName,
          size: existingWork.fileSize
        }] : []
      } else {
        workForm.value = {
          title: '',
          description: '',
          file: null,
          fileName: '',
          fileSize: 0
        }
        fileList.value = []
      }
    } catch (error) {
      console.error('加载作品信息失败:', error)
      workForm.value = {
        title: '',
        description: '',
        file: null,
        fileName: '',
        fileSize: 0
      }
      fileList.value = []
    }
  } else {
    workForm.value = {
      title: '',
      description: '',
      file: null,
      fileName: '',
      fileSize: 0
    }
    fileList.value = []
  }
  
  showWorkDialog.value = true
}

const handleFileChange = (file) => {
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过50MB')
    uploadRef.value.clearFiles()
    return
  }
  workForm.value.file = file.raw
  workForm.value.fileName = file.name
  workForm.value.fileSize = file.size
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const handleViewWork = async (row) => {
  try {
    // 获取作品详情
    const worksRes = await getMyWorks(row.competitionId)
    console.log('获取作品列表:', worksRes)
    const existingWork = worksRes.data?.find(w => w.registrationId === row.id)
    console.log('找到的作品:', existingWork)
    if (existingWork && existingWork.id) {
      const detailRes = await getWorkDetail(existingWork.id)
      console.log('作品详情:', detailRes)
      currentWork.value = detailRes.data
      showWorkViewDialog.value = true
    } else {
      ElMessage.warning('未找到作品信息')
    }
  } catch (error) {
    console.error('加载作品信息失败:', error)
    ElMessage.error('加载作品信息失败：' + (error.response?.data?.message || error.message))
  }
}

const handleDownloadWork = async (work) => {
  try {
    if (!work.filePath) {
      ElMessage.error('文件路径不存在')
      return
    }
    console.log('下载文件路径:', work.filePath)
    const response = await downloadWork(work.filePath)
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
    link.download = work.fileName || '作品文件'
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

const handleSubmitWorkConfirm = async () => {
  await workFormRef.value.validate(async (valid) => {
    if (valid) {
      // 如果是修改作品且没有选择新文件，使用原有文件路径
      let filePath = null
      let fileName = workForm.value.fileName
      let fileSize = workForm.value.fileSize
      
      if (workForm.value.file) {
        // 有新文件，需要上传
        submittingWork.value = true
        try {
          const uploadRes = await uploadFile(workForm.value.file)
          console.log('文件上传返回:', uploadRes)
          
          // 处理文件路径 - 支持多种返回格式
          if (uploadRes.data) {
            if (typeof uploadRes.data === 'string') {
              filePath = uploadRes.data
            } else if (uploadRes.data.filePath) {
              filePath = uploadRes.data.filePath
            } else if (uploadRes.data.path) {
              filePath = uploadRes.data.path
            }
          }
          
          if (!filePath) {
            throw new Error('文件上传失败，未获取到文件路径')
          }
          
          fileName = workForm.value.fileName
          fileSize = workForm.value.fileSize
        } catch (error) {
          ElMessage.error(error.response?.data?.message || error.message || '文件上传失败')
          submittingWork.value = false
          return
        }
      } else if (currentRegistration.value.workSubmitted) {
        // 修改作品但没有选择新文件，需要获取原有文件路径
        try {
          const worksRes = await getMyWorks(currentRegistration.value.competitionId)
          const existingWork = worksRes.data?.find(w => w.registrationId === currentRegistration.value.id)
          if (existingWork && existingWork.filePath) {
            filePath = existingWork.filePath
            fileName = existingWork.fileName || fileName
            fileSize = existingWork.fileSize || fileSize
          } else {
            ElMessage.warning('未找到原有文件，请重新选择文件')
            submittingWork.value = false
            return
          }
        } catch (error) {
          ElMessage.error('获取原有作品信息失败，请重新选择文件')
          submittingWork.value = false
          return
        }
      } else {
        // 首次提交但没有选择文件
        ElMessage.warning('请选择作品文件')
        submittingWork.value = false
        return
      }
      
      submittingWork.value = true
      try {
        // 提交作品信息
        const workData = {
          competitionId: currentRegistration.value.competitionId,
          registrationId: currentRegistration.value.id,
          title: workForm.value.title,
          description: workForm.value.description,
          filePath: filePath,
          fileName: fileName,
          fileSize: fileSize
        }
        
        console.log('提交作品数据:', workData)
        const submitRes = await submitWork(workData)
        console.log('作品提交返回:', submitRes)
        
        ElMessage.success(currentRegistration.value.workSubmitted ? '作品修改成功' : '作品提交成功')
        showWorkDialog.value = false
        // 重置表单
        workForm.value = {
          title: '',
          description: '',
          file: null,
          fileName: '',
          fileSize: 0
        }
        fileList.value = []
        uploadRef.value?.clearFiles()
        // 强制刷新数据
        await loadData()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || error.message || '作品提交失败')
      } finally {
        submittingWork.value = false
      }
    }
  })
}

const isTeamLeader = (teamId) => {
  if (!teamId) return false
  const team = myTeams.value.find(t => t.id === teamId)
  console.log('检查是否是队长:', { teamId, team, myTeams: myTeams.value, isLeader: team?.isLeader, leaderId: team?.leaderId })
  // 如果后端返回了isLeader字段，使用它；否则通过leaderId判断
  if (team) {
    if (team.isLeader !== undefined) {
      return team.isLeader === true
    }
    // 如果没有isLeader字段，通过leaderId判断
    const userStore = useUserStore()
    return team.leaderId === userStore.userInfo.id
  }
  return false
}

const handleManageTeam = async (row) => {
  currentTeamId.value = row.teamId
  currentCompetitionId.value = row.competitionId
  showTeamManageDialog.value = true
  teamManageTab.value = 'members'
  
  // 重新加载我的团队信息（确保数据最新，传入竞赛ID以获取该竞赛的团队）
  await loadMyTeams(row.competitionId)
  
  // 加载团队成员
  await loadTeamMembers()
  
  // 如果是邀请成员标签页，加载待审核成员
  if (teamManageTab.value === 'pending') {
    await loadPendingMembers()
  }
}

const loadTeamMembers = async () => {
  if (!currentTeamId.value) return
  teamMembersLoading.value = true
  try {
    const res = await getTeamMembers(currentTeamId.value)
    teamMembers.value = res.data || []
  } catch (error) {
    ElMessage.error('加载团队成员失败：' + (error.response?.data?.message || error.message))
  } finally {
    teamMembersLoading.value = false
  }
}

const loadPendingMembers = async () => {
  if (!currentTeamId.value) return
  pendingMembersLoading.value = true
  try {
    const res = await getPendingMembers(currentTeamId.value)
    pendingMembers.value = res.data || []
  } catch (error) {
    ElMessage.error('加载待审核成员失败：' + (error.response?.data?.message || error.message))
  } finally {
    pendingMembersLoading.value = false
  }
}

const handleApproveMember = async (memberId, status) => {
  try {
    await approveTeamMember(currentTeamId.value, memberId, status)
    ElMessage.success(status === 1 ? '已通过申请' : '已拒绝申请')
    await loadPendingMembers()
    await loadTeamMembers()
    // 刷新报名列表
    await loadData()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  }
}

const handleInviteMember = async () => {
  if (!inviteForm.value.studentNo.trim()) {
    ElMessage.warning('请输入学号')
    return
  }
  if (!isTeamLeader(currentTeamId.value)) {
    ElMessage.warning('只有队长可以邀请成员')
    return
  }
  inviting.value = true
  try {
    await inviteMember(currentTeamId.value, inviteForm.value.studentNo)
    ElMessage.success('邀请成功，该学生已加入团队')
    inviteForm.value.studentNo = ''
    await loadTeamMembers()
    // 刷新报名列表和团队信息
    await loadData()
    await loadMyTeams()
  } catch (error) {
    ElMessage.error('邀请失败：' + (error.response?.data?.message || error.message))
  } finally {
    inviting.value = false
  }
}

  // 监听团队管理标签页切换
  watch(teamManageTab, (newTab) => {
    if (newTab === 'pending' && showTeamManageDialog.value) {
      loadPendingMembers()
    }
  })

const loadMyTeams = async (competitionId = null) => {
  try {
    const res = await getMyTeams(competitionId)
    myTeams.value = res.data || []
    console.log('加载团队信息:', { competitionId, teams: myTeams.value })
  } catch (error) {
    console.error('加载团队信息失败:', error)
  }
}

onMounted(() => {
  loadData()
  loadMyTeams()
})
</script>

<style scoped>
.my-registrations {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.balance-info {
  display: flex;
  align-items: center;
}

.balance-label {
  color: #606266;
  font-size: 14px;
}

.balance-value {
  color: #409EFF;
  font-size: 18px;
  font-weight: bold;
  margin-left: 8px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.reject-reason {
  color: #F56C6C;
  cursor: pointer;
  text-decoration: underline;
}
</style>

