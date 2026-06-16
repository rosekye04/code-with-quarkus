package org.acme.login;

import jakarta.inject.Inject;
import io.vertx.ext.web.RoutingContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.io.InputStream;
import jakarta.transaction.Transactional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import java.nio.file.Paths;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/")
public class AuthResource {

    @Inject
    RoutingContext context;

    // 1. 로그인 페이지 호출
    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public Response loginPage() {
        InputStream html = getClass().getClassLoader().getResourceAsStream("META-INF/resources/login/login.html");
        if (html == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("파일 없음").build();
        }
        return Response.ok(html).build();
    }

    // 2. 로그인 처리
    @POST
    @Path("/login_check")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response loginCheck(@FormParam("username") String username, 
                               @FormParam("password") String password) {
        // 1. DB에서 해당 아이디를 가진 사용자 찾기
        User user = User.find("username", username).firstResult();
                
        System.out.println("조회된 유저 정보: " + user);
        
        if (user == null) {
            System.out.println("알림: 사용자를 찾을 수 없습니다.");
            return Response.seeOther(URI.create("/login?error=1")).build();
        }

        // 2. 입력받은 비밀번호를 해싱하고 소문자로 변환
        String hashedInput = hash(password).toLowerCase();
        
        // 서버 콘솔에서 확인 가능한 로그
        System.out.println("로그인 시도 아이디 : " + username);
        System.out.println("입력값 해시 : " + hashedInput);
        if (user != null) {
            System.out.println("DB 저장된 비밀번호: " + user.password.toLowerCase());
        } else {
            System.out.println("사용자를 찾을 수 없음");
        }
        
        // 3. 존재 여부와 비밀번호 일치 확인
        if (user != null && hashedInput.equals(user.password.toLowerCase())) {
            context.session().put("loginUser", username);
            return Response.seeOther(URI.create("/after_login")).build();
        } else {
            return Response.seeOther(URI.create("/login?error=1")).build();
        }
    }

    // 3. 로그인 후 페이지
    @GET
    @Path("/after_login")
    @Produces(MediaType.TEXT_HTML)
    public Response afterLogin() {
        String loginUser = context.session().get("loginUser");
        if (loginUser == null) {
            return Response.seeOther(URI.create("/login")).build();
        }
        InputStream html = getClass().getClassLoader().getResourceAsStream("META-INF/resources/login/main_after_login.html");
        return Response.ok(html).build();
    }

    // 4. 로그아웃
    @GET
    @Path("/logout")
    public Response logout() {
        context.session().destroy();
        return Response.seeOther(URI.create("/")).build();
    }

