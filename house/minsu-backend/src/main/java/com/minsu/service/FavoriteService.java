package com.minsu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.minsu.entity.Favorite;
import com.minsu.entity.House;

import java.util.List;

public interface FavoriteService extends IService<Favorite> {

    boolean addFavorite(Long userId, Long houseId);

    boolean removeFavorite(Long userId, Long houseId);

    boolean isFavorite(Long userId, Long houseId);

    /**
     * 收藏房源列表（分页，按收藏时间倒序）
     */
    IPage<House> pageFavoriteHouses(Long userId, Integer pageNum, Integer pageSize);
}

