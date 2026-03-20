<template>
  <div class="register-container">
    <!-- 动态背景 -->
    <div class="animated-background">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
      </div>
    </div>
    
    <div class="register-wrapper">
      <el-card class="register-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h2 class="register-title">学生注册</h2>
            <p class="register-subtitle">请填写您的注册信息</p>
          </div>
        </template>
        
        <el-form 
          :model="form" 
          :rules="rules" 
          ref="formRef" 
          label-width="100px"
          @submit.prevent="handleSubmit"
          class="register-form"
        >
          <el-form-item label="注册角色" prop="role">
            <el-select 
              v-model="form.role" 
              placeholder="请选择注册角色" 
              style="width: 100%"
              @change="onRoleChange"
              size="large"
            >
              <el-option label="学生" value="student" />
              <el-option label="老师" value="teacher" />
              <el-option label="评分员" value="judge" />
            </el-select>
            <div class="role-tip">提示：仅支持学生注册，如需注册老师或评分员，请联系管理员</div>
          </el-form-item>
          
          <el-form-item label="用户名" prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名（4-20个字符）"
              clearable
              size="large"
            />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码（至少6个字符）" 
              show-password
              clearable
              size="large"
            />
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="请再次输入密码" 
              show-password
              clearable
              size="large"
            />
          </el-form-item>
          
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" clearable size="large" />
          </el-form-item>
          
          <el-form-item label="学号" prop="studentNo">
            <el-input v-model="form.studentNo" placeholder="请输入学号" clearable size="large" />
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" clearable size="large" />
          </el-form-item>
          
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" clearable size="large" />
          </el-form-item>
          
          <el-form-item label="学院" prop="college">
            <el-select v-model="form.college" placeholder="请选择学院" style="width: 100%" size="large">
              <el-option 
                v-for="college in collegeList" 
                :key="college" 
                :label="college" 
                :value="college" 
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="专业" prop="major">
            <el-input v-model="form.major" placeholder="请输入专业" clearable size="large" />
          </el-form-item>
          
          <el-form-item label="班级" prop="className">
            <el-input v-model="form.className" placeholder="请输入班级" clearable size="large" />
          </el-form-item>
          
          <el-form-item label="年级" prop="grade">
            <el-input v-model="form.grade" placeholder="请输入年级（如：2021）" clearable size="large" />
          </el-form-item>
          
          <el-form-item label="身份证号" prop="idCard">
            <el-input 
              v-model="form.idCard" 
              placeholder="请输入身份证号" 
              maxlength="18"
              clearable
              size="large"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              @click.stop.prevent="handleSubmit"
              @mousedown.stop="handleSubmit"
              :loading="loading" 
              :disabled="loading"
              size="large"
              class="register-button"
            >
              <span v-if="!loading">立即注册</span>
              <span v-else>注册中...</span>
            </el-button>
          </el-form-item>
          
          <el-form-item style="margin-bottom: 0;">
            <div class="login-link-wrapper">
              <span class="login-link-text">已有账号？</span>
              <el-button link type="primary" @click="goToLogin" class="login-link-btn">
                立即登录
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { registerUser } from '../api/user'
import { getCollegeList } from '../utils/college'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const collegeList = ref([])

// 表单数据
const form = reactive({
  role: 'student',
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  studentNo: '',
  email: '',
  phone: '',
  college: '',
  major: '',
  className: '',
  grade: '',
  idCard: ''
})

// 提交处理函数 - 先定义，确保可以被引用
const handleSubmit = async (event) => {
  if (event) {
    event.preventDefault()
    event.stopPropagation()
  }
  
  console.log('=== handleSubmit 被调用 ===')
  console.log('formRef:', formRef.value)
  console.log('loading:', loading.value)
  console.log('form数据:', form)
  
  // 立即显示提示
  try {
    ElMessage.info('正在处理注册请求...')
  } catch (e) {
    console.error('显示消息失败:', e)
  }
  
  // 检查角色
  if (form.role !== 'student') {
    ElMessageBox.alert(
      '如需注册老师或评分员账号，请联系管理员进行注册。',
      '提示',
      {
        confirmButtonText: '确定',
        type: 'warning'
      }
    )
    return
  }
  
  // 检查表单引用
  if (!formRef.value) {
    console.error('formRef 为空')
    ElMessage.error('表单未初始化，请刷新页面重试')
    return
  }
  
  // 验证表单
  try {
    console.log('开始验证表单...')
    await formRef.value.validate()
    console.log('表单验证通过')
  } catch (error) {
    console.log('表单验证失败:', error)
    ElMessage.warning('请检查表单填写是否正确')
    return
  }
  
  // 提交注册
  if (loading.value) {
    console.log('正在处理中，忽略重复请求')
    return
  }
  
  loading.value = true
  console.log('开始提交注册数据...')
  
  try {
    const { confirmPassword, ...registerData } = form
    registerData.role = 'student'
    
    console.log('注册数据:', JSON.stringify(registerData, null, 2))
    
    const response = await registerUser(registerData)
    console.log('注册响应:', response)
    
    ElMessage.success('注册成功！即将跳转到登录页面...')
    
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error) {
    console.error('注册失败:', error)
    console.error('错误详情:', error.response)
    const errorMsg = error.response?.data?.message || error.message || '注册失败，请重试'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
    console.log('注册流程结束')
  }
}

// 立即绑定到全局，不等待 onMounted
window.testRegister = handleSubmit
window.handleRegisterSubmit = handleSubmit
console.log('Register组件脚本执行，函数已绑定到 window')

