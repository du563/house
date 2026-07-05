package com.minsu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minsu.entity.User;

public interface UserService extends IService<User> {
    
    /**
     * 用户注册
     */
    boolean register(String username, String password, String phone, String email, Integer role);
    
    /**
     * 用户登录
     */
    String login(String username, String password);
    
    /**
     * 根据用户名查询
     */
    User getByUsername(String username);
}
