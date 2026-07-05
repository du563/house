package com.minsu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minsu.entity.HouseTag;

import java.util.List;

public interface HouseTagService extends IService<HouseTag> {
    List<HouseTag> listEnabled();
}