// 加载学院列表
const loadCollegeList = async () => {
  try {
    collegeList.value = await getCollegeList()
  } catch (error) {
    console.error('加载学院列表失败:', error)
    // 使用默认学院列表
    collegeList.value = ['计算机学院', '软件学院', '信息学院', '管理学院', '经济学院']
  }
}

// 组件挂载时测试
onMounted(() => {
  console.log('Register组件已挂载')
  console.log('formRef:', formRef.value)
  console.log('handleSubmit函数:', handleSubmit)
  
  // 加载学院列表
  loadCollegeList()
  
  // 再次确保绑定
  window.testRegister = handleSubmit
  window.handleRegisterSubmit = handleSubmit
  
  // 直接测试按钮点击
  setTimeout(() => {
    const btn = document.querySelector('#register-submit-btn') || 
                document.querySelector('.register-card .el-button--primary')
    if (btn) {
      console.log('找到注册按钮:', btn)
      // 添加原生事件监听作为备用
      btn.addEventListener('click', (e) => {
        console.log('按钮原生点击事件触发')
        e.preventDefault()
        e.stopPropagation()
        handleSubmit(e)
      }, true) // 使用捕获阶段
    } else {
      console.error('未找到注册按钮')
    }
  }, 500)
})

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}

// 角色变化处理
const onRoleChange = (value) => {
  if (value === 'teacher' || value === 'judge') {
    form.role = 'student'
    ElMessageBox.alert(
      '如需注册老师或评分员账号，请联系管理员进行注册。',
      '提示',
      {
        confirmButtonText: '确定',
        type: 'warning'
      }
    )
  }
}

// 验证函数
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback()
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback()
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱地址'))
  } else {
    callback()
  }
}

const validateIdCard = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入身份证号'))
  } else if (!/^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/.test(value)) {
    callback(new Error('请输入正确的身份证号'))
  } else {
    callback()
  }
}

// 验证规则
const rules = {
  role: [
    { required: true, message: '请选择注册角色', trigger: 'change' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度为4-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  college: [
    { required: true, message: '请输入学院', trigger: 'blur' }
  ],
  major: [
    { required: true, message: '请输入专业', trigger: 'blur' }
  ],
  className: [
    { required: true, message: '请输入班级', trigger: 'blur' }
  ],
  grade: [
    { required: true, message: '请输入年级', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { validator: validateIdCard, trigger: 'blur' }
  ]
}
</script>

<style scoped>
.register-container {
  position: relative;
  width: 100%;
  min-height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 50%, #f0f4f8 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
}

/* 动态背景 */
.animated-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
  animation: floatOrb 20s infinite ease-in-out;
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  top: -200px;
  left: -200px;
  animation-delay: 0s;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
  bottom: -150px;
  right: -150px;
  animation-delay: 7s;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #E6A23C 0%, #ebb563 100%);
  top: 50%;
  right: 10%;
  animation-delay: 14s;
}

@keyframes floatOrb {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(50px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-30px, 30px) scale(0.9);
  }
}

.floating-shapes {
  position: relative;
  width: 100%;
  height: 100%;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(64, 158, 255, 0.08);
  animation: floatShape 25s infinite ease-in-out;
}

.shape-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 5%;
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  bottom: 20%;
  left: 15%;
  animation-delay: 5s;
}

.shape-3 {
  width: 180px;
  height: 180px;
  top: 60%;
  right: 20%;
  animation-delay: 10s;
}

.shape-4 {
  width: 120px;
  height: 120px;
  top: 30%;
  right: 10%;
  animation-delay: 15s;
}

@keyframes floatShape {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
    opacity: 0.08;
  }
  25% {
    transform: translate(30px, -40px) rotate(90deg);
    opacity: 0.12;
  }
  50% {
    transform: translate(-20px, 30px) rotate(180deg);
    opacity: 0.1;
  }
  75% {
    transform: translate(-30px, -20px) rotate(270deg);
    opacity: 0.15;
  }
}

.register-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 700px;
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.register-card {
  width: 100%;
  border-radius: 24px;
  border: none;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.register-card :deep(.el-card__header) {
  padding: 40px 40px 20px;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border-bottom: none;
}

.register-card :deep(.el-card__body) {
  padding: 40px;
  background: white;
}

.card-header {
  text-align: center;
}

.register-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 10px 0;
  color: white;
  letter-spacing: 1px;
}

.register-subtitle {
  font-size: 15px;
  margin: 0;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 400;
}

.register-form {
  margin-top: 10px;
}

.role-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  line-height: 1.5;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-form-item__label) {
  color: #606266;
  font-weight: 500;
  font-size: 14px;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1.5px #e4e7ed inset;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1.5px #409EFF inset;
  background: white;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #409EFF inset;
  background: white;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
}

.register-button {
  width: 100%;
  height: 52px;
  font-size: 17px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.35);
  margin-top: 10px;
}

.register-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(64, 158, 255, 0.45);
}

.register-button:active {
  transform: translateY(-1px);
}

.login-link-wrapper {
  width: 100%;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 10px;
}

.login-link-text {
  font-size: 14px;
  color: #909399;
}

.login-link-btn {
  font-size: 15px;
  font-weight: 600;
  padding: 0;
  color: #409EFF;
  transition: all 0.3s ease;
  position: relative;
}

.login-link-btn::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  transition: width 0.3s ease;
}

.login-link-btn:hover {
  color: #66b1ff;
}

.login-link-btn:hover::after {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-wrapper {
    max-width: 100%;
  }
  
  .register-card :deep(.el-card__body) {
    padding: 30px 20px;
  }
  
  .register-card :deep(.el-card__header) {
    padding: 30px 20px 15px;
  }
  
  :deep(.el-form-item__label) {
    width: 90px !important;
  }
}
</style>
