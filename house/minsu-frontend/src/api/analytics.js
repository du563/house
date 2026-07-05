import request from '@/utils/request'

export function adminOrderAnalytics (days = 7) {
  return request({
    url: '/analytics/admin/orders',
    method: 'get',
    params: { days }
  })
}

export function merchantOrderAnalytics (days = 7) {
  return request({
    url: '/analytics/merchant/orders',
    method: 'get',
    params: { days }
  })
}

