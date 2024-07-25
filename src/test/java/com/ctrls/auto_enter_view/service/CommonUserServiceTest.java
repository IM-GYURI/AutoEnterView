package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.EMAIL_SEND_FAILURE;
import static com.ctrls.auto_enter_view.enums.UserRole.ROLE_CANDIDATE;
import static com.ctrls.auto_enter_view.enums.UserRole.ROLE_COMPANY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.component.MailComponent;
import com.ctrls.auto_enter_view.dto.common.SignInDto;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.enums.UserRole;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.util.KeyGenerator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CommonUserServiceTest {

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private MailComponent mailComponent;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @Mock
  private BlacklistTokenService blacklistTokenService;

  @Mock
  private KeyGenerator keyGenerator;

  @InjectMocks
  private CommonUserService commonUserService;

  @Test
  @DisplayName("이메일 중복 확인 : 사용 가능")
  public void testCheckDuplicateEmail_Available() {
    String testEmail = "test@example.com";

    when(companyRepository.existsByEmail(testEmail)).thenReturn(false);
    when(candidateRepository.existsByEmail(testEmail)).thenReturn(false);

    String result = commonUserService.checkDuplicateEmail(testEmail);

    assertEquals("사용 가능한 이메일입니다.", result);
  }

  @Test
  @DisplayName("이메일 중복 확인 : 사용 불가")
  public void testCheckDuplicateEmail_Unavailable() {
    String testEmail = "test@example.com";

    when(companyRepository.existsByEmail(testEmail)).thenReturn(true);

    CustomException exception = assertThrows(CustomException.class, () -> {
      commonUserService.checkDuplicateEmail(testEmail);
    });

    assertEquals("이메일이 중복됩니다.", exception.getMessage());
  }

  @Test
  @DisplayName("이메일 인증 코드 전송 : 성공")
  public void testSendVerificationCode_Success() {
    String testEmail = "test@example.com";
    long expirationTime = 5L;
    TimeUnit expirationUnit = TimeUnit.MINUTES;

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

    assertDoesNotThrow(() -> {
      commonUserService.sendVerificationCode(testEmail);
    });

    verify(redisTemplate, times(1)).opsForValue();
    verify(valueOperations, times(1)).set(eq(testEmail), codeCaptor.capture(),
        eq(expirationTime), eq(expirationUnit));
    verify(mailComponent, times(1)).sendVerificationCode(eq(testEmail), codeCaptor.capture());

    String capturedCode = codeCaptor.getValue();

    assertNotNull(capturedCode);
    assertEquals(6, capturedCode.length(), "인증 코드는 6자리 문자여야 합니다.");
    assertTrue(capturedCode.matches("[A-Za-z0-9!@#$%^&*()+\\-=\\[\\]{};':\"\\\\|,.<>/?~`]+"),
        "인증 코드는 영문 대소문자, 숫자, 특수문자로 이루어져야 합니다.");
  }

  @Test
  @DisplayName("이메일 인증 코드 전송 : 실패 - 이메일 전송 실패")
  public void testSendVerificationCode_EmailSendFailure() {
    String email = "test@example.com";
    String verificationCode = "123456";

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    doThrow(new RuntimeException("Mail send failure")).when(mailComponent)
        .sendVerificationCode(email, verificationCode);

    assertThrows(CustomException.class, () -> {
      commonUserService.sendVerificationCode(email);
    });
  }

  @Test
  @DisplayName("이메일 인증 코드 전송 : 실패 - 레디스 저장 실패")
  public void testSendVerificationCode_RedisFailure() {
    String email = "test@example.com";
    String verificationCode = "123456";

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    doThrow(new RuntimeException("Redis failure")).when(valueOperations)
        .set(email, verificationCode, 5, TimeUnit.MINUTES);

    assertThrows(CustomException.class, () -> {
      commonUserService.sendVerificationCode(email);
    });
  }

  @Test
  @DisplayName("인증 코드 확인 : 성공 - 유효한 코드")
  public void testVerifyEmailVerificationCode_Success() {

    String testEmail = "test@example.com";
    String correctVerificationCode = "123456";

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(testEmail)).thenReturn(correctVerificationCode);

    assertDoesNotThrow(
        () -> commonUserService.verifyEmailVerificationCode(testEmail, correctVerificationCode));
  }

  @Test
  @DisplayName("인증 코드 확인 : 실패 - 유효하지 않은 코드")
  public void testVerifyEmailVerificationCode_InvalidVerificationCodeFailure() {

    String testEmail = "test@example.com";
    String correctVerificationCode = "123456";
    String incorrectVerificationCode = "654321";

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(testEmail)).thenReturn(correctVerificationCode);

    CustomException exception = assertThrows(CustomException.class,
        () -> commonUserService.verifyEmailVerificationCode(testEmail, incorrectVerificationCode));

    assertEquals("유효하지 않은 인증 코드입니다.", exception.getMessage());
  }

  @Test
  @DisplayName("인증 코드 확인 : 실패 - 존재하지 않는 코드")
  public void testVerifyEmailVerificationCode_VerificationCodeNotFoundFailure() {

    String testEmail = "test@example.com";
    String correctVerificationCode = "123456";

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(testEmail)).thenReturn(null);

    CustomException exception = assertThrows(CustomException.class,
        () -> commonUserService.verifyEmailVerificationCode(testEmail, correctVerificationCode));

    assertEquals("유효하지 않은 인증 코드입니다.", exception.getMessage());
  }

  @Test
  @DisplayName("임시 비밀번호 전송 : 성공 - 회사")
  public void testSendTemporaryPassword_CompanySuccess() {
    String testEmail = "test@company.com";
    String testCompanyName = "TestCompany";
    String temporaryPassword = "Password123!";
    String encodedPassword = "encodedTemporaryPassword123";

    CompanyEntity company = CompanyEntity.builder()
        .email(testEmail)
        .companyName(testCompanyName)
        .companyKey("companyKey")
        .companyNumber("02-0000-0000")
        .password("Password000!")
        .role(ROLE_COMPANY)
        .build();

    when(companyRepository.existsByEmail(testEmail)).thenReturn(true);
    when(companyRepository.findByEmail(testEmail)).thenReturn(Optional.of(company));
    when(passwordEncoder.encode(temporaryPassword)).thenReturn(encodedPassword);
    doNothing().when(mailComponent).sendTemporaryPassword(testEmail, temporaryPassword);

    CommonUserService spyService = spy(commonUserService);
    doReturn(temporaryPassword).when(spyService).generateTemporaryPassword();

    assertDoesNotThrow(
        () -> spyService.sendTemporaryPassword(testEmail, testCompanyName));

    verify(companyRepository, times(1)).save(company);
    verify(mailComponent, times(1)).sendTemporaryPassword(testEmail, temporaryPassword);
  }

  @Test
  @DisplayName("임시 비밀번호 전송 : 성공 - 지원자")
  public void testSendTemporaryPassword_CandidateSuccess() {
    String testEmail = "test@company.com";
    String testCandidateName = "TestCandidate";
    String temporaryPassword = "Password123!";
    String encodedPassword = "encodedTemporaryPassword123";

    CandidateEntity candidate = CandidateEntity.builder()
        .email(testEmail)
        .name(testCandidateName)
        .candidateKey("candidateKey")
        .phoneNumber("010-0000-0000")
        .password("Password000!")
        .role(ROLE_CANDIDATE)
        .build();

    when(candidateRepository.existsByEmail(testEmail)).thenReturn(true);
    when(candidateRepository.findByEmail(testEmail)).thenReturn(Optional.of(candidate));
    when(passwordEncoder.encode(temporaryPassword)).thenReturn(encodedPassword);
    doNothing().when(mailComponent).sendTemporaryPassword(testEmail, temporaryPassword);

    CommonUserService spyService = spy(commonUserService);
    doReturn(temporaryPassword).when(spyService).generateTemporaryPassword();

    assertDoesNotThrow(
        () -> spyService.sendTemporaryPassword(testEmail, testCandidateName));

    verify(candidateRepository, times(1)).save(candidate);
    verify(mailComponent, times(1)).sendTemporaryPassword(testEmail, temporaryPassword);
  }

  @Test
  @DisplayName("임시 비밀번호 전송 : 실패 - 회사 계정을 찾을 수 없음")
  public void testSendTemporaryPassword_CompanyNotFound() {
    String testEmail = "test@company.com";
    String testCompanyName = "TestCompany";

    when(companyRepository.existsByEmail(testEmail)).thenReturn(true);
    when(companyRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

    assertThrows(CustomException.class, () ->
        commonUserService.sendTemporaryPassword(testEmail, testCompanyName));
  }

  @Test
  @DisplayName("임시 비밀번호 전송 : 실패 - 지원자 계정을 찾을 수 없음")
  public void testSendTemporaryPassword_CandidateNotFound() {
    String testEmail = "test@company.com";
    String testCompanyName = "TestCompany";

    when(companyRepository.existsByEmail(testEmail)).thenReturn(true);
    when(companyRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

    assertThrows(CustomException.class, () ->
        commonUserService.sendTemporaryPassword(testEmail, testCompanyName));
  }

  @Test
  @DisplayName("임시 비밀번호 전송 : 실패 - 이메일 전송 실패")
  public void testSendTemporaryPassword_EmailSendFailure() {
    String testEmail = "test@company.com";
    String testCompanyName = "TestCompany";
    String temporaryPassword = "Password123!";
    String encodedPassword = "encodedTemporaryPassword123";

    CompanyEntity company = CompanyEntity.builder()
        .email(testEmail)
        .companyName(testCompanyName)
        .password("Password000!")
        .role(ROLE_CANDIDATE)
        .companyNumber("02-0000-0000")
        .companyKey("companyKey")
        .build();

    when(companyRepository.existsByEmail(testEmail)).thenReturn(true);
    when(companyRepository.findByEmail(testEmail)).thenReturn(Optional.of(company));
    when(passwordEncoder.encode(temporaryPassword)).thenReturn(encodedPassword);
    doThrow(new CustomException(EMAIL_SEND_FAILURE)).when(mailComponent)
        .sendTemporaryPassword(testEmail, temporaryPassword);

    CommonUserService spyService = spy(commonUserService);
    doReturn(temporaryPassword).when(spyService).generateTemporaryPassword();

    assertThrows(CustomException.class, () ->
        spyService.sendTemporaryPassword(testEmail, testCompanyName));
  }

  @Test
  @DisplayName("COMPANY 로그인 성공 테스트")
  public void testLoginUser_company_success() {
    // given
    String email = "test@company.com";
    String companyName = "TestCompany";
    String password = "password123#";
    String encodedPassword = "encodedPassword";
    String mockedCompanyKey = "companyKey";

    when(keyGenerator.generateKey()).thenReturn(mockedCompanyKey);
    String companyKey = keyGenerator.generateKey();

    CompanyEntity company = CompanyEntity.builder()
        .email(email)
        .companyName(companyName)
        .password(encodedPassword)
        .role(UserRole.ROLE_COMPANY)
        .companyKey(companyKey)
        .build();

    when(companyRepository.findByEmail(email)).thenReturn(Optional.of(company));
    when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

    // when
    SignInDto.Response response = commonUserService.loginUser(email, password);

    // then
    assertEquals(email, response.getEmail());
    assertEquals(companyKey, response.getKey());
    assertEquals(companyName, response.getName());
    assertEquals(UserRole.ROLE_COMPANY, response.getRole());
  }

  @Test
  @DisplayName("CANDIDATE 로그인 성공 테스트")
  public void testLoginUser_candidate_success() {
    // given
    String email = "test@candidate.com";
    String name = "TestCandidate";
    String password = "password123#";
    String encodedPassword = "encodedPassword";
    String mockedCandidateKey = "candidateKey";

    // KeyGenerator mock 설정
    when(keyGenerator.generateKey()).thenReturn(mockedCandidateKey);
    String candidateKey = keyGenerator.generateKey();

    CandidateEntity candidate = CandidateEntity.builder()
        .email(email)
        .name(name)
        .password(encodedPassword)
        .role(ROLE_CANDIDATE)
        .candidateKey(candidateKey)
        .build();

    when(candidateRepository.findByEmail(email)).thenReturn(Optional.of(candidate));
    when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

    // when
    SignInDto.Response response = commonUserService.loginUser(email, password);

    // then
    assertEquals(email, response.getEmail());
    assertEquals(candidateKey, response.getKey());
    assertEquals(name, response.getName());
    assertEquals(ROLE_CANDIDATE, response.getRole());
  }

  @Test
  @DisplayName("비밀번호 불일치 테스트")
  public void testLoginUser_passwordMismatch() {
    // given
    String email = "test@email.com";
    String encodedPassword = "encodedPassword";
    String wrongPassword = "wrongPassword";

    CompanyEntity company = CompanyEntity.builder()
        .email(email)
        .password(encodedPassword)
        .role(UserRole.ROLE_COMPANY)
        .build();

    when(companyRepository.findByEmail(email)).thenReturn(Optional.of(company));
    when(passwordEncoder.matches(wrongPassword, encodedPassword)).thenReturn(false);

    // when
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> commonUserService.loginUser(email, wrongPassword));

    // then
    assertEquals("이메일 또는 비밀번호가 일치하지 않습니다.", exception.getMessage());
  }

  @Test
  @DisplayName("가입되지 않은 이메일 테스트")
  public void testLoginUser_emailNotFound() {
    // given
    String email = "test@email.com";
    String password = "password123#";

    when(companyRepository.findByEmail(email)).thenReturn(Optional.empty());
    when(candidateRepository.findByEmail(email)).thenReturn(Optional.empty());

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> commonUserService.loginUser(email, password));

    // then
    assertEquals("이메일 또는 비밀번호가 일치하지 않습니다.", exception.getMessage());
  }

  @Test
  @DisplayName("로그아웃 테스트")
  void logoutUser() {
    // given
    String token = "testToken";
    // 실제로 동작하지 않아도 됨
    doNothing().when(blacklistTokenService).addToBlacklist(token);

    // when
    commonUserService.logoutUser(token);

    // then
    verify(blacklistTokenService, times(1)).addToBlacklist(token);
  }
}