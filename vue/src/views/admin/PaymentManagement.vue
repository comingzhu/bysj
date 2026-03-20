<template>
  <div class="payment-management">
    <el-card>
      <template #header>
        <span>缴费管理</span>
      </template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="缴费记录" name="list">
          <el-table :data="tableData" v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="competitionName" label="竞赛名称" width="200">
              <template #default="{ row }">
                {{ row.competitionName || '未知竞赛' }}
              </template>
            </el-table-column>
            <el-table-column prop="userName" label="报名人" width="120">
              <template #default="{ row }">
                {{ row.userName || '未知用户' }}
              </template>
            </el-table-column>
            <el-table-column prop="paymentAmount" label="缴费金额">
              <template #default="{ row }">
                ¥{{ row.paymentAmount }}
              </template>
            </el-table-column>
            <el-table-column prop="paymentStatus" label="缴费状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.paymentStatus)">
                  {{ getStatusText(row.paymentStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="paymentTime" label="缴费时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.paymentTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250">
              <template #default="{ row }">
                <el-button
                  v-if="row.paymentStatus === 1"
                  type="warning"
                  size="small"
                  @click="handleRefund(row.id)"
                >
                  退款
                </el-button>
                <el-button
                  v-if="row.paymentStatus === 0 && row.status === 1"
                  type="primary"
                  size="small"
                  @click="handleSendNotice(row.id)"
                >
                  发送缴费通知
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
        </el-tab-pane>
        <el-tab-pane label="缴费统计" name="statistics">
          <div class="statistics">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-label">总报名数</div>
                    <div class="stat-value">{{ statistics.total }}</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-label">已缴费</div>
                    <div class="stat-value">{{ statistics.paid }}</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-label">未缴费</div>
                    <div class="stat-value">{{ statistics.unpaid }}</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-label">缴费率</div>
                    <div class="stat-value">{{ statistics.paidRate.toFixed(2) }}%</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="refundDialogVisible" title="退款处理" width="500px">
      <el-form>
        <el-form-item label="退款原因" required>
          <el-input v-model="refundReason" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRefund">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPaymentList, refund, sendPaymentNotice, getPaymentStatistics } from '../../api/payment'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '../../utils/dateFormat'

const loading = ref(false)
const tableData = ref([])
const activeTab = ref('list')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const statistics = ref({
  total: 0,
  paid: 0,
  unpaid: 0,
  paidRate: 0
})
const refundDialogVisible = ref(false)
const refundReason = ref('')
const currentRefundId = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPaymentList({ page: page.value, size: size.value })
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

const loadStatistics = async () => {
  try {
    const res = await getPaymentStatistics()
    statistics.value = {
      total: res.data.total || 0,
      paid: res.data.paid || 0,
      unpaid: res.data.unpaid || 0,
      paidRate: res.data.paidRate || 0
    }
  } catch (error) {
    ElMessage.error('加载统计失败')
  }
}

const getStatusText = (status) => {
  const map = { 0: '未缴费', 1: '已缴费', 2: '已退款' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

const handleRefund = (id) => {
  currentRefundId.value = id
  refundReason.value = ''
  refundDialogVisible.value = true
}

const confirmRefund = async () => {
  if (!refundReason.value) {
    ElMessage.warning('请输入退款原因')
    return
  }
  try {
    await refund(currentRefundId.value, refundReason.value)
    ElMessage.success('退款成功')
    refundDialogVisible.value = false
    loadData()
    loadStatistics()
  } catch (error) {
    ElMessage.error('退款失败')
  }
}

const handleSendNotice = async (id) => {
  try {
    await sendPaymentNotice(id)
    ElMessage.success('缴费通知已发送')
  } catch (error) {
    ElMessage.error('发送失败')
  }
}

onMounted(() => {
  loadData()
  loadStatistics()
})
</script>

<style scoped>
.payment-management {
  padding: 20px;
}

.statistics {
  padding: 40px 0;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}
</style>