    // 5. 회원가입 페이지 호출
    @GET
    @Path("/register")
    @Produces(MediaType.TEXT_HTML)
    public Response registerPage() {
        InputStream html = getClass().getClassLoader().getResourceAsStream("META-INF/resources/login/register.html");
        if (html == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(html).build();
    }

    // 6. 회원가입 처리 (POST)
    @POST
    @Path("/register_check")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response registerCheck( 
        @FormParam("username") String username,
        @FormParam("password") String password, 
        @FormParam("email") String email,
        @FormParam("phone") String phone) {
        
        // 1. 아이디 중복 체크
        if (User.findByUsername(username) != null) {
            return Response.seeOther(URI.create("/register?error=duplicate_username")).build();
        }
        // 2. 이메일 중복 체크
        if (User.findByEmail(email) != null) {
            return Response.seeOther(URI.create("/register?error=duplicate_email")).build();
        }
        // 3. DB 삽입
        User newUser = new User();
        newUser.username = username;
        newUser.password = hash(password).toLowerCase(); 
        newUser.email = email;
        newUser.phone = phone;
        newUser.persist();

        // 4. 가입 완료 페이지 이동
        return Response.seeOther(URI.create("/register_success")).build();
    }

    @GET
    @Path("/register_success")
    @Produces(MediaType.TEXT_HTML)
    public Response registerSuccess() {
        InputStream html = getClass().getClassLoader().getResourceAsStream("META-INF/resources/login/register_success.html");
        if (html == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("가입 완료 페이지를 찾을 수 없습니다.").build();
        }
        return Response.ok(html).build();
    }

    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            BigInteger number = new BigInteger(1, hash);
            StringBuilder sb = new StringBuilder(number.toString(16));
            while (sb.length() < 64) {
                sb.insert(0, '0');
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // 세션 유무에 따라 메인 페이지 분기
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response mainPage() {
        String loginUser = context.session().get("loginUser");

        System.out.println("=== [GET /] 세션 ID : " + context.session().id());
        System.out.println("=== [GET /] loginUser : " + loginUser);

        String htmlPath = (loginUser != null)
            ? "META-INF/resources/login/main_after_login.html"
            : "META-INF/resources/main_index.html";

        InputStream html = getClass().getClassLoader().getResourceAsStream(htmlPath);

        if (html == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("메인 페이지를 찾을 수 없습니다.").build();
        }
        return Response.ok(html).build();
    }

    @GET
    @Path("/profile")
    @Produces(MediaType.TEXT_HTML)
    public Response profilePage() {
        // 1. 세션 체크 (로그인 안한 사용자 차단)
        String loginUser = context.session().get("loginUser");
        if (loginUser == null) {
            return Response.seeOther(URI.create("/login")).build();
        }
        
        // 2. DB에서 사용자 정보 조회
        User user = User.findByUsername(loginUser);

        if (user == null) {
            context.session().destroy();
            return Response.seeOther(URI.create("/login?error=user_not_found")).build();
        }
        
        // 3. 세션에 사용자 정보 저장(HTML에 활용)
        context.session().put("userEmail", user.email != null ? user.email : "");
        context.session().put("userPhone", user.phone != null ? user.phone : "");
        
        String currentProfileImage = (user.profileImage != null && !user.profileImage.isEmpty())
            ? user.profileImage
            : "default.png";
        context.session().put("profileImage", currentProfileImage);

        // 4. 프로필 페이지 반환
        InputStream html = getClass().getClassLoader().getResourceAsStream("META-INF/resources/login/profile.html");
        if (html == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("프로필 페이지를 찾을 수 없습니다.").build();
        }
        return Response.ok(html).build();
    }

    @GET
    @Path("/profile/info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response profileInfo() {
        // 세션 체크
        String loginUser = context.session().get("loginUser");
        if (loginUser == null) {
            return Response.status(401).build();
        }
        // DB조회
        User user = User.findByUsername(loginUser);
        // JSON 응답
        return Response.ok(
            Map.of(
                "username", user.username,
                "email", user.email != null ? user.email : "",
                "phone", user.phone != null ? user.phone : "",
                "profileImage", user.profileImage != null ? user.profileImage : ""
            )
        ).build();
    }

    @POST
    @Path("/profile/upload")
    @Transactional
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response profileUpload(@RestForm("profileImage") FileUpload file) {

        // 1. 세션 체크
        String loginUser = context.session().get("loginUser");
        if (loginUser == null) {
            return Response.seeOther(URI.create("/login")).build();
        }

        try {
            // 2. 확장자 검사
            String original = file.fileName();
            String ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
                    
            if (!ext.matches("jpg|jpeg|png|gif|webp")) {
                return Response.seeOther(URI.create("/profile?error=invalid_type")).build();
            } // if문 중괄호만 정상적으로 닫힙니다.
            
            // 3. 파일 크기 검사
            if (file.size() > 5 * 1024 * 1024) {
                return Response.seeOther(URI.create("/profile?error=too_large")).build();
            }

            // 4. UUID 파일명 생성 + 저장
            String newFileName = UUID.randomUUID() + "." + ext;
            java.nio.file.Path uploadDir = Paths.get("src/main/resources/META-INF/resources/uploads/profile");
            java.nio.file.Files.createDirectories(uploadDir);
            java.nio.file.Files.copy(file.uploadedFile(),
                uploadDir.resolve(newFileName),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
            // 5. DB 업데이트
            User user = User.findByUsername(loginUser);
            if (user != null) {
                user.profileImage = newFileName;
            }

            return Response.seeOther(URI.create("/profile")).build();

        } catch (Exception e) {
            return Response.seeOther(URI.create("/profile?error=upload_fail")).build();
        }
        @POST
        @Path("/profile/update")
        @Transactional
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public Response profileUpdate(
            @FormParam("email") String email,
            @FormParam("phone") String phone) {
                //1. 세션 체크
                String loginUser = context.session().get("loginUser");
                if(loginUser == null) {
                    return Response
                    .seeOther(URI.create("/login"))
                    .build();
                }
                //2. 이메일 중복 체크
                User found = User.findByEmail(email);
                if(found != null && !found.username.equals(loginUser)) {
                    return Response
                    .seeOther(URI.create("/profile?error=duplicate_email"))
                    .build();
                }
                //3. DB 업데이트
                User user = User.findByUsername(loginUser);
                user.email = email;
                user.phone = phone;

                return Response
                .seeOther(URI.create("/profile?success=update"))
                .build()
            }
            @POST
@Path("/profile/password")
@Transactional
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public Response profilePassword(
@FormParam("currentPassword") String currentPassword,
@FormParam("newPassword")     String newPassword) {
// ①세션체크
String loginUser= context.session().get("loginUser");
if (loginUser== null) {
return Response
.seeOther(URI.create("/login"))
.build();
}
// ②현재비밀번호확인(해시값비교)
User user= User.findByUsername(loginUser);
if (!user.password.equals(currentPassword)) {
return Response
.seeOther(URI.create("/profile?error=wrong_password"))
.build();
}
// ③새비밀번호로DB 업데이트
user.password= newPassword;
return Response
.seeOther(URI.create("/profile?success=password_changed"))
.build();

@POST
@Path("/profile/password")
@Transactional
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public Response profilePassword(
    @FormParam("currentPassword") String currentPassword,
    @FormParam("newPassword") String newPassword) {
        //1. 세션체크
        String loginUser = context.session().get("loginUser");
        if(loginUser == null) {
            return Response
            .seeOther(URI.create("/login"))
            .build();
        }
        //2. 현재 비밀번호 확인
        User user = Usre.findByUsername(loginUser);
        if(!user.password.equals(currentPassword)) {
            return Response
            .seeOther(URI.create("/profile?error=wrong_password"))
            .build();
        }
        //3. 새 비밀번호로 DB 업데이트
        user.password = newPassword;

        return Response
        .seeOther(URI.create("/profile?success=password_changed"))
        .build();

        @GET
        @Path("/logout")
        public Response logout(@QueryParam("next") String next) P
        context.session().destroy();
        String redirect = (next ! = null && next.equals("login")) ? "/login" : "/";
        return Response
        .seeOther(URI.create(redirect))
        .build();
    }
    } // profileUpload 메서드 끝
} // AuthResource 클래스 끝