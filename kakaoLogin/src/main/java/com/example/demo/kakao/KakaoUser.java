package com.example.demo.kakao;

import com.example.demo.user.StringArrayConverter;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "kakao_tb")
public class KakaoUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 12, nullable = false)
    private Long kakaoID;

    @Column(length = 30)
    @Convert(converter = StringArrayConverter.class)
    private List<String> roles = new ArrayList<>();

    @Builder
    public KakaoUser(int id, Long kakaoId, List<String> roles) {
        this.id = id;
        this.kakaoID = kakaoId;
        this.roles = roles;
    }

    public void output(){
        System.out.println(id);
        System.out.println(kakaoID);
        System.out.println(roles);
    }
}
