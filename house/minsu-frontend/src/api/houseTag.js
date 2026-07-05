import request from '@/utils/request'

export function getHouseTags () {
  return request({
    url: '/house-tag/list',
    method: 'get'
  })
}

export function adminHouseTagList () {
  return request({
    url: '/house-tag/admin/list',
    method: 'get'
  })
}

export function adminHouseTagAdd (data) {
  return request({
    url: '/house-tag/admin',
    method: 'post',
    data
  })
}

export function adminHouseTagUpdate (data) {
  return request({
    url: '/house-tag/admin',
    method: 'put',
    data
  })
}

export function adminHouseTagDelete (id) {
  return request({
    url: `/house-tag/admin/${id}`,
    method: 'delete'
  })
}

