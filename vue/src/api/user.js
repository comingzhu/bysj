import request from '../utils/request'

export const login = (data) => {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

export const register = (data) => {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

// 新的注册API，带更详细的错误处理
export const registerUser = async (data) => {
  try {
    console.log('调用 registerUser API，数据:', data)
    const response = await request({
      url: '/user/register',
      method: 'post',
      data
    })
    console.log('registerUser 响应:', response)
    return response
  } catch (error) {
    console.error('registerUser API 错误:', error)
    throw error
  }
}

export const getUserInfo = () => {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export const getUserById = (id) => {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

export const updateUserInfo = (data) => {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

export const getUserList = (params) => {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

export const createUser = (data) => {
  return request({
    url: '/user/create',
    method: 'post',
    data
  })
}

export const updateUserStatus = (userId, status) => {
  return request({
    url: `/user/status/${userId}`,
    method: 'put',
    params: { status }
  })
}

export const updateUserByAdmin = (data) => {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

export const changePassword = (data) => {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

export const rechargeBalance = (amount) => {
  return request({
    url: '/user/balance/recharge',
    method: 'post',
    data: { amount }
  })
}

export const getBalance = () => {
  return request({
    url: '/user/balance',
    method: 'get'
  })
}

