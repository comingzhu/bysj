<template>
  <div class="home">
    <!-- 管理员首页 -->
    <template v-if="userStore.role === 'admin'">
      <!-- 欢迎卡片 -->
      <div class="welcome-section">
        <div class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-avatar">
              <img 
                :src="getAvatarUrl(userStore.userInfo.realName || userStore.userInfo.username)" 
                :alt="userStore.userInfo.realName || userStore.userInfo.username"
                class="avatar-img"
              />
            </div>
            <div class="welcome-text">
              <h1>欢迎回来，{{ userStore.userInfo.realName || userStore.userInfo.username }}</h1>
              <p class="welcome-subtitle">今天是 {{ currentDate }}，祝您工作愉快！</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 统计卡片 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="6" v-for="(stat, index) in adminStats" :key="index">
            <el-card class="stat-card" shadow="hover" @click="router.push(stat.route)">
              <div class="stat-card-inner">
                <div class="stat-icon-wrapper" :style="{ background: stat.gradient }">
                  <el-icon size="28"><component :is="stat.icon" /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ stat.value }}</div>
                  <div class="stat-label">{{ stat.label }}</div>
                  <div class="stat-desc">{{ stat.desc }}</div>
                </div>
                <div class="stat-arrow">
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 图表和快捷操作 -->
      <el-row :gutter="20" class="content-section">
        <el-col :span="16">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><DataAnalysis /></el-icon>
                  <span class="header-title">数据概览</span>
                </div>
                <el-button link type="primary" @click="router.push('/admin/statistics')" class="view-more-btn">
                  查看详情
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </template>
            <div ref="chartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="quick-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><Operation /></el-icon>
                  <span class="header-title">快捷操作</span>
                </div>
              </div>
            </template>
            <div class="quick-actions">
              <div 
                v-for="(action, index) in quickActions" 
                :key="index"
                class="action-item"
                @click="router.push(action.route)"
              >
                <div class="action-icon" :style="{ background: action.color }">
                  <el-icon><component :is="action.icon" /></el-icon>
                </div>
                <span class="action-text">{{ action.label }}</span>
                <el-icon class="action-arrow"><ArrowRight /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
    
    <!-- 老师端首页 -->
    <template v-else-if="userStore.role === 'teacher'">
      <!-- 欢迎卡片 -->
      <div class="welcome-section">
        <div class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-avatar">
              <img 
                :src="getAvatarUrl(userStore.userInfo.realName || userStore.userInfo.username)" 
                :alt="userStore.userInfo.realName || userStore.userInfo.username"
                class="avatar-img"
              />
            </div>
            <div class="welcome-text">
              <h1>欢迎回来，{{ userStore.userInfo.realName || userStore.userInfo.username }}</h1>
              <p class="welcome-subtitle">今天是 {{ currentDate }}，祝您工作愉快！</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 统计卡片 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="6" v-for="(stat, index) in teacherStatsList" :key="index">
            <el-card class="stat-card" shadow="hover" @click="router.push(stat.route)">
              <div class="stat-card-inner">
                <div class="stat-icon-wrapper" :style="{ background: stat.gradient }">
                  <el-icon size="28"><component :is="stat.icon" /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ stat.value }}</div>
                  <div class="stat-label">{{ stat.label }}</div>
                  <div class="stat-desc">{{ stat.desc }}</div>
                </div>
                <div class="stat-arrow">
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 图表和快捷操作 -->
      <el-row :gutter="20" class="content-section">
        <el-col :span="16">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><DataAnalysis /></el-icon>
                  <span class="header-title">数据概览</span>
                </div>
                <el-button link type="primary" @click="router.push('/teacher/statistics')" class="view-more-btn">
                  查看详情
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </template>
            <div ref="teacherChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="quick-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><Operation /></el-icon>
                  <span class="header-title">快捷操作</span>
                </div>
              </div>
            </template>
            <div class="quick-actions">
              <div 
                v-for="(action, index) in teacherQuickActions" 
                :key="index"
                class="action-item"
                @click="router.push(action.route)"
              >
                <div class="action-icon" :style="{ background: action.color }">
                  <el-icon><component :is="action.icon" /></el-icon>
                </div>
                <span class="action-text">{{ action.label }}</span>
                <el-icon class="action-arrow"><ArrowRight /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
    
    <!-- 评分员端首页 -->
    <template v-else-if="userStore.role === 'judge'">
      <!-- 欢迎卡片 -->
      <div class="welcome-section">
        <div class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-avatar">
              <img 
                :src="getAvatarUrl(userStore.userInfo.realName || userStore.userInfo.username)" 
                :alt="userStore.userInfo.realName || userStore.userInfo.username"
                class="avatar-img"
              />
            </div>
            <div class="welcome-text">
              <h1>欢迎回来，{{ userStore.userInfo.realName || userStore.userInfo.username }}</h1>
              <p class="welcome-subtitle">今天是 {{ currentDate }}，祝您工作愉快！</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 统计卡片 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="6" v-for="(stat, index) in judgeStatsList" :key="index">
            <el-card class="stat-card" shadow="hover" @click="router.push(stat.route)">
              <div class="stat-card-inner">
                <div class="stat-icon-wrapper" :style="{ background: stat.gradient }">
                  <el-icon size="28"><component :is="stat.icon" /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ stat.value }}</div>
                  <div class="stat-label">{{ stat.label }}</div>
                  <div class="stat-desc">{{ stat.desc }}</div>
                </div>
                <div class="stat-arrow">
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 图表和快捷操作 -->
      <el-row :gutter="20" class="content-section">
        <el-col :span="16">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><DataAnalysis /></el-icon>
                  <span class="header-title">评分数据概览</span>
                </div>
                <el-button link type="primary" @click="router.push('/judge/statistics')" class="view-more-btn">
                  查看详情
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </template>
            <div ref="judgeChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="quick-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><Operation /></el-icon>
                  <span class="header-title">快捷操作</span>
                </div>
              </div>
            </template>
            <div class="quick-actions">
              <div 
                v-for="(action, index) in judgeQuickActions" 
                :key="index"
                class="action-item"
                @click="router.push(action.route)"
              >
                <div class="action-icon" :style="{ background: action.color }">
                  <el-icon><component :is="action.icon" /></el-icon>
                </div>
                <span class="action-text">{{ action.label }}</span>
                <el-icon class="action-arrow"><ArrowRight /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
    
    <!-- 其他角色首页 -->
    <!-- 学生端首页 -->
    <template v-else-if="userStore.role === 'student'">
      <!-- 欢迎卡片 -->
      <div class="welcome-section">
        <div class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-avatar">
              <img 
                :src="getAvatarUrl(userStore.userInfo.realName || userStore.userInfo.username)" 
                :alt="userStore.userInfo.realName || userStore.userInfo.username"
                class="avatar-img"
              />
            </div>
            <div class="welcome-text">
              <h1>欢迎回来，{{ userStore.userInfo.realName || userStore.userInfo.username }}</h1>
              <p class="welcome-subtitle">今天是 {{ currentDate }}，祝您学习愉快！</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 快捷操作和余额 -->
      <el-row :gutter="20" class="content-section">
        <el-col :span="16">
          <el-card class="quick-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><Trophy /></el-icon>
                  <span class="header-title">竞赛列表</span>
                </div>
                <el-button link type="primary" @click="router.push('/competition/list')" class="view-more-btn">
                  查看更多
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </template>
            <div v-loading="competitionLoading" class="competition-preview">
              <div v-if="competitionList.length === 0" class="empty-state">
                <el-empty description="暂无竞赛" />
              </div>
              <div v-else>
                <div 
                  v-for="item in competitionList.slice(0, 5)" 
                  :key="item.id"
                  class="competition-item"
                  @click="router.push(`/competition/detail/${item.id}`)"
                >
                  <div class="competition-info">
                    <div class="competition-name">{{ item.name }}</div>
                    <div class="competition-meta">
                      <el-tag size="small" type="info">{{ item.category }}</el-tag>
                      <span class="competition-type">{{ item.type === 0 ? '个人赛' : '团队赛' }}</span>
                      <span class="competition-fee">报名费：¥{{ item.registrationFee }}</span>
                    </div>
                  </div>
                  <el-icon class="item-arrow"><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="balance-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><Money /></el-icon>
                  <span class="header-title">账户余额</span>
                </div>
              </div>
            </template>
            <div class="balance-content">
              <div class="balance-amount">¥{{ studentBalance.toFixed(2) }}</div>
              <el-button type="primary" @click="router.push('/my/registrations')" style="width: 100%; margin-top: 20px;">
                充值
              </el-button>
            </div>
          </el-card>
          
          <el-card class="quick-card" shadow="hover" style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><Operation /></el-icon>
                  <span class="header-title">快捷操作</span>
                </div>
              </div>
            </template>
            <div class="quick-actions">
              <div 
                v-for="(action, index) in studentQuickActions" 
                :key="index"
                class="action-item"
                @click="router.push(action.route)"
              >
                <div class="action-icon" :style="{ background: action.color }">
                  <el-icon><component :is="action.icon" /></el-icon>
                </div>
                <span class="action-text">{{ action.label }}</span>
                <el-icon class="action-arrow"><ArrowRight /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 消息推送 -->
      <el-card class="notice-card" shadow="hover" style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <el-icon class="header-icon"><Bell /></el-icon>
              <span class="header-title">消息推送</span>
            </div>
            <el-button link type="primary" @click="router.push('/my/notices')" class="view-more-btn">
              查看全部
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
        <div v-loading="noticeLoading" class="notice-list">
          <div v-if="noticeList.length === 0" class="empty-state">
            <el-empty description="暂无消息" />
          </div>
          <div v-else>
            <div 
              v-for="notice in noticeList.slice(0, 5)" 
              :key="notice.id"
              class="notice-item"
              :class="{ 'unread': notice.isRead === 0 }"
              @click="handleViewNotice(notice)"
            >
              <div class="notice-content">
                <div class="notice-title">
                  <el-icon v-if="notice.isRead === 0" class="unread-dot"><Bell /></el-icon>
                  {{ notice.title }}
                </div>
                <div class="notice-time">{{ formatDateTime(notice.createTime) }}</div>
              </div>
              <el-icon class="item-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </el-card>
    </template>
    
    <template v-else>
      <el-card>
        <template #header>
          <span>欢迎，{{ userStore.userInfo.realName || userStore.userInfo.username }}</span>
        </template>
        <div class="welcome-content">
          <h2>大学生竞赛活动管理平台</h2>
          <p>当前角色：{{ getRoleName(userStore.role) }}</p>
          <el-button type="primary" @click="goToHomePage" style="margin-top: 20px">
            进入工作台
          </el-button>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getDashboardStatistics } from '../api/statistics'
