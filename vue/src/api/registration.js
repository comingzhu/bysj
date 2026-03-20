import request from '../utils/request'

export const register = (data) => {
  return request({
    url: '/registration/register',
    method: 'post',
    data
  })
}

export const approveRegistration = (id, status, rejectReason) => {
  return request({
    url: `/registration/approve/${id}`,
    method: 'put',
    params: { status, rejectReason }
  })
}

export const getRegistrationList = (params) => {
  return request({
    url: '/registration/list',
    method: 'get',
    params
  })
}

export const getMyRegistrations = (params) => {
  return request({
    url: '/registration/my',
    method: 'get',
    params
  })
}

export const pay = (id) => {
  return request({
    url: `/registration/pay/${id}`,
    method: 'post'
  })
}





