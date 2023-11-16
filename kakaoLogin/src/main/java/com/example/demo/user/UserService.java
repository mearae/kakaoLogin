package com.example.demo.user;

import com.example.demo.core.error.exception.Exception400;
import com.example.demo.core.error.exception.Exception401;
import com.example.demo.core.error.exception.Exception500;
import com.example.demo.core.security.CustomUserDetails;
import com.example.demo.core.security.JwtTokenProvider;
import com.example.demo.core.utils.ApiUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

            // 혹시 모르는 경우의 수!!!
            User user = customUserDetails.getUser();
            user.setAccess_token(access_token);
            user.setRefresh_token(refreshToken);
            userRepository.save(user);

            return prefixJwt;
        }catch (Exception e){
            throw new Exception401("인증되지 않음.");
        }
    }

//    public void login(UserRequest.JoinDto joinDto, HttpSession session) {
//        try {
//            final String oauthUrl = "http://localhost:8080/user/oauth";
//            String requestBody = "{\"email\": \"" + joinDto.getEmail() + "\", " +
//                    "\"password\": \"" + joinDto.getPassword() + "\", " +
//                    "\"name\": \"" + joinDto.getName() + "\", " +
//                    "\"phoneNumber\": \"" + joinDto.getPhoneNumber() + "\", " +
//                    "\"access_token\": \"" + joinDto.getAccess_token() + "\", " +
//                    "\"refresh_token\": \"" + joinDto.getRefresh_token() + "\", " +
//                    "\"platform\": \"" + joinDto.getPlatform() + "\"}";
//
//            final HttpResponse response = userPost(oauthUrl, null, requestBody);
//
//            final String infoUrl = "http://localhost:8080/user/user_info";
//            String access_token = response.getFirstHeader(JwtTokenProvider.HEADER).getValue();
//            session.setAttribute("access_token", access_token);
//            session.setAttribute("platform", "user");
//            userPost(infoUrl, access_token, null);
//        } catch (Exception e){
//            throw new Exception500(e.getMessage());
//        }
//    }

    public void login(UserRequest.JoinDto joinDto, HttpSession session) {
        try {
            final String oauthUrl = "http://localhost:8080/user/oauth";

            final ResponseEntity<?> response = testPost(oauthUrl, null, joinDto);

            System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaa");
            final String infoUrl = "http://localhost:8080/user/user_info";
            String access_token = response.getHeaders().getFirst(JwtTokenProvider.HEADER);
            System.out.println(access_token);
            session.setAttribute("access_token", access_token);
            session.setAttribute("platform", "user");
            testPost(infoUrl, access_token, null);
        } catch (Exception e){
            System.out.println(e.getMessage());
            throw new Exception500(e.getMessage());
        }
    }

    public void checkEmail(String email){
        Optional<User> users = userRepository.findByEmail(email);
        if (users.isPresent()){
            throw new Exception400("이미 존재하는 이메일입니다. : " + email);
        }
    }

    public User getUserInfo(int id){
        try {
            return userRepository.findById(id).orElseThrow(
                    () -> new Exception401("인증되지 않았습니다.")
            );
        } catch (Exception e){
            throw new Exception500("현재 로그인된 user의 정보 없음");
        }
    }

    @Transactional
    public String logout(HttpSession session){
        String access_token = (String) session.getAttribute("access_token");
        if (session.getAttribute("platform").equals("kakao")){
            return "http://localhost:8080/kakao/logout";
        } else {
            final String infoUrl = "http://localhost:8080/user/user_info";
            ResponseEntity<?> response = testPost(infoUrl, access_token, null);

            System.out.println("1번 : " + response.getBody());
            //System.out.println(jsonResponse(response).asText());
//            user.setAccess_token(null);
//            user.setRefresh_token(null);
//            userRepository.save(user);
            session.removeAttribute("access_token");
            session.removeAttribute("platform");
            session.invalidate();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            JwtTokenProvider.invalidateToken(authentication);
        }
        return "index.html";
    }

    public void findAll() {
        List<User> all = userRepository.findAll();

        for (User user : all){
            user.output();
            System.out.println();
        }
    }

    public JsonNode isAccessed(HttpSession session) {
        return (JsonNode) session.getAttribute("access_token");
    }

    public HttpResponse userPost(String requestUrl, String authorization, String body){
        try {
            final HttpClient client = HttpClientBuilder.create().build();

            // 위에서 설정한 매개변수와 값 리스트로 post 요청 객체 완성
            HttpPost post = new HttpPost(requestUrl);

            if (authorization != null)
                post.addHeader("Authorization", authorization);
            if (body != null && !body.isEmpty()) {
                StringEntity entity = new StringEntity(body, ContentType.APPLICATION_JSON);
                post.setEntity(entity);
            }
            // 클라이언트(나)가 링크로 post 요청 보냄 -> 그 응답 넣음
            return client.execute(post);
        }catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    public <T> ResponseEntity<ApiUtils.ApiResult<T>> testPost(String requestUrl, String authorization, UserRequest.JoinDto joinDto, Class<T> responseType){
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        if (authorization != null)
            headers.set("Authorization", authorization);
        HttpEntity<UserRequest.JoinDto> requestEntity = new HttpEntity<>(joinDto, headers);

        return restTemplate.postForEntity(requestUrl, requestEntity, (Class<ApiUtils.ApiResult<T>>) responseType);
    }

    public HttpResponse userGet(String requestUrl, String authorization, String content_type){
        try {
            final HttpClient client = HttpClientBuilder.create().build();

            // 위에서 설정한 매개변수와 값 리스트로 get 요청 객체 완성
            HttpGet get = new HttpGet(requestUrl);
            if (authorization != null)
                get.addHeader("Authorization", authorization);
            if (content_type != null)
                get.addHeader("Content-type", content_type);
            // 클라이언트(나)가 링크로 get 요청 보냄 -> 그 응답 넣음
            return client.execute(get);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //// 바꿀 곳 !!!!!!!!!!!!!!!!!!!!!!
    public JsonNode jsonResponse(HttpResponse response) {
        try {
            JsonNode returnNode = null;
            ObjectMapper mapper = new ObjectMapper();
            returnNode = mapper.readTree(response.getEntity().getContent());

            return returnNode;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