import { getMyCompetitions, getCompetitionList } from '../api/competition'
import { getRegistrationList } from '../api/registration'
import { getWorkList } from '../api/work'
import { getAwardList } from '../api/award'
import { getScoreStatistics } from '../api/score'
import { getNoticeList } from '../api/message'
import { getBalance } from '../api/user'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { User, Trophy, Document, Money, DataAnalysis, FolderOpened, ArrowRight, Operation, Edit, Bell, Check, Clock } from '@element-plus/icons-vue'
import { formatDateTime } from '../utils/dateFormat'

const router = useRouter()
const userStore = useUserStore()
const dashboardData = ref({})
const teacherStats = ref({})
const judgeStats = ref({})
const chartRef = ref(null)
const teacherChartRef = ref(null)
const judgeChartRef = ref(null)
let chart = null
let teacherChart = null
let judgeChart = null
let resizeHandler = null
let teacherResizeHandler = null
let judgeResizeHandler = null

// 学生端数据
const studentBalance = ref(0)
const competitionList = ref([])
const competitionLoading = ref(false)
const noticeList = ref([])
const noticeLoading = ref(false)

const currentDate = computed(() => {
  const date = new Date()
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekday = weekdays[date.getDay()]
  return `${month}月${day}日 ${weekday}`
})

