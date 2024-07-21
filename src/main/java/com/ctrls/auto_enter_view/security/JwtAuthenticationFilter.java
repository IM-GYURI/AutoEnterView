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
        if (jwtTokenProvider.validateToken(token)) {
          if (!blacklistTokenService.isTokenBlacklist(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
          } else {
            log.info("토큰이 블랙리스트에 존재하여 사용할 수 없는 토큰입니다.");
            throw new CustomException(ErrorCode.TOKEN_BLACKLISTED);
          }
        } else {
          log.info("유효하지 않은 토큰입니다.");
          throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
      } catch (CustomException e) {
        log.error("CustomException 발생", e);
        setErrorResponse(response, e.getErrorCode());
        return;
      } catch (Exception e) {
        log.error("필터 오류 발생", e);
        throw e;
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

  private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode)
      throws IOException {

    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(errorCode.getStatus());
    response.getWriter().write(errorCode.getMessage());
  }
}