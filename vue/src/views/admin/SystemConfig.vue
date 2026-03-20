<template>
  <div class="system-config">
    <el-card>
      <template #header>
        <div class="header">
          <span>系统参数设置</span>
          <el-button type="primary" @click="handleAdd">添加配置</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="configKey" label="配置键" />
        <el-table-column prop="configValue" label="配置值" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
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
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="配置键" required>
          <el-input v-model="form.configKey" :disabled="form.id != null" />
        </el-form-item>
        <el-form-item label="配置值" required>
          <el-input v-model="form.configValue" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" />
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
import { ref, onMounted } from 'vue'
import { getConfigList, saveOrUpdateConfig, deleteConfig } from '../../api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('添加配置')
const form = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getConfigList()
    if (Array.isArray(res.data)) {
      // 前端分页
      total.value = res.data.length
      const start = (page.value - 1) * size.value
      const end = start + size.value
      tableData.value = res.data.slice(start, end)
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  form.value = {}
  dialogTitle.value = '添加配置'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogTitle.value = '编辑配置'
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.configKey || !form.value.configValue) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await saveOrUpdateConfig(form.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteConfig(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.system-config {
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

