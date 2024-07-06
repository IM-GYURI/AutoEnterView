package com.ctrls.auto_enter_view.security;

import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.service.BlacklistTokenService;
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
  private final BlacklistTokenService blacklistTokenService;

  @Override
  // 필터가 실제로 HTTP 요청을 필터링하는데 사용
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String token = resolveToken(request);

    if (StringUtils.hasText(token)) {
      try {
        if (jwtTokenProvider.validateToken(token) && !blacklistTokenService.isTokenBlacklist(token)) {
          Authentication authentication = jwtTokenProvider.getAuthentication(token);
          SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
          log.info("토큰이 블랙리스트에 존재하여 사용할 수 없는 토큰입니다.");
          throw new CustomException(ErrorCode.TOKEN_BLACKLISTED);
        }
      } catch (CustomException e) {
        log.error("토큰 검증 실패", e);
        throw e;
      } catch (Exception e) {
        log.error("사용자 인증 실패", e);
        throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
      }
    }

    filterChain.doFilter(request, response);
  }

  // HTTP 요청에서 JWT 토큰을 추출하는 역할
  private String resolveToken(HttpServletRequest request) {

    String bearerToken = request.getHeader(TOKEN_HEADER);

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
      return bearerToken.substring(TOKEN_PREFIX.length());
    }
    return null;
  }
}