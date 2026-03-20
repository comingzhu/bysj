import request from '../utils/request'

// 获取评分任务列表
export const getScoreTasks = (params) => {
  return request({
    url: '/score/tasks',
    method: 'get',
    params
  })
}

// 获取评分任务详情
export const getScoreTaskDetail = (id) => {
  return request({
    url: `/score/task/${id}`,
    method: 'get'
  })
}

// 获取作品详情
export const getWorkDetail = (id) => {
  return request({
    url: `/work/${id}`,
    method: 'get'
  })
}

// 提交评分
export const submitScore = (data) => {
  return request({
    url: '/score/submit',
    method: 'post',
    data
  })
}

// 更新评分
export const updateScore = (data) => {
  return request({
    url: '/score/update',
    method: 'put',
    data
  })
}

// 获取评分详情
export const getScoreDetail = (taskId) => {
  return request({
    url: `/score/detail/${taskId}`,
    method: 'get'
  })
}

// 获取评分统计
export const getScoreStatistics = () => {
  return request({
    url: '/score/statistics',
    method: 'get'
  })
}

// 获取评分记录列表
export const getScoreRecords = (params) => {
  return request({
    url: '/score/records',
    method: 'get',
    params
  })
}




