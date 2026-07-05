package com.minsu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minsu.entity.Post;
import com.minsu.entity.PostLike;
import com.minsu.mapper.PostMapper;
import com.minsu.service.PostService;
import com.minsu.service.TopicService;
import com.minsu.service.UserService;
import com.minsu.mapper.PostLikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private UserService userService;
    @Autowired
    private TopicService topicService;
    @Autowired
    private PostLikeMapper postLikeMapper;

    @Override
    public IPage<Post> pageApprovedPosts(Integer pageNum, Integer pageSize, Long topicId, Long currentUserId) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> w = new LambdaQueryWrapper<>();
        w.eq(Post::getStatus, 1);
        if (topicId != null) {
            w.eq(Post::getTopicId, topicId);
        }
        w.orderByDesc(Post::getCreateTime);
        IPage<Post> result = page(page, w);
        enrich(result.getRecords(), currentUserId);
        return result;
    }

    @Override
    public IPage<Post> pageAdminPosts(Integer pageNum, Integer pageSize, Integer status) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> w = new LambdaQueryWrapper<>();
        if (status != null && status >= 0) {
            w.eq(Post::getStatus, status);
        }
        w.orderByDesc(Post::getCreateTime);
        IPage<Post> result = page(page, w);
        enrich(result.getRecords(), null);
        return result;
    }

    @Override
    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        if (postId == null || userId == null) return false;
        Post post = getById(postId);
        if (post == null || !Integer.valueOf(1).equals(post.getStatus())) return false;

        LambdaQueryWrapper<PostLike> q = new LambdaQueryWrapper<>();
        q.eq(PostLike::getPostId, postId).eq(PostLike::getUserId, userId);
        PostLike exist = postLikeMapper.selectOne(q);
        if (exist != null) {
            postLikeMapper.deleteById(exist.getId());
            Post upd = new Post();
            upd.setId(postId);
            upd.setLikeCount(Math.max(0, (post.getLikeCount() == null ? 0 : post.getLikeCount()) - 1));
            return updateById(upd);
        } else {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            postLikeMapper.insert(like);
            Post upd = new Post();
            upd.setId(postId);
            upd.setLikeCount((post.getLikeCount() == null ? 0 : post.getLikeCount()) + 1);
            return updateById(upd);
        }
    }

    private void enrich(List<Post> records, Long currentUserId) {
        if (records == null || records.isEmpty()) return;
        Set<Long> userIds = records.stream().map(Post::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> topicIds = records.stream().map(Post::getTopicId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userService.listByIds(userIds).forEach(u -> userMap.put(u.getId(), u.getUsername()));
        }
        Map<Long, String> topicMap = new HashMap<>();
        if (!topicIds.isEmpty()) {
            topicService.listByIds(topicIds).forEach(t -> topicMap.put(t.getId(), t.getName()));
        }

        Set<Long> likedPostIds = new HashSet<>();
        if (currentUserId != null) {
            LambdaQueryWrapper<PostLike> lw = new LambdaQueryWrapper<>();
            lw.eq(PostLike::getUserId, currentUserId)
                    .in(PostLike::getPostId, records.stream().map(Post::getId).collect(Collectors.toList()));
            postLikeMapper.selectList(lw).forEach(l -> likedPostIds.add(l.getPostId()));
        }

        for (Post p : records) {
            p.setUsername(userMap.getOrDefault(p.getUserId(), "用户"));
            p.setTopicName(topicMap.getOrDefault(p.getTopicId(), "话题"));
            p.setLiked(currentUserId != null && likedPostIds.contains(p.getId()));
        }
    }
}

