package com.ctrls.auto_enter_view.security;

import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.enums.UserRole;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

  private static final String KEY_ROLE = "role";

  @Value("${jwt.secret.key}")
  private String secretKeyString;

  @Value("${jwt.secret.expiration}")
  private long tokenValidTime;

  private Key secretKey;

  private final CompanyRepository companyRepository;
  private final CandidateRepository candidateRepository;

  // secret key 디코딩하여 Key 객체로 초기화
  @PostConstruct
  public void init() {

    byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
    secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    log.info("Secret key initialized");
  }

  /**
   * JWT 토큰을 생성합니다.
   *
   * @param email 사용자의 이메일
   * @param role  사용자의 역할
   * @return 생성된 JWT 토큰
   */
  public String generateToken(String email, UserRole role) {

    log.info("Generating token for email: {} with role: {}", email, role);

    Claims claims = Jwts.claims().setSubject(email);
    claims.put(KEY_ROLE, role.name());

    Date now = new Date();
    Date expiredTime = new Date(now.getTime() + tokenValidTime);

    String token = Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(expiredTime)
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();

    log.info("Generated token: {}", token);

    return token;
  }

  /**
   * JWT 토큰의 유효성을 검사합니다.
   *
   * @param token 검사할 JWT 토큰
   * @return 토큰의 유효성 여부
   */
  public boolean validateToken(String token) {

    log.info("토큰 유효성 체크 시작");
    try {
      Jwts.parserBuilder()
          .setSigningKey(secretKey)
          .build()
          .parseClaimsJws(token);
      return true;
    } catch (ExpiredJwtException e) {
      log.error("JWT 토큰이 만료되었습니다", e);
    } catch (Exception e) {
      log.error("JWT 토큰 유효성 검사 실패", e);
    }
    return false;
  }

  /**
   * JWT 토큰에서 인증 정보를 추출합니다.
   *
   * @param token JWT 토큰
   * @return 인증 정보
   */
  public Authentication getAuthentication(String token) {

    Claims claims = Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody();

    String email = claims.getSubject();
    String roleString = claims.get(KEY_ROLE, String.class);
    UserRole role = UserRole.valueOf(roleString);

    log.info("Getting authentication for email: {}, role: {}", email, role);

    UserDetails userDetails = getUserDetails(email, role);
    return new UsernamePasswordAuthenticationToken(userDetails, token,
        userDetails.getAuthorities());
  }

  /**
   * 사용자의 이메일과 역할을 기반으로 UserDetails 객체를 가져옵니다.
   *
   * @param email 사용자의 이메일
   * @param role  사용자의 역할
   * @return 사용자의 UserDetails 객체
   * @throws UsernameNotFoundException 사용자가 존재하지 않을 경우 예외 발생
   */

  private UserDetails getUserDetails(String email, UserRole role) {

    if (role == UserRole.ROLE_COMPANY) {
      CompanyEntity company = companyRepository.findByEmail(email)
          .orElseThrow(
              () -> new UsernameNotFoundException("Company not found for email: " + email));
      return buildUserDetails(company.getEmail(), company.getPassword(), role);
    } else if (role == UserRole.ROLE_CANDIDATE) {
      CandidateEntity candidate = candidateRepository.findByEmail(email)
          .orElseThrow(
              () -> new UsernameNotFoundException("Candidate not found for email: " + email));
      return buildUserDetails(candidate.getEmail(), candidate.getPassword(), role);
    }
    throw new IllegalArgumentException("Unsupported role: " + role);
  }

  /**
   * 사용자의 이메일과 역할을 기반으로 UserDetails 객체를 생성합니다.
   *
   * @param email    사용자의 이메일
   * @param password 사용자의 비밀번호
   * @param role     사용자의 역할
   * @return 생성된 UserDetails 객체
   */
  private UserDetails buildUserDetails(String email, String password, UserRole role) {

    return User.builder()
        .username(email)
        .password(password)
        .authorities(Collections.singletonList(new SimpleGrantedAuthority(role.name())))
        .build();
  }
}