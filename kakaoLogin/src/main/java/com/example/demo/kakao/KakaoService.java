package com.example.demo.kakao;

import com.example.demo.core.error.exception.Exception400;
import com.example.demo.core.error.exception.Exception500;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Transactional(readOnly = true) // 데이터 안정성을 위해 넣음
@RequiredArgsConstructor // 생성자
@Service // service로 인식시켜 줌
public class KakaoService {

    private final String restApi = "f12393a3d014f5b41c1891bca7f2c800";
    private final String redirectUri1 = "http://localhost:8080/kakao/callback";
    private final String adminKey = "c1c3d919965c4c45df1da058b54a53f4";

    private final HttpClient client = HttpClientBuilder.create().build();
    private HttpPost post = null;
    private HttpGet get = null;

    public String kakaoConnect(){
        StringBuffer url = new StringBuffer();
        url.append("https://kauth.kakao.com/oauth/authorize?");
        url.append("client_id=").append(restApi);
        url.append("&redirect_uri=").append(redirectUri1);
        url.append("&response_type=" + "code");
        System.out.println("\nSending 'GET' request to URL : " + "https://kauth.kakao.com/oauth/authorize");

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
    public void kakaoLogin(String code,HttpSession session){
        try {
            // 인가코드 출력

            System.out.println("\nkakao code:" + code);
            // 인카코드에 있는 토큰을 추출
            JsonNode token = getKakaoAccessToken(code);
            JsonNode access_token = token.get("access_token");
            session.setAttribute("access_token", access_token.asText());
            // 토큰에서 접근 토큰 획득 및 출력
            System.out.println("access_token:" + access_token.asText());

            // 로그인한 클라이언트의 사용자 정보를 json 타입으로 획득
            JsonNode userInfo = getKakaoUserInfo(access_token);

            // get vs. path
            // get : 값이 없어도 추출하려함 (값이 null이면 오류 발생!)
            // path : 값이 없으면 추출 안 하고 null 리턴

            // 사용자 정보에서 properties 값 추출 (이름, 프로필, ...)
            JsonNode properties = userInfo.path("properties");
            // properties에서 id, nickname 출력
            System.out.println("id : " + userInfo.get("id").asText());
            System.out.println("name : " + properties.path("nickname").asText());
        }
        catch (Exception500 e){
            throw new Exception500("서버 오류");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public JsonNode getKakaoAccessToken(String code) {
        // 요청 보낼 링크(토큰 얻기)
        final String RequestUrl = "https://kauth.kakao.com/oauth/token";

        // 매개변수와 값의 리스트
        final List<NameValuePair> postParams = new ArrayList<>();
        // 매개변수와 값 추가
        postParams.add(new BasicNameValuePair("grant_type", "authorization_code")); // 인증 타입 (고정값임)
        postParams.add(new BasicNameValuePair("client_id", restApi)); // REST API KEY
        postParams.add(new BasicNameValuePair("redirect_uri", redirectUri1)); // 리다이렉트 URI
        postParams.add(new BasicNameValuePair("code", code)); // 인가 코드

        try {
            // 위에서 설정한 매개변수와 값 리스트로 post 요청 객체 완성
            post = new HttpPost(RequestUrl);
            post.setEntity(new UrlEncodedFormEntity(postParams));

            // 클라이언트(나)가 링크로 post 요청 보냄 -> 그 응답 넣음
            final HttpResponse response = client.execute(post);

            // post 보낸 링크, 요청의 매개변수들, 요청에 의한 응답 코드
            System.out.println("\nSending 'POST' request to URL : " + RequestUrl);
            System.out.println("Post parameters : " + postParams);
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            return jsonResponse(response);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JsonNode getKakaoUserInfo(JsonNode accessToken) {

        final String requestUrl = "https://kapi.kakao.com/v2/user/me";

        try {
            post = new HttpPost(requestUrl);
            post.addHeader("Authorization", "Bearer " + accessToken);

            final HttpResponse response = client.execute(post);

            System.out.println("\nSending 'POST' request to URL : " + requestUrl);
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
            System.out.println("Response Code : " + response.getStatusLine().getReasonPhrase());

            return jsonResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void kakaoLogout(HttpSession session){
        final String RequestUrl = "https://kapi.kakao.com/v1/user/logout";
        String access_token = (String) session.getAttribute("access_token");

        try{
            post = new HttpPost(RequestUrl);
            post.addHeader("Authorization", "Bearer " + access_token);

            final HttpResponse response = client.execute(post);
            JsonNode returnNode = jsonResponse(response);

            System.out.println("\nSending 'POST' request to URL : " + RequestUrl);
            System.out.println("id : " + returnNode.get("id").asText());
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
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
            final String redirectUri2 = "http://localhost:8080/kakao/oauth";
            url.append("&logout_redirect_uri=").append(redirectUri2);

            System.out.println("\nSending 'GET' request to URL : " + "https://kauth.kakao.com/oauth/logout");
            System.out.println("카카오톡에서 로그아웃 됨");

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
        final String RequestUrl = "https://kapi.kakao.com/v1/user/unlink";
        String access_token = (String) session.getAttribute("access_token");

        try{
            post = new HttpPost(RequestUrl);
            post.addHeader("Authorization", "Bearer " + access_token);

            final HttpResponse response = client.execute(post);
            final int responseCode = response.getStatusLine().getStatusCode();

            JsonNode returnNode = jsonResponse(response);

            System.out.println("\nSending 'POST' request to URL : " + RequestUrl);
            System.out.println("id : " + returnNode.get("id").asText());
            System.out.println("Response Code : " + responseCode);
        }
        catch (Exception400 e){
            throw new Exception400("연결 해제 도중 오류 발생");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void kakaoUserList(){
        final String RequestUrl = "https://kapi.kakao.com/v1/user/ids";

        try{
            get = new HttpGet(RequestUrl);
            get.addHeader("Authorization", "KakaoAK " + adminKey);
            get.addHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            final HttpResponse response = client.execute(get);
            JsonNode returnNode = jsonResponse(response);

            System.out.println("\nSending 'POST' request to URL : " + RequestUrl);
            System.out.print("id : ");
            for (JsonNode id : returnNode.get("elements")){
                System.out.print(id.asText() + " ");
            }
            System.out.println("\nResponse Code : " + response.getStatusLine().getStatusCode());
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


}
