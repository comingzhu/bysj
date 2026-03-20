<template>
  <div class="competition-management">
    <el-card>
      <template #header>
        <div class="header">
          <span>竞赛管理</span>
          <el-button type="primary" @click="handleCreate">发布竞赛</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="name" label="竞赛名称" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="驳回原因" width="250" v-if="hasRejectedCompetitions">
          <template #default="{ row }">
            <div v-if="row.status === 3">
              <el-tooltip v-if="row.rejectReason" :content="row.rejectReason" placement="top">
                <span style="color: #f56c6c; cursor: pointer;">
                  {{ row.rejectReason.length > 20 ? row.rejectReason.substring(0, 20) + '...' : row.rejectReason }}
                </span>
              </el-tooltip>
              <span v-else style="color: #909399;">无驳回原因</span>
            </div>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="400">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              {{ (row.status === 4 || row.status === 6) ? '申请修改竞赛' : '编辑' }}
            </el-button>
            <el-button 
              v-if="row.status !== 3" 
              type="info" 
              size="small" 
              @click="handleViewProgress(row.id)"
            >
              查看报名进度
            </el-button>
            <el-button 
              v-if="row.status !== 3" 
              type="success" 
              size="small" 
              :icon="Download"
              @click="handleExportStudents(row)"
            >
              导出参赛学生
            </el-button>
            <el-button 
              v-if="row.status === 3" 
              type="warning" 
              size="small" 
              @click="handleViewRejectReason(row)"
            >
              查看驳回原因
            </el-button>
            <el-button 
              v-if="row.status === 2" 
              type="success" 
              size="small" 
              @click="handlePublish(row.id)"
            >
              发布
            </el-button>
            <el-button 
              v-if="row.status === 4" 
              type="warning" 
              size="small" 
              @click="handlePause(row.id)"
            >
              暂停
            </el-button>
            <el-button 
              v-if="row.status === 6" 
              type="success" 
              size="small" 
              @click="handleResume(row.id)"
            >
              恢复
            </el-button>
            <el-button 
              v-if="row.status === 6 && row.needWork === 1 && isRegistrationEnded(row)" 
              type="info" 
              size="small" 
              @click="handleCreateScoreTasks(row.id)"
            >
              创建评分任务
            </el-button>
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

    <!-- 发布/编辑竞赛对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="竞赛名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="竞赛分类" required>
          <el-select v-model="form.category" placeholder="请选择竞赛分类" style="width: 100%" :loading="loadingCategories">
            <el-option 
              v-for="category in categories" 
              :key="category" 
              :label="category" 
              :value="category" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="竞赛类型" required>
          <el-radio-group v-model="form.type">
            <el-radio :label="0">个人赛</el-radio>
            <el-radio :label="1">团队赛</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="获奖方式" required>
          <el-radio-group v-model="form.awardMode">
            <el-radio :label="0">固定名额（一二三等奖各1名）</el-radio>
            <el-radio :label="1">按比例评奖（类似蓝桥杯）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.awardMode === 1" label="奖项比例">
          <div style="display: flex; gap: 12px; align-items: center;">
            <span>一等：</span>
            <el-input-number v-model="form.firstAwardRatio" :min="0" :max="1" :step="0.05" :precision="2" />
            <span>二等：</span>
            <el-input-number v-model="form.secondAwardRatio" :min="0" :max="1" :step="0.05" :precision="2" />
            <span>三等：</span>
            <el-input-number v-model="form.thirdAwardRatio" :min="0" :max="1" :step="0.05" :precision="2" />
            <span style="color:#909399;">（0-1 之间的小数，例如 0.10 表示10%）</span>
          </div>
        </el-form-item>
        <el-form-item label="是否需要作品" required>
          <el-radio-group v-model="form.needWork">
            <el-radio :label="0">否</el-radio>
            <el-radio :label="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="报名费" required>
          <el-input-number v-model="form.registrationFee" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="报名开始时间" required>
          <el-date-picker v-model="form.registrationStart" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="报名结束时间" required>
          <el-date-picker v-model="form.registrationEnd" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="8"
            placeholder="请输入竞赛描述，支持HTML格式，例如：\n<h1>一级标题</h1>\n<h2>二级标题</h2>\n<p>段落内容</p>\n<ul><li>列表项1</li><li>列表项2</li></ul>"
          />
          <div class="editor-tip">
            <el-tag size="small" type="info">提示</el-tag>
            <span>支持使用HTML标签进行格式化，如 &lt;h1&gt;一级标题&lt;/h1&gt;、&lt;h2&gt;二级标题&lt;/h2&gt;、&lt;p&gt;段落&lt;/p&gt;、&lt;ul&gt;&lt;li&gt;列表&lt;/li&gt;&lt;/ul&gt; 等</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <!-- 报名进度对话框 -->
    <el-dialog v-model="progressDialogVisible" title="报名进度" width="800px">
      <div v-if="progressData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="竞赛名称">{{ progressData.competitionName }}</el-descriptions-item>
          <el-descriptions-item label="总报名数">{{ progressData.totalRegistrations }}</el-descriptions-item>
          <el-descriptions-item label="已通过">{{ progressData.approvedRegistrations }}</el-descriptions-item>
          <el-descriptions-item label="待审核">{{ progressData.pendingRegistrations }}</el-descriptions-item>
          <el-descriptions-item label="已缴费">{{ progressData.paidRegistrations }}</el-descriptions-item>
          <el-descriptions-item label="未缴费">{{ progressData.unpaidRegistrations }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 驳回原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回原因" width="600px">
      <div v-if="rejectData">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="竞赛名称">{{ rejectData.competitionName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag type="danger">已驳回</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="驳回原因">
            <div style="padding: 10px; background-color: #fef0f0; border-radius: 4px; color: #f56c6c;">
              {{ rejectData.rejectReason || '管理员未填写驳回原因' }}
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(rejectData.createTime) }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 20px; color: #909399; font-size: 14px;">
          <p>提示：您可以编辑竞赛信息后重新提交审核。</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import { getMyCompetitions, deleteCompetition, createCompetition, updateCompetition, getCompetitionById, publishCompetition, pauseCompetition, resumeCompetition, createScoreTasks } from '../../api/competition'
import { getRegistrationList } from '../../api/registration'
import { getUserInfo } from '../../api/user'
import { getTeamMembers } from '../../api/team'
import { getAwardListByCompetition } from '../../api/award'
import { getConfigByKey, getConfigList } from '../../api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { formatDateTime } from '../../utils/dateFormat'
import * as XLSX from 'xlsx'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('发布竞赛')
const form = ref({})
const progressDialogVisible = ref(false)
const progressData = ref(null)
const hasRejectedCompetitions = ref(false)
const rejectDialogVisible = ref(false)
const rejectData = ref(null)
const categories = ref([])
const loadingCategories = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyCompetitions({ page: page.value, size: size.value })
    console.log('加载竞赛数据:', res) // 调试信息
    if (res && res.data) {
      let records = []
      if (res.data.records !== undefined) {
        records = res.data.records || []
        total.value = res.data.total || 0
      } else if (Array.isArray(res.data)) {
        records = res.data
        total.value = res.data.length
      } else {
        records = []
        total.value = 0
      }
      // 额外过滤：确保不显示已删除的记录（双重保险）
      records = records.filter(r => r.deleted !== 1 && r.deleted !== true)
      tableData.value = records
      // 检查是否有被驳回的竞赛
      hasRejectedCompetitions.value = records.some(r => r.status === 3)
    } else {
      tableData.value = []
      total.value = 0
      hasRejectedCompetitions.value = false
    }
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => {
  const map = { 0: '草稿', 1: '待审核', 2: '已通过', 3: '已驳回', 4: '已发布', 5: '已结束', 6: '已暂停' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'success', 5: 'info', 6: 'warning' }
  return map[status] || 'info'
}

// 加载竞赛分类
const loadCategories = async () => {
  loadingCategories.value = true
  try {
    console.log('开始加载竞赛分类...')
    // 尝试使用不同的API端点获取配置
    let categoriesConfig = null
    
    // 先尝试通过key获取配置
    try {
      const res1 = await getConfigByKey('competition_category')
      console.log('通过key获取配置:', res1)
      if (res1 && res1.data) {
        categoriesConfig = res1.data
      }
    } catch (error) {
      console.error('通过key获取配置失败:', error)
    }
    
    // 如果通过key获取失败，尝试获取所有配置并查找
    if (!categoriesConfig) {
      try {
        const res2 = await getConfigList()
        console.log('获取所有配置:', res2)
        if (res2 && res2.data && Array.isArray(res2.data)) {
          const configItem = res2.data.find(item => item.key === 'competition_category')
          if (configItem) {
            categoriesConfig = configItem
          }
        }
      } catch (error) {
        console.error('获取所有配置失败:', error)
      }
    }
    
    // 处理配置数据
    if (categoriesConfig && (categoriesConfig.value || categoriesConfig.configValue)) {
      const configValue = categoriesConfig.value || categoriesConfig.configValue
      // 假设配置值是逗号分隔的字符串
      categories.value = configValue.split(',').map(c => c.trim()).filter(c => c)
      console.log('解析后的竞赛分类:', categories.value)
    } else {
      // 默认分类，防止配置不存在时无选项
      categories.value = ['程序设计', '算法竞赛', '创新创业', '数学建模', '英语竞赛', '体育竞赛']
      console.log('使用默认竞赛分类:', categories.value)
    }
  } catch (error) {
    console.error('加载竞赛分类失败:', error)
    // 加载失败时使用默认分类
    categories.value = ['程序设计', '算法竞赛', '创新创业', '数学建模', '英语竞赛', '体育竞赛']
  } finally {
    loadingCategories.value = false
  }
}

// 监听对话框显示状态
watch(() => dialogVisible.value, (newVal) => {
  if (!newVal) {
    // 对话框关闭后重置表单
    // 这里可以添加需要的重置逻辑
  }
});

const handleCreate = async () => {
  // 打开对话框前加载最新的竞赛分类
  await loadCategories()
  form.value = {
    type: 0,
    needWork: 0,
    registrationFee: 0,
    awardMode: 0,
    firstAwardRatio: 0.1,
    secondAwardRatio: 0.2,
    thirdAwardRatio: 0.3,
    description: ''
  }
  dialogTitle.value = '发布竞赛'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  // 打开对话框前加载最新的竞赛分类
  await loadCategories()
  try {
    const res = await getCompetitionById(row.id)
    if (res.data) {
      form.value = { ...res.data }
      // 转换日期格式
      if (form.value.registrationStart) {
        form.value.registrationStart = form.value.registrationStart.replace('T', ' ')
      }
      if (form.value.registrationEnd) {
        form.value.registrationEnd = form.value.registrationEnd.replace('T', ' ')
      }
      if (form.value.startTime) {
        form.value.startTime = form.value.startTime.replace('T', ' ')
      }
      if (form.value.endTime) {
        form.value.endTime = form.value.endTime.replace('T', ' ')
      }
      dialogTitle.value = '编辑竞赛'
      dialogVisible.value = true
      // 编辑器初始化后会自动设置内容，因为我们在setup中监听了Change事件
      // 这里不需要额外设置，form.value.description已经包含了正确的内容
    }
  } catch (error) {
    ElMessage.error('加载竞赛详情失败')
  }
}

const handleSave = async () => {
  if (!form.value.name || !form.value.category || form.value.type === undefined || 
      form.value.needWork === undefined || form.value.registrationFee === undefined ||
      !form.value.registrationStart || !form.value.registrationEnd ||
      !form.value.startTime || !form.value.endTime) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  // 检查赛事名称是否重复
  try {
    const res = await getMyCompetitions({ page: 1, size: 1000 })
    const competitions = res.data?.records || res.data || []
    const currentCompetitionId = form.value.id
    
    const duplicateCompetition = competitions.find(comp => {
      // 排除当前编辑的竞赛
      if (currentCompetitionId && comp.id === currentCompetitionId) {
        return false
      }
      // 检查名称是否重复（不区分大小写）
      return comp.name && comp.name.trim().toLowerCase() === form.value.name.trim().toLowerCase()
    })
    
    if (duplicateCompetition) {
      ElMessage.warning('赛事名称已存在，请使用其他名称')
      return
    }
  } catch (error) {
    console.error('检查赛事名称失败:', error)
    ElMessage.error('检查赛事名称失败，请稍后重试')
    return
  }
  
  if (form.value.awardMode === 1) {
    const firstRatio = Number(form.value.firstAwardRatio || 0)
    const secondRatio = Number(form.value.secondAwardRatio || 0)
    const thirdRatio = Number(form.value.thirdAwardRatio || 0)
    const sumRatio = firstRatio + secondRatio + thirdRatio
    
    if (sumRatio <= 0) {
      ElMessage.warning('按比例评奖时，请设置合理的奖项比例（大于0）')
      return
    }
    
    if (sumRatio > 1) {
      ElMessage.warning('按比例评奖时，总比例不能超过100%')
      return
    }
    
    if (firstRatio >= secondRatio) {
      ElMessage.warning('按比例评奖时，一等奖比例必须小于二等奖比例')
      return
    }
    
    if (secondRatio >= thirdRatio) {
      ElMessage.warning('按比例评奖时，二等奖比例必须小于三等奖比例')
      return
    }
  }
  try {
    // 处理日期格式和数据清理
    const submitData = {
      name: form.value.name?.trim(),
      category: form.value.category?.trim(),
      type: Number(form.value.type),
      needWork: Number(form.value.needWork),
      registrationFee: Number(form.value.registrationFee) || 0,
      awardMode: form.value.awardMode === undefined ? 0 : Number(form.value.awardMode),
      firstAwardRatio: form.value.firstAwardRatio,
      secondAwardRatio: form.value.secondAwardRatio,
      thirdAwardRatio: form.value.thirdAwardRatio,
      registrationStart: form.value.registrationStart,
      registrationEnd: form.value.registrationEnd,
      startTime: form.value.startTime,
      endTime: form.value.endTime,
      location: form.value.location?.trim() || '',
      description: form.value.description || ''
    }
    
    // 验证日期格式
    const dateFormat = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/
    if (!dateFormat.test(submitData.registrationStart) || 
        !dateFormat.test(submitData.registrationEnd) ||
        !dateFormat.test(submitData.startTime) ||
        !dateFormat.test(submitData.endTime)) {
      ElMessage.warning('日期格式不正确，请重新选择')
      return
    }
    
    // 如果是编辑，添加id
    if (form.value.id) {
      submitData.id = form.value.id
      await updateCompetition(submitData)
      ElMessage.success('更新成功')
    } else {
      await createCompetition(submitData)
      ElMessage.success('创建成功，等待审核')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('操作失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '操作失败'
    ElMessage.error(errorMessage)
  }
}

const handleViewProgress = async (competitionId) => {
  try {
    const res = await getRegistrationList({ competitionId, page: 1, size: 1000 })
    let registrations = []
    if (res.data && res.data.records) {
      registrations = res.data.records
    } else if (Array.isArray(res.data)) {
      registrations = res.data
    }
    
    progressData.value = {
      competitionName: tableData.value.find(c => c.id === competitionId)?.name || '未知竞赛',
      totalRegistrations: registrations.length,
      approvedRegistrations: registrations.filter(r => r.status === 1).length,
      pendingRegistrations: registrations.filter(r => r.status === 0).length,
      paidRegistrations: registrations.filter(r => r.paymentStatus === 1).length,
      unpaidRegistrations: registrations.filter(r => r.paymentStatus === 0).length
    }
    progressDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载报名进度失败')
  }
}

const handleViewRejectReason = (row) => {
  rejectData.value = {
    competitionName: row.name,
    rejectReason: row.rejectReason,
    createTime: row.createTime
  }
  rejectDialogVisible.value = true
}

const handlePublish = async (id) => {
  try {
    await ElMessageBox.confirm('确认发布该竞赛？发布后学生可以报名', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await publishCompetition(id)
    ElMessage.success('发布成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
      const errorMessage = error.response?.data?.message || error.message || '发布失败'
      ElMessage.error(errorMessage)
    }
  }
}

const handlePause = async (id) => {
  try {
    await ElMessageBox.confirm('确认暂停该竞赛？暂停后学生将无法报名', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await pauseCompetition(id)
    ElMessage.success('暂停成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('暂停失败:', error)
      const errorMessage = error.response?.data?.message || error.message || '暂停失败'
      ElMessage.error(errorMessage)
    }
  }
}

const handleResume = async (id) => {
  try {
    await ElMessageBox.confirm('确认恢复该竞赛？恢复后学生可以继续报名', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await resumeCompetition(id)
    ElMessage.success('恢复成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('恢复失败:', error)
      const errorMessage = error.response?.data?.message || error.message || '恢复失败'
      ElMessage.error(errorMessage)
    }
  }
}

const isRegistrationEnded = (row) => {
  if (!row.registrationEnd) return false
  const endTime = new Date(row.registrationEnd)
  return new Date() > endTime
}

const handleCreateScoreTasks = async (id) => {
  try {
    await ElMessageBox.confirm('确定要为该竞赛的所有已提交作品创建评分任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await createScoreTasks(id)
    ElMessage.success(res.message || '评分任务创建成功')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || error.message || '创建失败')
    }
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteCompetition(id)
    if (res && res.code === 200) {
      ElMessage.success('删除成功')
      // 如果当前页删除后没有数据了，且不是第一页，则返回上一页
      if (tableData.value.length === 1 && page.value > 1) {
        page.value--
      }
      // 强制刷新数据
      await loadData()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '删除失败')
    }
  }
}

// 导出参赛学生信息到Excel
const handleExportStudents = async (row) => {
  try {
    ElMessage.info('正在导出，请稍候...')
    
    // 获取该竞赛的所有报名记录
    const res = await getRegistrationList({ competitionId: row.id, page: 1, size: 10000 })
    let registrations = []
    if (res.data && res.data.records) {
      registrations = res.data.records
    } else if (Array.isArray(res.data)) {
      registrations = res.data
    }
    
    if (registrations.length === 0) {
      ElMessage.warning('该竞赛暂无报名记录')
      return
    }
    
    // 获取该竞赛的获奖记录
    let awardMap = new Map()
    try {
      const awardRes = await getAwardListByCompetition(row.id)
      if (awardRes.data) {
        const awards = Array.isArray(awardRes.data) ? awardRes.data : []
        // 创建报名记录ID到获奖信息的映射
        awards.forEach(award => {
          if (award.registrationId) {
            awardMap.set(award.registrationId, award)
          }
        })
      }
    } catch (error) {
      console.error('获取获奖记录失败:', error)
      // 即使获取失败也继续导出，只是没有获奖信息
    }
    
    // 准备导出数据
    const exportData = []
    
    // 表头
    if (row.type === 1) {
      // 团队赛
      exportData.push(['团队名称', '姓名', '学号', '班级', '学院', '专业', '是否队长', '报名状态', '缴费状态', '获奖序列号', '报名时间'])
    } else {
      // 个人赛
      exportData.push(['姓名', '学号', '班级', '报名状态', '缴费状态', '获奖序列号', '报名时间'])
    }
    
    // 获取所有学生详细信息
    const processedTeamIds = new Set() // 用于去重团队
    
    for (const registration of registrations) {
      if (registration.teamId) {
        // 团队报名：获取团队成员信息
        if (processedTeamIds.has(registration.teamId)) {
          continue // 避免重复处理同一团队
        }
        processedTeamIds.add(registration.teamId)
        
        try {
          const teamRes = await getTeamMembers(registration.teamId)
          const members = teamRes.data || []
          
          for (const member of members) {
            // 获取获奖序列号
            const award = awardMap.get(registration.id)
            const certificateNumber = award?.certificateNumber || ''
            
            exportData.push([
              registration.userName || '未知团队', // 团队名称
              member.realName || member.username || '未知',
              member.studentNo || '',
              member.className || '',
              member.college || '',
              member.major || '',
              member.isLeader ? '是' : '否',
              getStatusText(registration.status),
              registration.paymentStatus === 1 ? '已缴费' : '未缴费',
              certificateNumber,
              formatDateTime(registration.createTime)
            ])
          }
        } catch (error) {
          console.error('获取团队成员失败:', error)
          // 如果获取失败，至少导出基本信息
          const award = awardMap.get(registration.id)
          const certificateNumber = award?.certificateNumber || ''
          
          exportData.push([
            registration.userName || '未知团队',
            '未知',
            '',
            '',
            '',
            '',
            '',
            getStatusText(registration.status),
            registration.paymentStatus === 1 ? '已缴费' : '未缴费',
            certificateNumber,
            formatDateTime(registration.createTime)
          ])
        }
      } else if (registration.userId) {
        // 个人报名：导出姓名、学号、班级等基本信息
        // 获取获奖序列号
        const award = awardMap.get(registration.id)
        const certificateNumber = award?.certificateNumber || ''
        
        exportData.push([
          registration.userName || '未知用户',
          registration.studentNo || '',
          registration.className || '',
          getStatusText(registration.status),
          registration.paymentStatus === 1 ? '已缴费' : '未缴费',
          certificateNumber,
          formatDateTime(registration.createTime)
        ])
      }
    }
    
    // 创建工作簿
    const ws = XLSX.utils.aoa_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '参赛学生')
    
    // 设置列宽
    const colWidths = row.type === 1 
      ? [
          { wch: 20 }, // 团队名称
          { wch: 12 }, // 姓名
          { wch: 15 }, // 学号
          { wch: 12 }, // 班级
          { wch: 15 }, // 学院
          { wch: 20 }, // 专业
          { wch: 10 }, // 是否队长
          { wch: 12 }, // 报名状态
          { wch: 12 }, // 缴费状态
          { wch: 20 }, // 获奖序列号
          { wch: 20 }  // 报名时间
        ]
      : [
          { wch: 12 }, // 姓名
          { wch: 15 }, // 学号
          { wch: 12 }, // 班级
          { wch: 12 }, // 报名状态
          { wch: 12 }, // 缴费状态
          { wch: 20 }, // 获奖序列号
          { wch: 20 }  // 报名时间
        ]
    ws['!cols'] = colWidths
    
    // 生成文件名
    const now = new Date()
    const dateStr = now.toISOString().split('T')[0]
    const timeStr = `${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}${String(now.getSeconds()).padStart(2, '0')}`
    const fileName = `${row.name}_参赛学生_${dateStr}_${timeStr}.xlsx`
    
    // 导出文件
    XLSX.writeFile(wb, fileName)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败：' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.competition-management {
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

.editor-tip {
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #f0f9eb;
  border: 1px solid #e1f5c4;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #67c23a;
}

.editor-tip span {
  flex: 1;
  line-height: 1.4;
}
</style>

