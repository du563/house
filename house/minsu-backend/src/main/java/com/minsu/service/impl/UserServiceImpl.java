package com.minsu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minsu.entity.User;
import com.minsu.mapper.UserMapper;
import com.minsu.service.UserService;
import com.minsu.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtils jwtUtils;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public boolean register(String username, String password, String phone, String email, Integer role) {
        // 检查用户名是否存在
        User existUser = getByUsername(username);
        if (existUser != null) {
            return false;
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setEmail(email);
        Integer safeRole = role == null ? 0 : role;
        // 仅允许普通用户(0)与商户(2)注册，管理员由后台创建
        if (!Integer.valueOf(0).equals(safeRole) && !Integer.valueOf(2).equals(safeRole)) {
            safeRole = 0;
        }
        user.setRole(safeRole);
        user.setStatus(1);
        
        return save(user);
    }

    @Override
    public String login(String username, String password) {
        User user = getByUsername(username);
        if (user == null) {
            return null;
        }
        
        if (user.getStatus() != 1) {
            return null;
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        
        return jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }
}
