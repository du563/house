import request from '@/utils/request'

// 获取浏览历史
export function getBrowseHistory(params) {
  return request({
    url: '/history/list',
    method: 'get',
    params
  })
}

// 删除浏览记录
export function deleteHistory(id) {
  return request({
    url: `/history/delete/${id}`,
    method: 'delete'
  })
}

// 清空浏览历史
export function clearHistory() {
  return request({
    url: '/history/clear',
    method: 'delete'
  })
}
