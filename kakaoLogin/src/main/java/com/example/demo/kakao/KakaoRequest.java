package com.example.demo.kakao;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.Collections;

public class KakaoRequest {

    @Setter
    @Getter
    public static class JoinDto {

        @NotEmpty
        private Long kakaoID;

        public KakaoUser toEntity(){
            return KakaoUser.builder()
                    .kakaoId(kakaoID)
                    .roles(Collections.singletonList("ROLE_USER"))
                    .build();
        }
    }
}
