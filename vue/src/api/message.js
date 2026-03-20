import request from '../utils/request'

export const getMyMessages = (params) => {
  return request({
    url: '/message/my',
    method: 'get',
    params
  })
}

export const getAllMessages = (params) => {
  return request({
    url: '/message/all',
    method: 'get',
    params
  })
}

export const markAsRead = (id) => {
  return request({
    url: `/message/read/${id}`,
    method: 'put'
  })
}

export const sendMessage = (data) => {
  return request({
    url: '/message/send',
    method: 'post',
    data
  })
}

export const updateMessage = (id, data) => {
  return request({
    url: `/message/${id}`,
    method: 'put',
    data
  })
}

export const deleteMessage = (id) => {
  return request({
    url: `/message/${id}`,
    method: 'delete'
  })
}

export const getNoticeList = (params) => {
  return request({
    url: '/message/my',
    method: 'get',
    params
  })
}

