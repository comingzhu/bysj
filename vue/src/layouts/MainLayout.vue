<template>
  <el-container class="main-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span class="logo-text">Competition</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
        background-color="#ffffff"
        text-color="#606266"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/home" class="menu-item">
          <el-icon><HomeFilled /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        
        <!-- 学生端菜单 -->
        <template v-if="userStore.role === 'student'">
          <el-menu-item index="/competition/list" class="menu-item">
            <el-icon><Trophy /></el-icon>
            <span>竞赛列表</span>
          </el-menu-item>
          <el-menu-item index="/my/registrations" class="menu-item">
            <el-icon><Document /></el-icon>
            <span>我的报名</span>
          </el-menu-item>
          <el-menu-item index="/my/awards" class="menu-item">
            <el-icon><Medal /></el-icon>
            <span>我的获奖</span>
          </el-menu-item>
          <el-menu-item index="/my/notices" class="menu-item">
            <el-icon><Bell /></el-icon>
            <span>通知公告</span>
          </el-menu-item>
        </template>

        <!-- 管理员端菜单 -->
        <template v-if="userStore.role === 'admin'">
          <el-menu-item index="/admin/users" class="menu-item">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/competitions" class="menu-item">
            <el-icon><Trophy /></el-icon>
            <span>赛事审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/school-competitions" class="menu-item">
            <el-icon><List /></el-icon>
            <span>所有校赛</span>
          </el-menu-item>
          <el-menu-item index="/admin/system/competition" class="menu-item">
            <el-icon><Promotion /></el-icon>
            <span>省赛信息发布</span>
          </el-menu-item>

          <el-menu-item index="/admin/payment" class="menu-item">
            <el-icon><Money /></el-icon>
            <span>缴费管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/awards" class="menu-item">
            <el-icon><Medal /></el-icon>
            <span>获奖管理</span>
          </el-menu-item>

          <el-menu-item index="/admin/works" class="menu-item">
            <el-icon><FolderOpened /></el-icon>
            <span>作品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/statistics" class="menu-item">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据可视化</span>
          </el-menu-item>
          <el-menu-item index="/admin/notice" class="menu-item">
            <el-icon><Bell /></el-icon>
            <span>通知公告管理</span>
          </el-menu-item>
          <el-sub-menu index="system" class="sub-menu">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/admin/system/config" class="sub-menu-item">
              <el-icon><Tools /></el-icon>
              <span>系统参数设置</span>
            </el-menu-item>
          </el-sub-menu>
        </template>

        <!-- 老师端菜单 -->
        <template v-if="userStore.role === 'teacher'">
          <el-menu-item index="/teacher/competitions" class="menu-item">
            <el-icon><Trophy /></el-icon>
            <span>竞赛管理</span>
          </el-menu-item>

          <el-menu-item index="/teacher/awards" class="menu-item">
            <el-icon><Medal /></el-icon>
            <span>获奖管理</span>
          </el-menu-item>

          <el-menu-item index="/teacher/registrations" class="menu-item">
            <el-icon><Document /></el-icon>
            <span>报名管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/works" class="menu-item">
            <el-icon><FolderOpened /></el-icon>
            <span>作品管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/statistics" class="menu-item">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据可视化</span>
          </el-menu-item>
          <el-menu-item index="/teacher/notices" class="menu-item">
            <el-icon><Bell /></el-icon>
            <span>通知公告</span>
          </el-menu-item>
        </template>

        <!-- 评分员端菜单 -->
        <template v-if="userStore.role === 'judge'">
          <el-menu-item index="/judge/tasks" class="menu-item">
            <el-icon><Edit /></el-icon>
            <span>评分任务</span>
          </el-menu-item>
          <el-menu-item index="/judge/statistics" class="menu-item">
            <el-icon><DataAnalysis /></el-icon>
            <span>评分统计</span>
          </el-menu-item>
          <el-menu-item index="/judge/notices" class="menu-item">
            <el-icon><Bell /></el-icon>
            <span>通知公告</span>
          </el-menu-item>
        </template>

        <!-- 个人信息（所有角色都可以访问） -->
        <el-menu-item index="/profile" class="menu-item">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container class="main-content">
      <el-header class="main-header">
        <div class="header-content">
          <span class="title">大学生竞赛活动管理平台</span>
          <div class="user-info">
            <span class="username">{{ userStore.userInfo.realName || userStore.userInfo.username }}</span>
            <el-button link type="primary" @click="handleLogout" class="logout-btn">退出</el-button>
          </div>
        </div>
      </el-header>
      <el-main class="main-body">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

