package com.minsu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minsu.entity.Topic;

import java.util.List;

public interface TopicService extends IService<Topic> {
    List<Topic> listEnabled();
}

