import request from '@/utils/request'

// 获取房源列表
export function getHouseList(params) {
  return request({
    url: '/house/list',
    method: 'get',
    params
  })
}

// 获取房源详情
export function getHouseDetail(id) {
  return request({
    url: `/house/detail/${id}`,
    method: 'get'
  })
}

// 查看房源并记录浏览历史
export function viewHouse(id) {
  return request({
    url: `/house/view/${id}`,
    method: 'get'
  })
}

// 获取热门房源
export function getHotHouses(limit = 8) {
  return request({
    url: '/house/hot',
    method: 'get',
    params: { limit }
  })
}

// 获取推荐房源
export function getRecommendHouses(limit = 8) {
  return request({
    url: '/recommend/list',
    method: 'get',
    params: { limit }
  })
}

// 管理员接口
export function adminGetHouseList(params) {
  return request({
    url: '/house/admin/list',
    method: 'get',
    params
  })
}

export function merchantGetHouseList(params) {
  return request({
    url: '/house/merchant/list',
    method: 'get',
    params
  })
}

export function addHouse(data) {
  return request({
    url: '/house/add',
    method: 'post',
    data
  })
}

export function updateHouse(data) {
  return request({
    url: '/house/update',
    method: 'put',
    data
  })
}

export function deleteHouse(id) {
  return request({
    url: `/house/delete/${id}`,
    method: 'delete'
  })
}

export function changeHouseStatus(id, status) {
  return request({
    url: `/house/status/${id}`,
    method: 'put',
    params: { status }
  })
}

export function auditHouse(id, data) {
  return request({
    url: `/house/audit/${id}`,
    method: 'put',
    data
  })
}

export function resubmitHouse(id) {
  return request({
    url: `/house/resubmit/${id}`,
    method: 'put'
  })
}

export function getMerchantHouseStats() {
  return request({
    url: '/house/merchant/stats',
    method: 'get'
  })
}
