package com.example.demo.kakao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KakaoRepository extends JpaRepository<KakaoUser, Integer> {
    Optional<KakaoUser> findByKakaoID(Long kakaoID);
}
