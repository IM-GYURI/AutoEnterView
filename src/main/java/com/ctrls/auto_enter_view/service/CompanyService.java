package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_DUPLICATION;

import com.ctrls.auto_enter_view.dto.company.SignUpDto;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.util.KeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // 이메일 중복 체크
    if (companyRepository.existsByEmail(request.getEmail())) {
      throw new CustomException(EMAIL_DUPLICATION);
    }

    // 전화번호 중복 체크
    if (companyRepository.existsByCompanyNumber(request.getCompanyNumber())) {
      throw new CustomException(ErrorCode.COMPANY_NUMBER_DUPLICATION);
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

}