<template>
  <div class="notice-list">
    <el-card>
      <template #header>
        <span>通知公告</span>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyMessages, markAsRead } from '../../api/message'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '../../utils/dateFormat'
import { Bell, View, Check } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const currentNotice = ref(null)

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

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.notice-list {
  padding: 20px;
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




