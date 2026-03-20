import request from '../utils/request'

export const getWorkList = (params) => {
  return request({
    url: '/work/list',
    method: 'get',
    params
  })
}

export const updateWorkStatus = (id, status) => {
  return request({
    url: `/work/status/${id}`,
    method: 'put',
    params: { status }
  })
}

export const downloadWork = (filePath) => {
  return request({
    url: `/file/download?path=${encodeURIComponent(filePath)}`,
    method: 'get',
    responseType: 'blob'
  })
}

export const submitWork = (data) => {
  return request({
    url: '/work/submit',
    method: 'post',
    data
  })
}

export const getMyWorks = (competitionId) => {
  return request({
    url: '/work/my',
    method: 'get',
    params: competitionId ? { competitionId } : {}
  })
}

export const getWorkDetail = (workId) => {
  return request({
    url: `/work/${workId}`,
    method: 'get'
  })
}

export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}


