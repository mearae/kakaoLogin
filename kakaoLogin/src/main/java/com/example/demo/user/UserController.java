package com.example.demo.user;

import com.example.demo.core.security.CustomUserDetails;
import com.example.demo.core.security.JwtTokenProvider;
import com.example.demo.core.utils.ApiUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/user/user_info")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        User user = customUserDetails.getUser();
        if (user == null){
            System.out.println("값 없음");
        } else {
            System.out.println(user.getId());
            System.out.println(user.getEmail());
            System.out.println(user.getPassword());
            System.out.println(user.getAccess_token());
            System.out.println(user.getRefresh_token());
        }
        return ResponseEntity.ok(ApiUtils.success(null));
    }
}
