<template>
  <div class="competition-list">
    <el-card>
      <template #header>
        <div class="header">
          <span>竞赛列表</span>
          <div class="filters">
            <el-select
              v-model="category"
              placeholder="选择竞赛类别"
              clearable
              style="flex: 1; min-width: 150px;"
              :loading="loadingCategories"
              @change="handleFilterChange"
            >
              <el-option 
                v-for="cat in categories" 
                :key="cat" 
                :label="cat" 
                :value="cat" 
              />
            </el-select>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              format="YYYY-MM-DD"
              style="flex: 1; min-width: 250px;"
              @change="handleFilterChange"
            />
            <el-input
              v-model="keyword"
              placeholder="搜索竞赛名称"
              style="flex: 0 0 200px;"
              @keyup.enter="loadData"
            >
              <template #append>
                <el-button @click="loadData">搜索</el-button>
                <el-button @click="resetFilters">重置</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="name" label="竞赛名称" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            {{ row.type === 0 ? '个人赛' : '团队赛' }}
          </template>
        </el-table-column>
        <el-table-column prop="registrationFee" label="报名费" width="100">
          <template #default="{ row }">
            ¥{{ row.registrationFee }}
          </template>
        </el-table-column>
        <el-table-column prop="registrationStart" label="报名开始" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.registrationStart) }}
          </template>
        </el-table-column>
        <el-table-column prop="registrationEnd" label="报名结束" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.registrationEnd) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row.id)">查看详情</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCompetitionList } from '../../api/competition'
import { getConfigByKey, getConfigList } from '../../api/system'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '../../utils/dateFormat'

const router = useRouter()
const loading = ref(false)
const loadingCategories = ref(false)
const tableData = ref([])
const keyword = ref('')
const category = ref('')
const categories = ref([])
const dateRange = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 加载竞赛分类
const loadCategories = async () => {
  loadingCategories.value = true
  try {
    console.log('开始加载竞赛分类...')
    // 尝试使用不同的API端点获取配置
    let categoriesConfig = null
    
    // 先尝试通过key获取配置
    try {
      const res1 = await getConfigByKey('competition_category')
      console.log('通过key获取配置:', res1)
      if (res1 && res1.data) {
        categoriesConfig = res1.data
      }
    } catch (error) {
      console.error('通过key获取配置失败:', error)
    }
    
    // 如果通过key获取失败，尝试获取所有配置并查找
    if (!categoriesConfig) {
      try {
        const res2 = await getConfigList()
        console.log('获取所有配置:', res2)
        if (res2 && res2.data && Array.isArray(res2.data)) {
          const configItem = res2.data.find(item => item.key === 'competition_category')
          if (configItem) {
            categoriesConfig = configItem
          }
        }
      } catch (error) {
        console.error('获取所有配置失败:', error)
      }
    }
    
    // 处理配置数据
    if (categoriesConfig && (categoriesConfig.value || categoriesConfig.configValue)) {
      const configValue = categoriesConfig.value || categoriesConfig.configValue
      // 假设配置值是逗号分隔的字符串
      categories.value = configValue.split(',').map(c => c.trim()).filter(c => c)
      console.log('解析后的竞赛分类:', categories.value)
    } else {
      // 默认分类，防止配置不存在时无选项
      categories.value = ['程序设计', '算法竞赛', '创新创业', '数学建模', '英语竞赛', '体育竞赛']
      console.log('使用默认竞赛分类:', categories.value)
    }
  } catch (error) {
    console.error('加载竞赛分类失败:', error)
    // 加载失败时使用默认分类
    categories.value = ['程序设计', '算法竞赛', '创新创业', '数学建模', '英语竞赛', '体育竞赛']
  } finally {
    loadingCategories.value = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCompetitionList({
      // 不限定状态，由前端自行筛选可展示的竞赛
      category: category.value || undefined,
      keyword: keyword.value,
      page: page.value,
      size: size.value
    })
    if (res.data && res.data.records) {
      // 学生端：可以看到已发布 / 已暂停 / 已结束的竞赛，用于查看信息，但是否能报名由详情页时间判断
      let filtered = res.data.records.filter(c => [4, 5, 6].includes(c.status))
      
      // 按“报名开始时间”处于所选日期区间内进行筛选
      if (dateRange.value && dateRange.value.length === 2) {
        const [startDate, endDate] = dateRange.value
        filtered = filtered.filter(competition => {
          if (!competition.registrationStart) return false
          const regStart = new Date(competition.registrationStart)
          const start = new Date(startDate)
          const end = new Date(endDate)
          // 把结束日期的时间设置到当天 23:59:59，包含整天
          end.setHours(23, 59, 59, 999)
          return regStart >= start && regStart <= end
        })
      }
      
      // 高级筛选功能1：按报名结束时间从早到晚排序（null 的放在最后）
      filtered.sort((a, b) => {
        const aEnd = a.registrationEnd ? new Date(a.registrationEnd).getTime() : Number.MAX_SAFE_INTEGER
        const bEnd = b.registrationEnd ? new Date(b.registrationEnd).getTime() : Number.MAX_SAFE_INTEGER
        return aEnd - bEnd
      })
      tableData.value = filtered
      total.value = tableData.value.length
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

const viewDetail = (id) => {
  router.push(`/competition/detail/${id}`)
}

// 当筛选条件变化时，从第一页重新加载
const handleFilterChange = () => {
  page.value = 1
  loadData()
}

// 重置筛选条件
const resetFilters = () => {
  category.value = ''
  dateRange.value = []
  keyword.value = ''
  page.value = 1
  loadData()
}

onMounted(async () => {
  // 先加载竞赛分类
  await loadCategories()
  // 再加载竞赛数据
  loadData()
})
</script>

<style scoped>
.competition-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.filters {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>

