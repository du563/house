package com.minsu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minsu.entity.Favorite;
import com.minsu.entity.House;
import com.minsu.mapper.FavoriteMapper;
import com.minsu.service.FavoriteService;
import com.minsu.service.HouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private HouseService houseService;

    @Override
    @Transactional
    public boolean addFavorite(Long userId, Long houseId) {
        if (userId == null || houseId == null) {
            return false;
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getHouseId, houseId);

        Favorite existing = getOne(wrapper);
        if (existing != null) {
            return false;
        }

        // 可选校验：房源必须存在
        if (houseService.getById(houseId) == null) {
            return false;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setHouseId(houseId);
        return save(favorite);
    }

    @Override
    @Transactional
    public boolean removeFavorite(Long userId, Long houseId) {
        if (userId == null || houseId == null) {
            return false;
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getHouseId, houseId);
        return remove(wrapper);
    }

    @Override
    public boolean isFavorite(Long userId, Long houseId) {
        if (userId == null || houseId == null) {
            return false;
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getHouseId, houseId);
        return count(wrapper) > 0;
    }

    @Override
    public IPage<House> pageFavoriteHouses(Long userId, Integer pageNum, Integer pageSize) {
        Page<Favorite> favoritePage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime);

        IPage<Favorite> recordsPage = page(favoritePage, wrapper);
        List<Favorite> favorites = recordsPage.getRecords();
        if (favorites == null || favorites.isEmpty()) {
            Page<House> empty = new Page<>(pageNum, pageSize);
            empty.setTotal(recordsPage.getTotal());
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        List<Long> houseIds = favorites.stream()
                .map(Favorite::getHouseId)
                .distinct()
                .collect(Collectors.toList());

        List<House> houses = houseService.listByIds(houseIds);
        Map<Long, House> houseMap = new HashMap<>();
        for (House h : houses) {
            houseMap.put(h.getId(), h);
        }

        // 按收藏时间顺序组装房源列表
        List<House> ordered = new ArrayList<>();
        for (Favorite f : favorites) {
            House h = houseMap.get(f.getHouseId());
            if (h != null) {
                ordered.add(h);
            }
        }

        Page<House> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setTotal(recordsPage.getTotal());
        resultPage.setRecords(ordered);
        return resultPage;
    }
}

