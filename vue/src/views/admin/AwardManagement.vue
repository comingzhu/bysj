<template>
  <div class="award-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>获奖管理</span>
          <div>
            <el-select v-model="selectedCompetitionId" placeholder="选择竞赛" style="width: 300px; margin-right: 10px;" clearable @change="loadAwards">
              <el-option
                v-for="comp in competitions"
                :key="comp.id"
                :label="comp.name"
                :value="comp.id"
              />
            </el-select>
            <el-button 
              v-if="selectedCompetitionId" 
              type="primary" 
              @click="handleGenerateAwards"
              :loading="generating"
            >
              自动生成获奖记录
            </el-button>
            <el-button type="success" @click="handleCreate">手动添加</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="competitionName" label="竞赛名称" width="200" />
        <el-table-column prop="submitterName" label="获奖者" width="200" />
        <el-table-column prop="awardLevel" label="奖项等级" width="120">
          <template #default="{ row }">
            <el-tag :type="getAwardLevelType(row.awardLevel)">
              {{ row.awardLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rank" label="排名" width="100" />
        <el-table-column prop="score" label="得分" width="100">
          <template #default="{ row }">
            {{ row.score }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
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
          @size-change="loadAwards"
          @current-change="loadAwards"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="竞赛" prop="competitionId">
          <el-select v-model="form.competitionId" placeholder="请选择竞赛" style="width: 100%" @change="handleCompetitionChange">
            <el-option
              v-for="comp in competitions"
              :key="comp.id"
              :label="comp.name"
              :value="comp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="报名记录ID" prop="registrationId">
          <el-input-number v-model="form.registrationId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="获奖者（个人）" prop="userId">
          <el-input-number v-model="form.userId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="获奖者（团队）" prop="teamId">
          <el-input-number v-model="form.teamId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="奖项等级" prop="awardLevel">
          <el-select v-model="form.awardLevel" placeholder="请选择奖项等级" style="width: 100%">
            <el-option label="一等奖" value="一等奖" />
            <el-option label="二等奖" value="二等奖" />
            <el-option label="三等奖" value="三等奖" />
            <el-option label="优秀奖" value="优秀奖" />
          </el-select>
        </el-form-item>
        <el-form-item label="排名" prop="rank">
          <el-input-number v-model="form.rank" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="得分" prop="score">
          <el-input-number v-model="form.score" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAwardListByCompetition, generateAwards, createAward, updateAward, deleteAward } from '../../api/award'
import { getCompetitionList } from '../../api/competition'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '../../utils/dateFormat'

const loading = ref(false)
const generating = ref(false)
const saving = ref(false)
const tableData = ref([])
const competitions = ref([])
const selectedCompetitionId = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('添加获奖记录')
const formRef = ref(null)
const form = ref({
  id: null,
  competitionId: null,
  registrationId: null,
  userId: null,
  teamId: null,
  awardLevel: '',
  rank: 1,
  score: 0
})

const rules = {
  competitionId: [{ required: true, message: '请选择竞赛', trigger: 'change' }],
  registrationId: [{ required: true, message: '请输入报名记录ID', trigger: 'blur' }],
  awardLevel: [{ required: true, message: '请选择奖项等级', trigger: 'change' }],
  rank: [{ required: true, message: '请输入排名', trigger: 'blur' }],
  score: [{ required: true, message: '请输入得分', trigger: 'blur' }]
}

const loadCompetitions = async () => {
  try {
    const res = await getCompetitionList({ page: 1, size: 1000 })
    if (res.data && res.data.records) {
      competitions.value = res.data.records.filter(c => c.needWork === 1)
    }
  } catch (error) {
    console.error('加载竞赛列表失败:', error)
  }
}

const loadAwards = async () => {
  if (!selectedCompetitionId.value) {
    tableData.value = []
    total.value = 0
    return
  }
  
  loading.value = true
  try {
    const res = await getAwardListByCompetition(selectedCompetitionId.value)
    if (res.data) {
      tableData.value = res.data
      total.value = res.data.length
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('加载失败：' + (error.response?.data?.message || error.message))
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const getAwardLevelType = (level) => {
  const map = {
    '一等奖': 'danger',
    '二等奖': 'warning',
    '三等奖': 'success',
    '优秀奖': 'info'
  }
  return map[level] || 'info'
}

const handleGenerateAwards = async () => {
  try {
    await ElMessageBox.confirm('确定要根据评分结果自动生成获奖记录吗？将删除该竞赛的旧获奖记录。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    generating.value = true
    const res = await generateAwards(selectedCompetitionId.value)
    ElMessage.success(res.message || '生成成功')
    await loadAwards()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || error.message || '生成失败')
    }
  } finally {
    generating.value = false
  }
}

const handleCreate = () => {
  form.value = {
    id: null,
    competitionId: selectedCompetitionId.value || null,
    registrationId: null,
    userId: null,
    teamId: null,
    awardLevel: '',
    rank: 1,
    score: 0
  }
  dialogTitle.value = '添加获奖记录'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = {
    id: row.id,
    competitionId: row.competitionId,
    registrationId: row.registrationId,
    userId: row.userId,
    teamId: row.teamId,
    awardLevel: row.awardLevel,
    rank: row.rank,
    score: row.score
  }
  dialogTitle.value = '编辑获奖记录'
  dialogVisible.value = true
}

const handleCompetitionChange = () => {
  // 竞赛改变时，可以加载该竞赛的报名记录供选择
}

const handleSave = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    saving.value = true
    try {
      if (form.value.id) {
        await updateAward(form.value)
        ElMessage.success('更新成功')
      } else {
        await createAward(form.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      await loadAwards()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || error.message || '操作失败')
    } finally {
      saving.value = false
    }
  })
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAward(id)
    ElMessage.success('删除成功')
    await loadAwards()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadCompetitions()
})
</script>

<style scoped>
.award-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>




