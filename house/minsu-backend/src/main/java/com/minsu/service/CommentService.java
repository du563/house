package com.minsu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minsu.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {
    
    /**
     * 获取房源评价列表
     */
    List<Comment> getHouseComments(Long houseId);
    
    /**
     * 添加评价
     */
    boolean addComment(Comment comment);
}
