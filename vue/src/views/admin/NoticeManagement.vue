<template>
  <div class="notice-management">
    <el-card>
      <template #header>
        <div class="header">
          <span>通知公告管理</span>
          <el-button type="primary" @click="handleCreate">发布通知</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.type || '系统通知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="接收对象" width="120">
          <template #default="{ row }">
            {{ row.userId ? '指定用户(ID:' + row.userId + ')' : '全体用户' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="通知标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="通知类型">
          <el-select v-model="form.type">
            <el-option label="系统通知" value="系统通知" />
            <el-option label="报名通知" value="报名通知" />
            <el-option label="缴费提醒" value="缴费提醒" />
            <el-option label="获奖通知" value="获奖通知" />
            <el-option label="竞赛公告" value="竞赛公告" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收对象">
          <el-radio-group v-model="form.targetType">
            <el-radio label="all">全体用户</el-radio>
            <el-radio label="specific">指定用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="指定用户" v-if="form.targetType === 'specific'">
          <el-input v-model="form.userId" placeholder="请输入用户ID，多个用逗号分隔" />
        </el-form-item>
        <el-form-item label="通知内容" required>
          <el-input v-model="form.content" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllMessages, sendMessage, updateMessage, deleteMessage } from '../../api/message'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '../../utils/dateFormat'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('发布通知')
const form = ref({
  title: '',
  type: '系统通知',
  content: '',
  targetType: 'all',
  userId: null
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAllMessages({ page: page.value, size: size.value })
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

const handleCreate = () => {
  form.value = {
    title: '',
    type: '系统通知',
    content: '',
    targetType: 'all',
    userId: null
  }
  dialogTitle.value = '发布通知'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = {
    id: row.id,
    title: row.title,
    type: row.type || '系统通知',
    content: row.content,
    targetType: row.userId ? 'specific' : 'all',
    userId: row.userId
  }
  dialogTitle.value = '编辑通知'
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const messageData = {
      title: form.value.title,
      type: form.value.type,
      content: form.value.content,
      userId: form.value.targetType === 'all' ? null : (form.value.userId ? parseInt(form.value.userId) : null)
    }
    if (form.value.id) {
      // 编辑
      await updateMessage(form.value.id, messageData)
      ElMessage.success('更新成功')
    } else {
      // 创建
      await sendMessage(messageData)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteMessage(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.notice-management {
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

