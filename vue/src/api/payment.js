import request from '../utils/request'

export const getPaymentList = (params) => {
  return request({
    url: '/payment/list',
    method: 'get',
    params
  })
}

export const refund = (id, reason) => {
  return request({
    url: `/payment/refund/${id}`,
    method: 'post',
    params: { reason }
  })
}

export const sendPaymentNotice = (id) => {
  return request({
    url: `/payment/notice/${id}`,
    method: 'post'
  })
}

export const getPaymentStatistics = () => {
  return request({
    url: '/payment/statistics',
    method: 'get'
  })
}





