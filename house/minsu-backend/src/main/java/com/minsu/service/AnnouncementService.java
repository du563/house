package com.minsu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minsu.entity.Announcement;

import java.util.List;

public interface AnnouncementService extends IService<Announcement> {

    /** 前台：仅启用，按排序与时间 */
    List<Announcement> listActiveForHome();
}
