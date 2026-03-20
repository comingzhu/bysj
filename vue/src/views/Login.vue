<template>
  <div class="login-container">
    <!-- 简化背景 -->
    <div class="simple-background">
      <div class="background-gradient"></div>
    </div>
    
    <div class="login-wrapper">
      <div class="login-left">
        <div class="logo-section">
          <div class="logo-icon">
            <el-icon size="60"><Trophy /></el-icon>
          </div>
          <h1 class="platform-title">大学生竞赛活动管理平台</h1>
          <p class="platform-subtitle">Competition Management Platform</p>
        </div>

      </div>
      
      <div class="login-right">
        <el-card class="login-card" shadow="never">
          <template #header>
            <div class="card-header">
              <h2 class="login-title">欢迎登录</h2>
              <p class="login-subtitle">请输入您的账号信息</p>
            </div>
          </template>
          
          <el-form :model="form" :rules="rules" ref="formRef" label-width="0" class="login-form">
            <el-form-item prop="username">
              <div class="input-wrapper">
                <el-icon class="input-icon"><User /></el-icon>
                <el-input 
                  v-model="form.username" 
                  placeholder="请输入用户名" 
                  size="large"
                  class="custom-input"
                />
              </div>
            </el-form-item>
            <el-form-item prop="password">
              <div class="input-wrapper">
                <el-icon class="input-icon"><Lock /></el-icon>
                <el-input 
                  v-model="form.password" 
                  type="password" 
                  placeholder="请输入密码" 
                  size="large"
                  show-password
                  class="custom-input"
                  @keyup.enter="handleLogin"
                />
              </div>
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                @click="handleLogin" 
                :loading="loading" 
                size="large"
                class="login-button"
              >
                <span v-if="!loading">登录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
            <el-form-item>
              <div class="register-link-wrapper">
                <span class="register-link-text">还没有账号？</span>
                <el-button link type="primary" @click="showRegisterDialog = true" class="register-link-btn">
                  立即注册
                </el-button>
              </div>
            </el-form-item>
          </el-form>
          

        </el-card>
      </div>
    </div>
    
    <!-- 注册弹窗 -->
    <RegisterDialog 
      v-model="showRegisterDialog" 
      @success="handleRegisterSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { Trophy, User, Lock } from '@element-plus/icons-vue'
import RegisterDialog from '../components/RegisterDialog.vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const showRegisterDialog = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 所有角色登录后都跳转到仪表盘
const getHomePathByRole = (role) => {
  return '/home'
}



const handleLogin = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 登录前强制清除所有缓存
        localStorage.clear()
        sessionStorage.clear()
        
        await userStore.login(form.username, form.password)
        
        // 验证用户信息是否正确
        console.log('登录成功后的用户信息:', userStore.userInfo)
        console.log('用户名:', userStore.userInfo.username)
        console.log('真实姓名:', userStore.userInfo.realName)
        console.log('角色:', userStore.userInfo.role)
        
        if (!userStore.userInfo || !userStore.userInfo.id) {
          ElMessage.error('获取用户信息失败，请重新登录')
          return
        }
        
        ElMessage.success('登录成功')
        // 根据角色跳转到对应首页
        const homePath = getHomePathByRole(userStore.role)
        router.push(homePath)
      } catch (error) {
        ElMessage.error(error.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 注册成功回调
const handleRegisterSuccess = () => {
  showRegisterDialog.value = false
  ElMessage.success('注册成功，请使用新账号登录')
}
</script>

<style scoped>
.login-container {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

/* 简化背景 */
.simple-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  background-image: url('./1.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.background-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.5) 0%, rgba(0, 0, 0, 0.3) 100%);
}


.login-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  width: 100%;
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px;
  box-sizing: border-box;
}

.login-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding-right: 80px;
  color: #2c3e50;
}

.logo-section {
  margin-bottom: 60px;
  animation: fadeInUp 0.8s ease-out;
}

.logo-icon {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.25);
  color: white;
}

.platform-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 15px 0;
  color: #ffffff;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.platform-subtitle {
  font-size: 18px;
  color: #f0f0f0;
  margin: 0;
  font-weight: 400;
  letter-spacing: 1px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}



.login-right {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 480px;
}

.login-card {
  width: 100%;
  border-radius: 24px;
  border: none;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.login-card :deep(.el-card__header) {
  padding: 40px 40px 20px;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border-bottom: none;
}

.login-card :deep(.el-card__body) {
  padding: 40px;
  background: white;
}

.card-header {
  text-align: center;
}

.login-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 10px 0;
  color: white;
  letter-spacing: 1px;
}

.login-subtitle {
  font-size: 15px;
  margin: 0;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 400;
}

.login-form {
  margin-top: 10px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 18px;
  z-index: 1;
  color: #909399;
  font-size: 20px;
}

.custom-input :deep(.el-input__wrapper) {
  padding-left: 50px;
  border-radius: 12px;
  box-shadow: 0 0 0 1.5px #e4e7ed inset;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1.5px #409EFF inset;
  background: white;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #409EFF inset;
  background: white;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.3);
  margin-top: 10px;
}

.login-button:hover {
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

.register-link-wrapper {
  width: 100%;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 10px;
}

.register-link-text {
  font-size: 14px;
  color: #606266;
}

.register-link-btn {
  font-size: 14px;
  font-weight: 600;
  padding: 0;
  color: #409EFF;
  transition: all 0.3s ease;
}

.register-link-btn:hover {
  color: #66b1ff;
}



/* 响应式设计 */
@media (max-width: 1024px) {
  .login-wrapper {
    flex-direction: column;
    padding: 20px;
  }
  
  .login-left {
    padding-right: 0;
    margin-bottom: 40px;
    text-align: center;
  }
  
  .login-right {
    width: 100%;
    max-width: 480px;
    margin: 0 auto;
  }
  
  .platform-title {
    font-size: 32px;
  }
  
  .platform-subtitle {
    font-size: 16px;
  }
}
</style>
