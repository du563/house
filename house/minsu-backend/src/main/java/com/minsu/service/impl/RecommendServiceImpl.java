package com.minsu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minsu.entity.House;
import com.minsu.service.BrowseHistoryService;
import com.minsu.service.HouseService;
import com.minsu.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能推荐服务实现
 * 基于用户浏览历史 + 房源相似度算法
 */
@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private BrowseHistoryService browseHistoryService;

    @Autowired
    private HouseService houseService;

    @Override
    public List<House> getRecommendList(Long userId, int limit) {
        // 获取用户最近浏览的房源ID
        List<Long> recentHouseIds = browseHistoryService.getRecentHouseIds(userId, 10);
        if (recentHouseIds.isEmpty()) {
            // 无浏览记录，返回热门房源
            return houseService.getHotList(limit);
        }
        // 获取用户浏览过的房源信息
        List<House> browsedHouses = houseService.listByIds(recentHouseIds);
        if (browsedHouses.isEmpty()) {
            return houseService.getHotList(limit);
        }
        // 提取用户偏好特征
        UserPreference preference = extractPreference(browsedHouses);
        // 获取所有可预订房源（排除已浏览的）
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getStatus, 1)
               .notIn(House::getId, recentHouseIds);
        List<House> allHouses = houseService.list(wrapper);
        if (allHouses.isEmpty()) {
            return houseService.getHotList(limit);
        }
        // 计算相似度并排序
        List<HouseSimilarity> similarities = new ArrayList<>();
        for (House house : allHouses) {
            double similarity = calculateSimilarity(house, preference);
            similarities.add(new HouseSimilarity(house, similarity));
        }
        // 按相似度降序排序，取前limit个
        return similarities.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                .limit(limit)
                .map(HouseSimilarity::getHouse)
                .collect(Collectors.toList());
    }
    /**
     * 提取用户偏好特征
     */
    private UserPreference extractPreference(List<House> houses) {
        UserPreference preference = new UserPreference();
        // 统计区域偏好
        Map<String, Long> areaCount = houses.stream()
                .collect(Collectors.groupingBy(House::getArea, Collectors.counting()));
        preference.setPreferredAreas(areaCount.keySet());
        // 统计户型偏好
        Map<String, Long> typeCount = houses.stream()
                .collect(Collectors.groupingBy(House::getType, Collectors.counting()));
        preference.setPreferredTypes(typeCount.keySet());
        // 计算价格区间偏好
        BigDecimal avgPrice = houses.stream()
                .map(House::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(houses.size()), 2, BigDecimal.ROUND_HALF_UP);
        preference.setAvgPrice(avgPrice);
        // 计算评分偏好
        BigDecimal avgScore = houses.stream()
                .map(House::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(houses.size()), 2, BigDecimal.ROUND_HALF_UP);
        preference.setAvgScore(avgScore);
        return preference;
    }
    
    /**
     * 计算房源与用户偏好的相似度
     */
    private double calculateSimilarity(House house, UserPreference preference) {
        double score = 0.0;
        
        // 区域匹配（权重30%）
        if (preference.getPreferredAreas().contains(house.getArea())) {
            score += 0.3;
        }
        // 户型匹配（权重20%）
        if (preference.getPreferredTypes().contains(house.getType())) {
            score += 0.2;
        }
        // 价格相似度（权重30%）
        double priceDiff = Math.abs(house.getPrice().subtract(preference.getAvgPrice()).doubleValue());
        double priceRange = preference.getAvgPrice().doubleValue() * 0.5; // 允许50%的价格差异
        if (priceDiff <= priceRange) {
            score += 0.3 * (1 - priceDiff / priceRange);
        }
        // 评分相似度（权重20%）
        double scoreDiff = Math.abs(house.getScore().subtract(preference.getAvgScore()).doubleValue());
        if (scoreDiff <= 1.0) {
            score += 0.2 * (1 - scoreDiff);
        }
        
        return score;
    }
    
    /**
     * 用户偏好特征类
     */
    private static class UserPreference {
        private Set<String> preferredAreas = new HashSet<>();
        private Set<String> preferredTypes = new HashSet<>();
        private BigDecimal avgPrice = BigDecimal.ZERO;
        private BigDecimal avgScore = BigDecimal.ZERO;
        public Set<String> getPreferredAreas() { return preferredAreas; }
        public void setPreferredAreas(Set<String> areas) { this.preferredAreas = areas; }
        public Set<String> getPreferredTypes() { return preferredTypes; }
        public void setPreferredTypes(Set<String> types) { this.preferredTypes = types; }
        public BigDecimal getAvgPrice() { return avgPrice; }
        public void setAvgPrice(BigDecimal price) { this.avgPrice = price; }
        public BigDecimal getAvgScore() { return avgScore; }
        public void setAvgScore(BigDecimal score) { this.avgScore = score; }
    }
    
    /**
     * 房源相似度包装类
     */
    private static class HouseSimilarity {
        private House house;
        private double similarity;
        public HouseSimilarity(House house, double similarity) {
            this.house = house;
            this.similarity = similarity;
        }
        public House getHouse() { return house; }
        public double getSimilarity() { return similarity; }
    }
}
