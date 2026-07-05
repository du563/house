package com.minsu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minsu.entity.Topic;
import com.minsu.mapper.TopicMapper;
import com.minsu.service.TopicService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {
    @Override
    public List<Topic> listEnabled() {
        LambdaQueryWrapper<Topic> w = new LambdaQueryWrapper<>();
        w.eq(Topic::getStatus, 1)
                .orderByAsc(Topic::getSortOrder)
                .orderByDesc(Topic::getId);
        return list(w);
    }
}

