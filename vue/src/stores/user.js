import { defineStore } from 'pinia'
import { login, getUserInfo } from '../api/user'

export const useUserStore = defineStore('user', {
  state: () => {
    // 初始化时检查localStorage中的数据
    const storedUserInfo = localStorage.getItem('userInfo')
    let userInfo = {}
    if (storedUserInfo) {
      try {
        userInfo = JSON.parse(storedUserInfo)
        // 如果存储的是旧数据（比如"测试学生"），清除它
        if (userInfo.username === 'student001' || userInfo.realName === '测试学生' || userInfo.realName === '张三') {
          console.warn('检测到旧的用户数据，已清除')
          localStorage.removeItem('userInfo')
          localStorage.removeItem('token')
          userInfo = {}
        }
      } catch (e) {
        console.error('解析用户信息失败:', e)
        userInfo = {}
      }
    }
    return {
      token: localStorage.getItem('token') || '',
      userInfo: userInfo
    }
  },
  getters: {
    isLogin: (state) => !!state.token,
    role: (state) => state.userInfo.role || ''
  },
  actions: {
    async login(username, password) {
      // 先清除旧的用户信息
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      
      const res = await login({ username, password })
      this.token = res.data.token
      // 先保存登录返回的用户信息
      this.userInfo = res.data.user || {}
      localStorage.setItem('token', this.token)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      
      // 登录后立即获取最新的用户信息（确保数据完整）
      try {
        const userInfoRes = await this.getUserInfo()
        // 确保使用最新的用户信息
        if (userInfoRes && userInfoRes.data) {
          this.userInfo = userInfoRes.data
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        // 如果获取失败，至少使用登录时返回的用户信息
      }
      
      // 调试信息
      console.log('登录后的用户信息:', this.userInfo)
      console.log('realName:', this.userInfo.realName)
      console.log('username:', this.userInfo.username)
      
      return res
    },
    async getUserInfo() {
      const res = await getUserInfo()
      if (res && res.data) {
        this.userInfo = res.data
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        // 调试信息
        console.log('获取到的用户信息:', this.userInfo)
        console.log('realName:', this.userInfo.realName)
        console.log('username:', this.userInfo.username)
      }
      return res
    },
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})

