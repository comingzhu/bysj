<template>
  <div class="notice-list">
    <el-card>
      <template #header>
        <div class="header-content">
          <span>通知公告</span>
          <el-button type="primary" :icon="Message" @click="showSendDialog = true">
            发送通知
          </el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <div class="title-cell">
              <el-icon v-if="row.isRead === 0" class="unread-icon"><Bell /></el-icon>
              <span :class="{ 'unread-title': row.isRead === 0 }">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ row.type || '系统通知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="small" 
                :icon="View"
                @click="handleView(row)"
              >
                查看
              </el-button>
              <el-button 
                v-if="row.isRead === 0" 
                type="success" 
                size="small" 
                :icon="Check"
                @click="handleMarkRead(row.id)"
              >
                已读
              </el-button>
            </div>
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

    <!-- 通知详情对话框 -->
    <el-dialog v-model="dialogVisible" :title="currentNotice?.title" width="700px">
      <div class="notice-detail">
        <div class="notice-meta">
          <el-tag :type="getTypeTagType(currentNotice?.type)">{{ currentNotice?.type || '系统通知' }}</el-tag>
          <span class="notice-time">{{ formatDateTime(currentNotice?.createTime) }}</span>
        </div>
        <div class="notice-content">
          <p>{{ currentNotice?.content }}</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button 
            v-if="currentNotice?.isRead === 0" 
            type="primary" 
            :icon="Check"
            @click="handleMarkReadAndClose(currentNotice?.id)"
          >
            标记已读并关闭
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 发送通知对话框 -->
    <el-dialog v-model="showSendDialog" title="发送通知" width="700px">
      <el-form :model="sendForm" :rules="sendRules" ref="sendFormRef" label-width="100px">
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="sendForm.title" placeholder="请输入通知标题" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="通知类型" prop="type">
          <el-select v-model="sendForm.type" placeholder="请选择通知类型" style="width: 100%;">
            <el-option label="系统通知" value="系统通知" />
            <el-option label="报名通知" value="报名通知" />
            <el-option label="缴费提醒" value="缴费提醒" />
            <el-option label="获奖通知" value="获奖通知" />
            <el-option label="竞赛公告" value="竞赛公告" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收对象" prop="receiverType">
          <el-select v-model="sendForm.receiverType" placeholder="请选择接收对象" style="width: 100%;" @change="handleReceiverTypeChange">
            <el-option label="所有学生" value="all" />
            <el-option label="特定竞赛学生" value="competition" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="sendForm.receiverType === 'competition'" label="选择竞赛" prop="competitionId">
          <el-select v-model="sendForm.competitionId" placeholder="请选择竞赛" style="width: 100%;">
            <el-option v-for="competition in competitionList" :key="competition.id" :label="competition.name" :value="competition.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input v-model="sendForm.content" type="textarea" :rows="6" placeholder="请输入通知内容" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showSendDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSendNotice" :loading="sending">
            发送通知
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyMessages, markAsRead, sendMessage } from '../../api/message'
import { getMyCompetitions } from '../../api/competition'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '../../utils/dateFormat'
import { Bell, View, Check, Message } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const currentNotice = ref(null)
const showSendDialog = ref(false)
const sendFormRef = ref(null)
const sending = ref(false)
const sendForm = ref({
  title: '',
  type: '系统通知',
  content: '',
  receiverType: 'all',
  competitionId: null
})
const sendRules = {
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择通知类型', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }],
  receiverType: [{ required: true, message: '请选择接收对象', trigger: 'blur' }],
  competitionId: [
    {
      validator: (rule, value, callback) => {
        if (sendForm.value.receiverType === 'competition' && !value) {
          callback(new Error('请选择竞赛'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}
const competitionList = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyMessages({ page: page.value, size: size.value })
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
    ElMessage.error('加载失败：' + (error.message || '未知错误'))
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleView = (row) => {
  currentNotice.value = row
  dialogVisible.value = true
  // 如果未读，自动标记为已读
  if (row.isRead === 0) {
    handleMarkRead(row.id)
  }
}

const handleMarkRead = async (id) => {
  try {
    await markAsRead(id)
    ElMessage.success('已标记为已读')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.message || '未知错误'))
  }
}

const handleMarkReadAndClose = async (id) => {
  await handleMarkRead(id)
  dialogVisible.value = false
}

const getTypeTagType = (type) => {
  const typeMap = {
    '系统通知': 'info',
    '报名通知': 'success',
    '缴费提醒': 'warning',
    '获奖通知': 'success',
    '竞赛公告': 'primary'
  }
  return typeMap[type] || 'info'
}

// 加载竞赛列表
const loadCompetitions = async () => {
  try {
    const res = await getMyCompetitions({ page: 1, size: 1000 })
    if (res.data && res.data.records) {
      competitionList.value = res.data.records
    } else if (Array.isArray(res.data)) {
      competitionList.value = res.data
    } else {
      competitionList.value = []
    }
  } catch (error) {
    console.error('加载竞赛列表失败:', error)
    competitionList.value = []
  }
}

// 处理接收对象类型变化
const handleReceiverTypeChange = () => {
  if (sendForm.value.receiverType !== 'competition') {
    sendForm.value.competitionId = null
  }
}

// 发送通知
const handleSendNotice = async () => {
  await sendFormRef.value.validate(async (valid) => {
    if (valid) {
      sending.value = true
      try {
        const messageData = {
          title: sendForm.value.title,
          type: sendForm.value.type,
          content: sendForm.value.content,
          userId: sendForm.value.receiverType === 'all' ? null : null,
          competitionId: sendForm.value.competitionId
        }
        await sendMessage(messageData)
        ElMessage.success('通知发送成功')
        showSendDialog.value = false
        // 重置表单
        sendForm.value = {
          title: '',
          type: '系统通知',
          content: '',
          receiverType: 'all',
          competitionId: null
        }
      } catch (error) {
        ElMessage.error('发送失败：' + (error.message || '未知错误'))
      } finally {
        sending.value = false
      }
    }
  })
}

onMounted(() => {
  loadData()
  loadCompetitions()
})
</script>

<style scoped>
.notice-list {
  padding: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-content .el-button {
  transition: all 0.3s ease;
}

.header-content .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.unread-icon {
  color: #409EFF;
  font-size: 16px;
}

.unread-title {
  font-weight: 600;
  color: #303133;
}

.notice-detail {
  padding: 20px 0;
}

.notice-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.notice-time {
  color: #909399;
  font-size: 14px;
}

.notice-content {
  line-height: 1.8;
  color: #606266;
  font-size: 15px;
  white-space: pre-wrap;
  word-break: break-word;
}

.action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  margin: 0;
  transition: all 0.3s ease;
}

.action-buttons .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 10px 0;
}

.dialog-footer .el-button {
  min-width: 100px;
  transition: all 0.3s ease;
}

.dialog-footer .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
</style>

