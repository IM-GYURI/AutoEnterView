package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_DUPLICATION;
import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_SEND_FAILURE;
import static com.ctrls.auto_enter_view.enums.ErrorCode.INVALID_VERIFICATION_CODE;
import static com.ctrls.auto_enter_view.enums.ErrorCode.PASSWORD_NOT_MATCH;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ResponseMessage.USABLE_EMAIL;

import com.ctrls.auto_enter_view.component.MailComponent;
import com.ctrls.auto_enter_view.dto.common.ChangePasswordDto.Request;
import com.ctrls.auto_enter_view.dto.common.SignInDto;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.enums.UserRole;
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
import org.springframework.security.core.context.SecurityContextHolder;
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
  private final RedisTemplate<String, String> redisTemplate;



  /**
   * 이메일 중복 확인
   *
   * @param email 중복 확인할 이메일
   * @return ResponseMessage
   * @throws CustomException EMAIL_DUPLICATION : 이메일이 중복된 경우
   */
  public String checkDuplicateEmail(String email) {

    if (!validateCompanyExistsByEmail(email) && !validateCandidateExistsByEmail(email)) {
      return USABLE_EMAIL.getMessage();
    } else {
      throw new CustomException(EMAIL_DUPLICATION);
    }
  }

  /**
   * 회사 이메일 존재 여부 확인하기
   * @param email 회사 이메일
   * @return boolean : 이메일이 존재하면 true, 존재하지 않으면 false
   */
  private boolean validateCompanyExistsByEmail(String email) {

    return companyRepository.existsByEmail(email);
  }

  /**
   * 지원자 이메일 존재 여부 확인하기
   *
   * @param email 지원자 이메일
   * @return boolean : 이메일이 존재하면 true, 존재하지 않으면 false
   */
  private boolean validateCandidateExistsByEmail(String email) {

    return candidateRepository.existsByEmail(email);
  }

  /**
   * 인증 코드 생성
   *
   * @return 랜덤 로직으로 생성된 인증 코드
   */
  private String generateVerificationCode() {

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
   * @param email 인증 코드를 확인할 이메일
   * @param verificationCode 입력 받은 인증 코드
   * @throws CustomException INVALID_VERIFICATION_CODE : 전송한 인증 코드가 없거나(시간초과) 불일치하는 경우
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

  /**
   * 임시 비밀번호 생성
   *
   * @return 랜덤 로직으로 생성된 임시 비밀번호
   */
  private String generateTemporaryPassword() {

    return RandomGenerator.generateTemporaryPassword();
  }

  /**
   * 임시 비밀번호 전송
   *
   * @param email 임시 비밀번호를 전송할 이메일
   * @param name 사용자 이름
   * @throws CustomException USER_NOT_FOUND : 사용자를 찾을 수 없는 경우
   * @throws CustomException EMAIL_SEND_FAILURE 이메일 전송에 실패한 경우
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

  /**
   * 로그인 : 이메일 조회 + 비밀번호 일치 확인
   *
   * @param email 가입할 때 작성한 이메일
   * @param password 가입할 때 작성한 비밀번호
   * @return SignInDto.Response
   * @throws CustomException INVALID_EMAIL_OR_PASSWORD : 이메일이 존재하지 않거나 비밀번호가 일치하지 않는 경우
   */
  public SignInDto.Response loginUser(String email, String password) {

    log.info("로그인 요청 - 이메일 : {}", email);

    // 이메일로 회사 엔티티 조회
    Optional<CompanyEntity> companyOptional = companyRepository.findByEmail(email);

    // 회사 엔티티 존재하고 비밀번호가 일치하는 경우
    if (companyOptional.isPresent() && passwordEncoder.matches(password, companyOptional.get().getPassword())) {
      return SignInDto.fromCompany(companyOptional.get());
    }

    // 이메일로 지원자 엔티티 조회
    Optional<CandidateEntity> candidateOptional = candidateRepository.findByEmail(email);

    // 지원자 엔티티가 존재하는 경우
    if (candidateOptional.isPresent() && passwordEncoder.matches(password, candidateOptional.get().getPassword())) {
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

    blacklistTokenService.addToBlacklist(token);
  }

  /**
   * 비밀번호 수정
   *
   * @param key CompanyKey 또는 CandidateKey
   * @param request ChangePasswordDto.Request
   * @throws CustomException USER_NOT_FOUND : 사용자를 찾을 수 없는 경우
   * @throws CustomException PASSWORD_NOT_MATCH : 입력한 비밀번호와 기존 비밀번호가 일치하지 않는 경우
   */
  public void changePassword(String key, Request request) {

    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();

    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

    for (GrantedAuthority authority : authorities) {
      String role = authority.getAuthority();

      if (role.equals(UserRole.ROLE_CANDIDATE.name())) {

        CandidateEntity candidateEntity = candidateRepository.findByCandidateKey(key)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 입력한 비밀번호가 맞는 지 확인
        if (!passwordEncoder.matches(request.getOldPassword(), candidateEntity.getPassword())) {
          throw new CustomException(PASSWORD_NOT_MATCH);
        }

        candidateEntity.setPassword(passwordEncoder.encode(request.getNewPassword()));

        candidateRepository.save(candidateEntity);

      } else {

        CompanyEntity companyEntity = companyRepository.findByCompanyKey(key)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 입력한 비밀번호가 맞는 지 확인
        if (!passwordEncoder.matches(request.getOldPassword(), companyEntity.getPassword())) {
          throw new CustomException(PASSWORD_NOT_MATCH);
        }

        companyEntity.setPassword(passwordEncoder.encode(request.getNewPassword()));

        companyRepository.save(companyEntity);

      }
    }
  }

  /**
   * 회원 탈퇴
   *
   * @param key CompanyKey 또는 CandidateKey
   * @throws CustomException USER_NOT_FOUND : 사용자를 찾을 수 없는 경우
   * @throws CustomException KEY_NOT_MATCH : 키가 일치하지 않는 경우
   */
  @Transactional
  public void withdraw(String key) {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();

    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

    for (GrantedAuthority authority : authorities) {
      String role = authority.getAuthority();

      if (role.equals(UserRole.ROLE_CANDIDATE.name())) {

        CandidateEntity candidateEntity = candidateRepository.findByCandidateKey(key)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 응시자 정보의 응시자키와 URL 응시자키 일치 확인
        if (!candidateEntity.getCandidateKey().equals(key)) {
          throw new CustomException(ErrorCode.KEY_NOT_MATCH);
        }

        // 지원자가 작성한 이력서 삭제
        resumeRepository.deleteByCandidateKey(candidateEntity.getCandidateKey());

        // 지원자 삭제
        candidateRepository.delete(candidateEntity);

      } else {

        CompanyEntity companyEntity = companyRepository.findByCompanyKey(key)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 회사 정보의 회사키와 URL 회사키 일치 확인
        if (!companyEntity.getCompanyKey().equals(key)) {
          throw new CustomException(ErrorCode.KEY_NOT_MATCH);
        }

        // 회사 정보 삭제
        companyInfoRepository.deleteByCompanyKey(companyEntity.getCompanyKey());

        // 회사 삭제
        companyRepository.delete(companyEntity);

      }
    }

  }
}