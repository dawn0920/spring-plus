package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    // parameter -> 컨트롤러 메서드의 파라미터 정보를 가져옴
    public boolean supportsParameter(MethodParameter parameter) {
        // 파라미터에 @Auth 어노테이션이 있는지 확인 함
        boolean hasAuthAnnotation = parameter.getParameterAnnotation(Auth.class) != null;
        // 파라미터 타입이 AuthUser.class 인지 확인
        boolean isAuthUserType = parameter.getParameterType().equals(AuthUser.class);

        // @Auth 어노테이션과 AuthUser 타입이 함께 사용되지 않은 경우 예외 발생
        if (hasAuthAnnotation != isAuthUserType) {
            throw new AuthException("@Auth와 AuthUser 타입은 함께 사용되어야 합니다.");
        }

        return hasAuthAnnotation;
    }

    @Override
    public Object resolveArgument(
            // 처리 중인 파라미터의 타입, 이름, 어노테이션 등을 담고 있음
            @Nullable MethodParameter parameter,
            // Model과 View에 대한 정보를 담고 있음
            @Nullable ModelAndViewContainer mavContainer,
            // Spring 범용 요청 객체
            NativeWebRequest webRequest,
            // Spring가 제공하는 데이터 바인딩 관련 도구
            @Nullable WebDataBinderFactory binderFactory
    ) {

        // 로그인한 사용자의 인증 정보를 꺼내는 방법
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // authentication.isAuthenticated() 현재 사용자가 인증되었는지 판별
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthException("인증 정보가 존재하지 않습니다.");
        }

        // authentication.getPrincipal() -> AuthUser 객체를 반환
        // Principal -> 인증된 사용자의 기본 정보 객체
        return authentication.getPrincipal();

//        // webRequest -> 웹 요청 객체 / HttpServletRequest 를 꺼내기 위한 형변환 (헤더, 속성, 세션 등을 다룸)
//        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
//
//        // JwtFilter 에서 set 한 userId, email, userRole 값을 가져옴
//        Long userId = (Long) request.getAttribute("userId");
//        String email = (String) request.getAttribute("email");
//        // 문자열로 받은 역할을 실제 Enum 타입으로 변환
//        UserRole userRole = UserRole.of((String) request.getAttribute("userRole"));
//        String nickname = (String) request.getAttribute("nickname");
//
//        return new AuthUser(userId, email, userRole, nickname);
    }
}
