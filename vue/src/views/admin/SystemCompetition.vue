<template>
  <div class="system-competition">
    <el-card>
      <template #header>
        <div class="header">
          <span>系统推送省赛信息</span>
          <el-button type="primary" @click="handleCreate">录入省赛信息</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="name" label="竞赛名称" />
        <el-table-column prop="category" label="分类" />
        <el-table-column prop="registrationFee" label="报名费">
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
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 4 ? 'success' : 'info'">
              {{ row.status === 4 ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handlePublish(row.id)" v-if="row.status !== 4">
              发布
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

    <el-dialog v-model="dialogVisible" title="录入省赛信息" width="800px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="竞赛名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="竞赛分类" required>
          <el-select v-model="form.category" placeholder="请选择竞赛分类" style="width: 100%">
            <el-option label="程序设计" value="程序设计" />
            <el-option label="算法竞赛" value="算法竞赛" />
            <el-option label="创新创业" value="创新创业" />
            <el-option label="数学建模" value="数学建模" />
            <el-option label="英语竞赛" value="英语竞赛" />
            <el-option label="体育竞赛" value="体育竞赛" />
          </el-select>
        </el-form-item>
        <el-form-item label="竞赛类型" required>
          <el-radio-group v-model="form.type">
            <el-radio :label="0">个人赛</el-radio>
            <el-radio :label="1">团队赛</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否需要作品">
          <el-radio-group v-model="form.needWork">
            <el-radio :label="0">否</el-radio>
            <el-radio :label="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="报名费" required>
          <el-input-number v-model="form.registrationFee" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="报名开始时间" required>
          <el-date-picker v-model="form.registrationStart" type="datetime" />
        </el-form-item>
        <el-form-item label="报名结束时间" required>
          <el-date-picker v-model="form.registrationEnd" type="datetime" />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker v-model="form.startTime" type="datetime" />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker v-model="form.endTime" type="datetime" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="4" />
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
import { getCompetitionList, publishCompetition } from '../../api/competition'
import request from '../../utils/request'
import { formatDateTime } from '../../utils/dateFormat'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const form = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCompetitionList({ isSystem: 1, page: page.value, size: size.value })
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

const handleCreate = () => {
  form.value = {
    type: 0,
    needWork: 0,
    registrationFee: 0
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  // 验证必填字段
  if (!form.value.name) {
    ElMessage.warning('请填写竞赛名称')
    return
  }
  if (!form.value.category) {
    ElMessage.warning('请选择竞赛分类')
    return
  }
  if (!form.value.registrationStart) {
    ElMessage.warning('请选择报名开始时间')
    return
  }
  if (!form.value.registrationEnd) {
    ElMessage.warning('请选择报名结束时间')
    return
  }
  if (!form.value.startTime) {
    ElMessage.warning('请选择竞赛开始时间')
    return
  }
  if (!form.value.endTime) {
    ElMessage.warning('请选择竞赛结束时间')
    return
  }
  
  // 验证时间逻辑
  if (form.value.registrationStart && form.value.registrationEnd && form.value.registrationStart > form.value.registrationEnd) {
    ElMessage.warning('报名开始时间不能晚于报名结束时间')
    return
  }
  if (form.value.startTime && form.value.endTime && form.value.startTime > form.value.endTime) {
    ElMessage.warning('竞赛开始时间不能晚于结束时间')
    return
  }
  if (form.value.registrationEnd && form.value.endTime && form.value.registrationEnd > form.value.endTime) {
    ElMessage.warning('报名结束时间不能晚于竞赛结束时间')
    return
  }
  
  try {
    // 格式化时间字段为后端期望的格式
    const formData = {
      ...form.value,
      registrationStart: form.value.registrationStart ? form.value.registrationStart.toISOString().slice(0, 19).replace('T', ' ') : null,
      registrationEnd: form.value.registrationEnd ? form.value.registrationEnd.toISOString().slice(0, 19).replace('T', ' ') : null,
      startTime: form.value.startTime ? form.value.startTime.toISOString().slice(0, 19).replace('T', ' ') : null,
      endTime: form.value.endTime ? form.value.endTime.toISOString().slice(0, 19).replace('T', ' ') : null
    }
    
    // 使用系统创建接口
    await request({
      url: '/competition/system/create',
      method: 'post',
      data: formData
    })
    ElMessage.success('创建成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('创建失败：' + (error.response?.data?.message || error.message))
  }
}

const handlePublish = async (id) => {
  try {
    await publishCompetition(id)
    ElMessage.success('发布成功')
    loadData()
  } catch (error) {
    ElMessage.error('发布失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.system-competition {
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

