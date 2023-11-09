package com.example.demo.user;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collections;

public class UserRequest {

    @Setter
    @Getter
    public static class JoinDto {

        @NotEmpty
        @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "유효한 이메일 주소를 입력해주세요.")
        private String email;

        @NotEmpty(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, max = 20, message = "8자 이상 20자 이내로 작성 가능합니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!@#$%^&*()_~+=\\[\\]|\\\\;:'\"<>,.?/-])[A-Za-z\\d@#$%^&+=!@#$%^&*()_~+=\\[\\]|\\\\;:'\"<>,.?/-]{8,20}$", message = "영문자, 숫자, 특수문자를 혼합하여 입력해주세요.")
        private String password;

        @NotEmpty
        private String name;

        @NotEmpty
        @Pattern(regexp = "^[0-9]{10,11}$", message = "휴대폰 번호는 숫자 10~11자리만 가능합니다.")
        private String phoneNumber;

        @NotEmpty
        private String access_token;

        @NotEmpty
        private String refresh_token;

        @NotEmpty
        private String platform;

        public JoinDto() {}

        public JoinDto(User user){
            this.email = user.getEmail();
            this.password = user.getEmail();
            this.name = user.getEmail();
            this.phoneNumber = user.getEmail();
            this.access_token = user.getEmail();
            this.refresh_token = user.getEmail();
            this.platform = user.getEmail();
        }

        public User toEntity(){
            return User.builder()
                    .email(email)
                    .password(password)
                    .name(name)
                    .phoneNumber(phoneNumber)
                    .roles(Collections.singletonList("ROLE_USER"))
                    .access_token(access_token)
                    .refresh_token(refresh_token)
                    .platform(platform)
                    .build();
        }
    }
}
