package com.cupk.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件发送服务（用于发送验证码等）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /** 发件人邮箱 */
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送简单邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件正文
     */
    public void send(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("邮件发送成功 -> {}", to);
        } catch (Exception e) {
            log.error("邮件发送失败 -> {}", to, e);
        }
    }

    /**
     * 发送验证码
     *
     * @param to   收件人邮箱
     * @param code 六位验证码
     * @param type 验证码类型（register / reset）
     */
    public void sendCode(String to, String code, String type) {
        String subject = type.equals("reset") ? "找回密码验证码" : "注册验证码";
        String content = "您好，您的验证码为：" + code + "，有效期5分钟，请勿泄露给他人。";
        send(to, subject, content);
    }
}
