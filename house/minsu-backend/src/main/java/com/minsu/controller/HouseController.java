package com.minsu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.minsu.common.Result;
import com.minsu.entity.House;
import com.minsu.service.BrowseHistoryService;
import com.minsu.service.HouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 房源控制器
 */
@RestController
@RequestMapping("/house")
public class HouseController {

    @Autowired
    private HouseService houseService;

    @Autowired
    private BrowseHistoryService browseHistoryService;

    /**
     * 分页查询房源列表
     */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        IPage<House> page = houseService.pageList(pageNum, pageSize, name, area, type, minPrice, maxPrice);
        return Result.success(page);
    }

    /**
     * 获取房源详情
     */
    @GetMapping("/detail/{id}")
    public Result<?> detail(@PathVariable Long id) {
        House house = houseService.getById(id);
        if (house != null && house.getStatus() == 1 && Integer.valueOf(1).equals(house.getAuditStatus())) {
            return Result.success(house);
        }
        return Result.error("房源不存在");
    }

    /**
     * 获取房源详情并记录浏览历史（需登录）
     */
    @GetMapping("/view/{id}")
    public Result<?> view(@PathVariable Long id, HttpServletRequest request) {
        House house = houseService.getById(id);
        if (house == null || house.getStatus() != 1 || !Integer.valueOf(1).equals(house.getAuditStatus())) {
            return Result.error("房源不存在");
        }
        
        // 记录浏览历史
        Long userId = (Long) request.getAttribute("userId");
        if (userId != null) {
            browseHistoryService.recordBrowse(userId, id);
        }
        
        return Result.success(house);
    }

    /**
     * 获取热门房源
     */
    @GetMapping("/hot")
    public Result<?> hot(@RequestParam(defaultValue = "8") Integer limit) {
        List<House> list = houseService.getHotList(limit);
        return Result.success(list);
    }

    /**
     * 新增房源（管理员/商户）
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody House house, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        if (role == null || (role != 1 && role != 2)) {
            return Result.error(403, "无权限操作");
        }
        
        house.setScoreCount(0);
        house.setScore(BigDecimal.valueOf(5.0));
        house.setMerchantId(role == 2 ? userId : house.getMerchantId());
        house.setAuditStatus(role == 1 ? 1 : 0);
        house.setAuditRemark(role == 1 ? "管理员创建，默认审核通过" : null);
        if (house.getStatus() == null) {
            house.setStatus(role == 1 ? 1 : 0);
        }
        if (role == 2) {
            house.setStatus(0);
        }
        boolean success = houseService.save(house);
        if (success) {
            return Result.success(role == 2 ? "房源已提交，等待管理员审核" : "添加成功", house);
        }
        return Result.error("添加失败");
    }

    /**
     * 更新房源（管理员/商户）
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody House house, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        if (role == null || (role != 1 && role != 2)) {
            return Result.error(403, "无权限操作");
        }
        House existHouse = houseService.getById(house.getId());
        if (existHouse == null) {
            return Result.error("房源不存在");
        }
        if (role == 2 && !userId.equals(existHouse.getMerchantId())) {
            return Result.error(403, "只能编辑自己的房源");
        }
        if (role == 2) {
            house.setMerchantId(userId);
            house.setAuditStatus(0);
            house.setAuditRemark(null);
            house.setStatus(0);
        } else {
            house.setMerchantId(existHouse.getMerchantId());
        }
        boolean success = houseService.updateById(house);
        if (success) {
            return Result.success(role == 2 ? "修改成功，已重新提交审核" : "更新成功", null);
        }
        return Result.error("更新失败");
    }

    /**
     * 删除房源（管理员/商户）
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        if (role == null || (role != 1 && role != 2)) {
            return Result.error(403, "无权限操作");
        }
        House house = houseService.getById(id);
        if (house == null) {
            return Result.error("房源不存在");
        }
        if (role == 2 && !userId.equals(house.getMerchantId())) {
            return Result.error(403, "只能删除自己的房源");
        }
        boolean success = houseService.removeById(id);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }

    /**
     * 上下架房源（管理员/商户）
     */
    @PutMapping("/status/{id}")
    public Result<?> changeStatus(@PathVariable Long id, @RequestParam Integer status, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        if (role == null || (role != 1 && role != 2)) {
            return Result.error(403, "无权限操作");
        }
        House existHouse = houseService.getById(id);
        if (existHouse == null) {
            return Result.error("房源不存在");
        }
        if (role == 2 && !userId.equals(existHouse.getMerchantId())) {
            return Result.error(403, "只能操作自己的房源");
        }
        if (role == 2 && !Integer.valueOf(1).equals(existHouse.getAuditStatus())) {
            return Result.error("房源审核通过后才能上架");
        }
        House house = new House();
        house.setId(id);
        house.setStatus(status);
        boolean success = houseService.updateById(house);
        if (success) {
            return Result.success(status == 1 ? "上架成功" : "下架成功", null);
        }
        return Result.error("操作失败");
    }
    
    /**
     * 管理员查询所有房源（包含下架）
     */
    @GetMapping("/admin/list")
    public Result<?> adminList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer auditStatus,
            HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.error(403, "无权限操作");
        }
        
        IPage<House> page = houseService.adminPageList(pageNum, pageSize, name, auditStatus);
        return Result.success(page);
    }
    
    /**
     * 商户查询自己的房源
     */
    @GetMapping("/merchant/list")
    public Result<?> merchantList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer auditStatus,
            HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        if (role == null || role != 2) {
            return Result.error(403, "无权限操作");
        }
        IPage<House> page = houseService.merchantPageList(userId, pageNum, pageSize, name, auditStatus);
        return Result.success(page);
    }
    
    /**
     * 管理员审核房源
     */
    @PutMapping("/audit/{id}")
    public Result<?> audit(@PathVariable Long id, @RequestBody(required = false) House payload, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.error(403, "无权限操作");
        }
        House existHouse = houseService.getById(id);
        if (existHouse == null) {
            return Result.error("房源不存在");
        }
        Integer auditStatus = payload == null ? null : payload.getAuditStatus();
        if (auditStatus == null || (auditStatus != 1 && auditStatus != 2)) {
            return Result.error(400, "审核状态无效");
        }
        House house = new House();
        house.setId(id);
        house.setAuditStatus(auditStatus);
        house.setAuditRemark(payload == null ? null : payload.getAuditRemark());
        if (auditStatus == 1) {
            house.setStatus(existHouse.getStatus() != null && existHouse.getStatus() == 1 ? 1 : 0);
        } else {
            house.setStatus(0);
        }
        boolean success = houseService.updateById(house);
        if (success) {
            return Result.success(auditStatus == 1 ? "审核通过" : "已驳回", null);
        }
        return Result.error("审核失败");
    }
    
    /**
     * 商户申请重新审核
     */
    @PutMapping("/resubmit/{id}")
    public Result<?> resubmit(@PathVariable Long id, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        if (role == null || role != 2) {
            return Result.error(403, "无权限操作");
        }
        House existHouse = houseService.getById(id);
        if (existHouse == null) {
            return Result.error("房源不存在");
        }
        if (!userId.equals(existHouse.getMerchantId())) {
            return Result.error(403, "只能操作自己的房源");
        }
        House house = new House();
        house.setId(id);
        house.setAuditStatus(0);
        house.setAuditRemark(null);
        house.setStatus(0);
        boolean success = houseService.updateById(house);
        if (success) {
            return Result.success("已重新提交审核", null);
        }
        return Result.error("提交失败");
    }
    
    /**
     * 根据商户查询房源
     */
    @GetMapping("/merchant/{merchantId}")
    public Result<?> listByMerchant(@PathVariable Long merchantId) {
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getMerchantId, merchantId)
                .eq(House::getStatus, 1)
                .eq(House::getAuditStatus, 1)
                .orderByDesc(House::getCreateTime);
        return Result.success(houseService.list(wrapper));
    }

    /**
     * 我的房源统计（商户）
     */
    @GetMapping("/merchant/stats")
    public Result<?> merchantStats(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        if (role == null || role != 2) {
            return Result.error(403, "无权限操作");
        }
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getMerchantId, userId);
        long total = houseService.count(wrapper);
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalCount", total);
        stats.put("pendingCount", houseService.count(new LambdaQueryWrapper<House>()
                .eq(House::getMerchantId, userId)
                .eq(House::getAuditStatus, 0)));
        stats.put("approvedCount", houseService.count(new LambdaQueryWrapper<House>()
                .eq(House::getMerchantId, userId)
                .eq(House::getAuditStatus, 1)));
        stats.put("rejectedCount", houseService.count(new LambdaQueryWrapper<House>()
                .eq(House::getMerchantId, userId)
                .eq(House::getAuditStatus, 2)));
        stats.put("onlineCount", houseService.count(new LambdaQueryWrapper<House>()
                .eq(House::getMerchantId, userId)
                .eq(House::getStatus, 1)
                .eq(House::getAuditStatus, 1)));
        return Result.success(stats);
    }
}
