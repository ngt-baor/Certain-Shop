package com.certainshop.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String taoToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList());
        return taoToken(claims, userDetails.getUsername());
    }

    public String taoToken(String tenDangNhap, String vaiTro) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", vaiTro);
        return taoToken(claims, tenDangNhap);
    }

    private String taoToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String layTenDangNhap(String token) {
        return layThongTin(token, Claims::getSubject);
    }

    public Date layNgayHetHan(String token) {
        return layThongTin(token, Claims::getExpiration);
    }

    public <T> T layThongTin(String token, Function<Claims, T> claimsResolver) {
        Claims claims = layTatCaThongTin(token);
        return claimsResolver.apply(claims);
    }

    private Claims layTatCaThongTin(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean kiemTraToken(String token, UserDetails userDetails) {
        String tenDangNhap = layTenDangNhap(token);
        return tenDangNhap.equals(userDetails.getUsername()) && !daHetHan(token);
    }

    public boolean tokenHopLe(String token) {
        try {
            layTatCaThongTin(token);
            return !daHetHan(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean daHetHan(String token) {
        return layNgayHetHan(token).before(new Date());
    }
}
