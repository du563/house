import request from '@/utils/request'

// 获取统计数据
export function getStats() {
  return request({
    url: '/admin/stats',
    method: 'get'
  })
}

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params
  })
}

// 修改用户状态
export function changeUserStatus(id, status) {
  return request({
    url: `/admin/user/status/${id}`,
    method: 'put',
    params: { status }
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/admin/user/delete/${id}`,
    method: 'delete'
  })
}

// 新增用户
export function addUser(data) {
  return request({
    url: '/admin/user/add',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(data) {
  return request({
    url: '/admin/user/update',
    method: 'put',
    data
  })
}
