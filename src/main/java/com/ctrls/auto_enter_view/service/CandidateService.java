package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.dto.candidate.SignUpDto;
import com.ctrls.auto_enter_view.dto.candidate.WithdrawDto;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CandidateService {

  private final CandidateRepository candidateRepository;

  private final PasswordEncoder passwordEncoder;

  public void signUp(SignUpDto.Request signUpDto) {

    if (candidateRepository.existsByEmail(signUpDto.getEmail())) {
      throw new RuntimeException("이미 존재하는 이메일 입니다.");
    }

    String encoded = passwordEncoder.encode(signUpDto.getPassword());

    CandidateEntity candidate = signUpDto.toEntity(signUpDto, encoded);

    candidateRepository.save(candidate);

  }

  public SignUpDto.Response getSignUpDto(SignUpDto.Request signUpDto) {

    SignUpDto.Response signUpDtoResponse = SignUpDto.Response.builder()
        .email(signUpDto.getEmail())
        .name(signUpDto.getName())
        .message(signUpDto.getName() + "님 회원가입을 환영합니다")
        .build();

    return signUpDtoResponse;
  }

  public void withdraw(WithdrawDto.Request request, String candidateKey) {

    CandidateEntity candidate = candidateRepository.findByCandidateKey(candidateKey)
        .orElseThrow(() -> new RuntimeException("키값이 다릅니다."));

    if (!passwordEncoder.matches(request.getPassword(), candidate.getPassword())) {
      throw new RuntimeException("비밀번호가 다릅니다.");
    }

    candidateRepository.delete(candidate);

  }

}
