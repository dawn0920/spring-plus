package org.example.expert.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
// OncePerRequestFilter 요청마다 한 번씩 실행됨
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 헤더에서 Authorization 값을 꺼냄
        String bearerJwt = request.getHeader("Authorization");

        if (bearerJwt == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "JWT 토큰이 필요합니다.");
            return;
        }

        // "Bearer " 접두사를 제거한 실제 JWT 값을 꺼냄
        String jwt = jwtUtil.substringToken(bearerJwt);

        try {
            // JWT의 Payload를 파싱해 Claims 객체로 변환
            Claims claims = jwtUtil.extractClaims(jwt);


            Long userId = Long.parseLong(claims.getSubject());
            String email = claims.get("email", String.class);
            String role = claims.get("userRole", String.class);
            String nickname = claims.get("nickname", String.class);

            // SS 권한은 ROLE_ 접두어가 붙은 문자열로 처리
            List<GrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + role)
            );

            AuthUser authUser = new AuthUser(
                    userId, email, UserRole.valueOf(role), nickname);

            // 인증된 사용자를 나타내는 객체 (authUser - principal), credentials는 null로 비밀번호 사용 안함, 마지막은 권한 리스트
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    authUser, null, authorities
            );

            // SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        } catch (Exception e) {
            log.error("JWT ERROR : ", e);
        }
    filterChain.doFilter(request, response);
    }
}

