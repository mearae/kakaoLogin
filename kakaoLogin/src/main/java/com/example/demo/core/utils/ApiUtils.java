package com.example.demo.core.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.http.HttpStatus;

public class ApiUtils {

/*
    1. Spring은 @PostMapping("/join") 어노테이션이 붙은 컨트롤러 메서드를 찾아서 실행.
    2. @RequestBody와 @Valid 어노테이션을 통해, JSON 요청은 UserRequest.JoinDTO 객체로 변환.
    3. userService.join(requestDTO); 메서드가 호출 (이때 실제 비즈니스 로직을 처리)
    4. return ResponseEntity.ok( ApiUtils.success(null) ); 실행.
    ----------  여기까지 Controller 의 작동 과정 ----------


    ----------  여기서부터 현재 클래스 작동과정 ----------
    5. return ResponseEntity.ok( ApiUtils.success(null) );을 실행 하면서 ApiUtils.success(null) 메서드를 호출하여 ApiResult 객체를 생성.
    6. ApiResult 객체는 ResponseEntity.ok()를 통해 HTTP 200 OK 응답과 함께 ResponseEntity 객체로 래핑.
    7. 이때! Spring은 ApiResult 객체를 JSON으로 변환하기(직렬화 과정) 위해 ObjectMapper 클래스를 사용할 수 있음.
    8. 직렬화된 JSON 데이터는 HTTP 응답 본문에 담겨져 전송됨.
    9. 클라이언트는 HTTP 응답을 받아 본문에 있는 JSON 데이터를 처리. (프론트 작업 수행)
*/

    public static <T> ApiResult<T> success(T response){
        return new ApiResult<T>(true, response, null);
    }

    public static <T> ApiResult<T> error(String message, HttpStatus httpStatus){
        return new ApiResult<T>(false, null, new ApiError(message, httpStatus.value()));
    }

    // ** json으로 반환해야할 데이터
    @AllArgsConstructor @Getter
    public static  class ApiResult<T>{
        private final boolean success; // 현재 상태
        private final T response; // 반환할 실제 데이터
        private final ApiError error;

        public String toString(){
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("success", success)
                    .append("response", response)
                    .append("error", error)
                    .toString();
        }
    }

    @AllArgsConstructor @Getter
    public static class ApiError{
        private final String message;
        private final int status;

        public String toString(){
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("message", message)
                    .append("status", status)
                    .toString();
        }
    }
}
