package com.minsu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minsu.common.Result;
import com.minsu.entity.BrowseHistory;
import com.minsu.entity.House;
import com.minsu.entity.Orders;
import com.minsu.service.BrowseHistoryService;
import com.minsu.service.HouseService;
import com.minsu.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 运营数据看板
 */
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private HouseService houseService;

    @Autowired
    private BrowseHistoryService browseHistoryService;

    /**
     * 管理员看板：订单趋势、Top房源、转化漏斗
     */
    @GetMapping("/admin/orders")
    public Result<?> adminOrders(
            @RequestParam(defaultValue = "7") Integer days,
            HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error(403, "无权限操作");
        }
        return Result.success(buildOrdersAnalytics(null, normalizeDays(days)));
    }

    /**
     * 商户看板：仅统计自己房源相关订单/浏览
     */
    @GetMapping("/merchant/orders")
    public Result<?> merchantOrders(
            @RequestParam(defaultValue = "7") Integer days,
            HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        Long merchantId = (Long) request.getAttribute("userId");
        if (role == null || role != 2) {
            return Result.error(403, "无权限操作");
        }
        return Result.success(buildOrdersAnalytics(merchantId, normalizeDays(days)));
    }

    private Map<String, Object> buildOrdersAnalytics(Long merchantId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDay = today.minusDays(days - 1L);
        LocalDateTime startTime = startDay.atStartOfDay();

        List<Long> scopedHouseIds = null;
        if (merchantId != null) {
            List<House> houses = houseService.lambdaQuery()
                    .eq(House::getMerchantId, merchantId)
                    .list();
            scopedHouseIds = houses.stream().map(House::getId).collect(Collectors.toList());
            if (scopedHouseIds.isEmpty()) {
                // 商户没有房源：返回空指标
                Map<String, Object> empty = new HashMap<>();
                empty.put("days", days);
                empty.put("trend", buildEmptyTrend(startDay, days));
                empty.put("topHouses", new ArrayList<>());
                Map<String, Object> funnel = new HashMap<>();
                funnel.put("views", 0);
                funnel.put("orders", 0);
                funnel.put("paidOrders", 0);
                funnel.put("completedOrders", 0);
                empty.put("funnel", funnel);
                return empty;
            }
        }

        // 订单范围
        LambdaQueryWrapper<Orders> ow = new LambdaQueryWrapper<>();
        ow.ge(Orders::getCreateTime, startTime);
        if (scopedHouseIds != null) {
            ow.in(Orders::getHouseId, scopedHouseIds);
        }
        List<Orders> orders = ordersService.list(ow);

        // 浏览范围（漏斗 views）
        LambdaQueryWrapper<BrowseHistory> hw = new LambdaQueryWrapper<>();
        hw.ge(BrowseHistory::getBrowseTime, startTime);
        if (scopedHouseIds != null) {
            hw.in(BrowseHistory::getHouseId, scopedHouseIds);
        }
        int views = (int) browseHistoryService.count(hw);

        // 趋势：按天聚合
        Map<LocalDate, int[]> trendMap = new HashMap<>();
        for (Orders o : orders) {
            if (o.getCreateTime() == null) continue;
            LocalDate d = o.getCreateTime().toLocalDate();
            if (d.isBefore(startDay) || d.isAfter(today)) continue;
            int[] arr = trendMap.computeIfAbsent(d, k -> new int[4]); // created/paid/completed/cancelled
            arr[0] += 1;
            if (o.getStatus() != null) {
                if (o.getStatus() == 1) arr[1] += 1;
                if (o.getStatus() == 2) arr[2] += 1;
                if (o.getStatus() == 3) arr[3] += 1;
            }
        }
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = startDay.plusDays(i);
            int[] arr = trendMap.getOrDefault(d, new int[4]);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", d.toString());
            item.put("created", arr[0]);
            item.put("paid", arr[1]);
            item.put("completed", arr[2]);
            item.put("cancelled", arr[3]);
            trend.add(item);
        }

        // Top房源：按 houseId 聚合（订单数/已支付数/营业额）
        Map<Long, TopAgg> topAgg = new HashMap<>();
        for (Orders o : orders) {
            if (o.getHouseId() == null) continue;
            TopAgg agg = topAgg.computeIfAbsent(o.getHouseId(), k -> new TopAgg());
            agg.houseId = o.getHouseId();
            agg.houseName = o.getHouseName();
            agg.orderCount++;
            if (o.getStatus() != null && (o.getStatus() == 1 || o.getStatus() == 2)) {
                agg.paidCount++;
                if (o.getTotalPrice() != null) {
                    agg.revenue = agg.revenue.add(o.getTotalPrice());
                }
            }
        }
        List<Map<String, Object>> topHouses = topAgg.values().stream()
                .sorted((a, b) -> Integer.compare(b.orderCount, a.orderCount))
                .limit(10)
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("houseId", a.houseId);
                    m.put("houseName", a.houseName);
                    m.put("orderCount", a.orderCount);
                    m.put("paidCount", a.paidCount);
                    m.put("revenue", a.revenue);
                    return m;
                })
                .collect(Collectors.toList());

        // 漏斗
        int totalOrders = orders.size();
        int paidOrders = (int) orders.stream().filter(o -> o.getStatus() != null && (o.getStatus() == 1 || o.getStatus() == 2)).count();
        int completedOrders = (int) orders.stream().filter(o -> o.getStatus() != null && o.getStatus() == 2).count();
        Map<String, Object> funnel = new LinkedHashMap<>();
        funnel.put("views", views);
        funnel.put("orders", totalOrders);
        funnel.put("paidOrders", paidOrders);
        funnel.put("completedOrders", completedOrders);

        Map<String, Object> out = new HashMap<>();
        out.put("days", days);
        out.put("trend", trend);
        out.put("topHouses", topHouses);
        out.put("funnel", funnel);
        return out;
    }

    private static boolean isAdmin(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        return role != null && role == 1;
    }

    private static int normalizeDays(Integer days) {
        int d = days == null ? 7 : days;
        if (d < 3) d = 3;
        if (d > 30) d = 30;
        return d;
    }

    private static List<Map<String, Object>> buildEmptyTrend(LocalDate startDay, int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = startDay.plusDays(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", d.toString());
            item.put("created", 0);
            item.put("paid", 0);
            item.put("completed", 0);
            item.put("cancelled", 0);
            trend.add(item);
        }
        return trend;
    }

    private static class TopAgg {
        Long houseId;
        String houseName;
        int orderCount = 0;
        int paidCount = 0;
        BigDecimal revenue = BigDecimal.ZERO;
    }
}

