import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页' }
      },
      // 学生端
      {
        path: 'competition/list',
        name: 'CompetitionList',
        component: () => import('../views/student/CompetitionList.vue'),
        meta: { title: '竞赛列表', role: ['student'] }
      },
      {
        path: 'competition/detail/:id',
        name: 'CompetitionDetail',
        component: () => import('../views/student/CompetitionDetail.vue'),
        meta: { title: '竞赛详情', role: ['student'] }
      },
      {
        path: 'my/registrations',
        name: 'MyRegistrations',
        component: () => import('../views/student/MyRegistrations.vue'),
        meta: { title: '我的报名', role: ['student'] }
      },
      {
        path: 'my/awards',
        name: 'MyAwards',
        component: () => import('../views/student/MyAwards.vue'),
        meta: { title: '我的获奖', role: ['student'] }
      },
      {
        path: 'my/notices',
        name: 'MyNotices',
        component: () => import('../views/student/NoticeList.vue'),
        meta: { title: '通知公告', role: ['student'] }
      },
      // 管理员端
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('../views/admin/UserManagement.vue'),
        meta: { title: '用户管理', role: ['admin'] }
      },
      {
        path: 'admin/competitions',
        name: 'AdminCompetitions',
        component: () => import('../views/admin/CompetitionAudit.vue'),
        meta: { title: '赛事审核', role: ['admin'] }
      },
      {
        path: 'admin/school-competitions',
        name: 'SchoolCompetitionManagement',
        component: () => import('../views/admin/SchoolCompetitionManagement.vue'),
        meta: { title: '所有校赛', role: ['admin'] }
      },

      {
        path: 'admin/system/config',
        name: 'SystemConfig',
        component: () => import('../views/admin/SystemConfig.vue'),
        meta: { title: '系统参数设置', role: ['admin'] }
      },
      {
        path: 'admin/system/competition',
        name: 'SystemCompetition',
        component: () => import('../views/admin/SystemCompetition.vue'),
        meta: { title: '省赛信息发布', role: ['admin'] }
      },
      {
        path: 'admin/payment',
        name: 'PaymentManagement',
        component: () => import('../views/admin/PaymentManagement.vue'),
        meta: { title: '缴费管理', role: ['admin'] }
      },
      {
        path: 'admin/statistics',
        name: 'DataVisualization',
        component: () => import('../views/admin/DataVisualization.vue'),
        meta: { title: '数据可视化', role: ['admin'] }
      },
      {
        path: 'admin/notice',
        name: 'NoticeManagement',
        component: () => import('../views/admin/NoticeManagement.vue'),
        meta: { title: '通知公告管理', role: ['admin'] }
      },
      {
        path: 'admin/awards',
        name: 'AdminAwardManagement',
        component: () => import('../views/admin/AwardManagement.vue'),
        meta: { title: '获奖管理', role: ['admin'] }
      },
      {
        path: 'admin/works',
        name: 'AdminWorkManagement',
        component: () => import('../views/admin/WorkManagement.vue'),
        meta: { title: '作品管理', role: ['admin'] }
      },
      // 老师端
      {
        path: 'teacher/competitions',
        name: 'TeacherCompetitions',
        component: () => import('../views/teacher/CompetitionManagement.vue'),
        meta: { title: '竞赛管理', role: ['teacher'] }
      },
      {
        path: 'teacher/awards',
        name: 'TeacherAwards',
        component: () => import('../views/teacher/AwardManagement.vue'),
        meta: { title: '获奖管理', role: ['teacher'] }
      },
      {
        path: 'teacher/registrations',
        name: 'TeacherRegistrations',
        component: () => import('../views/teacher/RegistrationManagement.vue'),
        meta: { title: '报名管理', role: ['teacher'] }
      },
      {
        path: 'teacher/works',
        name: 'TeacherWorks',
        component: () => import('../views/teacher/WorkManagement.vue'),
        meta: { title: '作品管理', role: ['teacher'] }
      },
      {
        path: 'teacher/statistics',
        name: 'TeacherStatistics',
        component: () => import('../views/teacher/TeacherStatistics.vue'),
        meta: { title: '数据可视化', role: ['teacher'] }
      },
      {
        path: 'teacher/notices',
        name: 'TeacherNotices',
        component: () => import('../views/teacher/NoticeList.vue'),
        meta: { title: '通知公告', role: ['teacher'] }
      },
      // 评分员端
      {
        path: 'judge/tasks',
        name: 'JudgeTasks',
        component: () => import('../views/judge/ScoreTasks.vue'),
        meta: { title: '评分任务', role: ['judge'] }
      },
      {
        path: 'judge/statistics',
        name: 'JudgeStatistics',
        component: () => import('../views/judge/ScoreStatistics.vue'),
        meta: { title: '评分统计', role: ['judge'] }
      },
      {
        path: 'judge/notices',
        name: 'JudgeNotices',
        component: () => import('../views/judge/NoticeList.vue'),
        meta: { title: '通知公告', role: ['judge'] }
      },
      // 个人信息（所有角色都可以访问）
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人信息' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 所有角色登录后都跳转到仪表盘
const getHomePathByRole = (role) => {
  return '/home'
}

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  // 允许未登录访问的页面
  const publicPaths = ['/login', '/register']
  
  if (publicPaths.includes(to.path)) {
    if (userStore.isLogin) {
      // 已登录，跳转到对应角色的首页
      const homePath = getHomePathByRole(userStore.role)
      if (homePath === '/home') {
        next('/home')
      } else {
        next(homePath)
      }
    } else {
      next()
    }
  } else if (to.path === '/' || to.path === '/home') {
    // 访问根路径或home时，所有角色都可以访问仪表盘
    if (!userStore.isLogin) {
      next('/login')
    } else {
      // 所有已登录用户都可以访问 /home 仪表盘
      next()
    }
  } else {
    if (!userStore.isLogin) {
      next('/login')
    } else {
      const role = userStore.role
      if (to.meta.role && !to.meta.role.includes(role)) {
        ElMessage.error('无权限访问')
        // 无权限时跳转到对应角色的首页
        const homePath = getHomePathByRole(role)
        next(homePath)
      } else {
        next()
      }
    }
  }
})

export default router

