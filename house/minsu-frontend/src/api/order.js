import request from '@/utils/request'

// 创建订单
export function createOrder(data) {
  return request({
    url: '/order/create',
    method: 'post',
    data
  })
}

// 获取我的订单
export function getMyOrders(params) {
  return request({
    url: '/order/my',
    method: 'get',
    params
  })
}

// 获取订单详情
export function getOrderDetail(id) {
  return request({
    url: `/order/detail/${id}`,
    method: 'get'
  })
}

// 支付订单
export function payOrder(id) {
  return request({
    url: `/order/pay/${id}`,
    method: 'put'
  })
}

// 取消订单
export function cancelOrder(id) {
  return request({
    url: `/order/cancel/${id}`,
    method: 'put'
  })
}

// 管理员接口
export function adminGetOrders(params) {
  return request({
    url: '/order/admin/list',
    method: 'get',
    params
  })
}

export function completeOrder(id) {
  return request({
    url: `/order/complete/${id}`,
    method: 'put'
  })
}

// 商户：确认入住
export function merchantCheckInOrder (id) {
  return request({
    url: `/order/merchant/checkin/${id}`,
    method: 'put'
  })
}

// 商户：完成订单
export function merchantCompleteOrder (id) {
  return request({
    url: `/order/merchant/complete/${id}`,
    method: 'put'
  })
}
