import request from '@/utils/request'

export function getTopics () {
  return request({
    url: '/community/topic/list',
    method: 'get'
  })
}

export function getPosts (params) {
  return request({
    url: '/community/post/list',
    method: 'get',
    params
  })
}

export function addPost (data) {
  return request({
    url: '/community/post/add',
    method: 'post',
    data
  })
}

export function togglePostLike (postId) {
  return request({
    url: `/community/post/like/${postId}`,
    method: 'put'
  })
}

export function adminPostList (params) {
  return request({
    url: '/community/admin/post/list',
    method: 'get',
    params
  })
}

export function adminAuditPost (id, data) {
  return request({
    url: `/community/admin/post/audit/${id}`,
    method: 'put',
    data
  })
}

export function adminDeletePost (id) {
  return request({
    url: `/community/admin/post/${id}`,
    method: 'delete'
  })
}

export function adminTopicList () {
  return request({
    url: '/community/admin/topic/list',
    method: 'get'
  })
}

export function adminAddTopic (data) {
  return request({
    url: '/community/admin/topic',
    method: 'post',
    data
  })
}

export function adminUpdateTopic (data) {
  return request({
    url: '/community/admin/topic',
    method: 'put',
    data
  })
}

export function adminDeleteTopic (id) {
  return request({
    url: `/community/admin/topic/${id}`,
    method: 'delete'
  })
}

