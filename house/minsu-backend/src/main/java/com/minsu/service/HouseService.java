package com.minsu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.minsu.entity.House;

import java.math.BigDecimal;
import java.util.List;

public interface HouseService extends IService<House> {
    
    /**
     * 分页查询房源列表
     */
    IPage<House> pageList(Integer pageNum, Integer pageSize, String name, String area,
                          String type, BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * 管理端分页查询（不过滤上下架/审核状态）
     */
    IPage<House> adminPageList(Integer pageNum, Integer pageSize, String name, Integer auditStatus);
    
    /**
     * 商户分页查询自己的房源
     */
    IPage<House> merchantPageList(Long merchantId, Integer pageNum, Integer pageSize, String name, Integer auditStatus);
    
    /**
     * 获取热门房源
     */
    List<House> getHotList(int limit);
    
    /**
     * 更新房源评分
     */
    void updateScore(Long houseId, Integer score);
}
