package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.common.SignInDto;
import com.ctrls.auto_enter_view.dto.common.SignInDto.Response;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.security.JwtTokenProvider;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommonUserService {

  private final CompanyRepository companyRepository;
  private final CandidateRepository candidateRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final PasswordEncoder passwordEncoder;

  // 로그인 : 이메일 조회 + 비밀번호 일치 확인
  public SignInDto.Response loginUser(String email, String password) {
    log.info("로그인 요청 - 이메일 : {}", email);

    // 이메일로 회사 엔티티 조회
    Optional<CompanyEntity> companyOptional = companyRepository.findByEmail(email);

    // 회사 엔티티가 존재하고 비밀번호가 일치하는 경우
    if (companyOptional.isPresent() && passwordEncoder.matches(password, companyOptional.get().getPassword())) {
      CompanyEntity company = companyOptional.get();
      String token = jwtTokenProvider.generateToken(company.getEmail(), company.getRole());
      return SignInDto.fromCompany(company, token);
    }

    // 이메일로 후보자 엔티티 조회
    Optional<CandidateEntity> candidateOptional = candidateRepository.findByEmail(email);

    // 후보자 엔티티가 존재하고 비밀번호가 일치하는 경우
    if (candidateOptional.isPresent() && passwordEncoder.matches(password, candidateOptional.get().getPassword())) {
      CandidateEntity candidate = candidateOptional.get();
      String token = jwtTokenProvider.generateToken(candidate.getEmail(), candidate.getRole());
      return SignInDto.fromCandidate(candidate, token);
    }

    // 이메일이 존재하지 않는 경우
    if (companyOptional.isEmpty() && candidateOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response()).getBody();
    }

    // 비밀번호가 일치하지 않는 경우
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response()).getBody();
  }
}