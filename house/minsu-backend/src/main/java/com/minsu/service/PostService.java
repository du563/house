package com.minsu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.minsu.entity.Post;

public interface PostService extends IService<Post> {
    IPage<Post> pageApprovedPosts(Integer pageNum, Integer pageSize, Long topicId, Long currentUserId);
    IPage<Post> pageAdminPosts(Integer pageNum, Integer pageSize, Integer status);
    boolean toggleLike(Long postId, Long userId);
}

