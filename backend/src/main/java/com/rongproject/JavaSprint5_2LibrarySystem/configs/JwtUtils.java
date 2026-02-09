package com.rongproject.JavaSprint5_2LibrarySystem.configs;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // 1. 签名密钥（生产环境建议放在配置文件中）
    // 密钥长度至少 256 位（32 字节）
    private static final String SECRET_KEY = "your-very-secure-and-very-long-secret-key-for-library-project";
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 2. 有效期（示例：24小时）
    private static final long EXPIRATION_TIME = 86400000;

    // --- 功能 A：造钥匙 (Generate Token) ---
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // 放入用户名
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 过期时间
                .signWith(key, SignatureAlgorithm.HS256) // 签名算法
                .compact();
    }

    // --- 功能 B：看钥匙上的名字 (Get Username from Token) ---
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    // --- 功能 C：验钥匙真伪 (Validate Token) ---
    public boolean validateToken(String token) {
        try {
            getClaims(token); // 如果解析成功且没过期，说明是真的
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 私有辅助方法：解密并获取 Token 里的数据 (Claims)
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