// 生成虚拟头像URL
const getAvatarUrl = (name) => {
  if (!name) return 'https://api.dicebear.com/7.x/avataaars/svg?seed=User'
  // 使用DiceBear生成头像，基于用户名
  const seed = encodeURIComponent(name)
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${seed}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf`
}

const paymentRate = computed(() => {
  const total = dashboardData.value.registrations?.total || 0
  const paid = dashboardData.value.registrations?.paid || 0
  return total > 0 ? ((paid / total) * 100).toFixed(1) : 0
})

const adminStats = computed(() => [
  {
    icon: 'User',
    label: '用户总数',
    value: dashboardData.value.users?.total || 0,
    desc: `学生 ${dashboardData.value.users?.students || 0} | 老师 ${dashboardData.value.users?.teachers || 0}`,
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    route: '/admin/users'
  },
  {
    icon: 'Trophy',
    label: '竞赛总数',
    value: dashboardData.value.competitions?.total || 0,
    desc: `已发布 ${dashboardData.value.competitions?.published || 0} | 待审核 ${dashboardData.value.competitions?.pending || 0}`,
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    route: '/admin/competitions'
  },
  {
    icon: 'Document',
    label: '报名总数',
    value: dashboardData.value.registrations?.total || 0,
    desc: `已通过 ${dashboardData.value.registrations?.approved || 0} | 待审核 ${dashboardData.value.registrations?.pending || 0}`,
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    route: '/admin/registrations'
  },
  {
    icon: 'Money',
    label: '已缴费',
    value: dashboardData.value.registrations?.paid || 0,
    desc: `缴费率 ${paymentRate.value}%`,
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    route: '/admin/payment'
  }
])

const quickActions = [
  { icon: 'User', label: '用户管理', route: '/admin/users', color: '#409EFF' },
  { icon: 'Trophy', label: '竞赛审核', route: '/admin/competitions', color: '#67C23A' },
  { icon: 'Document', label: '报名审核', route: '/admin/registrations', color: '#E6A23C' },
  { icon: 'Money', label: '缴费管理', route: '/admin/payment', color: '#F56C6C' },
  { icon: 'DataAnalysis', label: '数据可视化', route: '/admin/statistics', color: '#909399' }
]

const teacherQuickActions = [
  { icon: 'Trophy', label: '竞赛管理', route: '/teacher/competitions', color: '#409EFF' },
  { icon: 'Document', label: '报名管理', route: '/teacher/registrations', color: '#67C23A' },
  { icon: 'FolderOpened', label: '作品管理', route: '/teacher/works', color: '#E6A23C' },
  { icon: 'DataAnalysis', label: '数据可视化', route: '/teacher/statistics', color: '#F56C6C' }
]

const judgeQuickActions = [
  { icon: 'Edit', label: '评分任务', route: '/judge/tasks', color: '#409EFF' },
  { icon: 'DataAnalysis', label: '评分统计', route: '/judge/statistics', color: '#67C23A' },
  { icon: 'Bell', label: '通知公告', route: '/judge/notices', color: '#E6A23C' }
]

const studentQuickActions = [
  { icon: 'Trophy', label: '竞赛列表', route: '/competition/list', color: '#409EFF' },
  { icon: 'Document', label: '我的报名', route: '/my/registrations', color: '#67C23A' },
  { icon: 'Medal', label: '我的获奖', route: '/my/awards', color: '#E6A23C' },
  { icon: 'Bell', label: '通知公告', route: '/my/notices', color: '#F56C6C' }
]

const judgeStatsList = computed(() => [
  {
    icon: 'Document',
    label: '总任务数',
    value: judgeStats.value.total || 0,
    desc: '分配的评分任务总数',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    route: '/judge/tasks'
  },
  {
    icon: 'Check',
    label: '已评分',
    value: judgeStats.value.scored || 0,
    desc: '已完成评分任务数',
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    route: '/judge/tasks'
  },
  {
    icon: 'Clock',
    label: '待评分',
    value: judgeStats.value.pending || 0,
    desc: '待完成评分任务数',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    route: '/judge/tasks'
  },
  {
    icon: 'DataAnalysis',
    label: '平均分',
    value: judgeStats.value.avgScore ? judgeStats.value.avgScore.toFixed(1) : '0.0',
    desc: '所有评分平均分',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    route: '/judge/statistics'
  }
])

const teacherStatsList = computed(() => [
  {
    icon: 'Trophy',
    label: '我的竞赛',
    value: teacherStats.value.competitions || 0,
    desc: '已发布竞赛数量',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    route: '/teacher/competitions'
  },
  {
    icon: 'Document',
    label: '报名总数',
    value: teacherStats.value.registrations || 0,
    desc: '所有竞赛报名人数',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    route: '/teacher/registrations'
  },
  {
    icon: 'FolderOpened',
    label: '作品总数',
    value: teacherStats.value.works || 0,
    desc: '已提交作品数量',
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    route: '/teacher/works'
  },
  {
    icon: 'DataAnalysis',
    label: '获奖总数',
    value: teacherStats.value.awards || 0,
    desc: '竞赛获奖人数',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    route: '/teacher/statistics'
  }
])

const loadDashboardData = async () => {
  try {
    if (userStore.role === 'admin') {
      const res = await getDashboardStatistics()
      if (res.data) {
        dashboardData.value = res.data
        await nextTick()
        renderChart()
      }
    } else if (userStore.role === 'teacher') {
      // 加载老师的数据 - 只统计该老师发布的竞赛相关数据
      const compRes = await getMyCompetitions({ page: 1, size: 1000 })
      let competitionIds = []
      if (compRes.data?.records) {
        competitionIds = compRes.data.records.map(c => c.id)
      } else if (Array.isArray(compRes.data)) {
        competitionIds = compRes.data.map(c => c.id)
      }
      
      let competitions = competitionIds.length
      let registrations = 0
      let works = 0
      let awards = 0
      
      if (competitionIds.length > 0) {
        // 统计报名数
        const regPromises = competitionIds.map(id => getRegistrationList({ competitionId: id, page: 1, size: 1000 }))
        const regResults = await Promise.all(regPromises)
        regResults.forEach(res => {
          if (res.data?.records) {
            registrations += res.data.records.length
          } else if (Array.isArray(res.data)) {
            registrations += res.data.length
          }
        })
        
        // 统计作品数
        const workPromises = competitionIds.map(id => getWorkList({ competitionId: id, page: 1, size: 1000 }))
        const workResults = await Promise.all(workPromises)
        workResults.forEach(res => {
          if (res.data?.records) {
            works += res.data.records.length
          } else if (Array.isArray(res.data)) {
            works += res.data.length
          }
        })
        
        // 统计获奖数
        try {
          const awardRes = await getAwardList(competitionIds)
          if (Array.isArray(awardRes.data)) {
            awards = awardRes.data.length
          }
        } catch (error) {
          console.error('加载获奖数据失败:', error)
        }
      }
      
      teacherStats.value = {
        competitions,
        registrations,
        works,
        awards
      }
      await nextTick()
      setTimeout(() => {
        renderTeacherChart()
      }, 100)
    } else if (userStore.role === 'judge') {
      // 加载评分员的数据
      const res = await getScoreStatistics()
      if (res.data) {
        judgeStats.value = res.data
        await nextTick()
        setTimeout(() => {
          renderJudgeChart()
        }, 100)
      }
    } else if (userStore.role === 'student') {
      // 加载学生端数据
      await loadStudentData()
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const loadStudentData = async () => {
  try {
    // 加载余额
    const balanceRes = await getBalance()
    if (balanceRes.data) {
      studentBalance.value = balanceRes.data.balance || 0
    }
    
    // 加载竞赛列表
    competitionLoading.value = true
    const compRes = await getCompetitionList({
      // 不限定状态，由前端控制展示哪些竞赛
      page: 1,
      size: 5
    })
    if (compRes.data && compRes.data.records) {
      const all = compRes.data.records
      // 只保留已发布 / 已暂停 / 已结束的竞赛，学生可以查看信息
      competitionList.value = all.filter(c => [4, 5, 6].includes(c.status))
    }
    competitionLoading.value = false
    
    // 加载消息列表
    noticeLoading.value = true
    const noticeRes = await getNoticeList({ page: 1, size: 5 })
    if (noticeRes.data && noticeRes.data.records) {
      noticeList.value = noticeRes.data.records
    }
    noticeLoading.value = false
  } catch (error) {
    console.error('加载学生数据失败:', error)
    competitionLoading.value = false
    noticeLoading.value = false
  }
}

const handleViewNotice = (notice) => {
  router.push('/my/notices')
}

const renderChart = () => {
  if (!chartRef.value || !dashboardData.value) return
  
  if (chart) {
    chart.dispose()
  }
  
  chart = echarts.init(chartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['用户', '竞赛', '报名', '缴费']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['总数', '待审核', '已通过']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '用户',
        type: 'bar',
        data: [
          dashboardData.value.users?.total || 0,
          0,
          dashboardData.value.users?.total || 0
        ],
        itemStyle: { color: '#667eea' }
      },
      {
        name: '竞赛',
        type: 'bar',
        data: [
          dashboardData.value.competitions?.total || 0,
          dashboardData.value.competitions?.pending || 0,
          dashboardData.value.competitions?.published || 0
        ],
        itemStyle: { color: '#f5576c' }
      },
      {
        name: '报名',
        type: 'bar',
        data: [
          dashboardData.value.registrations?.total || 0,
          dashboardData.value.registrations?.pending || 0,
          dashboardData.value.registrations?.approved || 0
        ],
        itemStyle: { color: '#4facfe' }
      },
      {
        name: '缴费',
        type: 'bar',
        data: [
          dashboardData.value.registrations?.paid || 0,
          0,
          dashboardData.value.registrations?.paid || 0
        ],
        itemStyle: { color: '#43e97b' }
      }
    ]
  }
  
  chart.setOption(option)
  
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
  resizeHandler = () => {
    chart?.resize()
  }
  window.addEventListener('resize', resizeHandler)
}

const renderTeacherChart = () => {
  if (!teacherChartRef.value || !teacherStats.value) return
  
  if (teacherChart) {
    teacherChart.dispose()
  }
  
  teacherChart = echarts.init(teacherChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['竞赛', '报名', '作品', '获奖']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['我的数据']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '竞赛',
        type: 'bar',
        data: [teacherStats.value.competitions || 0],
        itemStyle: { color: '#667eea' }
      },
      {
        name: '报名',
        type: 'bar',
        data: [teacherStats.value.registrations || 0],
        itemStyle: { color: '#4facfe' }
      },
      {
        name: '作品',
        type: 'bar',
        data: [teacherStats.value.works || 0],
        itemStyle: { color: '#43e97b' }
      },
      {
        name: '获奖',
        type: 'bar',
        data: [teacherStats.value.awards || 0],
        itemStyle: { color: '#f5576c' }
      }
    ]
  }
  
  teacherChart.setOption(option)
  
  if (teacherResizeHandler) {
    window.removeEventListener('resize', teacherResizeHandler)
  }
  teacherResizeHandler = () => {
    teacherChart?.resize()
  }
  window.addEventListener('resize', teacherResizeHandler)
}

const renderJudgeChart = () => {
  if (!judgeChartRef.value || !judgeStats.value) return
  
  if (judgeChart) {
    judgeChart.dispose()
  }
  
  judgeChart = echarts.init(judgeChartRef.value)
  
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
          { value: judgeStats.value.scored || 0, name: '已评分', itemStyle: { color: '#43e97b' } },
          { value: judgeStats.value.pending || 0, name: '待评分', itemStyle: { color: '#f5576c' } }
        ]
      }
    ]
  }
  
  judgeChart.setOption(option)
  
  if (judgeResizeHandler) {
    window.removeEventListener('resize', judgeResizeHandler)
  }
  judgeResizeHandler = () => {
    judgeChart?.resize()
  }
  window.addEventListener('resize', judgeResizeHandler)
}

const getRoleName = (role) => {
  const roleMap = {
    student: '学生',
    teacher: '老师',
    judge: '评分员',
    admin: '管理员'
  }
  return roleMap[role] || role
}

const getHomePathByRole = (role) => {
  const roleMap = {
    'admin': '/home',
    'teacher': '/teacher/competitions',
    'student': '/competition/list',
    'judge': '/judge/tasks'
  }
  return roleMap[role] || '/home'
}

const goToHomePage = () => {
  const homePath = getHomePathByRole(userStore.role)
  router.push(homePath)
}

onMounted(async () => {
  // 确保用户信息是最新的
  if (userStore.token && (!userStore.userInfo.id || !userStore.userInfo.realName)) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
  loadDashboardData()
})

onBeforeUnmount(() => {
  if (chart) {
    chart.dispose()
    chart = null
  }
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
  if (teacherChart) {
    teacherChart.dispose()
    teacherChart = null
  }
  if (teacherResizeHandler) {
    window.removeEventListener('resize', teacherResizeHandler)
  }
  if (judgeChart) {
    judgeChart.dispose()
    judgeChart = null
  }
  if (judgeResizeHandler) {
    window.removeEventListener('resize', judgeResizeHandler)
  }
})
</script>

<style scoped>
.home {
  padding: 0;
}

/* 欢迎区域 */
.welcome-section {
  margin-bottom: 24px;
}

.welcome-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 32px 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e7ed;
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: 24px;
}

.welcome-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  overflow: hidden;
  border: 3px solid #409EFF;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.welcome-text h1 {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.welcome-subtitle {
  font-size: 15px;
  color: #606266;
  margin: 0;
}

/* 统计卡片区域 */
.stats-section {
  margin-bottom: 24px;
}

.stat-card {
  height: 100%;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e4e7ed;
  border-radius: 12px;
}

.stat-card:hover {
  transform: translateY(-6px);
  border-color: #409EFF;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.15);
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 8px 0;
  position: relative;
}

.stat-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.stat-card:hover .stat-icon-wrapper {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 6px;
  line-height: 1;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.stat-label {
  font-size: 15px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 4px;
}

.stat-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-arrow {
  color: #c0c4cc;
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateX(-10px);
}

.stat-card:hover .stat-arrow {
  opacity: 1;
  transform: translateX(0);
  color: #409EFF;
}

/* 内容区域 */
.content-section {
  margin-top: 0;
}

.chart-card,
.quick-card {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.chart-card:hover,
.quick-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 18px;
  color: #409EFF;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.view-more-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.view-more-btn:hover {
  gap: 8px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #fafafa;
  border: 1px solid transparent;
}

.action-item:hover {
  background-color: #f0f9ff;
  border-color: #b3d8ff;
  transform: translateX(4px);
}

.action-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  font-size: 18px;
}

.action-text {
  flex: 1;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.action-arrow {
  color: #c0c4cc;
  transition: all 0.3s ease;
  opacity: 0;
}

.action-item:hover .action-arrow {
  opacity: 1;
  color: #409EFF;
  transform: translateX(4px);
}

/* 其他角色样式 */
.welcome-content h2 {
  margin-bottom: 20px;
  color: #409EFF;
  font-size: 28px;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stat-value {
    font-size: 28px;
  }
  
  .stat-icon-wrapper {
    width: 56px;
    height: 56px;
  }
}

@media (max-width: 768px) {
  .welcome-content {
    flex-direction: column;
    text-align: center;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .stat-icon-wrapper {
    width: 48px;
    height: 48px;
  }
  
  .stat-card-inner {
    gap: 12px;
  }
}

/* 学生端样式 */
.competition-preview {
  min-height: 200px;
}

.competition-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  margin-bottom: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.competition-item:hover {
  border-color: #409EFF;
  background-color: #f5f7fa;
  transform: translateX(4px);
}

.competition-info {
  flex: 1;
}

.competition-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.competition-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #909399;
}

.competition-type {
  color: #606266;
}

.competition-fee {
  color: #F56C6C;
  font-weight: 500;
}

.item-arrow {
  color: #c0c4cc;
  transition: all 0.3s ease;
}

.competition-item:hover .item-arrow {
  color: #409EFF;
  transform: translateX(4px);
}

.balance-card {
  margin-bottom: 20px;
}

.balance-content {
  text-align: center;
  padding: 20px 0;
}

.balance-amount {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 20px;
}

.notice-list {
  min-height: 200px;
}

.notice-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  margin-bottom: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.notice-item:hover {
  border-color: #409EFF;
  background-color: #f5f7fa;
}

.notice-item.unread {
  background-color: #ecf5ff;
  border-color: #b3d8ff;
}

.notice-content {
  flex: 1;
}

.notice-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.unread-dot {
  color: #F56C6C;
  font-size: 12px;
}

.notice-time {
  font-size: 13px;
  color: #909399;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}
</style>












