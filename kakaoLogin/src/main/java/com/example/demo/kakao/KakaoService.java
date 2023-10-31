package com.example.demo.kakao;

import com.example.demo.core.error.exception.Exception400;
import com.example.demo.core.error.exception.Exception401;
import com.example.demo.core.error.exception.Exception500;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Transactional(readOnly = true) // 데이터 안정성을 위해 넣음
@RequiredArgsConstructor // 생성자
@Service // service로 인식시켜 줌
public class KakaoService {

    private String restApi = "f12393a3d014f5b41c1891bca7f2c800";
    private String redirectUri1 = "http://localhost:8080/kakao/callback";
    private String redirectUri2 = "http://localhost:8080/kakao/oauth";

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
        url.append("https://kauth.kakao.com/oauth/authorize?");
        url.append("response_type=" + "code");
        url.append("&client_id=").append(restApi);
        url.append("&redirect_uri=").append(redirectUri1);
        url.append("&prompt=" + "login");
        System.out.println("\nSending 'GET' request to URL : " + "https://kauth.kakao.com/oauth/authorize");
        return url.toString();
    }

    // kakaoConnect의 결과값(인가코드)가 아래의 매개변수 code로 들어감
    public void kakaoLogin(String code,HttpSession session){
        try {
            // 인가코드 출력

            System.out.println("\nkakao code:" + code);
            // 인카코드에 있는 토큰을 추출
            JsonNode access_token = getKakaoAccessToken(code);
            session.setAttribute("access_token",access_token.get("access_token").asText());
            // 토큰에서 접근 토큰 획득 및 출력
            System.out.println("access_token:" + access_token.get("access_token"));

            // 로그인한 클라이언트의 사용자 정보를 json 타입으로 획득
            JsonNode userInfo = KakaoUserInfo.getKakaoUserInfo(access_token.get("access_token"));

            // 사용자 정보에서 id 값을 추출
            String member_id = userInfo.get("id").asText();

            // get vs. path
            // get : 값이 없어도 추출하려함 (값이 null이면 오류 발생!)
            // path : 값이 없으면 추출 안 하고 null 리턴

            // 사용자 정보에서 properties 값 추출 (이름, 프로필, ...)
            JsonNode properties = userInfo.path("properties");
            String member_name = properties.path("nickname").asText();

            System.out.println("id : " + member_id);
            System.out.println("name : " + member_name);
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

        // 클라이언트(나)
        final HttpClient client = HttpClientBuilder.create().build();
        // RequestUrl에 보낼 post 요청
        final HttpPost post = new HttpPost(RequestUrl);

        JsonNode returnNode = null;

        try {
            // 위에서 설정한 매개변수와 값 리스트로 post 요청 객체 완성
            post.setEntity(new UrlEncodedFormEntity(postParams));

            // 클라이언트(나)가 링크로 post 요청 보냄 -> 그 응답 넣음
            final HttpResponse response = client.execute(post);
            // 응답의 상태 코드 추출
            final int responseCode = response.getStatusLine().getStatusCode();

            // post 보낸 링크, 요청의 매개변수들, 요청에 의한 응답 코드
            System.out.println("\nSending 'POST' request to URL : " + RequestUrl);
            System.out.println("Post parameters : " + postParams);
            System.out.println("Response Code : " + responseCode);

            // JSON 형태 반환값 처리
            ObjectMapper mapper = new ObjectMapper();
            returnNode = mapper.readTree(response.getEntity().getContent());

        }
        catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        catch (ClientProtocolException cpe) {
            cpe.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return returnNode;
    }

    public void kakaoLogout(HttpSession session){
        String access_token = (String) session.getAttribute("access_token");

        final String RequestUrl = "https://kapi.kakao.com/v1/user/logout";

        final HttpClient client = HttpClientBuilder.create().build();
        final HttpPost post = new HttpPost(RequestUrl);

        try{
            post.addHeader("Authorization","Bearer " + access_token);

            final HttpResponse response = client.execute(post);
            final int responseCode = response.getStatusLine().getStatusCode();

            JsonNode returnNode = null;
            ObjectMapper mapper = new ObjectMapper();
            returnNode = mapper.readTree(response.getEntity().getContent());

            System.out.println("\nSending 'POST' request to URL : " + RequestUrl);
            System.out.println("id : " + returnNode.get("id").asText());
            System.out.println("Response Code : " + responseCode);
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
            url.append("&logout_redirect_uri=").append(redirectUri2);

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
        String access_token = (String) session.getAttribute("access_token");

        final String RequestUrl = "https://kapi.kakao.com/v1/user/unlink";

        final HttpClient client = HttpClientBuilder.create().build();
        final HttpPost post = new HttpPost(RequestUrl);

        try{
            post.addHeader("Authorization","Bearer " + access_token);

            final HttpResponse response = client.execute(post);
            final int responseCode = response.getStatusLine().getStatusCode();

            JsonNode returnNode = null;
            ObjectMapper mapper = new ObjectMapper();
            returnNode = mapper.readTree(response.getEntity().getContent());

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

}
