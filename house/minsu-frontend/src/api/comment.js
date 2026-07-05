import request from '@/utils/request'

// 获取房源评价列表
export function getComments(houseId) {
  return request({
    url: `/comment/list/${houseId}`,
    method: 'get'
  })
}

// 添加评价
export function addComment(data) {
  return request({
    url: '/comment/add',
    method: 'post',
    data
  })
}

// 删除评价
export function deleteComment(id) {
  return request({
    url: `/comment/delete/${id}`,
    method: 'delete'
  })
}

export function adminCommentList (params) {
  return request({
    url: '/comment/admin/list',
    method: 'get',
    params
  })
}

export function adminAuditComment (id, data) {
  return request({
    url: `/comment/admin/audit/${id}`,
    method: 'put',
    data
  })
}

export function merchantCommentList (params) {
  return request({
    url: '/comment/merchant/list',
    method: 'get',
    params
  })
}

export function merchantReplyComment (id, data) {
  return request({
    url: `/comment/merchant/reply/${id}`,
    method: 'put',
    data
  })
}
