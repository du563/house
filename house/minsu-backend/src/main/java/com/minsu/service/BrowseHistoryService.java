package com.minsu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minsu.entity.BrowseHistory;
import com.minsu.entity.House;

import java.util.List;

public interface BrowseHistoryService extends IService<BrowseHistory> {
    
    /**
     * 记录浏览历史
     */
    void recordBrowse(Long userId, Long houseId);
    
    /**
     * 获取用户浏览历史
     */
    List<BrowseHistory> getUserHistory(Long userId, int limit);
    
    /**
     * 获取用户最近浏览的房源ID列表
     */
    List<Long> getRecentHouseIds(Long userId, int limit);
}
