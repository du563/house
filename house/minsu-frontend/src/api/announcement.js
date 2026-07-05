import request from '@/utils/request'

/** 前台：启用的公告列表（无需登录） */
export function getActiveAnnouncementList () {
  return request({
    url: '/announcement/list',
    method: 'get'
  })
}

export function adminAnnouncementList () {
  return request({
    url: '/announcement/admin/list',
    method: 'get'
  })
}

export function adminAnnouncementAdd (data) {
  return request({
    url: '/announcement/admin',
    method: 'post',
    data
  })
}

export function adminAnnouncementUpdate (data) {
  return request({
    url: '/announcement/admin',
    method: 'put',
    data
  })
}

export function adminAnnouncementDelete (id) {
  return request({
    url: `/announcement/admin/${id}`,
    method: 'delete'
  })
}
