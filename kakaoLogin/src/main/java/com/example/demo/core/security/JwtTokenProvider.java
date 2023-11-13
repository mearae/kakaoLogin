package com.example.demo.core.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.user.StringArrayConverter;
import com.example.demo.user.User;
import org.springframework.security.core.Authentication;

import java.util.Date;

public class JwtTokenProvider {
    private static final Long EXP = 1000L * 60 * 60;
    private static final Long REFRESH_EXP = 1000L * 60 * 60 * 24 * 60;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER = "Authorization";
    private static final String SECRET = "SECRET_KEY";

    // ** User 객체의 정보를 사용해 JWT 토큰을 생성하고 반환.
    public static String create(User user){
        StringArrayConverter stringArrayConverter = new StringArrayConverter();

        String roles = stringArrayConverter.convertToDatabaseColumn(
                user.getRoles()
        );

        String jwt = JWT.create()
                .withSubject(user.getEmail())// ** 토큰의 대상정보 셋팅
                .withExpiresAt(new Date(System.currentTimeMillis() + EXP))
                .withClaim("id", user.getId())
                .withClaim("roles", roles)
                .sign(Algorithm.HMAC512(SECRET));// ** JWT 생성 알고리즘 설정
        return TOKEN_PREFIX + jwt;
    }

    public static String createRefresh(User user){
        StringArrayConverter stringArrayConverter = new StringArrayConverter();

        String roles = stringArrayConverter.convertToDatabaseColumn(
                user.getRoles()
        );

        String jwt = JWT.create()
                .withSubject(user.getEmail())// ** 토큰의 대상정보 셋팅
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_EXP))
                .withClaim("id", user.getId())
                .withClaim("roles", roles)
                .sign(Algorithm.HMAC512(SECRET));// ** JWT 생성 알고리즘 설정
        return jwt;
    }

    // **  JWT 토큰 문자열을 검증하고, 유효하다면 디코딩된 DecodedJWT 객체를 반환.
    public static DecodedJWT verify(String jwt) throws SignatureVerificationException, TokenExpiredException {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(SECRET)) // 검증 방법
                .build()
                .verify(jwt); // 검증 할 것

        return decodedJWT;
    }

    public static void invalidateToken(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // 현재 인증된 사용자의 토큰을 무효화하는 로직을 구현합니다.
            // 예를 들어, 토큰을 블랙리스트에 추가하거나, 토큰의 만료일을 조정하여 무효화할 수 있습니다.
            // 구체적인 구현 방법은 프로젝트의 요구사항과 토큰 관리 방식에 따라 다를 수 있습니다.
            // 예시로는 블랙리스트에 추가하는 방법을 보여드리겠습니다.

            String token = (String) authentication.getCredentials();
            Blacklist.addToken(token);
        }
    }
}
