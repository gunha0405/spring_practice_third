package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.example.util.JwtFilter;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final JwtFilter jwtFilter;
	
	private final JwtLoginSuccessHandler jwtLoginSuccessHandler;
	
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
	
	@Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
	    .csrf(csrf -> csrf.disable()) // JWT 기반이면 CSRF 끔
	    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

	    .authorizeHttpRequests(auth -> auth
	        .requestMatchers("/h2-console/**", "/user/login", "/css/**", "/js/**", "/images/**").permitAll()
	        .anyRequest().authenticated()
	    )

	    .formLogin(form -> form
	        .loginPage("/user/login")
	        .loginProcessingUrl("/user/login")
	        .successHandler(jwtLoginSuccessHandler)  // 성공 시 쿠키 세팅
	        .permitAll()
	    )
	    
	    .oauth2Login(oauth2 -> oauth2
	            .loginPage("/user/login")
	            .successHandler(oAuth2LoginSuccessHandler) // 새로 만들 핸들러
	    )

	    .logout(logout -> logout
	        .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
	        .logoutSuccessHandler((request, response, authentication) -> {
	            Cookie expired = new Cookie("ACCESS_TOKEN", "");
	            expired.setPath("/");
	            expired.setMaxAge(0);
	            response.addCookie(expired);
	            response.sendRedirect("/");
	        })
	    )

	    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
