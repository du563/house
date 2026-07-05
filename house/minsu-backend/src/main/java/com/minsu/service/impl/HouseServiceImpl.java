package com.minsu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minsu.entity.House;
import com.minsu.mapper.HouseMapper;
import com.minsu.service.HouseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class HouseServiceImpl extends ServiceImpl<HouseMapper, House> implements HouseService {

    @Override
    public IPage<House> pageList(Integer pageNum, Integer pageSize, String name, String area,
                                  String type, BigDecimal minPrice, BigDecimal maxPrice) {
        Page<House> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(House::getStatus, 1)
               .eq(House::getAuditStatus, 1);
        
        if (StringUtils.hasText(name)) {
            wrapper.like(House::getName, name);
        }
        if (StringUtils.hasText(area)) {
            wrapper.eq(House::getArea, area);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(House::getType, type);
        }
        if (minPrice != null) {
            wrapper.ge(House::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(House::getPrice, maxPrice);
        }
        wrapper.orderByDesc(House::getScore)
                .orderByDesc(House::getScoreCount)
                .orderByDesc(House::getId);

        return page(page, wrapper);
    }
    
    @Override
    public IPage<House> adminPageList(Integer pageNum, Integer pageSize, String name, Integer auditStatus) {
        Page<House> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(House::getName, name);
        }
        if (auditStatus != null) {
            wrapper.eq(House::getAuditStatus, auditStatus);
        }
        wrapper.orderByAsc(House::getId);
        return page(page, wrapper);
    }
    
    @Override
    public IPage<House> merchantPageList(Long merchantId, Integer pageNum, Integer pageSize, String name, Integer auditStatus) {
        Page<House> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getMerchantId, merchantId);
        if (StringUtils.hasText(name)) {
            wrapper.like(House::getName, name);
        }
        if (auditStatus != null) {
            wrapper.eq(House::getAuditStatus, auditStatus);
        }
        wrapper.orderByAsc(House::getId);
        return page(page, wrapper);
    }

    @Override
    public List<House> getHotList(int limit) {
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getStatus, 1)
               .eq(House::getAuditStatus, 1)
               .orderByDesc(House::getScore)
               .orderByDesc(House::getScoreCount)
               .orderByDesc(House::getId)
               .last("LIMIT " + limit);
        return list(wrapper);
    }

    @Override
    public void updateScore(Long houseId, Integer score) {
        House house = getById(houseId);
        if (house != null) {
            int newCount = house.getScoreCount() + 1;
            BigDecimal totalScore = house.getScore().multiply(BigDecimal.valueOf(house.getScoreCount()))
                    .add(BigDecimal.valueOf(score));
            BigDecimal newScore = totalScore.divide(BigDecimal.valueOf(newCount), 1, RoundingMode.HALF_UP);
            
            house.setScore(newScore);
            house.setScoreCount(newCount);
            updateById(house);
        }
    }
}
