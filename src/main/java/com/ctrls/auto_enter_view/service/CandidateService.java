package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_DUPLICATION;
import static com.ctrls.auto_enter_view.enums.ErrorCode.PASSWORD_NOT_MATCH;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;

import com.ctrls.auto_enter_view.dto.candidate.ChangePasswordDto.Request;
import com.ctrls.auto_enter_view.dto.candidate.SignUpDto;
import com.ctrls.auto_enter_view.dto.candidate.WithdrawDto;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CandidateService {

  private final CandidateRepository candidateRepository;

  private final PasswordEncoder passwordEncoder;

  // 회원 가입
  public SignUpDto.Response signUp(SignUpDto.Request signUpDto) {

    if (candidateRepository.existsByEmail(signUpDto.getEmail())) {
      throw new CustomException(EMAIL_DUPLICATION);
    }

    String encoded = passwordEncoder.encode(signUpDto.getPassword());

    CandidateEntity candidate = signUpDto.toEntity(signUpDto, encoded);

    candidateRepository.save(candidate);

    return SignUpDto.Response.builder()
        .email(signUpDto.getEmail())
        .name(signUpDto.getName())
        .message(signUpDto.getName() + "님 회원가입을 환영합니다")
        .build();
  }

  // 회원 탈퇴
  public void withdraw(WithdrawDto.Request request, String candidateKey) {

    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    CandidateEntity candidateEntity = candidateRepository.findByEmail(principal.getUsername())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    // 응시자 정보의 응시자키와 URL 응시자키 일치 확인
    if (!candidateEntity.getCandidateKey().equals(candidateKey)) {
      throw new CustomException(USER_NOT_FOUND);
    }

    if (!passwordEncoder.matches(request.getPassword(), candidateEntity.getPassword())) {
      throw new CustomException(PASSWORD_NOT_MATCH);
    }

    candidateRepository.delete(candidateEntity);
  }

  // 비밀번호 수정
  public void changePassword(String candidateKey, Request request) {

    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    CandidateEntity candidateEntity = candidateRepository.findByEmail(principal.getUsername())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    // 응시자 정보의 응시자키와 URL 응시자키 일치 확인
    if (!candidateEntity.getCandidateKey().equals(candidateKey)) {
      throw new CustomException(USER_NOT_FOUND);
    }

    if (!passwordEncoder.matches(request.getOldPassword(), candidateEntity.getPassword())) {
      throw new CustomException(PASSWORD_NOT_MATCH);
    }

    candidateEntity.setPassword(passwordEncoder.encode(request.getNewPassword()));

    candidateRepository.save(candidateEntity);
  }
}
