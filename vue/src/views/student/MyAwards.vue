<template>
  <div class="my-awards">
    <el-card>
      <template #header>
        <span>我的获奖</span>
      </template>
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="competitionName" label="竞赛名称" min-width="200" />
        <el-table-column prop="awardLevel" label="奖项等级" />
        <el-table-column prop="rank" label="排名" />
        <el-table-column prop="score" label="得分" />
        <el-table-column prop="certificateNumber" label="证书编号" min-width="150" />
        <el-table-column prop="createTime" label="获奖时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>

      </el-table>
    </el-card>


  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyAwards } from '../../api/award'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '../../utils/dateFormat'

const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyAwards()
    if (res.data) {
      tableData.value = res.data
    } else {
      tableData.value = []
    }
  } catch (error) {
    ElMessage.error('加载失败：' + (error.response?.data?.message || error.message))
    tableData.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-awards {
  padding: 20px;
}

.competition-detail {
  max-height: 600px;
  overflow-y: auto;
}

.no-data {
  text-align: center;
  padding: 40px 0;
  color: #999;
}
</style>


