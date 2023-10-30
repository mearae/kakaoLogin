package com.example.demo.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class KakaoUserInfo {
    // 토큰으로 로그인한 사용자의 정보 추출
    public static JsonNode getKakaoUserInfo(JsonNode accessToken) {
        // 카카오톡 사용자 정보를 log파일에 저장한다고 생각
        Logger logger = LoggerFactory.getLogger(KakaoUserInfo.class);

        // 요청 보낼 링크
        final String RequestUrl = "https://kapi.kakao.com/v2/user/me";
        // 클라이언트(나)
        final HttpClient client = HttpClientBuilder.create().build();
        // RequestUrl에 보낼 post 요청
        final HttpPost post = new HttpPost(RequestUrl);

        // post 요청에 헤더 추가 -> 토큰으로 authorization 권한 얻는 것.
        post.addHeader("Authorization", "Bearer " + accessToken);

        JsonNode returnNode = null;

        try {
            // 클라이언트(나)가 링크로 post 요청 보냄 -> 그 응답 넣음
            final HttpResponse response = client.execute(post);
            // 응답의 상태 코드 추출 (숫자 코드, 문자 코드)
            final int responseCode = response.getStatusLine().getStatusCode();
            final String msg = response.getStatusLine().getReasonPhrase();
            // post 보낸 링크, 응답의 상태 코드 출력
            System.out.println("\nSending 'POST' request to URL : " + RequestUrl);
            System.out.println("Response Code : " + responseCode);
            System.out.println("Response Code : " + msg);

            // JSON 형태 반환값 처리
            ObjectMapper mapper = new ObjectMapper();
            returnNode = mapper.readTree(response.getEntity().getContent());

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnNode;
    }
}