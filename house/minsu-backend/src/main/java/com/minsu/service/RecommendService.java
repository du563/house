package com.minsu.service;

import com.minsu.entity.House;

import java.util.List;

public interface RecommendService {
    
    /**
     * 获取推荐房源列表
     */
    List<House> getRecommendList(Long userId, int limit);
}
