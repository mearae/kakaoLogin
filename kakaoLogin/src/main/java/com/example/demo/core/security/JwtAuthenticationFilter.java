package com.example.demo.core.security;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.core.error.exception.Exception401;
import com.example.demo.user.StringArrayConverter;
import com.example.demo.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager){
        super(authenticationManager);
    }

    // ** Http 요청이 발생할 때마다 호출되는 메서드
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String prefixJwt = request.getHeader(JwtTokenProvider.HEADER);

        // ** 헤더가 없다면 이 메소드에서 더 할 일은 없음, 다음으로 넘김.
        if (prefixJwt == null){
            chain.doFilter(request, response);
            return;
        }

        // ** Bearer 제거
        String jwt = prefixJwt.replace(JwtTokenProvider.TOKEN_PREFIX, "");
        try {
            log.debug("토큰 있음.");

            // ** 토큰 검증
            DecodedJWT decodedJWT = JwtTokenProvider.verify(jwt);

            if (Blacklist.isTokenBlacklisted(jwt)){
                throw new Exception401("사용불가능한 토큰입니다.");
            }

            // ** 사용자 정보 추출
            Long id = decodedJWT.getClaim("id").asLong();
            String roles = decodedJWT.getClaim("roles").asString();

            // ** 권한 정보를 문자열 리스트로 변환
            StringArrayConverter stringArrayConverter = new StringArrayConverter();
            List<String> rolesList = stringArrayConverter.convertToEntityAttribute(roles);

            // ** 추출한 정보로 User를 생성
            User user = User.builder()
                    .id(id)
                    .roles(rolesList)
                    .build();
            CustomUserDetails customUserDetails = new CustomUserDetails(user);

            // ** Spring Security / 인증 정보를 관리하는데 사용
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    customUserDetails.getPassword(),
                    customUserDetails.getAuthorities()
            );

            // ** SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("인증 객체 생성");
        }
        catch (SignatureVerificationException sve) {
            log.debug("토큰 검증 실패");
        } catch (TokenExpiredException tee){
            log.debug("토큰 사용 만료");
        } finally {
            // ** 필터로 응답을 넘긴다.
            chain.doFilter(request, response);
        }
    }
}
