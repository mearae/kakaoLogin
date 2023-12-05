# kakaoLogin

## ※ 개발환경
IDE: IntelliJ IDEA Community
Gradle - Groovy, Java 2.7.17
Jar 11
Spring Boot 2.7.6
jvm.convert 3.3.2
JDK 11
mysql 8.0.35
Lombok
Spring Web
Spring Data JPA

## ※ 회원가입 및 로그인 주요기능    
### 일반 회원 [PostmanTest](https://github.com/mearae/PostmanTest)
1. 회원가입(/user/join) <br>
  \- 이메일, 비밀번호, 이름, 전화번호

2. 이메일 중복 확인(/user/check)

3. 로그인(/user/login)

4. 로그인 인증토큰 발급(/user/oauth)

5. 로그아웃(/user/logout) <br>
  \- 모든 플랫폼에서 로그아웃 가능

6. 가입된 회원들 출력(/user/users) <br>
  \- 프로그램 정상 실행 확인용

7. 로그인한 회원 자신의 id(/user/user_id) <br>
  \- 프로그램 내에서만 사용(직접 사용 불가)

8. 토큰 갱신(/user/refresh)

9. 로그인한 회원 자신의 id 전송(/user/send_user_id) <br>
  \- 프로그램 내에서만 사용(직접 사용 불가)

### 카카오톡 회원
1. 카카오톡로 회원가입 및 로그인(/kakao/callback)

2. 카카오톡 인증코드 발급(/kakao/oauth)

3. 카카오톡로 다시 로그인(/kakao/relogin) <br>
  \- 프로그램 정상 실행 확인용

4. 카카오톡로 로그아웃(/kakao/logout) <br>
  \- 카카오톡은 로그아웃 안 됨

5. 카카오톡도 함께 로그아웃(/kakao/fulllogout)

6. 카카오톡과 웹 연결 끊기(/kakao/disconnect)

7. 카카오톡 회원들 출력(/kakao/userlist) <br>
  \- 프로그램 정상 실행 확인용

8. 서버 종료(/kakao/end) <br>
  \- 프로그램 정상 실행 확인용

#### v2.0.0 (2023.10.31)
1. PostmanTest과 취합

#### v1.3.0 (2023.10.30)
1. [추가] 카카오톡 회원들 출력(/kakao/userlist) <br>
  \- 프로그램 정상 실행 확인용
2. 화면 디자인 수정

#### v1.2.0 (2023.10.27)
1. [추가] 기존 로그인 여부와 상관없이 카카오톡으로 로그인하기(/kakao/relogin) <br>
  \- 프로그램 정상 실행 확인 및 실험
2. [추가] 로그아웃(/kakao/logout)
3. [추가] 카카오톡도 함께 로그아웃(/kakao/fulllogout)
4. [추가] 카카오톡과 웹 연결 끊기(/kakao/disconnect) <br>
  \- 계정 탈퇴 <br>
  \- 회원가입 시 나오는 동의 화면이 다시 나옴
5. 로그인 화면과 로그인 후 화면으로 분리 <br>
  \- 로그인 화면 : '카카오로 로그인하기', '카카오로 다시 로그인하기' <br>
  \- 로그인 후 화면 : '로그아웃하기', '카카오 로그아웃하기', '카카오와 연결 끊기'

#### v1.0.0 (2023.10.27)
1. [추가] 카카오톡 인가코드 발급(/kakao/oauth) <br>
  \- 인가코드를 '/kakao/callback'으로 보냄
2. [추가] 카카오톡으로 로그인(/kakao/callback) <br>
  \- 받은 인가코드로 인증 토큰을 얻고 사용자 정보를 출력
3. '카카오로 로그인하기' 버튼과 '로그아웃' 버튼이 있는 화면
