package com.example.demo.user;

import com.example.demo.core.security.CustomUserDetails;
import com.example.demo.core.security.JwtTokenProvider;
import com.example.demo.core.utils.ApiUtils;
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
    public ResponseEntity<Object> join(@RequestBody @Valid UserRequest.JoinDto joinDto, Error error){
        userService.join(joinDto);

        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @PostMapping("/user/check")
    public ResponseEntity<Object> check(@RequestBody @Valid UserRequest.JoinDto joinDto, Error error){
        userService.checkEmail(joinDto.getEmail());

        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @PostMapping("/user/oauth")
    public ResponseEntity<Object> connect(@RequestBody @Valid UserRequest.JoinDto joinDto, Error error){
        String jwt = userService.connect(joinDto);

        User user = new User();

        return ResponseEntity.ok().header(JwtTokenProvider.HEADER, jwt)
                .body(ApiUtils.success(user));
    }

    @PostMapping("/user/login")
    public ResponseEntity<Object> login(@RequestBody @Valid UserRequest.JoinDto joinDto, HttpServletRequest req, Error error){
        userService.login(joinDto, req.getSession());
        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @GetMapping("/user/logout")
    public String logout(HttpServletRequest req, Error error){
        return userService.logout(req.getSession());
    }

    @GetMapping("/user/users")
    public ResponseEntity<Object> printUsers(){
        userService.findAll();

        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @GetMapping("/user/accessed")
    public String isAccessed(HttpServletRequest req){
        String ll = userService.isAccessed(req.getSession());
        return ll;
    }

    @PostMapping("/user/user_info")
    public ResponseEntity<ApiUtils.ApiResult<User>> getCurrentUser(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        if (customUserDetails.getUser() == null){
            return ResponseEntity.ok(ApiUtils.error("현재 로그인된 user가 없습니다.", HttpStatus.UNAUTHORIZED));
        }
        User user = userService.getUserInfo(customUserDetails.getUser().getId());
        user.output();
        return ResponseEntity.ok(ApiUtils.success(user));
    }
}
