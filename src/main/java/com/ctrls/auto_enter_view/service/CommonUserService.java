package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.component.MailComponent;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.util.RandomGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CommonUserService {

  private final CompanyRepository companyRepository;
  private final CandidateRepository candidateRepository;
  private final MailComponent mailComponent;
  private final PasswordEncoder passwordEncoder;

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
    if (validateCompanyExistsByEmail(email) && validateCandidateExistsByEmail(email)) {
      return "사용 가능한 이메일입니다.";
    } else {
      return "이미 사용 중인 이메일입니다.";
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
  public boolean sendVerificationCode(String email) {
    try {
      /**
       * Redis DB에 인증 코드 저장 추가해야 함
       */
      mailComponent.sendVerificationCode(email, generateVerificationCode());
      return true;
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * 이메일 인증 코드 확인
   *
   * @param email
   * @param verificationCode
   * @return
   */
  public boolean verifyEmailVerificationCode(String email, String verificationCode) {
    /**
     * Redis DB에서 해당 email에 보내진 verificationCode를 받아와야 함
     * 만약 sentVerificationCode를 찾을 수 없다면(null이라면) Exception 처리할 것
     */
    String sentVerificationCode = "";

    if (verificationCode.equals(sentVerificationCode)) {
      return true;
    } else {
      throw new RuntimeException("인증 코드가 일치하지 않습니다.");
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
  public boolean sendTemporaryPassword(String email, String name) {
    // 회사 계정일 경우
    if (validateCompanyExistsByEmail(email)) {
      CompanyEntity company = companyRepository.findByEmail(email)
          .orElseThrow(RuntimeException::new);

      if (!name.equals(company.getCompanyName())) {
        throw new RuntimeException("작성해주신 계정 정보가 올바르지 않습니다.");
      }

      try {
        String temporaryPassword = generateTemporaryPassword();

        company.setPassword(passwordEncoder.encode(temporaryPassword));
        companyRepository.save(company);

        mailComponent.sendTemporaryPassword(email, temporaryPassword);
      } catch (Exception e) {
        throw new RuntimeException(e.getMessage());
      }

      return true;
    }

    // 지원자 계정일 경우
    if (validateCandidateExistsByEmail(email)) {
      CandidateEntity candidate = candidateRepository.findByEmail(email)
          .orElseThrow(RuntimeException::new);

      if (!name.equals(candidate.getName())) {
        throw new RuntimeException("작성해주신 계정 정보가 올바르지 않습니다.");
      }

      try {
        String temporaryPassword = generateTemporaryPassword();

        candidate.setPassword(passwordEncoder.encode(temporaryPassword));
        candidateRepository.save(candidate);

        mailComponent.sendTemporaryPassword(email, temporaryPassword);
      } catch (Exception e) {
        throw new RuntimeException(e.getMessage());
      }

      return true;
    }

    return false;
  }
}
