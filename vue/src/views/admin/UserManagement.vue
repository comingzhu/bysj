<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleCreate">创建用户</el-button>
        </div>
      </template>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="学生管理" name="student">
          <el-table :data="studentData" v-loading="loading">
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="realName" label="姓名" />
            <el-table-column prop="studentNo" label="学号" />
            <el-table-column prop="college" label="学院" />
            <el-table-column prop="major" label="专业" />
            <el-table-column prop="className" label="班级" />
            <el-table-column prop="grade" label="年级" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button
                  :type="row.status === 1 ? 'danger' : 'success'"
                  size="small"
                  @click="handleToggleStatus(row)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
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
        </el-tab-pane>
        
        <el-tab-pane label="老师管理" name="teacher">
          <el-table :data="teacherData" v-loading="loading">
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="realName" label="姓名" />
            <el-table-column prop="teacherNo" label="工号" />
            <el-table-column prop="college" label="学院" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="phone" label="手机号" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button
                  :type="row.status === 1 ? 'danger' : 'success'"
                  size="small"
                  @click="handleToggleStatus(row)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
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
        </el-tab-pane>
        
        <el-tab-pane label="评分员管理" name="judge">
          <el-table :data="judgeData" v-loading="loading">
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="realName" label="姓名" />
            <el-table-column prop="teacherNo" label="工号" />
            <el-table-column prop="college" label="学院" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="phone" label="手机号" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button
                  :type="row.status === 1 ? 'danger' : 'success'"
                  size="small"
                  @click="handleToggleStatus(row)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
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
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 创建/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username" required>
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="密码" prop="password" :required="!form.id">
          <el-input v-model="form.password" type="password" :placeholder="form.id ? '不修改请留空' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName" required>
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="role" required>
          <el-select v-model="form.role" :disabled="!!form.id">
            <el-option label="学生" value="student" />
            <el-option label="老师" value="teacher" />
            <el-option label="评分员" value="judge" />
          </el-select>
        </el-form-item>
        <el-form-item label="学号" v-if="form.role === 'student'" prop="studentNo" required>
          <el-input v-model="form.studentNo" />
        </el-form-item>
        <el-form-item label="工号" v-if="form.role === 'teacher' || form.role === 'judge'" prop="teacherNo" required>
          <el-input v-model="form.teacherNo" />
        </el-form-item>
        <el-form-item label="学院" prop="college" required>
          <el-select v-model="form.college" placeholder="请选择学院" style="width: 100%">
            <el-option 
              v-for="college in collegeList" 
              :key="college" 
              :label="college" 
              :value="college" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="专业" v-if="form.role === 'student'" prop="major" required>
          <el-input v-model="form.major" />
        </el-form-item>
        <el-form-item label="班级" v-if="form.role === 'student'" prop="className" required>
          <el-input v-model="form.className" />
        </el-form-item>
        <el-form-item label="年级" v-if="form.role === 'student'" prop="grade" required>
          <el-input v-model="form.grade" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email" required>
          <el-input v-model="form.email" type="email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone" required>
          <el-input v-model="form.phone" maxlength="11" placeholder="请输入11位手机号" />
        </el-form-item>
        <el-form-item label="身份证号" v-if="form.role === 'student'" prop="idCard" required>
          <el-input v-model="form.idCard" maxlength="18" placeholder="请输入18位身份证号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getUserList, updateUserStatus, createUser, updateUserByAdmin } from '../../api/user'
import { ElMessage } from 'element-plus'
import { getCollegeList } from '../../utils/college'

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { 
      validator: (rule, value, callback) => {
        // 编辑时密码可以为空，新增时必填
        if (!form.value.id && (!value || value.trim() === '')) {
          callback(new Error('请输入密码'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  studentNo: [
    { 
      validator: (rule, value, callback) => {
        if (form.value.role === 'student' && (!value || value.trim() === '')) {
          callback(new Error('请输入学号'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  teacherNo: [
    { 
      validator: (rule, value, callback) => {
        if ((form.value.role === 'teacher' || form.value.role === 'judge') && (!value || value.trim() === '')) {
          callback(new Error('请输入工号'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  college: [
    { required: true, message: '请输入学院', trigger: 'blur' }
  ],
  major: [
    { 
      validator: (rule, value, callback) => {
        if (form.value.role === 'student' && (!value || value.trim() === '')) {
          callback(new Error('请输入专业'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  className: [
    { 
      validator: (rule, value, callback) => {
        if (form.value.role === 'student' && (!value || value.trim() === '')) {
          callback(new Error('请输入班级'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  grade: [
    { 
      validator: (rule, value, callback) => {
        if (form.value.role === 'student' && (!value || value.trim() === '')) {
          callback(new Error('请输入年级'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value || value.trim() === '') {
          callback(new Error('请输入手机号'))
        } else if (!/^1[3-9]\d{9}$/.test(value)) {
          callback(new Error('请输入正确的手机号格式（11位数字，以1开头）'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  idCard: [
    { 
      validator: (rule, value, callback) => {
        if (form.value.role === 'student') {
          if (!value || value.trim() === '') {
            callback(new Error('请输入身份证号'))
          } else if (!/^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/.test(value)) {
            callback(new Error('请输入正确的身份证号格式（18位）'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

const formRef = ref(null)

const loading = ref(false)
const activeTab = ref('student')
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('创建用户')
const form = ref({})
const collegeList = ref([])

// 根据标签页过滤数据（现在后端已经按角色过滤，所以直接使用）
const studentData = computed(() => {
  return tableData.value
})

const teacherData = computed(() => {
  return tableData.value
})

const judgeData = computed(() => {
  return tableData.value
})

const page = ref(1)
const size = ref(10)
const total = ref(0)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({ 
      role: activeTab.value === 'student' ? 'student' : activeTab.value === 'teacher' ? 'teacher' : 'judge',
      page: page.value, 
      size: size.value 
    })
    // 处理返回的数据格式
    if (res.data) {
      // MyBatis Plus 的 Page 对象
      if (res.data.records !== undefined) {
        tableData.value = res.data.records || []
        total.value = res.data.total || 0
      } 
      // 如果是数组格式
      else if (Array.isArray(res.data)) {
        tableData.value = res.data
        total.value = res.data.length
      } 
      // 其他格式
      else {
        tableData.value = []
        total.value = 0
      }
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('加载失败')
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  // 切换标签页时重新加载数据
  page.value = 1
  loadData()
}

const handleToggleStatus = async (row) => {
  try {
    await updateUserStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('操作成功')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleCreate = () => {
  form.value = {
    role: activeTab.value,
    status: 1
  }
  dialogTitle.value = '创建用户'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
}

const handleSave = async () => {
  // 表单验证
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch (error) {
    ElMessage.warning('请检查表单填写是否正确')
    return
  }
  
  try {
    if (form.value.id) {
      // 编辑 - 使用管理员更新接口
      const updateData = { ...form.value }
      if (!updateData.password || updateData.password.trim() === '') {
        // 密码为空时不更新密码
        updateData.password = null
      }
      await updateUserByAdmin(updateData)
      ElMessage.success('更新成功')
    } else {
      // 创建
      await createUser(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const loadCollegeList = async () => {
  try {
    collegeList.value = await getCollegeList()
  } catch (error) {
    console.error('加载学院列表失败:', error)
    // 使用默认学院列表
    collegeList.value = ['计算机学院', '软件学院', '信息学院', '管理学院', '经济学院']
  }
}

onMounted(() => {
  loadData()
  loadCollegeList()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
