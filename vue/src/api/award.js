import request from '../utils/request'

export const getAwardList = (competitionIds) => {
  return request({
    url: '/award/list',
    method: 'get',
    params: { competitionIds: competitionIds ? competitionIds.join(',') : null }
  })
}

export const getMyAwards = () => {
  return request({
    url: '/award/my',
    method: 'get'
  })
}

export const generateAwards = (competitionId) => {
  return request({
    url: `/award/generate/${competitionId}`,
    method: 'post'
  })
}

export const getAwardListByCompetition = (competitionId) => {
  return request({
    url: `/award/competition/${competitionId}`,
    method: 'get'
  })
}

export const createAward = (data) => {
  return request({
    url: '/award/create',
    method: 'post',
    data
  })
}

export const updateAward = (data) => {
  return request({
    url: '/award/update',
    method: 'put',
    data
  })
}

export const deleteAward = (id) => {
  return request({
    url: `/award/${id}`,
    method: 'delete'
  })
}