// 页面加载时刷新用户信息
onMounted(async () => {
  if (userStore.token) {
    try {
      // 强制刷新用户信息，确保显示正确
      const userInfo = await userStore.getUserInfo()
      // 调试信息
      console.log('MainLayout加载时的用户信息:', userStore.userInfo)
      console.log('realName:', userStore.userInfo.realName)
      console.log('username:', userStore.userInfo.username)
      console.log('role:', userStore.userInfo.role)
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果获取失败，可能是token过期，清除登录状态
      if (error.response?.status === 401) {
        userStore.logout()
        router.push('/login')
      }
    }
  } else {
    // 如果没有token，清除可能存在的旧数据
    userStore.logout()
  }
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
  ElMessage.success('退出成功')
}
</script>

<style scoped>
.main-container {
  height: 100vh;
  background-color: #f5f7fa;
}

.sidebar {
  background-color: #ffffff;
  border-right: 1px solid #e4e7ed;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e4e7ed;
  background: #f8f9fa;
}

.logo-text {
  color: #303133;
  font-size: 20px;
  font-weight: bold;
  letter-spacing: 1px;
}

.sidebar-menu {
  border-right: none;
  padding-top: 10px;
}

.menu-item {
  margin: 4px 12px;
  border-radius: 6px;
  height: 44px;
  line-height: 44px;
  transition: all 0.3s ease;
  position: relative;
}

.menu-item:hover {
  background-color: #ecf5ff !important;
  color: #409EFF !important;
}

.menu-item.is-active {
  background-color: #409EFF !important;
  color: #ffffff !important;
  font-weight: 500;
}

.menu-item.is-active .el-icon {
  color: #ffffff !important;
}

.menu-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background-color: #ffffff;
  border-radius: 0 2px 2px 0;
}

.menu-item .el-icon {
  margin-right: 8px;
  font-size: 18px;
}

.sub-menu {
  margin: 4px 12px;
}

.sub-menu :deep(.el-sub-menu__title) {
  border-radius: 6px;
  height: 44px;
  line-height: 44px;
  margin-bottom: 4px;
  transition: all 0.3s ease;
}

.sub-menu :deep(.el-sub-menu__title):hover {
  background-color: #ecf5ff !important;
  color: #409EFF !important;
}

.sub-menu-item {
  margin: 4px 0;
  padding-left: 48px !important;
  border-radius: 6px;
  height: 40px;
  line-height: 40px;
  transition: all 0.3s ease;
}

.sub-menu-item:hover {
  background-color: #ecf5ff !important;
  color: #409EFF !important;
}

.sub-menu-item.is-active {
  background-color: #409EFF !important;
  color: #ffffff !important;
  font-weight: 500;
}

.main-content {
  display: flex;
  flex-direction: column;
}

.main-header {
  background-color: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  height: 60px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  z-index: 100;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.username {
  color: #606266;
  font-size: 14px;
}

.logout-btn {
  padding: 0;
  font-size: 14px;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  color: #409EFF;
  transform: translateY(-1px);
}

.main-body {
  background-color: #f5f7fa;
  padding: 24px;
  overflow-y: auto;
}

/* 全局样式覆盖 */
:deep(.el-menu-item) {
  transition: all 0.3s ease;
}

:deep(.el-sub-menu__title) {
  transition: all 0.3s ease;
}

/* 卡片样式增强 */
:deep(.el-card) {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

:deep(.el-card):hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* 按钮悬浮效果 */
:deep(.el-button) {
  transition: all 0.3s ease;
}

:deep(.el-button--primary):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

:deep(.el-button--danger):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4);
}

/* 表格悬浮效果 */
:deep(.el-table__row) {
  transition: all 0.3s ease;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
  transform: scale(1.01);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>

