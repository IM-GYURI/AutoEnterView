package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_DUPLICATION;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;

import com.ctrls.auto_enter_view.dto.company.SignUpDto;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.util.KeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CompanyService {

  private final CompanyRepository companyRepository;
  private final PasswordEncoder passwordEncoder;

  // 회원 가입
  public SignUpDto.Response signUp(SignUpDto.Request request) {

    if (companyRepository.existsByEmail(request.getEmail())) {
      throw new CustomException(EMAIL_DUPLICATION);
    }

    // 키 생성
    String companyKey = KeyGenerator.generateKey();

    CompanyEntity companyEntity = request.toEntity(companyKey,
        passwordEncoder.encode(request.getPassword()));

    CompanyEntity saved = companyRepository.save(companyEntity);

    return SignUpDto.Response.builder()
        .companyKey(saved.getCompanyKey())
        .email(saved.getEmail())
        .name(saved.getCompanyName())
        .build();
  }

  // 회원 탈퇴
  public void withdraw(String companyKey) {

    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    CompanyEntity companyEntity = companyRepository.findByEmail(principal.getUsername())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    // 회사 정보의 회사키와 URL 회사키 일치 확인
    if (!companyEntity.getCompanyKey().equals(companyKey)) {
      throw new CustomException(USER_NOT_FOUND);
    }

    companyRepository.delete(companyEntity);
  }
}