package com.example.demo.user;

import com.example.demo.core.security.CustomUserDetails;
import com.example.demo.core.security.JwtTokenProvider;
import com.example.demo.core.utils.ApiUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    @PostMapping("/user/join")
    public ResponseEntity<?> join(@RequestBody @Valid UserRequest.JoinDto joinDto, Error error){
        userService.join(joinDto);

        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @PostMapping("/user/check")
    public ResponseEntity<?> check(@RequestBody @Valid UserRequest.JoinDto joinDto, Error error){
        userService.checkEmail(joinDto.getEmail());

        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @PostMapping("/user/oauth")
    public ResponseEntity<?> connect(@RequestBody @Valid UserRequest.JoinDto joinDto, Error error){
        String jwt = userService.connect(joinDto);

        return ResponseEntity.ok().header(JwtTokenProvider.HEADER, jwt)
                .body(ApiUtils.success(null));
    }

    @PostMapping("/user/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserRequest.JoinDto joinDto, Error error){
        userService.login(joinDto);
        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @GetMapping("/user/logout")
    public String logout(HttpServletRequest req, Error error){
        return userService.logout();
    }

    @GetMapping("/users")
    public ResponseEntity<?> printUsers(){
        userService.findAll();

        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @GetMapping("/accessed")
    public JsonNode isAccessed(HttpServletRequest req){
        return userService.isAccessed(req.getSession());
    }

//    @PostMapping("/user_info")
//    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails customUserDetails){
//        if (customUserDetails.getUser() == null){
//            return ResponseEntity.ok(ApiUtils.error("현재 로그인된 user가 없습니다.", HttpStatus.UNAUTHORIZED));
//        }
//        User user = userService.getUserInfo(customUserDetails.getUser().getId());
//        user.output();
//        return ResponseEntity.ok(ApiUtils.success(user));
//    }

    @GetMapping("/user_infooo")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        System.out.println("0000000000000000000000000");
        if (customUserDetails == null){
            System.out.println("333333333333333333333333");
        }
        if (customUserDetails.getUser() == null){
            System.out.println("11111111111111111111111111");
            return ResponseEntity.ok(ApiUtils.error("현재 로그인된 user가 없습니다.", HttpStatus.UNAUTHORIZED));
        }
        System.out.println("222222222222222222222222222222");
        User user = userService.getUserInfo(customUserDetails.getUser().getId());
        user.output();
        return ResponseEntity.ok(ApiUtils.success(user));
    }
}
