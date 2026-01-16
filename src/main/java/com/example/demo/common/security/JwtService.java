package com.example.demo.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // 1. 產生 Token (給使用者用)
    // 這裡通常傳入 UserDetails，這樣我們可以直接拿 username
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // (進階) 產生 Token，可以放入額外的資訊 (如 userId, role)
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // 通常放 email
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // 設定過期時間
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // 簽名
                .compact();
    }

    // 2. 驗證 Token 是否有效
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // 檢查兩件事：1. Token 裡的 username 跟傳進來的人是不是同一個 2. Token 有沒有過期
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // 3. 從 Token 取得 Username (Email)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // --- 以下是內部輔助方法 (Private Helpers) ---

    // 泛型方法：用來提取 Token 裡的任何資訊
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 解析 Token 的核心方法
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 把 Base64 的字串轉成 Key 物件
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 檢查是否過期
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
