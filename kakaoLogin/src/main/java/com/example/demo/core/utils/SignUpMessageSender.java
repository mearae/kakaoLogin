package com.example.demo.core.utils;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SignUpMessageSender {

    // ** 인증 헤더에 사용될 토큰의 접두어 ("Bearer ")
    public static final String API_KEY = "내 nurigo api key";

    // ** 인증 헤더의 이름을 "Authorization"으로 설정.
    public static final String APISECRETKEY = "내 nurigo api secret key";

    // ** 토큰의 서명을 생성하고 검증할 때 사용하는 비밀 키
    // ** nurigo -> 좌측 메뉴 -> 아래쪽 SDK 다운로드 -> Java용 SDK -> Github Repo -> gradle-spring-demo/src/main/java/net/nurigo/gradlespringdemo/ExampleController.java
    private static final String DOMAIN = "https://api.solapi.com";


    private static DefaultMessageService messageService = null;

    @Bean
    public void initialize() {
        // ** 반드시 계정 내 등록된 유효한 API 키, API Secret Key를 입력해주셔야 합니다!
        /* this.messageService = NurigoApp.INSTANCE.initialize(
                "INSERT_API_KEY",               apiKey
                "INSERT_API_SECRET_KEY",        apiSecretKey
                "https://api.coolsms.co.kr");   domain
         */
        messageService = NurigoApp.INSTANCE.initialize(API_KEY, APISECRETKEY, DOMAIN);
    }

    public static void sendMessage(String fromPhoneNumber, String toPhoneNumber, String text) {
        Message message = new Message();

        message.setFrom(fromPhoneNumber);
        message.setTo(toPhoneNumber);
        message.setText(text);

        SingleMessageSentResponse response = messageService.sendOne(new SingleMessageSendingRequest(message));
        System.out.println(response);
    }
}
