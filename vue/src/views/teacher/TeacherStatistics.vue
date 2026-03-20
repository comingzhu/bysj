<template>
  <div class="teacher-statistics">
    <el-card>
      <template #header>
        <span>数据可视化</span>
      </template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="报名统计" name="registration">
          <el-select v-model="selectedCompetitionId" @change="loadRegistrationStats" style="margin-bottom: 20px; width: 300px;" clearable>
            <el-option label="全部竞赛" :value="undefined" />
            <el-option 
              v-for="comp in competitionList" 
              :key="comp.id" 
              :label="comp.name" 
              :value="comp.id" 
            />
          </el-select>
          <el-select v-model="registrationDimension" @change="loadRegistrationStats" style="margin-bottom: 20px; width: 200px; margin-left: 10px;">
            <el-option label="按学院" value="college" />
            <el-option label="按班级" value="class" />
            <el-option label="按报名时间" value="time" />
          </el-select>
          <div style="height: 400px; margin-top: 20px;">
            <el-empty v-if="!registrationData || registrationData.length === 0" description="暂无数据" />
            <div v-else ref="registrationChartRef" style="width: 100%; height: 100%;"></div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="获奖统计" name="award">
          <el-select v-model="selectedCompetitionIdForAward" @change="loadAwardStats" style="margin-bottom: 20px; width: 300px;" clearable>
            <el-option label="全部竞赛" :value="undefined" />
            <el-option 
              v-for="comp in competitionList" 
              :key="comp.id" 
              :label="comp.name" 
              :value="comp.id" 
            />
          </el-select>
          <el-table :data="awardData" v-loading="loading">
            <el-table-column prop="competitionName" label="竞赛名称" />
            <el-table-column prop="awardLevel" label="奖项等级" />
            <el-table-column prop="submitterName" label="获奖者" />
            <el-table-column prop="score" label="分数" />
            <el-table-column prop="rank" label="排名" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { getMyCompetitions } from '../../api/competition'
import { getRegistrationList } from '../../api/registration'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '../../utils/dateFormat'
import * as echarts from 'echarts'
import { getCollegeList } from '../../utils/college'

const loading = ref(false)
const activeTab = ref('registration')
const competitionList = ref([])
const selectedCompetitionId = ref(undefined) // 默认为 undefined，表示全部竞赛
const selectedCompetitionIdForAward = ref(undefined) // 默认为 undefined，表示全部竞赛
const registrationDimension = ref('college')
const registrationData = ref([])
const registrationChartRef = ref(null)
let registrationChart = null
let resizeHandler = null
const awardData = ref([])
const collegeList = ref([])

const loadCompetitions = async () => {
  try {
    const res = await getMyCompetitions({ page: 1, size: 1000 })
    if (res.data && res.data.records) {
      competitionList.value = res.data.records
    } else if (Array.isArray(res.data)) {
      competitionList.value = res.data
    }
    // 加载完竞赛列表后，自动加载全部竞赛的统计数据
    await loadRegistrationStats()
    await loadAwardStats()
  } catch (error) {
    console.error('加载竞赛列表失败:', error)
  }
}

