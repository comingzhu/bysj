<template>
  <div class="data-visualization">
    <el-card>
      <template #header>
        <span>数据可视化</span>
      </template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="报名统计" name="registration">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <el-select v-model="registrationDimension" @change="loadRegistrationStats">
              <el-option label="按竞赛" value="competition" />
              <el-option label="按学院" value="college" />
            </el-select>
            <el-button 
              type="primary" 
              :icon="Download" 
              @click="exportRegistrationToExcel"
              :disabled="!registrationData || registrationData.length === 0"
            >
              导出Excel
            </el-button>
          </div>
          <div style="height: 400px; margin-top: 20px;">
            <el-empty v-if="!registrationData || registrationData.length === 0" description="暂无数据" />
            <div v-else ref="registrationChartRef" style="width: 100%; height: 100%;"></div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="缴费统计" name="payment">
          <div class="statistics">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-label">总报名数</div>
                    <div class="stat-value">{{ paymentStats.total }}</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-label">已缴费</div>
                    <div class="stat-value">{{ paymentStats.paid }}</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-label">未缴费</div>
                    <div class="stat-value">{{ paymentStats.unpaid }}</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-label">缴费率</div>
                    <div class="stat-value">{{ paymentStats.paidRate.toFixed(2) }}%</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
        <el-tab-pane label="获奖统计" name="award">
          <el-select v-model="awardDimension" @change="loadAwardStats" style="margin-bottom: 20px">
            <el-option label="按竞赛" value="competition" />
            <el-option label="按等级" value="level" />
            <el-option label="按学院" value="college" />
          </el-select>
          <el-table :data="awardData" v-loading="loading">
            <el-table-column prop="competitionName" label="竞赛名称" v-if="awardDimension === 'competition'" />
            <el-table-column prop="level" label="奖项等级" v-if="awardDimension === 'level'" />
            <el-table-column prop="college" label="学院" v-if="awardDimension === 'college'" />
            <el-table-column prop="count" label="获奖人数" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { getRegistrationStatistics, getPaymentStatistics, getAwardStatistics } from '../../api/statistics'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import * as XLSX from 'xlsx'

const loading = ref(false)
const activeTab = ref('registration')
const registrationDimension = ref('competition')
const registrationData = ref([])
const registrationChartRef = ref(null)
let registrationChart = null
let resizeHandler = null
const paymentStats = ref({
  total: 0,
  paid: 0,
  unpaid: 0,
  paidRate: 0
})
const awardDimension = ref('competition')
const awardData = ref([])

const loadRegistrationStats = async () => {
  loading.value = true
  try {
    const res = await getRegistrationStatistics(registrationDimension.value)
    // 后端返回格式: { code: 200, message: "操作成功", data: { data: [...] } }
    if (res.data && res.data.data) {
      registrationData.value = res.data.data
    } else if (Array.isArray(res.data)) {
      registrationData.value = res.data
    } else {
      registrationData.value = []
    }
    await nextTick()
    renderRegistrationChart()
  } catch (error) {
    console.error('加载报名统计失败:', error)
    ElMessage.error('加载失败')
    registrationData.value = []
  } finally {
    loading.value = false
  }
}

const renderRegistrationChart = () => {
  if (!registrationChartRef.value || !registrationData.value || registrationData.value.length === 0) {
    return
  }
  
  if (registrationChart) {
    registrationChart.dispose()
  }
  
  registrationChart = echarts.init(registrationChartRef.value)
  
  const xData = []
  const yData = []
  
  registrationData.value.forEach(item => {
    if (registrationDimension.value === 'competition') {
      xData.push(item.competitionName || '未知竞赛')
    } else if (registrationDimension.value === 'college') {
      xData.push(item.college || '未知学院')
    }
    yData.push(item.count || 0)
  })
  
  const option = {
    title: {
      text: registrationDimension.value === 'competition' ? '按竞赛统计' : '按学院统计',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xData,
      axisLabel: {
        rotate: 45,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '报名人数'
    },
    series: [
      {
        name: '报名人数',
        type: 'bar',
        data: yData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        }
      }
    ]
  }
  
  registrationChart.setOption(option)
  
  // 响应式调整
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
  resizeHandler = () => {
    registrationChart?.resize()
  }
  window.addEventListener('resize', resizeHandler)
}

watch(registrationDimension, () => {
  loadRegistrationStats()
})

// 导出报名统计到Excel
const exportRegistrationToExcel = () => {
  if (!registrationData.value || registrationData.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  
  try {
    // 准备导出数据
    const exportData = []
    
    // 表头
    if (registrationDimension.value === 'competition') {
      exportData.push(['竞赛名称', '报名人数'])
      registrationData.value.forEach(item => {
        exportData.push([
          item.competitionName || '未知竞赛',
          item.count || 0
        ])
      })
    } else if (registrationDimension.value === 'college') {
      exportData.push(['学院', '报名人数'])
      registrationData.value.forEach(item => {
        exportData.push([
          item.college || '未知学院',
          item.count || 0
        ])
      })
    }
    
    // 创建工作簿
    const ws = XLSX.utils.aoa_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '报名统计')
    
    // 设置列宽
    const colWidths = [
      { wch: 30 }, // 第一列宽度
      { wch: 15 }  // 第二列宽度
    ]
    ws['!cols'] = colWidths
    
    // 生成文件名
    const dimensionText = registrationDimension.value === 'competition' ? '按竞赛' : '按学院'
    const fileName = `报名统计_${dimensionText}_${new Date().toISOString().split('T')[0]}.xlsx`
    
    // 导出文件
    XLSX.writeFile(wb, fileName)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败：' + (error.message || '未知错误'))
  }
}

// 组件销毁时清理图表
onBeforeUnmount(() => {
  if (registrationChart) {
    registrationChart.dispose()
    registrationChart = null
  }
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
})

const loadPaymentStats = async () => {
  try {
    const res = await getPaymentStatistics()
    paymentStats.value = {
      total: res.data.total || 0,
      paid: res.data.paid || 0,
      unpaid: res.data.unpaid || 0,
      paidRate: res.data.paidRate || 0
    }
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const loadAwardStats = async () => {
  loading.value = true
  try {
    const res = await getAwardStatistics(awardDimension.value)
    awardData.value = res.data.data
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRegistrationStats()
  loadPaymentStats()
  loadAwardStats()
})
</script>

<style scoped>
.data-visualization {
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

