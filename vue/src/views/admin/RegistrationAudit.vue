<template>
  <div class="registration-audit">
    <el-card>
      <template #header>
        <span>报名审核</span>
      </template>
      <el-table :data="tableData" v-loading="loading">
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
        <el-table-column prop="createTime" label="报名时间" width="180">
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
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="handleApprove(row.id, 1)"
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRegistrationList, approveRegistration } from '../../api/registration'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '../../utils/dateFormat'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const loadData = async () => {
  loading.value = true
  try {
    // 管理端只审核省赛（系统竞赛）的报名
    const res = await getRegistrationList({ status: 0, isSystem: 1, page: page.value, size: size.value })
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
    ElMessage.error('操作失败')
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
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.registration-audit {
  padding: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>

