import request from '../utils/request'

export const createTeam = (data) => {
  return request({
    url: '/team/create',
    method: 'post',
    data
  })
}

export const joinTeam = (teamId) => {
  return request({
    url: `/team/join/${teamId}`,
    method: 'post'
  })
}

export const getTeamMembers = (teamId) => {
  return request({
    url: `/team/members/${teamId}`,
    method: 'get'
  })
}

export const getMyTeams = (competitionId) => {
  return request({
    url: '/team/my',
    method: 'get',
    params: { competitionId }
  })
}

export const searchTeams = (competitionId, keyword) => {
  return request({
    url: '/team/search',
    method: 'get',
    params: { competitionId, keyword }
  })
}

export const approveTeamMember = (teamId, memberId, status) => {
  return request({
    url: '/team/member/approve',
    method: 'put',
    params: { teamId, memberId, status }
  })
}

export const getPendingMembers = (teamId) => {
  return request({
    url: `/team/pending/${teamId}`,
    method: 'get'
  })
}

export const inviteMember = (teamId, studentNo) => {
  return request({
    url: '/team/invite',
    method: 'post',
    params: { teamId, studentNo }
  })
}

