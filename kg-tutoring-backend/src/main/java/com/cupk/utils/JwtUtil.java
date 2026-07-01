package com.cupk.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 工具类 —— 生成和校验 Token
 */
public class JwtUtil {

    /** 密钥（至少 256 位，这里用 HMAC-SHA256） */
    private static final SecretKey KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /** 过期时间：24 小时 */
    private static final long EXPIRATION = 24 * 60 * 60 * 1000L;

    /**
     * 生成 token
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     角色
     * @return JWT token 字符串
     */
    public static String generateToken(Integer userId, String username, String role) {
        return Jwts.builder()
                .setId(String.valueOf(userId))
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY)
                .compact();
    }

    /**
     * 解析 token，获取 Claims
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
