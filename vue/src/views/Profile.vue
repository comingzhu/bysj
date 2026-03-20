<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-icon style="color: #409EFF; font-size: 20px;"><User /></el-icon>
            <h2>个人信息</h2>
          </div>
        </div>
      </template>

      <div class="profile-content">
        <!-- 个人信息展示 -->
        <div class="info-section" v-if="!isEditing">
          <el-descriptions :column="2" border class="info-descriptions">
            <el-descriptions-item label="用户名">
              <span class="info-value">{{ userInfo.username || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="真实姓名">
              <span class="info-value">{{ userInfo.realName || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag :type="getRoleType(userInfo.role)">
                {{ getRoleText(userInfo.role) }}
              </el-tag>
            </el-descriptions-item>
            <!-- 学生显示学号，老师/评分员显示工号，管理员不显示 -->
            <el-descriptions-item v-if="userInfo.role !== 'admin'" :label="userInfo.role === 'student' ? '学号' : '工号'">
              <span class="info-value">{{ userInfo.role === 'student' ? (userInfo.studentNo || '-') : (userInfo.teacherNo || '-') }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              <span class="info-value">{{ userInfo.email || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="手机号">
              <span class="info-value">{{ userInfo.phone || '-' }}</span>
            </el-descriptions-item>
            <!-- 管理员不显示学院 -->
            <el-descriptions-item v-if="userInfo.role !== 'admin'" label="学院">
              <span class="info-value">{{ userInfo.college || '-' }}</span>
            </el-descriptions-item>
            <!-- 只有学生显示专业、班级、年级、身份证号 -->
            <el-descriptions-item v-if="userInfo.role === 'student'" label="专业">
              <span class="info-value">{{ userInfo.major || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item v-if="userInfo.role === 'student'" label="班级">
              <span class="info-value">{{ userInfo.className || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item v-if="userInfo.role === 'student'" label="年级">
              <span class="info-value">{{ userInfo.grade || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item v-if="userInfo.role === 'student'" label="身份证号" :span="2">
              <span class="info-value">{{ userInfo.idCard || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="注册时间" :span="2">
              <span class="info-value">{{ formatDateTime(userInfo.createTime) }}</span>
            </el-descriptions-item>
          </el-descriptions>

          <div class="action-buttons">
            <el-button type="primary" size="large" @click="startEdit">
              <el-icon><Edit /></el-icon>
              编辑信息
            </el-button>
            <el-button type="warning" size="large" @click="showPasswordDialog">
              <el-icon><Lock /></el-icon>
              修改密码
            </el-button>
          </div>
        </div>

        <!-- 编辑表单 -->
        <div class="edit-section" v-else>
          <el-form :model="editForm" label-width="120px" :rules="rules" ref="editFormRef">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <!-- 管理员不显示学号/工号 -->
            <el-form-item v-if="userInfo.role !== 'admin'" :label="userInfo.role === 'student' ? '学号' : '工号'">
              <el-input 
                v-if="userInfo.role === 'student'"
                v-model="editForm.studentNo" 
                placeholder="请输入学号" 
              />
              <el-input 
                v-else
                v-model="editForm.teacherNo" 
                placeholder="请输入工号" 
              />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="editForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="editForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <!-- 管理员不显示学院 -->
            <el-form-item v-if="userInfo.role !== 'admin'" label="学院">
              <el-input v-model="editForm.college" placeholder="请输入学院" />
            </el-form-item>
            <!-- 只有学生显示专业、班级、年级、身份证号 -->
            <el-form-item v-if="userInfo.role === 'student'" label="专业">
              <el-input v-model="editForm.major" placeholder="请输入专业" />
            </el-form-item>
            <el-form-item v-if="userInfo.role === 'student'" label="班级">
              <el-input v-model="editForm.className" placeholder="请输入班级" />
            </el-form-item>
            <el-form-item v-if="userInfo.role === 'student'" label="年级">
              <el-input v-model="editForm.grade" placeholder="请输入年级" />
            </el-form-item>
            <el-form-item v-if="userInfo.role === 'student'" label="身份证号" prop="idCard">
              <el-input v-model="editForm.idCard" placeholder="请输入身份证号" maxlength="18" />
            </el-form-item>
          </el-form>

          <div class="action-buttons">
            <el-button size="large" @click="cancelEdit">取消</el-button>
            <el-button type="primary" size="large" @click="saveEdit" :loading="saving">
              保存
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="500px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码（至少6位）"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePassword" :loading="changingPassword">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { getUserInfo, updateUserInfo, changePassword } from '../api/user'
import { ElMessage } from 'element-plus'
import { Edit, Lock, User } from '@element-plus/icons-vue'
import { formatDateTime } from '../utils/dateFormat'

const userStore = useUserStore()
const userInfo = ref({})
const isEditing = ref(false)
const saving = ref(false)
const editForm = ref({})
const editFormRef = ref(null)
const passwordDialogVisible = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordFormRef = ref(null)
const changingPassword = ref(false)

const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  idCard: [
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.data) {
      userInfo.value = res.data
      userStore.userInfo = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    }
  } catch (error) {
    ElMessage.error('加载用户信息失败')
  }
}

const getRoleText = (role) => {
  const map = {
    admin: '管理员',
    teacher: '教师',
    student: '学生',
    judge: '评分员'
  }
  return map[role] || role
}

const getRoleType = (role) => {
  const map = {
    admin: 'danger',
    teacher: 'warning',
    student: 'success',
    judge: 'info'
  }
  return map[role] || 'info'
}

const startEdit = () => {
  editForm.value = {
    realName: userInfo.value.realName || '',
    email: userInfo.value.email || '',
    phone: userInfo.value.phone || ''
  }
  
  // 管理员不显示学号/工号、学院、专业、班级、年级、身份证号
  if (userInfo.value.role !== 'admin') {
    if (userInfo.value.role === 'student') {
      editForm.value.studentNo = userInfo.value.studentNo || ''
      editForm.value.major = userInfo.value.major || ''
      editForm.value.className = userInfo.value.className || ''
      editForm.value.grade = userInfo.value.grade || ''
      editForm.value.idCard = userInfo.value.idCard || ''
    } else {
      editForm.value.teacherNo = userInfo.value.teacherNo || ''
    }
    editForm.value.college = userInfo.value.college || ''
  }
  
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  editForm.value = {}
  if (editFormRef.value) {
    editFormRef.value.resetFields()
  }
}

const saveEdit = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    saving.value = true
    
    const submitData = {
      realName: editForm.value.realName,
      email: editForm.value.email,
      phone: editForm.value.phone
    }
    
    // 管理员不提交学号/工号、学院、专业、班级、年级、身份证号
    if (userInfo.value.role !== 'admin') {
      if (userInfo.value.role === 'student') {
        submitData.studentNo = editForm.value.studentNo
        submitData.major = editForm.value.major
        submitData.className = editForm.value.className
        submitData.grade = editForm.value.grade
        submitData.idCard = editForm.value.idCard
      } else {
        submitData.teacherNo = editForm.value.teacherNo
      }
      submitData.college = editForm.value.college
    }
    
    await updateUserInfo(submitData)
    ElMessage.success('更新成功')
    await loadUserInfo()
    isEditing.value = false
  } catch (error) {
    if (error !== false) {
      console.error('保存失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '保存失败')
    }
  } finally {
    saving.value = false
  }
}

const showPasswordDialog = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordDialogVisible.value = true
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields()
  }
}

const savePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true
    
    await changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    if (error !== false) {
      console.error('修改密码失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '修改密码失败')
    }
  } finally {
    changingPassword.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  min-height: calc(100vh - 60px);
  background-color: #f5f7fa;
}

.profile-card {
  width: 100%;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.profile-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.card-header h2 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-content {
  padding: 20px;
}

.info-section {
  animation: fadeIn 0.3s ease-in;
}

.edit-section {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.info-descriptions {
  margin-bottom: 24px;
}

.info-descriptions :deep(.el-descriptions__label) {
  font-weight: 600;
  color: #606266;
  background-color: #f5f7fa;
  width: 140px;
  font-size: 14px;
}

.info-descriptions :deep(.el-descriptions__content) {
  color: #303133;
  font-size: 14px;
}

.info-value {
  font-size: 14px;
  color: #303133;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.action-buttons .el-button {
  padding: 10px 24px;
  font-size: 14px;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.action-buttons .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.action-buttons .el-button--primary {
  background-color: #409EFF;
  border-color: #409EFF;
}

.action-buttons .el-button--primary:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.action-buttons .el-button--warning {
  background-color: #E6A23C;
  border-color: #E6A23C;
}

.action-buttons .el-button--warning:hover {
  background-color: #ebb563;
  border-color: #ebb563;
}

.edit-section .el-form {
  max-width: 700px;
  margin: 0 auto;
}

.edit-section .el-form-item {
  margin-bottom: 20px;
}

.edit-section .el-input {
  border-radius: 6px;
}

.edit-section .el-input__inner {
  padding: 10px 15px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.edit-section .el-input__inner:focus {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafafa;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  padding: 20px 20px 10px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-input__inner) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

:deep(.el-input__inner:focus) {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

:deep(.el-descriptions) {
  background-color: #ffffff;
}

:deep(.el-descriptions__table) {
  border-radius: 8px;
  overflow: hidden;
}
</style>

