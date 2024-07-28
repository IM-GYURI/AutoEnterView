package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_DUPLICATION;
import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_SEND_FAILURE;
import static com.ctrls.auto_enter_view.enums.ErrorCode.INVALID_VERIFICATION_CODE;
import static com.ctrls.auto_enter_view.enums.ErrorCode.PASSWORD_NOT_MATCH;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ResponseMessage.USABLE_EMAIL;
import static com.ctrls.auto_enter_view.enums.UserRole.ROLE_CANDIDATE;

import com.ctrls.auto_enter_view.component.MailComponent;
import com.ctrls.auto_enter_view.dto.common.ChangePasswordDto.Request;
import com.ctrls.auto_enter_view.dto.common.SignInDto;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyInfoRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.ResumeRepository;
import com.ctrls.auto_enter_view.util.RandomGenerator;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor

public class CommonUserService {

  private final BlacklistTokenService blacklistTokenService;
  private final CompanyRepository companyRepository;
  private final CompanyInfoRepository companyInfoRepository;
  private final CandidateRepository candidateRepository;
  private final ResumeRepository resumeRepository;
  private final MailComponent mailComponent;
  private final PasswordEncoder passwordEncoder;
  private final RedisTemplate<String, String> redisStringTemplate;

  /**
   * 이메일 중복 확인
   *
   * @param email 중복 확인할 이메일
   * @return ResponseMessage
   * @throws CustomException EMAIL_DUPLICATION : 이메일이 중복된 경우
   */
  public String checkDuplicateEmail(String email) {
    log.info("이메일 중복 확인");
    if (!validateCompanyExistsByEmail(email) && !validateCandidateExistsByEmail(email)) {
      return USABLE_EMAIL.getMessage();
    } else {
      throw new CustomException(EMAIL_DUPLICATION);
    }
  }

  /**
   * 회사 이메일 존재 여부 확인하기
   *
   * @param email 회사 이메일
   * @return boolean : 이메일이 존재하면 true, 존재하지 않으면 false
   */
  private boolean validateCompanyExistsByEmail(String email) {
    log.info("email로 회사 계정 존재 여부 확인");
    return companyRepository.existsByEmail(email);
  }

  /**
   * 지원자 이메일 존재 여부 확인하기
   *
   * @param email 지원자 이메일
   * @return boolean : 이메일이 존재하면 true, 존재하지 않으면 false
   */
  private boolean validateCandidateExistsByEmail(String email) {
    log.info("email로 지원자 계정 존재 여부 확인");
    return candidateRepository.existsByEmail(email);
  }

  /**
   * 인증 코드 생성
   *
   * @return 랜덤 로직으로 생성된 인증 코드
   */
  private String generateVerificationCode() {
    log.info("인증 코드 생성");
    return RandomGenerator.generateRandomCode();
  }

  /**
   * 이메일 인증 코드 전송
   *
   * @param email 인증 코드를 전송할 이메일
   * @throws CustomException EMAIL_SEND_FAILURE : 이메일 전송에 실패한 경우
   */
  public void sendVerificationCode(String email) {
    try {
      String verificationCode = generateVerificationCode();
      log.info("인증 코드 : " + verificationCode);

      redisStringTemplate.opsForValue().set(email, verificationCode, 5, TimeUnit.MINUTES);
      log.info("Redis DB에 저장 : 유효시간 5분");

      log.info("이메일 인증 코드 전송 : " + email + " - " + verificationCode);
      mailComponent.sendVerificationCode(email, verificationCode);
    } catch (Exception e) {
      throw new CustomException(EMAIL_SEND_FAILURE);
    }
  }

