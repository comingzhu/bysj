import request from '../utils/request'

export const getRegistrationStatistics = (dimension, value) => {
  return request({
    url: '/statistics/registration',
    method: 'get',
    params: { dimension, value }
  })
}

export const getPaymentStatistics = () => {
  return request({
    url: '/statistics/payment',
    method: 'get'
  })
}

export const getAwardStatistics = (dimension) => {
  return request({
    url: '/statistics/award',
    method: 'get',
    params: { dimension }
  })
}

export const getDashboardStatistics = () => {
  return request({
    url: '/statistics/dashboard',
    method: 'get'
  })
}

