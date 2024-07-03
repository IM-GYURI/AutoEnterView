package com.ctrls.auto_enter_view.security;

import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.enums.UserRole;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Base64;
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

  @PostConstruct
  public void init() {
    byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
    secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
  }

  public String generateToken(String email, UserRole role) {
    Claims claims = Jwts.claims().setSubject(email);
    claims.put(KEY_ROLE, role.name());

    Date now = new Date();
    Date expiredTime = new Date(now.getTime() + tokenValidTime);

    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(expiredTime)
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      log.error("JWT 토큰 유효성 없음", e);
    }
    return false;
  }

  public Authentication getAuthentication(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody();

    String email = claims.getSubject();
    UserRole role = UserRole.valueOf(claims.get(KEY_ROLE, String.class));

    UserDetails userDetails;
    if (role == UserRole.ROLE_COMPANY) {
      CompanyEntity company = companyRepository.findByEmail(email)
          .orElseThrow(() -> new UsernameNotFoundException("Company not found"));
      userDetails = User.builder()
          .username(company.getEmail())
          .password(company.getPassword())
          .authorities(new SimpleGrantedAuthority(role.name()))
          .build();
    } else {
      CandidateEntity candidate = candidateRepository.findByEmail(email)
          .orElseThrow(() -> new UsernameNotFoundException("Candidate not found"));
      userDetails = User.builder()
          .username(candidate.getEmail())
          .password(candidate.getPassword())
          .authorities(new SimpleGrantedAuthority(role.name()))
          .build();
    }

    return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
  }
}