  /**
   * 이메일 인증 코드 확인
   *
   * @param email            인증 코드를 확인할 이메일
   * @param verificationCode 입력 받은 인증 코드
   * @throws CustomException INVALID_VERIFICATION_CODE : 전송한 인증 코드가 없거나(시간초과) 불일치하는 경우
   */
  public void verifyEmailVerificationCode(String email, String verificationCode) {
    log.info("이메일 인증 코드 확인");

    String sentVerificationCode = redisStringTemplate.opsForValue().get(email);
    log.info("입력 받은 인증 코드 : " + verificationCode + "\nRedis DB에 저장되어 있는 인증 코드 : "
        + sentVerificationCode);

    if (sentVerificationCode == null) {
      throw new CustomException(INVALID_VERIFICATION_CODE);
    }

    if (!verificationCode.equals(sentVerificationCode)) {
      throw new CustomException(INVALID_VERIFICATION_CODE);
    }
  }

  /**
   * 임시 비밀번호 생성
   *
   * @return 랜덤 로직으로 생성된 임시 비밀번호
   */
  public String generateTemporaryPassword() {
    return RandomGenerator.generateTemporaryPassword();
  }

  /**
   * 임시 비밀번호 전송
   *
   * @param email 임시 비밀번호를 전송할 이메일
   * @param name  사용자 이름
   * @throws CustomException USER_NOT_FOUND : 사용자를 찾을 수 없는 경우
   * @throws CustomException EMAIL_SEND_FAILURE 이메일 전송에 실패한 경우
   */
  public void sendTemporaryPassword(String email, String name) {
    if (validateCompanyExistsByEmail(email)) {
      log.info("회사 계정일 경우");
      CompanyEntity company = companyRepository.findByEmail(email)
          .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

      if (!name.equals(company.getCompanyName())) {
        throw new CustomException(USER_NOT_FOUND);
      }

      try {
        String temporaryPassword = generateTemporaryPassword();
        log.info("임시 비밀번호 : " + temporaryPassword);

        company.setPassword(passwordEncoder.encode(temporaryPassword));
        companyRepository.save(company);

        log.info("이메일로 임시 비밀번호 전송");
        mailComponent.sendTemporaryPassword(email, temporaryPassword);
      } catch (Exception e) {
        throw new CustomException(EMAIL_SEND_FAILURE);
      }
    }

    if (validateCandidateExistsByEmail(email)) {
      log.info("지원자 계정일 경우");
      CandidateEntity candidate = candidateRepository.findByEmail(email)
          .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

      if (!name.equals(candidate.getName())) {
        throw new CustomException(USER_NOT_FOUND);
      }

      try {
        String temporaryPassword = generateTemporaryPassword();
        log.info("임시 비밀번호 : " + temporaryPassword);

        candidate.setPassword(passwordEncoder.encode(temporaryPassword));
        candidateRepository.save(candidate);

        log.info("이메일로 임시 비밀번호 전송");
        mailComponent.sendTemporaryPassword(email, temporaryPassword);
      } catch (Exception e) {
        throw new CustomException(EMAIL_SEND_FAILURE);
      }
    }
  }

  /**
   * 로그인 : 이메일 조회 + 비밀번호 일치 확인
   *
   * @param email    가입할 때 작성한 이메일
   * @param password 가입할 때 작성한 비밀번호
   * @return SignInDto.Response
   * @throws CustomException INVALID_EMAIL_OR_PASSWORD : 이메일이 존재하지 않거나 비밀번호가 일치하지 않는 경우
   */
  public SignInDto.Response loginUser(String email, String password) {
    log.info("로그인 요청 - 이메일 : {}", email);

    // 이메일로 회사 엔티티 조회
    Optional<CompanyEntity> companyOptional = companyRepository.findByEmail(email);

    // 회사 엔티티 존재하고 비밀번호가 일치하는 경우
    if (companyOptional.isPresent() && passwordEncoder.matches(password,
        companyOptional.get().getPassword())) {
      return SignInDto.fromCompany(companyOptional.get());
    }

    // 이메일로 지원자 엔티티 조회
    Optional<CandidateEntity> candidateOptional = candidateRepository.findByEmail(email);

    // 지원자 엔티티가 존재하는 경우
    if (candidateOptional.isPresent() && passwordEncoder.matches(password,
        candidateOptional.get().getPassword())) {
      return SignInDto.fromCandidate(candidateOptional.get());
    }

    // 이메일이 존재하지 않거나 비밀번호가 일치하지 않는 경우
    throw new CustomException(ErrorCode.INVALID_EMAIL_OR_PASSWORD);
  }

