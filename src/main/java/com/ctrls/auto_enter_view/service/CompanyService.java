package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.company.ChangePasswordDto;
import com.ctrls.auto_enter_view.dto.company.SignUpDto;
import com.ctrls.auto_enter_view.dto.company.WithdrawDto;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.enums.ResponseMessage;
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
  public SignUpDto.Response signUp(SignUpDto.Request form) {

    // 키 생성
    String companyKey = KeyGenerator.generateKey();

    CompanyEntity companyEntity = form.toEntity(companyKey,
        passwordEncoder.encode(form.getPassword()));

    CompanyEntity saved = companyRepository.save(companyEntity);

    return SignUpDto.Response.builder()
        .companyKey(saved.getCompanyKey())
        .email(saved.getEmail())
        .name(saved.getCompanyName())
        .message(ResponseMessage.SIGNUP.getMessage())
        .build();
  }

  // 비밀번호 변경
  public void changePassword(String companyKey, ChangePasswordDto.Request form) {

    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    CompanyEntity companyEntity = companyRepository.findByEmail(principal.getUsername())
        .orElseThrow(RuntimeException::new);

    // 회사 정보의 회사키와 URL 회사키 일치 확인
    if (!companyEntity.getCompanyKey().equals(companyKey)) {
      throw new RuntimeException();
    }

    // 입력한 비밀번호가 맞는 지 확인
    if (!passwordEncoder.matches(form.getOldPassword(), companyEntity.getPassword())) {
      throw new RuntimeException();
    }

    companyEntity.setPassword(passwordEncoder.encode(form.getNewPassword()));

    companyRepository.save(companyEntity);
  }

  // 회원 탈퇴
  public void withdraw(String companyKey, WithdrawDto.Request form) {

    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    CompanyEntity companyEntity = companyRepository.findByEmail(principal.getUsername())
        .orElseThrow(RuntimeException::new);

    // 회사 정보의 회사키와 URL 회사키 일치 확인
    if (!companyEntity.getCompanyKey().equals(companyKey)) {
      throw new RuntimeException();
    }

    // 입력한 비밀번호가 맞는 지 확인
    if (!passwordEncoder.matches(form.getPassword(), companyEntity.getPassword())) {
      throw new RuntimeException();
    }

    companyRepository.delete(companyEntity);
  }
}