const loadRegistrationStats = async () => {
  loading.value = true
  try {
    // 获取该老师发布的竞赛的报名记录
    let allRegistrations = []
    const competitionsToQuery = selectedCompetitionId.value 
      ? competitionList.value.filter(c => c.id === selectedCompetitionId.value)
      : competitionList.value
    
    for (const comp of competitionsToQuery) {
      try {
        const res = await getRegistrationList({ competitionId: comp.id, page: 1, size: 1000 })
        if (res.data && res.data.records) {
          allRegistrations.push(...res.data.records.map(r => ({ ...r, competitionName: comp.name })))
        } else if (Array.isArray(res.data)) {
          allRegistrations.push(...res.data.map(r => ({ ...r, competitionName: comp.name })))
        }
      } catch (error) {
        console.error('加载报名记录失败:', error)
      }
    }
    
    // 根据维度统计
    const stats = {}
    if (registrationDimension.value === 'college') {
      // 按学院统计 - 需要查询用户信息
      const userIds = [...new Set(allRegistrations.filter(r => r.userId).map(r => r.userId))]
      if (userIds.length > 0) {
        try {
          const userRes = await request({
            url: '/user/list',
            method: 'get',
            params: { page: 1, size: 1000 }
          })
          const users = userRes.data?.records || userRes.data || []
          const userMap = new Map(users.map(u => [u.id, u]))
          
          allRegistrations.forEach(reg => {
            if (reg.userId && userMap.has(reg.userId)) {
              const college = userMap.get(reg.userId).college
              if (college) {
                stats[college] = (stats[college] || 0) + 1
              }
            }
          })
        } catch (error) {
          console.error('加载用户信息失败:', error)
        }
      }
    } else if (registrationDimension.value === 'class') {
      // 按班级统计
      const userIds = [...new Set(allRegistrations.filter(r => r.userId).map(r => r.userId))]
      if (userIds.length > 0) {
        try {
          const userRes = await request({
            url: '/user/list',
            method: 'get',
            params: { page: 1, size: 1000 }
          })
          const users = userRes.data?.records || userRes.data || []
          const userMap = new Map(users.map(u => [u.id, u]))
          
          allRegistrations.forEach(reg => {
            if (reg.userId && userMap.has(reg.userId)) {
              const className = userMap.get(reg.userId).className
              if (className) {
                stats[className] = (stats[className] || 0) + 1
              }
            }
          })
        } catch (error) {
          console.error('加载用户信息失败:', error)
        }
      }
    } else if (registrationDimension.value === 'time') {
      // 按报名时间统计（按天）
      allRegistrations.forEach(reg => {
        if (reg.createTime) {
          const date = reg.createTime.split(' ')[0]
          stats[date] = (stats[date] || 0) + 1
        }
      })
    }
    
    registrationData.value = Object.entries(stats).map(([key, value]) => ({
      name: key,
      count: value
    }))
    
    await nextTick()
    renderRegistrationChart()
  } catch (error) {
    ElMessage.error('加载失败')
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
  
  const xData = registrationData.value.map(item => item.name)
  const yData = registrationData.value.map(item => item.count)
  
  const dimensionLabels = {
    college: '按学院统计',
    class: '按班级统计',
    time: '按报名时间统计'
  }
  
  const option = {
    title: {
      text: dimensionLabels[registrationDimension.value] || '报名统计',
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
        }
      }
    ]
  }
  
  registrationChart.setOption(option)
  
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
  resizeHandler = () => {
    registrationChart?.resize()
  }
  window.addEventListener('resize', resizeHandler)
}

const loadAwardStats = async () => {
  loading.value = true
  try {
    const competitionsToQuery = selectedCompetitionIdForAward.value 
      ? competitionList.value.filter(c => c.id === selectedCompetitionIdForAward.value)
      : competitionList.value
    
    const competitionIds = competitionsToQuery.map(c => c.id)
    
    if (competitionIds.length === 0) {
      awardData.value = []
      loading.value = false
      return
    }
    
    // 查询获奖记录
    const res = await request({
      url: '/award/list',
      method: 'get',
      params: { competitionIds: competitionIds.join(',') }
    })
    
    if (res.data && Array.isArray(res.data)) {
      awardData.value = res.data
    } else {
      awardData.value = []
    }
  } catch (error) {
    ElMessage.error('加载失败')
    awardData.value = []
  } finally {
    loading.value = false
  }
}

watch([selectedCompetitionId, registrationDimension], () => {
  loadRegistrationStats()
})

watch(selectedCompetitionIdForAward, () => {
  loadAwardStats()
})

onMounted(() => {
  // 加载竞赛列表，加载完成后会自动加载统计数据
  loadCompetitions()
})

onBeforeUnmount(() => {
  if (registrationChart) {
    registrationChart.dispose()
    registrationChart = null
  }
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
})
</script>

<style scoped>
.teacher-statistics {
  padding: 20px;
}
</style>