  /**
   * 로그 아웃
   *
   * @param token 토큰 정보
   */
  public void logoutUser(String token) {
    log.info("로그 아웃");
    blacklistTokenService.addToBlacklist(token);
  }

  /**
   * 비밀번호 수정
   *
   * @param key     CompanyKey 또는 CandidateKey
   * @param request ChangePasswordDto.Request
   * @throws CustomException USER_NOT_FOUND : 사용자를 찾을 수 없는 경우
   * @throws CustomException PASSWORD_NOT_MATCH : 입력한 비밀번호와 기존 비밀번호가 일치하지 않는 경우
   */
  public void changePassword(UserDetails userDetails, String key, Request request) {
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    for (GrantedAuthority authority : authorities) {
      String role = authority.getAuthority();

      if (role.equals(ROLE_CANDIDATE.name())) {
        log.info("지원자 계정일 경우");

        CandidateEntity candidateEntity = candidateRepository.findByCandidateKey(key)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 입력한 비밀번호가 맞는 지 확인
        if (!passwordEncoder.matches(request.getOldPassword(), candidateEntity.getPassword())) {
          throw new CustomException(PASSWORD_NOT_MATCH);
        }

        log.info("비밀번호 수정");
        candidateEntity.setPassword(passwordEncoder.encode(request.getNewPassword()));

        candidateRepository.save(candidateEntity);

      } else {
        log.info("회사 계정일 경우");

        CompanyEntity companyEntity = companyRepository.findByCompanyKey(key)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 입력한 비밀번호가 맞는 지 확인
        if (!passwordEncoder.matches(request.getOldPassword(), companyEntity.getPassword())) {
          throw new CustomException(PASSWORD_NOT_MATCH);
        }

        log.info("비밀번호 수정");
        companyEntity.setPassword(passwordEncoder.encode(request.getNewPassword()));

        companyRepository.save(companyEntity);
      }
    }
  }

  /**
   * 회원 탈퇴
   *
   * @param userDetails 로그인 된 사용자 정보
   * @param key         CompanyKey 또는 CandidateKey
   * @throws CustomException USER_NOT_FOUND : 사용자를 찾을 수 없는 경우
   * @throws CustomException KEY_NOT_MATCH : 키가 일치하지 않는 경우
   */
  @Transactional
  public void withdraw(UserDetails userDetails, String key) {
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

    for (GrantedAuthority authority : authorities) {
      String role = authority.getAuthority();

      if (role.equals(ROLE_CANDIDATE.name())) {
        log.info("지원자 계정일 경우");

        CandidateEntity candidateEntity = candidateRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 응시자 정보의 응시자키와 URL 응시자키 일치 확인
        if (!candidateEntity.getCandidateKey().equals(key)) {
          throw new CustomException(ErrorCode.KEY_NOT_MATCH);
        }

        log.info("이력서 삭제");
        resumeRepository.deleteByCandidateKey(candidateEntity.getCandidateKey());

        log.info("지원자 삭제");
        candidateRepository.delete(candidateEntity);
      } else {
        log.info("회사 계정일 경우");

        CompanyEntity companyEntity = companyRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 회사 정보의 회사키와 URL 회사키 일치 확인
        if (!companyEntity.getCompanyKey().equals(key)) {
          throw new CustomException(ErrorCode.KEY_NOT_MATCH);
        }

        log.info("회사 정보 삭제");
        companyInfoRepository.deleteByCompanyKey(companyEntity.getCompanyKey());

        log.info("회사 삭제");
        companyRepository.delete(companyEntity);
      }
    }
  }
}