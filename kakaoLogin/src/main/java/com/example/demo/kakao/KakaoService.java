package com.example.demo.kakao;

import com.example.demo.core.error.exception.Exception400;
import com.example.demo.core.error.exception.Exception401;
import com.example.demo.core.error.exception.Exception500;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.example.demo.user.UserRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true) // 데이터 안정성을 위해 넣음
@RequiredArgsConstructor // 생성자
@Service // service로 인식시켜 줌
public class KakaoService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String restApi = "f12393a3d014f5b41c1891bca7f2c800";
    private final String redirectUri1 = "http://localhost:8080/kakao/callback";
    private final String adminKey = "c1c3d919965c4c45df1da058b54a53f4";

    public String kakaoConnect(){
        StringBuffer url = new StringBuffer();
        url.append("https://kauth.kakao.com/oauth/authorize?");
        url.append("client_id=").append(restApi);
        url.append("&redirect_uri=").append(redirectUri1);
        url.append("&response_type=" + "code");

        // https://kauth.kakao.com/oauth/authorize : Get 요청할 링크
        // ? : 뒤에 매개변수를 넣어줌
        // client_id : 클라이언트(고객)의 id (매개변수 이름)
        // = : 대입
        // f12393a3d014f5b41c1891bca7f2c800 : REST API 키 (매개변수 값)
        // & : 그리고
        // redirect_uri : 값을 보낼 url 링크 (매개변수 이름)
        // = : 대입
        // http://localhost:8080/kakao/callback : redirect_uri (kakao delvelopers에 미리 등록)(매개변수 값)
        // & : 그리고
        // response_type : response(결과값) 데이터 타입 (매개변수 이름)
        // = : 대입
        // code : 코드 타입 (매개변수 값)
        return url.toString();
    }

    public String kakaoAutoConnect(){
        StringBuffer url = new StringBuffer();
        url.append(kakaoConnect());
        url.append("&prompt=" + "login");

        return url.toString();
    }

    // kakaoConnect의 결과값(인가코드)가 아래의 매개변수 code로 들어감
    public String kakaoLogin(String code,HttpSession session){
        try {
            // 인카코드에 있는 토큰을 추출
            JsonNode token = getKakaoAccessToken(code);
            JsonNode access_token = token.get("access_token");
            session.setAttribute("access_token", access_token);
            session.setAttribute("platform", "kakao");

            // 로그인한 클라이언트의 사용자 정보를 json 타입으로 획득
            JsonNode userInfo = getKakaoUserInfo(access_token);

            JsonNode kakao_account = userInfo.path("kakao_account");
            if (!checkEmail(kakao_account.path("email").asText())) {
                return "/joinByKakao.html";
            }
        } catch (Exception e){
            throw new Exception401("인증되지 않음.");
        }
        return "/logined.html";
    }

    public boolean checkEmail(String email){
        // 동일한 이메일이 있는지 확인.
        Optional<User> users = userRepository.findByEmail(email);
        return users.isPresent();
    }

    @Transactional
    public void kakaoJoin(HttpSession session) {
        JsonNode access_token = (JsonNode) session.getAttribute("access_token");
        JsonNode userInfo = getKakaoUserInfo(access_token);

        UserRequest.JoinDto joinDto = new UserRequest.JoinDto();
        JsonNode properties = userInfo.path("properties");
        JsonNode kakao_account = userInfo.path("kakao_account");
        String encodedPassword = passwordEncoder.encode(access_token.asText());

        // 지금은 권한이 제한되어 있어 비밀번호는 카카오톡 토큰으로, 전화번호는 임시로 설정
        joinDto.setEmail(kakao_account.path("email").asText());
        joinDto.setPassword(encodedPassword);
        joinDto.setName(properties.path("nickname").asText());
        joinDto.setPhoneNumber("01012341234");
        try {
            userRepository.save(joinDto.toEntity());
        } catch (Exception e) {
            throw new Exception500(e.getMessage());
        }
    }

    public JsonNode getKakaoAccessToken(String code) {
        // 요청 보낼 링크(토큰 얻기)
        final String requestUrl = "https://kauth.kakao.com/oauth/token";

        // 매개변수와 값의 리스트
        final List<NameValuePair> postParams = new ArrayList<>();
        // 매개변수와 값 추가
        postParams.add(new BasicNameValuePair("grant_type", "authorization_code")); // 인증 타입 (고정값임)
        postParams.add(new BasicNameValuePair("client_id", restApi)); // REST API KEY
        postParams.add(new BasicNameValuePair("redirect_uri", redirectUri1)); // 리다이렉트 URI
        postParams.add(new BasicNameValuePair("code", code)); // 인가 코드

        final HttpResponse response = kakaoPost(requestUrl,null,postParams);

        return jsonResponse(response);
    }

    public JsonNode getKakaoUserInfo(JsonNode accessToken) {
        final String requestUrl = "https://kapi.kakao.com/v2/user/me";
        final HttpResponse response = kakaoPost(requestUrl,"Bearer " + accessToken,null);

        return jsonResponse(response);
    }

    public void kakaoLogout(HttpSession session){
        final String requestUrl = "https://kapi.kakao.com/v1/user/logout";
        JsonNode access_token = (JsonNode) session.getAttribute("access_token");

        try{
            kakaoPost(requestUrl,"Bearer " + access_token,null);
            session.removeAttribute("platform");
            session.removeAttribute("access_token");
            session.invalidate();
        }
        catch (Exception400 e){
            throw new Exception400("로그아웃 도중 오류 발생");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public String kakaoFullLogout() {
        try{
            StringBuffer url = new StringBuffer();
            url.append("https://kauth.kakao.com/oauth/logout?");
            url.append("client_id=").append(restApi);
            url.append("&logout_redirect_uri=" + "http://localhost:8080/kakao/oauth");

            return url.toString();
        }
        catch (Exception400 e){
            throw new Exception400("로그아웃 도중 오류 발생");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "/logined.html";
    }

    public void kakaoDisconnect(HttpSession session){
        final String requestUrl = "https://kapi.kakao.com/v1/user/unlink";
        JsonNode access_token = (JsonNode) session.getAttribute("access_token");

        try{
            kakaoPost(requestUrl,"Bearer " + access_token,null);
        }
        catch (Exception400 e){
            throw new Exception400("연결 해제 도중 오류 발생");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void kakaoUserList(){
        final String requestUrl = "https://kapi.kakao.com/v1/user/ids";

        try{
            final HttpResponse response = kakaoGet(requestUrl,"KakaoAK " + adminKey,"application/x-www-form-urlencoded;charset=utf-8");
            JsonNode returnNode = jsonResponse(response);

            System.out.print("id : ");
            for (JsonNode id : returnNode.get("elements")){
                System.out.print(id.asText() + " ");
            }
        }
        catch (Exception400 e){
            throw new Exception400("로그아웃 도중 오류 발생");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

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

    public HttpResponse kakaoPost(String requestUrl, String authorization, List<NameValuePair> body){
        try {
            final HttpClient client = HttpClientBuilder.create().build();

            // 위에서 설정한 매개변수와 값 리스트로 post 요청 객체 완성
            HttpPost post = new HttpPost(requestUrl);
            if (authorization != null)
                post.addHeader("Authorization", authorization);
            if (body != null && !body.isEmpty())
                post.setEntity(new UrlEncodedFormEntity(body));

            // 클라이언트(나)가 링크로 post 요청 보냄 -> 그 응답 넣음
            return client.execute(post);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public HttpResponse kakaoGet(String requestUrl, String authorization, String content_type){
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

    public void endServer(){

        System.exit(0);
    }
}
