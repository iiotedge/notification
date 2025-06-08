package com.iotmining.services.notification.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Base64;

public class JwtUtil {

    private static final String SECRET_BASE64 = "Vlo2vcFdiXGgWqZLEpLw6kk99sH8/4odgC2XgZV0IbA="; // base64 encoded 256-bit key
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_BASE64));

    public static Jws<Claims> validateToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token);
    }
}
