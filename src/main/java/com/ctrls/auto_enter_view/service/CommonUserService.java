package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_DUPLICATION;
import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_SEND_FAILURE;
import static com.ctrls.auto_enter_view.enums.ErrorCode.INVALID_VERIFICATION_CODE;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ResponseMessage.USABLE_EMAIL;

import com.ctrls.auto_enter_view.component.MailComponent;
import com.ctrls.auto_enter_view.dto.common.SignInDto;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.security.JwtTokenProvider;
import com.ctrls.auto_enter_view.util.RandomGenerator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor

public class CommonUserService {

  private final CompanyRepository companyRepository;
  private final CandidateRepository candidateRepository;
  private final MailComponent mailComponent;
  private final PasswordEncoder passwordEncoder;

  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTemplate<String, String> redisTemplate;

  // 이메일을 통해 이메일의 사용 여부를 확인 - 회사
  private boolean validateCompanyExistsByEmail(String email) {

    return companyRepository.existsByEmail(email);
  }

  // 이메일을 통해 이메일의 사용 여부를 확인 - 지원자
  private boolean validateCandidateExistsByEmail(String email) {

    return candidateRepository.existsByEmail(email);
  }

  /**
   * 이메일 중복 확인
   *
   * @param email
   * @return
   */
  public String checkDuplicateEmail(String email) {

    if (!validateCompanyExistsByEmail(email) && !validateCandidateExistsByEmail(email)) {
      return USABLE_EMAIL.getMessage();
    } else {
      throw new CustomException(EMAIL_DUPLICATION);
    }
  }

  // 인증 코드 생성
  private String generateVerificationCode() {

    return RandomGenerator.generateRandomCode();
  }

  /**
   * 이메일 인증 코드 전송
   *
   * @param email
   * @return
   */
  public void sendVerificationCode(String email) {

    try {
      String verificationCode = generateVerificationCode();
      // Redis 유효 시간 5분으로 설정
      redisTemplate.opsForValue().set(email, verificationCode, 5, TimeUnit.MINUTES);
      mailComponent.sendVerificationCode(email, verificationCode);
    } catch (Exception e) {
      throw new CustomException(EMAIL_SEND_FAILURE);
    }
  }

  /**
   * 이메일 인증 코드 확인
   *
   * @param email
   * @param verificationCode
   * @return
   */
  public void verifyEmailVerificationCode(String email, String verificationCode) {

    String sentVerificationCode = redisTemplate.opsForValue().get(email);

    if (sentVerificationCode == null) {
      throw new CustomException(INVALID_VERIFICATION_CODE);
    }

    if (!verificationCode.equals(sentVerificationCode)) {
      throw new CustomException(INVALID_VERIFICATION_CODE);
    }
  }

  // 임시 비밀번호 생성
  private String generateTemporaryPassword() {

    return RandomGenerator.generateTemporaryPassword();
  }

  /**
   * 임시 비밀번호 전송
   *
   * @param email
   * @param name
   * @return
   */
  public void sendTemporaryPassword(String email, String name) {
    // 회사 계정일 경우
    if (validateCompanyExistsByEmail(email)) {
      CompanyEntity company = companyRepository.findByEmail(email)
          .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

      if (!name.equals(company.getCompanyName())) {
        throw new CustomException(USER_NOT_FOUND);
      }

      try {
        String temporaryPassword = generateTemporaryPassword();

        company.setPassword(passwordEncoder.encode(temporaryPassword));
        companyRepository.save(company);

        mailComponent.sendTemporaryPassword(email, temporaryPassword);
      } catch (Exception e) {
        throw new CustomException(EMAIL_SEND_FAILURE);
      }
    }

    // 지원자 계정일 경우
    if (validateCandidateExistsByEmail(email)) {
      CandidateEntity candidate = candidateRepository.findByEmail(email)
          .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

      if (!name.equals(candidate.getName())) {
        throw new CustomException(USER_NOT_FOUND);
      }

      try {
        String temporaryPassword = generateTemporaryPassword();

        candidate.setPassword(passwordEncoder.encode(temporaryPassword));
        candidateRepository.save(candidate);

        mailComponent.sendTemporaryPassword(email, temporaryPassword);
      } catch (Exception e) {
        throw new CustomException(EMAIL_SEND_FAILURE);
      }
    }
  }

  // 로그인 : 이메일 조회 + 비밀번호 일치 확인
  public SignInDto.Response loginUser(String email, String password) {
    log.info("로그인 요청 - 이메일 : {}", email);

    // 이메일로 회사 엔티티 조회
    Optional<CompanyEntity> companyOptional = companyRepository.findByEmail(email);

    // 이메일로 후보자 엔티티 조회
    Optional<CandidateEntity> candidateOptional = candidateRepository.findByEmail(email);

    // 회사 엔티티가 존재하는 경우
    if (companyOptional.isPresent()) {
      CompanyEntity company = companyOptional.get();
      if (passwordEncoder.matches(password, company.getPassword())) {
        String token = jwtTokenProvider.generateToken(company.getEmail(), company.getRole());
        return SignInDto.fromCompany(company, token);
      } else {
        throw new RuntimeException("비밀번호가 일치하지 않습니다.");
      }
    }

    // 후보자 엔티티가 존재하는 경우
    if (candidateOptional.isPresent()) {
      CandidateEntity candidate = candidateOptional.get();
      if (passwordEncoder.matches(password, candidate.getPassword())) {
        String token = jwtTokenProvider.generateToken(candidate.getEmail(), candidate.getRole());
        return SignInDto.fromCandidate(candidate, token);
      } else {
        throw new RuntimeException("비밀번호가 일치하지 않습니다.");
      }
    }

    // 이메일이 존재하지 않는 경우
    throw new RuntimeException("가입된 정보가 없습니다.");
  }

}