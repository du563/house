package com.minsu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minsu.entity.Comment;
import com.minsu.mapper.CommentMapper;
import com.minsu.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Override
    public List<Comment> getHouseComments(Long houseId) {
        return baseMapper.selectByHouseIdWithUser(houseId);
    }

    @Override
    @Transactional
    public boolean addComment(Comment comment) {
        return save(comment);
    }
}
