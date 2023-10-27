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
    public static JsonNode getKakaoUserInfo(JsonNode accessToken) {
        Logger logger = LoggerFactory.getLogger(KakaoUserInfo.class);

        final String RequestUrl = "https://kapi.kakao.com/v2/user/me";
        final HttpClient client = HttpClientBuilder.create().build();
        final HttpPost post = new HttpPost(RequestUrl);

        // add header
        post.addHeader("Authorization", "Bearer " + accessToken);  //토큰으로 authorization권한 얻는것.

        JsonNode returnNode = null;

        try {
            final HttpResponse response = client.execute(post);
            final int responseCode = response.getStatusLine().getStatusCode();
            final String msg=response.getStatusLine().getReasonPhrase();
            System.out.println("\nSending 'POST' request to URL : " + RequestUrl);
            System.out.println("Response Code : " + responseCode);
            System.out.println("Response Code : " + msg);

            //HttpEntity entity = response.getEntity();  이 주석처리 되어있는 코드들은 혹시 오류가 나는 상황이라면 주석 없애고 실행 ㄱㄱ 무슨 오류인지 알려줄거임.
            //String responseString = EntityUtils.toString(entity, "UTF-8");
            //logger.info("responseString----->"+responseString);
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