package com.example.demo.kakao;


import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@Controller
public class KakaoController {

    private final KakaoService kakaoService;

    // 인가코드 받기 : https://kauth.kakao.com/oauth/authorize 는
    // kakao Developer에 보면 Get으로 연결하라고 함
    @GetMapping(value = "/kakao/oauth")
    public String kakaoConnect(Error error) {

        // "redirect:" -> 뒤에 오는 http 링크(String)로 이동
        // @RestController 일 경우에는 링크 이동이 불가(객체만 return 가능함)
        //              -> 화면에 문자열이 뜸
        // @Controller 일 경우 링크 이동과 객체 return 모두 가능
        return "redirect:" + kakaoService.kakaoConnect();
    }

    //
    @GetMapping(value = "/kakao/callback",produces="application/json")
    public String kakaoLogin(@RequestParam("code")String code,Error error) {
        // 로그인은 크롬 화면에서 하고 여기서 실제로는 토큰, 사용자 정보 얻기를 함
        kakaoService.kakaoLogin(code);

        // 다시 로그인 화면으로 돌아옴
        return "redirect:/index.html";
    }



}