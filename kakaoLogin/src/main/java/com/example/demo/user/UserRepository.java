package com.example.demo.user;

import com.example.demo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// ** JpaRepository<User, Integer> : CRUD를 정의하지 않아도 사용할 수 있음
// ** 메소드 추가 정의도 가능
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
