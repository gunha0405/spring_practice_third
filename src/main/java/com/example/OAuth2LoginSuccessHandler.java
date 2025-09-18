package com.example;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.user.SiteUser;
import com.example.user.UserRepository;
import com.example.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // DB에 사용자 없으면 새로 저장
        SiteUser user = userRepository.findByusername(email)
                .orElseGet(() -> {
                    SiteUser newUser = new SiteUser();
                    newUser.setUsername(email);
                    newUser.setPassword(""); // 소셜로그인은 패스워드 없음
                    return userRepository.save(newUser);
                });

        // JWT 발급
        String token = jwtUtil.generateToken(user);

        Cookie cookie = new Cookie("ACCESS_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        response.sendRedirect("/");
    }
}

