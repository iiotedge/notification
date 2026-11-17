package com.iotmining.services.notification.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;

public class JwtUtil {

    private static final String SECRET_BASE64 = "Vlo2vcFdiXGgWqZLEpLw6kk99sH8/4odgC2XgZV0IbA=";

    // CHANGE 1: Use 'SecretKey' instead of generic 'Key' for type safety
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_BASE64));

    public static Jws<Claims> validateToken(String token) throws JwtException {
        // CHANGE 2: parserBuilder() -> parser()
        return Jwts.parser()
                .verifyWith(SIGNING_KEY)   // CHANGE 3: setSigningKey() -> verifyWith()
                .build()
                .parseSignedClaims(token); // CHANGE 4: parseClaimsJws() -> parseSignedClaims()
    }
}

//package com.iotmining.services.notification.utils;
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//
//import java.security.Key;
//import java.util.Base64;
//
//public class JwtUtil {
//
//    private static final String SECRET_BASE64 = "Vlo2vcFdiXGgWqZLEpLw6kk99sH8/4odgC2XgZV0IbA="; // base64 encoded 256-bit key
//    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_BASE64));
//
//    public static Jws<Claims> validateToken(String token) throws JwtException {
//        return Jwts.parserBuilder()
//                .setSigningKey(SIGNING_KEY)
//                .build()
//                .parseClaimsJws(token);
//    }
//}
