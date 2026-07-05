package com.minsu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minsu.entity.HouseTag;
import com.minsu.mapper.HouseTagMapper;
import com.minsu.service.HouseTagService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HouseTagServiceImpl extends ServiceImpl<HouseTagMapper, HouseTag> implements HouseTagService {
    @Override
    public List<HouseTag> listEnabled() {
        LambdaQueryWrapper<HouseTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseTag::getStatus, 1)
                .orderByAsc(HouseTag::getSortOrder)
                .orderByDesc(HouseTag::getId);
        return list(wrapper);
    }
}

