package com.minsu.controller;

import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import com.minsu.common.Result;
import com.minsu.entity.User;
import com.minsu.service.EmailCodeService;
import com.minsu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserService userService;
    @Autowired
    private EmailCodeService emailCodeService;
    
    private static final long CAPTCHA_EXPIRE_MS = 2 * 60 * 1000L;
    private static final ConcurrentHashMap<String, CaptchaRecord> CAPTCHA_CACHE = new ConcurrentHashMap<>();

    private static class CaptchaRecord {
        private final String code;
        private final long expireAt;

        private CaptchaRecord(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }

    /**
     * 获取登录验证码
     */
    @GetMapping("/captcha")
    public Result<?> getCaptcha() {
        try {
            CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(120, 40, 4, 10);
            String code = captcha.getCode();
            String captchaId = UUID.randomUUID().toString().replace("-", "");
            long expireAt = System.currentTimeMillis() + CAPTCHA_EXPIRE_MS;
            CAPTCHA_CACHE.put(captchaId, new CaptchaRecord(code.toLowerCase(), expireAt));

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(captcha.getImage(), "png", outputStream);
            String imageBase64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());

            Map<String, Object> data = new HashMap<>();
            data.put("captchaId", captchaId);
            data.put("image", "data:image/png;base64," + imageBase64);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("验证码生成失败");
        }
    }

    /**
     * 发送注册邮箱验证码
     */
    @PostMapping("/email-code")
    public Result<?> sendEmailCode(@RequestBody Map<String, String> params) {
        String email = params == null ? null : params.get("email");
        String targetEmail = email == null ? null : email.trim();
        if (!StringUtils.hasText(targetEmail)) {
            return Result.error("邮箱不能为空");
        }
        try {
            boolean sent = emailCodeService.sendRegisterCode(targetEmail);
            if (sent) {
                return Result.success("验证码发送成功", null);
            }
            return Result.error("发送过于频繁，请60秒后再试");
        } catch (Exception e) {
            log.error("发送邮箱验证码失败, email={}", targetEmail, e);
            String msg = e.getMessage();
            return Result.error(StringUtils.hasText(msg) ? msg : "验证码发送失败，请检查邮箱配置");
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String phone = params.get("phone");
        String email = params.get("email");
        String emailCode = params.get("emailCode");
        String targetEmail = email == null ? null : email.trim();
        String roleStr = params.get("role");
        Integer role = null;
        if (roleStr != null && !roleStr.trim().isEmpty()) {
            try {
                role = Integer.valueOf(roleStr.trim());
            } catch (Exception ignored) {
                role = null;
            }
        }
        
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password) || !StringUtils.hasText(targetEmail)) {
            return Result.error("用户名、密码和邮箱不能为空");
        }
        if (!StringUtils.hasText(emailCode)) {
            return Result.error("邮箱验证码不能为空");
        }

        boolean codeValid = emailCodeService.verifyRegisterCode(targetEmail, emailCode.trim());
        if (!codeValid) {
            return Result.error("邮箱验证码错误或已过期");
        }
        
        boolean success = userService.register(username.trim(), password, phone, targetEmail, role);
        if (success) {
            return Result.success("注册成功", null);
        } else {
            return Result.error("用户名已存在");
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String captchaId = params.get("captchaId");
        String captchaCode = params.get("captchaCode");
        
        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }

        if (captchaId == null || captchaCode == null || captchaCode.trim().isEmpty()) {
            return Result.error("请输入验证码");
        }

        CaptchaRecord record = CAPTCHA_CACHE.remove(captchaId);
        if (record == null || record.expireAt < System.currentTimeMillis()) {
            return Result.error("验证码已过期，请刷新后重试");
        }
        if (!record.code.equals(captchaCode.trim().toLowerCase())) {
            return Result.error("验证码错误");
        }
        
        String token = userService.login(username, password);
        if (token != null) {
            User user = userService.getByUsername(username);
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("role", user.getRole());
            data.put("email", user.getEmail());
            return Result.success("登录成功", data);
        } else {
            return Result.error("用户名或密码错误");
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<?> updateUser(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        user.setId(userId);
        user.setPassword(null); // 不允许通过此接口修改密码
        user.setRole(null); // 不允许修改角色
        user.setUsername(null); // 不允许修改用户名
        
        boolean success = userService.updateById(user);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败");
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        User user = userService.getById(userId);
        // 验证旧密码
        String token = userService.login(user.getUsername(), oldPassword);
        if (token == null) {
            return Result.error("原密码错误");
        }
        if (!StringUtils.hasText(newPassword)) {
            return Result.error("新密码不能为空");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            return Result.error("新密码不能与旧密码相同");
        }

        // 更新新密码
        userService.register(user.getUsername() + "_temp", newPassword, null, null, user.getRole());
        User tempUser = userService.getByUsername(user.getUsername() + "_temp");
        user.setPassword(tempUser.getPassword());
        userService.updateById(user);
        userService.removeById(tempUser.getId());
        
        return Result.success("密码修改成功", null);
    }
}
