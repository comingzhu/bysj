<template>
  <el-dialog
    v-model="visible"
    title="学生注册"
    width="700px"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    class="register-dialog"
    @close="handleClose"
  >
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
        <el-select 
          v-model="form.college" 
          placeholder="请选择学院" 
          style="width: 100%"
          size="large"
          clearable
          filterable
        >
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
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" size="large">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSubmit"
          :loading="loading" 
          size="large"
          class="register-button"
        >
          <span v-if="!loading">立即注册</span>
          <span v-else>注册中...</span>
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { registerUser } from '../api/user'
import { getCollegeList } from '../utils/college'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const collegeList = ref([])

const visible = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    resetForm()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
  if (val) {
    loadCollegeList()
  }
})

// 加载学院列表
const loadCollegeList = async () => {
  try {
    collegeList.value = await getCollegeList()
  } catch (error) {
    console.error('获取学院列表失败:', error)
    // 使用默认学院列表
    collegeList.value = ['计算机学院', '软件学院', '信息学院', '管理学院', '经济学院']
  }
}

// 组件挂载时加载学院列表
onMounted(() => {
  loadCollegeList()
})

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

// 重置表单
const resetForm = () => {
  Object.assign(form, {
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
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 关闭弹窗
const handleClose = () => {
  visible.value = false
  resetForm()
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

// 提交处理
const handleSubmit = async () => {
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
    ElMessage.error('表单未初始化，请刷新页面重试')
    return
  }
  
  // 验证表单
  try {
    await formRef.value.validate()
  } catch (error) {
    ElMessage.warning('请检查表单填写是否正确')
    return
  }
  
  // 提交注册
  if (loading.value) {
    return
  }
  
  loading.value = true
  
  try {
    const { confirmPassword, ...registerData } = form
    registerData.role = 'student'
    
    await registerUser(registerData)
    
    ElMessage.success('注册成功！请登录')
    
    // 关闭弹窗并触发成功事件
    handleClose()
    emit('success')
    
    // 可以在这里自动填充用户名到登录表单
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '注册失败，请重试'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

.register-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  padding: 30px 30px 20px;
  margin: 0;
}

.register-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 24px;
  font-weight: 700;
}

.register-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 20px;
}

.register-dialog :deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: rgba(255, 255, 255, 0.8);
}

.register-dialog :deep(.el-dialog__body) {
  padding: 30px;
  max-height: 70vh;
  overflow-y: auto;
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 10px;
}

.register-button {
  min-width: 120px;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.35);
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.45);
}

.register-button:active {
  transform: translateY(0);
}

/* 滚动条样式 */
.register-dialog :deep(.el-dialog__body)::-webkit-scrollbar {
  width: 6px;
}

.register-dialog :deep(.el-dialog__body)::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.register-dialog :deep(.el-dialog__body)::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.register-dialog :deep(.el-dialog__body)::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>

