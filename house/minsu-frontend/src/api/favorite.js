import request from '@/utils/request'

// 检查是否已收藏
export function checkFavorite(houseId) {
  return request({
    url: `/favorite/check/${houseId}`,
    method: 'get'
  })
}

// 添加收藏
export function addFavorite(data) {
  return request({
    url: '/favorite/add',
    method: 'post',
    data
  })
}

// 取消收藏
export function deleteFavorite(houseId) {
  return request({
    url: `/favorite/delete/${houseId}`,
    method: 'delete'
  })
}

// 获取收藏列表（分页）
export function getFavorites(params) {
  return request({
    url: '/favorite/list',
    method: 'get',
    params
  })
}

