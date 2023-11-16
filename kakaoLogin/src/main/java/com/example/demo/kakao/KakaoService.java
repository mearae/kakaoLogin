package com.example.demo.kakao;

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

    private final String restApi = "내 RestApi";
    private final String adminKey = "내 AdminKey";

    public String kakaoConnect(){
        try {
            StringBuffer url = new StringBuffer();
            url.append("https://kauth.kakao.com/oauth/authorize?");
            url.append("client_id=").append(restApi);
            url.append("&redirect_uri=").append("http://localhost:8080/kakao/callback");
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
        } catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    public String kakaoAutoConnect(){
        try {
            StringBuffer url = new StringBuffer();
            url.append(kakaoConnect());
            url.append("&prompt=" + "login");

            return url.toString();
        } catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    @Transactional
    // kakaoConnect의 결과값(인가코드)가 아래의 매개변수 code로 들어감
    public String kakaoLogin(String code,HttpSession session){
        try {
            // 인카코드에 있는 토큰을 추출
            JsonNode token = getKakaoAccessToken(code);
            String access_token = token.get("access_token").asText();
            // Bearer 넣어야 할지도?
            session.setAttribute("access_token",access_token);
            String refresh_token = token.get("refresh_token").asText();
            session.setAttribute("platform", "kakao");

            // 로그인한 클라이언트의 사용자 정보를 json 타입으로 획득
            JsonNode userInfo = getKakaoUserInfo(access_token);

            JsonNode kakao_account = userInfo.path("kakao_account");
            if (!checkEmail(kakao_account.path("email").asText())) {
                kakaoJoin(userInfo, access_token);
            }
            User user = userRepository.findByEmail(kakao_account.path("email").asText()).orElseThrow(
                    () -> new Exception401("인증되지 않았습니다.")
            );
            user.setAccess_token(access_token);
            user.setRefresh_token(refresh_token);
            userRepository.save(user);
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
    public void kakaoJoin(JsonNode userInfo, String access_token) {
        UserRequest.JoinDto joinDto = new UserRequest.JoinDto();
        // 지금은 권한이 제한되어 있어 비밀번호는 카카오톡 토큰으로, 전화번호는 임시로 설정
        JsonNode kakao_account = userInfo.path("kakao_account");
        joinDto.setEmail(kakao_account.path("email").asText());

        String encodedPassword = passwordEncoder.encode(access_token);
        joinDto.setPassword(encodedPassword);

        JsonNode properties = userInfo.path("properties");
        joinDto.setName(properties.path("nickname").asText());
        joinDto.setPhoneNumber("01012341234");
        joinDto.setPlatform("kakao");
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
        postParams.add(new BasicNameValuePair("redirect_uri", "http://localhost:8080/kakao/callback")); // 리다이렉트 URI
        postParams.add(new BasicNameValuePair("code", code)); // 인가 코드

        final HttpResponse response = kakaoPost(requestUrl,null,postParams);

        return jsonResponse(response);
    }

    public JsonNode getKakaoUserInfo(String accessToken) {
        final String requestUrl = "https://kapi.kakao.com/v2/user/me";
        final HttpResponse response = kakaoPost(requestUrl,"Bearer " + accessToken,null);

        return jsonResponse(response);
    }

    @Transactional
    public void kakaoLogout(HttpSession session){
        final String requestUrl = "https://kapi.kakao.com/v1/user/logout";
        String access_token = (String) session.getAttribute("access_token");

        try{
            String email = getKakaoUserInfo(access_token).path("kakao_account").path("email").asText();
            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new Exception401("로그인된 사용자를 찾을 수 없습니다.")
            );
            System.out.println(user.getAccess_token());
            System.out.println(user.getRefresh_token());
            kakaoPost(requestUrl,"Bearer " + access_token,null);
            user.setAccess_token(null);
            user.setRefresh_token(null);
            userRepository.save(user);
            session.invalidate();
        }
        catch (Exception500 e){
            throw new Exception500("로그아웃 도중 오류 발생");
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
        catch (Exception500 e){
            throw new Exception500("로그아웃 도중 오류 발생");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "/logined.html";
    }

    public void kakaoDisconnect(HttpSession session){
        final String requestUrl = "https://kapi.kakao.com/v1/user/unlink";
        String access_token = (String) session.getAttribute("access_token");

        try{
            kakaoPost(requestUrl,"Bearer " + access_token,null);
        }
        catch (Exception500 e){
            throw new Exception500("연결 해제 도중 오류 발생");
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
        catch (Exception e){
            throw new Exception500(e.getMessage());
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

    public void getTokenInfo(JsonNode access_token) {
        final String requestUrl = "https://kapi.kakao.com/v1/user/access_token_info";

        final HttpResponse response = kakaoGet(requestUrl,"Bearer " + access_token,null);

        JsonNode token_info =  jsonResponse(response);
        System.out.println(token_info.toPrettyString());
    }
}
