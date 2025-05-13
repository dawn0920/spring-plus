package org.example.expert.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                // csrf 보호를 비활성화
                .csrf(csrf -> csrf.disable())
                // 세션을 만들지 않도록 설정 [STATELESS - 서버가 상태를 저장하지 않음] -> 요청마다 인증/인가 처리
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // URL 접근 허용/제한
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/signin", "/auth/signup").permitAll()
                        .anyRequest().authenticated()
                )
                // 필터 등록 (기본 인증 필터(UsernamePasswordAuthenticationFilter) 보다 앞에 서 실행 되도록 설정)
                .addFilterBefore(jwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                // 위의 내용을 기반으로 객체를 생성
                .build();
    }

    // JwtFilter는 사용자의 요청에서 JWT를 추출해 검증 -> 검증 성공시 SecurityContextHolder에 인증 정보를 설정
    // 이렇게 해야지 addFilterBefore에서 사용 가능
    @Bean
    public JwtFilter jwtFilter(JwtUtil jwtUtil) {
        return new JwtFilter(jwtUtil);
    }
}
