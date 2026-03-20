import { getConfigByKey } from '../api/system'

// 缓存学院列表
let collegeListCache = null

/**
 * 获取学院列表
 * @returns {Promise<string[]>}
 */
export const getCollegeList = async () => {
  if (collegeListCache) {
    return collegeListCache
  }
  
  try {
    const res = await getConfigByKey('college_list')
    if (res.data && res.data.configValue) {
      collegeListCache = res.data.configValue.split(',').map(item => item.trim()).filter(item => item)
      return collegeListCache
    }
  } catch (error) {
    console.error('获取学院列表失败:', error)
  }
  
  // 默认学院列表
  collegeListCache = ['计算机学院', '软件学院', '信息学院', '管理学院', '经济学院']
  return collegeListCache
}

/**
 * 清除学院列表缓存
 */
export const clearCollegeCache = () => {
  collegeListCache = null
}


