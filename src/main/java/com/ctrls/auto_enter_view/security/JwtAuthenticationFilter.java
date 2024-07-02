package com.ctrls.auto_enter_view.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  // 클라이언트로부터 전송된 JWT 토큰을 검증하고, 해당 토큰이 유효한 경우에만 인증을 허용

  public static final String TOKEN_HEADER = "Authorization";
  public static final String TOKEN_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  // 필터가 실제로 HTTP 요청을 필터링하는데 사용
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    log.info("JwtAuthenticationFilter - doFilterInternal 호출");

    String token = resolveToken(request);

    if (StringUtils.hasText(token)) {
      // 토큰 유효성 검사 및 블랙리스트 확인
      if (jwtTokenProvider.validateToken(token)) {
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } else {
        log.warn("유효하지 않은 토큰입니다: {}", token);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  // HTTP 요청에서 JWT 토큰을 추출하는 역할
  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(TOKEN_HEADER);

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
      return bearerToken.substring(TOKEN_PREFIX.length()).trim();
    }
    return null;
  }

}