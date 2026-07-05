package com.minsu.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.minsu.service.EmailCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@Service
public class EmailCodeServiceImpl implements EmailCodeService {
    private static final Logger log = LoggerFactory.getLogger(EmailCodeServiceImpl.class);

    private static final String REGISTER_CODE_KEY_PREFIX = "register:email:code:";
    private static final String REGISTER_CODE_LIMIT_PREFIX = "register:email:limit:";
    private static final long CODE_EXPIRE_MINUTES = 5L;
    private static final long SEND_LIMIT_SECONDS = 60L;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JavaMailSender mailSender;

    @Value("${mail.from}")
    private String from;
    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Override
    public boolean sendRegisterCode(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        if (!StringUtils.hasText(mailPassword)) {
            throw new IllegalStateException("邮箱配置错误：未配置MAIL_PASSWORD(QQ授权码)");
        }
        String limitKey = REGISTER_CODE_LIMIT_PREFIX + email;
        Boolean canSend;
        try {
            canSend = stringRedisTemplate.opsForValue().setIfAbsent(limitKey, "1", SEND_LIMIT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis写入限流键失败, email={}", email, e);
            throw new IllegalStateException("验证码发送失败：Redis连接异常");
        }
        if (Boolean.FALSE.equals(canSend)) {
            return false;
        }

        String code = RandomUtil.randomNumbers(6);
        String codeKey = REGISTER_CODE_KEY_PREFIX + email;
        try {
            stringRedisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Redis写入验证码失败, email={}", email, e);
            throw new IllegalStateException("验证码发送失败：Redis连接异常");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("智能民宿系统注册验证码");
        message.setText("您好，您的注册验证码为：" + code + "，5分钟内有效，请勿泄露给他人。");
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("邮箱发送失败, from={}, to={}", from, email, e);
            throw new IllegalStateException("验证码发送失败：邮箱认证或SMTP配置错误");
        }
        return true;
    }

    @Override
    public boolean verifyRegisterCode(String email, String code) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(code)) {
            return false;
        }
        String codeKey = REGISTER_CODE_KEY_PREFIX + email;
        String saved = stringRedisTemplate.opsForValue().get(codeKey);
        if (!code.equals(saved)) {
            return false;
        }
        stringRedisTemplate.delete(codeKey);
        return true;
    }
}

