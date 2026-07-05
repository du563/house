package com.minsu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minsu.common.Result;
import com.minsu.entity.House;
import com.minsu.entity.Orders;
import com.minsu.entity.User;
import com.minsu.service.HouseService;
import com.minsu.service.OrdersService;
import com.minsu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private HouseService houseService;

    @Autowired
    private OrdersService ordersService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 系统统计概览
     */
    @GetMapping("/stats")
    public Result<?> stats(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.error(403, "无权限操作");
        }

        Map<String, Object> stats = new HashMap<>();
        
        // 用户总数
        stats.put("userCount", userService.count());
        stats.put("merchantCount", userService.count(new LambdaQueryWrapper<User>().eq(User::getRole, 2)));
        
        // 房源总数
        stats.put("houseCount", houseService.count());
        
        // 上架且审核通过的房源数
        LambdaQueryWrapper<House> houseWrapper = new LambdaQueryWrapper<>();
        houseWrapper.eq(House::getStatus, 1).eq(House::getAuditStatus, 1);
        stats.put("activeHouseCount", houseService.count(houseWrapper));
        stats.put("pendingHouseCount", houseService.count(new LambdaQueryWrapper<House>().eq(House::getAuditStatus, 0)));
        
        // 订单总数
        stats.put("orderCount", ordersService.count());
        
        // 各状态订单数
        for (int i = 0; i <= 3; i++) {
            LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Orders::getStatus, i);
            stats.put("orderStatus" + i, ordersService.count(orderWrapper));
        }
        
        return Result.success(stats);
    }

    /**
     * 用户列表
     */
    @GetMapping("/user/list")
    public Result<?> userList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            HttpServletRequest request) {
        
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.error(403, "无权限操作");
        }

        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        
        wrapper.orderByAsc(User::getId);
        IPage<User> userPage = userService.page(page, wrapper);
        
        // 隐藏密码
        userPage.getRecords().forEach(u -> u.setPassword(null));
        
        return Result.success(userPage);
    }

    /**
     * 禁用/启用用户
     */
    @PutMapping("/user/status/{id}")
    public Result<?> changeUserStatus(@PathVariable Long id, @RequestParam Integer status, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.error(403, "无权限操作");
        }

        User user = new User();
        user.setId(id);
        user.setStatus(status);
        boolean success = userService.updateById(user);
        
        if (success) {
            return Result.success(status == 1 ? "启用成功" : "禁用成功", null);
        }
        return Result.error("操作失败");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/user/delete/{id}")
    public Result<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.error(403, "无权限操作");
        }

        boolean success = userService.removeById(id);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }

    /**
     * 新增用户（管理员）
     */
    @PostMapping("/user/add")
    public Result<?> addUser(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.error(403, "无权限操作");
        }

        String username = params.get("username") == null ? null : String.valueOf(params.get("username")).trim();
        String password = params.get("password") == null ? null : String.valueOf(params.get("password"));
        String phone = params.get("phone") == null ? null : String.valueOf(params.get("phone")).trim();
        String email = params.get("email") == null ? null : String.valueOf(params.get("email")).trim();
        String avatar = params.get("avatar") == null ? null : String.valueOf(params.get("avatar")).trim();

        Integer newRole = params.get("role") == null ? 0 : Integer.valueOf(String.valueOf(params.get("role")));
        Integer status = params.get("status") == null ? 1 : Integer.valueOf(String.valueOf(params.get("status")));

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return Result.error(400, "用户名和密码不能为空");
        }

        User exist = userService.getByUsername(username);
        if (exist != null) {
            return Result.error(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setEmail(email);
        user.setAvatar(avatar);
        user.setRole(newRole);
        user.setStatus(status);

        boolean success = userService.save(user);
        if (success) {
            user.setPassword(null);
            return Result.success("添加成功", user);
        }
        return Result.error("添加失败");
    }

    /**
     * 更新用户（管理员）
     * 允许更新：phone/avatar/role/status/password（password 传了就会被重新加密）
     */
    @PutMapping("/user/update")
    public Result<?> updateUser(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.error(403, "无权限操作");
        }

        Long id = params.get("id") == null ? null : Long.valueOf(String.valueOf(params.get("id")));
        if (id == null) {
            return Result.error(400, "用户ID不能为空");
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        if (params.containsKey("phone")) {
            String phone = params.get("phone") == null ? null : String.valueOf(params.get("phone")).trim();
            user.setPhone(phone);
        }
        if (params.containsKey("email")) {
            String email = params.get("email") == null ? null : String.valueOf(params.get("email")).trim();
            user.setEmail(email);
        }
        if (params.containsKey("avatar")) {
            String avatar = params.get("avatar") == null ? null : String.valueOf(params.get("avatar")).trim();
            user.setAvatar(avatar);
        }
        if (params.containsKey("role")) {
            user.setRole(Integer.valueOf(String.valueOf(params.get("role"))));
        }
        if (params.containsKey("status")) {
            user.setStatus(Integer.valueOf(String.valueOf(params.get("status"))));
        }

        // 可选更新密码
        String password = params.get("password") == null ? null : String.valueOf(params.get("password"));
        if (StringUtils.hasText(password)) {
            user.setPassword(passwordEncoder.encode(password));
        }

        boolean success = userService.updateById(user);
        if (success) {
            user.setPassword(null);
            return Result.success("更新成功", user);
        }
        return Result.error("更新失败");
    }
}
