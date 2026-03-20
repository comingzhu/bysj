import request from '../utils/request'

export const getConfigList = () => {
  return request({
    url: '/system/config/list',
    method: 'get'
  })
}

export const getConfigByKey = (key) => {
  return request({
    url: `/system/config/key/${key}`,
    method: 'get'
  })
}

export const saveOrUpdateConfig = (data) => {
  return request({
    url: '/system/config/save',
    method: 'post',
    data
  })
}

export const deleteConfig = (id) => {
  return request({
    url: `/system/config/${id}`,
    method: 'delete'
  })
}





