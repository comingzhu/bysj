import request from '../utils/request'

export const createCompetition = (data) => {
  return request({
    url: '/competition/create',
    method: 'post',
    data
  })
}

export const updateCompetition = (data) => {
  return request({
    url: '/competition/update',
    method: 'put',
    data
  })
}

export const getCompetitionList = (params) => {
  return request({
    url: '/competition/list',
    method: 'get',
    params
  })
}

export const getCompetitionById = (id) => {
  return request({
    url: `/competition/${id}`,
    method: 'get'
  })
}

export const getJudgesByCompetition = (id) => {
  return request({
    url: `/competition/${id}/judges`,
    method: 'get'
  })
}

export const approveCompetition = (id, status, rejectReason, judgeIds) => {
  const params = { status, rejectReason }
  if (Array.isArray(judgeIds) && judgeIds.length > 0) {
    params.judgeIds = judgeIds.join(',')
  }
  return request({
    url: `/competition/approve/${id}`,
    method: 'put',
    params
  })
}

export const publishCompetition = (id) => {
  return request({
    url: `/competition/publish/${id}`,
    method: 'put'
  })
}

export const pauseCompetition = (id) => {
  return request({
    url: `/competition/pause/${id}`,
    method: 'put'
  })
}

export const resumeCompetition = (id) => {
  return request({
    url: `/competition/resume/${id}`,
    method: 'put'
  })
}

export const getMyCompetitions = (params) => {
  return request({
    url: '/competition/my',
    method: 'get',
    params
  })
}

export const deleteCompetition = (id) => {
  return request({
    url: `/competition/${id}`,
    method: 'delete'
  })
}

export const createScoreTasks = (id) => {
  return request({
    url: `/competition/${id}/create-score-tasks`,
    method: 'post'
  })
}

export const getJudgesByCategory = (category) => {
  return request({
    url: '/competition/judges',
    method: 'get',
    params: { category }
  })
}