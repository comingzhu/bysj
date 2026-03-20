<template>
  <div class="score-statistics">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评分统计</span>
          <el-button type="primary" @click="loadData" :loading="loading">刷新</el-button>
        </div>
      </template>
      
      <div class="statistics">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">总任务数</div>
                <div class="stat-value">{{ statistics.total }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">已评分</div>
                <div class="stat-value" style="color: #67C23A;">{{ statistics.scored }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">待评分</div>
                <div class="stat-value" style="color: #E6A23C;">{{ statistics.pending }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">平均分</div>
                <div class="stat-value" style="color: #409EFF;">{{ statistics.avgScore.toFixed(2) }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <el-divider />
      
      <!-- 图表统计 -->
      <div class="charts-section">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <div class="chart-header">
                  <span>评分任务分布</span>
                </div>
              </template>
              <div ref="taskDistributionChartRef" class="chart-container"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <div class="chart-header">
                  <span>评分分布</span>
                </div>
              </template>
              <div ref="scoreDistributionChartRef" class="chart-container"></div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <el-divider />
      
      <div class="table-header">
        <h3>评分记录</h3>
      </div>
      
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="competitionName" label="竞赛名称" min-width="200" />
        <el-table-column prop="workTitle" label="作品标题" min-width="200" />
        <el-table-column prop="totalScore" label="评分" width="120">
          <template #default="{ row }">
            <span style="font-size: 16px; font-weight: bold; color: #409EFF;">{{ row.totalScore }}</span>
            <span style="color: #909399; margin-left: 4px;">分</span>
          </template>
        </el-table-column>
        <el-table-column prop="scoreTime" label="评分时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.scoreTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">查看详情</el-button>
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

    <!-- 评分详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="评分详情" width="800px">
      <div v-if="currentRecord" class="score-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="竞赛名称">{{ currentRecord.competitionName }}</el-descriptions-item>
          <el-descriptions-item label="作品标题">{{ currentRecord.workTitle }}</el-descriptions-item>
          <el-descriptions-item label="总分">
            <span style="font-size: 20px; font-weight: bold; color: #409EFF;">{{ currentRecord.totalScore }}</span>
            <span style="margin-left: 10px; color: #909399;">分</span>
          </el-descriptions-item>
          <el-descriptions-item label="评分时间">{{ formatDateTime(currentRecord.scoreTime) }}</el-descriptions-item>
          <el-descriptions-item label="评分详情" :span="2">
            <div v-if="scoreDetails" style="padding: 10px; background: #f5f7fa; border-radius: 4px;">
              <div v-for="(value, key) in scoreDetails" :key="key" style="margin-bottom: 8px; display: flex; justify-content: space-between;">
                <span style="font-weight: bold;">{{ key }}：</span>
                <span style="color: #409EFF; font-weight: bold;">{{ value }}分</span>
              </div>
            </div>
            <div v-else style="color: #909399;">无详细评分</div>
          </el-descriptions-item>
          <el-descriptions-item label="评语" :span="2">
            <div style="white-space: pre-wrap; padding: 10px; background: #f5f7fa; border-radius: 4px;">
              {{ currentRecord.comment || '无' }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getScoreStatistics, getScoreRecords } from '../../api/score'
import { formatDateTime } from '../../utils/dateFormat'
import * as echarts from 'echarts'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const detailDialogVisible = ref(false)
const currentRecord = ref(null)
const scoreDetails = ref(null)

const statistics = ref({
  total: 0,
  scored: 0,
  pending: 0,
  avgScore: 0
})

// 图表相关
const taskDistributionChartRef = ref(null)
const scoreDistributionChartRef = ref(null)
let taskDistributionChart = null
let scoreDistributionChart = null
let resizeHandlers = []

const loadData = async () => {
  loading.value = true
  try {
    // 加载统计数据
    const statsRes = await getScoreStatistics()
    if (statsRes.data) {
      statistics.value = statsRes.data
    }
    
    // 加载评分记录（获取所有记录用于图表）
    const recordsRes = await getScoreRecords({ page: 1, size: 1000 })
    let allRecords = []
    if (recordsRes.data && recordsRes.data.records) {
      allRecords = recordsRes.data.records
      // 分页显示
      const start = (page.value - 1) * size.value
      const end = start + size.value
      tableData.value = allRecords.slice(start, end)
      total.value = recordsRes.data.total || allRecords.length
    } else {
      tableData.value = []
      total.value = 0
    }
    
    // 渲染图表
    await nextTick()
    setTimeout(() => {
      renderTaskDistributionChart()
      renderScoreDistributionChart(allRecords)
    }, 100)
  } catch (error) {
    ElMessage.error('加载失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 渲染任务分布图表（饼图）
const renderTaskDistributionChart = () => {
  if (!taskDistributionChartRef.value) return
  
  if (taskDistributionChart) {
    taskDistributionChart.dispose()
  }
  
  taskDistributionChart = echarts.init(taskDistributionChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['已评分', '待评分']
    },
    series: [
      {
        name: '评分任务',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}\n({d}%)'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: [
          { value: statistics.value.scored || 0, name: '已评分', itemStyle: { color: '#67C23A' } },
          { value: statistics.value.pending || 0, name: '待评分', itemStyle: { color: '#E6A23C' } }
        ]
      }
    ]
  }
  
  taskDistributionChart.setOption(option)
  
  const handler = () => taskDistributionChart?.resize()
  resizeHandlers.push({ chart: taskDistributionChart, handler })
  window.addEventListener('resize', handler)
}

// 渲染评分分布图表（柱状图）
const renderScoreDistributionChart = (records) => {
  if (!scoreDistributionChartRef.value) return
  
  if (scoreDistributionChart) {
    scoreDistributionChart.dispose()
  }
  
  scoreDistributionChart = echarts.init(scoreDistributionChartRef.value)
  
  // 按分数段统计
  const scoreRanges = [
    { min: 90, max: 100, label: '90-100' },
    { min: 80, max: 89, label: '80-89' },
    { min: 70, max: 79, label: '70-79' },
    { min: 60, max: 69, label: '60-69' },
    { min: 0, max: 59, label: '0-59' }
  ]
  
  const scoreCounts = scoreRanges.map(range => {
    return records.filter(r => {
      const score = parseFloat(r.totalScore) || 0
      return score >= range.min && score <= range.max
    }).length
  })
  
  const option = {
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
      data: scoreRanges.map(r => r.label),
      axisLabel: {
        rotate: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '数量'
    },
    series: [
      {
        name: '评分数量',
        type: 'bar',
        data: scoreCounts,
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
  
  scoreDistributionChart.setOption(option)
  
  const handler = () => scoreDistributionChart?.resize()
  resizeHandlers.push({ chart: scoreDistributionChart, handler })
  window.addEventListener('resize', handler)
}


const handleViewDetail = (row) => {
  currentRecord.value = row
  
  // 解析评分详情
  try {
    scoreDetails.value = row.scoreDetails ? JSON.parse(row.scoreDetails) : null
  } catch (e) {
    scoreDetails.value = null
  }
  
  detailDialogVisible.value = true
}

onMounted(() => {
  loadData()
})

onBeforeUnmount(() => {
  if (taskDistributionChart) {
    taskDistributionChart.dispose()
    taskDistributionChart = null
  }
  if (scoreDistributionChart) {
    scoreDistributionChart.dispose()
    scoreDistributionChart = null
  }
  resizeHandlers.forEach(({ handler }) => {
    window.removeEventListener('resize', handler)
  })
  resizeHandlers = []
})
</script>

<style scoped>
.score-statistics {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistics {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 10px 0;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
}

.table-header {
  margin: 20px 0 10px 0;
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.score-detail {
  padding: 10px 0;
}

.charts-section {
  margin: 20px 0;
}

.chart-header {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.chart-container-large {
  width: 100%;
  height: 400px;
}
</style>
