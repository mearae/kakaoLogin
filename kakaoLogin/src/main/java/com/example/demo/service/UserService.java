package com.example.demo.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.DTO.UserRequest;
import com.example.demo.core.error.exception.Exception400;
import com.example.demo.core.error.exception.Exception401;
import com.example.demo.core.error.exception.Exception500;
import com.example.demo.core.security.CustomUserDetails;
import com.example.demo.core.security.JwtTokenProvider;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
// 메서드나 클래스에 적용가능.
// Transactional
// 어노테이션이 적용된 메서드가 호출되면, 새로운 트랜잭션이 시작됨.
// 메서드 실행이 성공적으로 완료되면, 트랜잭션은 자동으로 커밋.
// 메서드 실행 중에 예외가 발생하면, 트랜잭션은 자동으로 롤백.
//
// readOnly = true : 이 설정은 해당 트랜잭션이 데이터를 변경하지 않고 읽기전용으로만 사용이 가능하다는것을 명시적으로 나타냄.
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void join(UserRequest.JoinDto joinDto) {
        // 이미 있는 이메일인지 확인
        checkEmail(joinDto.getEmail());

        String encodedPassword = passwordEncoder.encode(joinDto.getPassword());
        joinDto.setPassword(encodedPassword);

        try {
            userRepository.save(joinDto.toEntity());

            // 자기 전화번호로 회원가입 메세지가 오도록 함
            //SignUpMessageSender.sendMessage("01074517172", joinDto.getPhoneNumber(),"환영합니다. 회원가입이 완료되었습니다.");
        } catch (Exception e) {
            throw new Exception500(e.getMessage());
        }
    }

    @Transactional
    public String connect(UserRequest.JoinDto joinDto) {
        // ** 인증 작업
        try{
            UsernamePasswordAuthenticationToken token
                    = new UsernamePasswordAuthenticationToken(
                    joinDto.getEmail(), joinDto.getPassword());
            Authentication authentication
                    = authenticationManager.authenticate(token);
            // ** 인증 완료 값을 받아온다.
            // 인증키
            CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();

            String prefixJwt = JwtTokenProvider.create(customUserDetails.getUser());
            String access_token = prefixJwt.replace(JwtTokenProvider.TOKEN_PREFIX, "");
            String refreshToken = JwtTokenProvider.createRefresh(customUserDetails.getUser());

            User user = customUserDetails.getUser();
            user.setAccess_token(access_token);
            user.setRefresh_token(refreshToken);
            userRepository.save(user);

            return prefixJwt;
        }catch (Exception e){
            throw new Exception401("인증되지 않음.");
        }
    }

    public void login(UserRequest.JoinDto joinDto, HttpSession session) {
        try {
            final String oauthUrl = "http://localhost:8080/user/oauth";
            ResponseEntity<JsonNode> response = userPost(oauthUrl,null, joinDto);
            String access_token = response.getHeaders().getFirst(JwtTokenProvider.HEADER);
            session.setAttribute("access_token", access_token);
            session.setAttribute("platform", "user");

            getUserInfo(access_token);
        } catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    public void checkEmail(String email){
        Optional<User> users = userRepository.findByEmail(email);
        if (users.isPresent()){
            throw new Exception400("이미 존재하는 이메일입니다. : " + email);
        }
    }

    public User getUserById(Long id){
        try {
            return userRepository.findById(id).orElseThrow(
                    () -> new Exception401("인증되지 않았습니다.")
            );
        } catch (Exception e){
            throw new Exception500("현재 로그인된 user의 정보 없음");
        }
    }

    @Transactional
    public String logout(HttpSession session) {
        String access_token = (String) session.getAttribute("access_token");
        try {
            if (session.getAttribute("platform").equals("kakao")) {
                return "/kakao/logout";
            } else {
                User user = getUserInfo(access_token);
                killToken(user);
                session.invalidate();
            }
            return "/";
        } catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    public void killToken(User user){
        user.setAccess_token(null);
        user.setRefresh_token(null);
        userRepository.save(user);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtTokenProvider.invalidateToken(authentication);
    }

    @Transactional
    public void refresh(String refreshToken, HttpSession session) {
        DecodedJWT decodedJWT = JwtTokenProvider.verify(refreshToken);

        // === Access Token 재발급 === //
        String username = decodedJWT.getSubject();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new Exception500("사용자를 찾을 수 없습니다."));
        if (!user.getRefresh_token().equals(refreshToken)) throw new Exception401("유효하지 않은 Refresh Token 입니다.");
        String new_access_Token = JwtTokenProvider.create(user);
        user.setAccess_token(new_access_Token);
        session.setAttribute("access_token", new_access_Token);

        // === 현재시간과 Refresh Token 만료날짜를 통해 남은 만료기간 계산 === //
        // === Refresh Token 만료시간 계산해 5일 미만일 시 refresh token도 발급 === //
        long endTime = decodedJWT.getClaim("exp").asLong() * 1000;
        long diffDay = (endTime - System.currentTimeMillis()) / 1000 / 60 / 60 / 24;
        if (diffDay < 5) {
            String new_refresh_token = JwtTokenProvider.createRefresh(user);
            user.setRefresh_token(new_refresh_token);
        }

        userRepository.save(user);
    }

    public void findAll() {
        List<User> all = userRepository.findAll();

        for (User user : all){
            user.output();
            System.out.println();
        }
    }

    public String isAccessed(HttpSession session) {
        return (String) session.getAttribute("access_token");
    }

    public <T> ResponseEntity<JsonNode> userPost(String requestUrl, HttpHeaders headers, T body){
        try{
            RestTemplate restTemplate = new RestTemplate();

            HttpEntity<T> requestEntity;
//            if (headers != null)
//                requestEntity = new HttpEntity<>(body, headers);
//            else
//                requestEntity = new HttpEntity<>(body);
            requestEntity = new HttpEntity<>(body, headers);

            return restTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, JsonNode.class);
        } catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    public <T> ResponseEntity<JsonNode> userGet(String requestUrl, HttpHeaders headers, T body){
        try{
            RestTemplate restTemplate = new RestTemplate();

            HttpEntity<T> requestEntity;
            if (headers != null)
                requestEntity = new HttpEntity<>(body, headers);
            else
                requestEntity = new HttpEntity<>(body);

            return restTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, JsonNode.class);
        } catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    public User getUserInfo(String access_token) {
        final String infoUrl = "http://localhost:8080/user/user_info";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", access_token);
        ResponseEntity<JsonNode> rr = userPost(infoUrl, headers, null);
        // asdfasdf도 안 나옴
        System.out.println("asdfasfd : "+rr);
        JsonNode response = rr.getBody().get("response");

        return userRepository.findById(response.get("id").asLong()).get();
    }
}
