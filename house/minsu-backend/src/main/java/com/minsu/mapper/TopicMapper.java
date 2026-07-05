package com.minsu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minsu.entity.Topic;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
}

