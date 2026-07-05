package com.minsu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minsu.entity.Announcement;
import com.minsu.mapper.AnnouncementMapper;
import com.minsu.service.AnnouncementService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
        implements AnnouncementService {

    @Override
    public List<Announcement> listActiveForHome() {
        LambdaQueryWrapper<Announcement> w = new LambdaQueryWrapper<>();
        w.eq(Announcement::getStatus, 1)
                .orderByAsc(Announcement::getSortOrder)
                .orderByDesc(Announcement::getId);
        return list(w);
    }
}
