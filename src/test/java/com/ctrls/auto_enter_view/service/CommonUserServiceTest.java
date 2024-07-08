package com.ctrls.auto_enter_view.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.component.MailComponent;
import com.ctrls.auto_enter_view.dto.common.SignInDto;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.enums.UserRole;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.security.JwtTokenProvider;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

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
  private ValueOperations<String, String> valueOperationsMock;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private BlacklistTokenService blacklistTokenService;

  @InjectMocks
  private CommonUserService commonUserService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    when(redisTemplate.opsForValue()).thenReturn(valueOperationsMock);
  }

  @Test
  @DisplayName("이메일 중복 확인 : 사용 가능")
  public void testCheckDuplicateEmail_available() {
    String testEmail = "test@example.com";

    when(companyRepository.existsByEmail(testEmail)).thenReturn(false);
    when(candidateRepository.existsByEmail(testEmail)).thenReturn(false);

    String result = commonUserService.checkDuplicateEmail(testEmail);

    assertEquals("사용 가능한 이메일입니다.", result);
  }

  @Test
  @DisplayName("이메일 중복 확인 : 사용 불가")
  public void testCheckDuplicateEmail_unavailable() {
    String testEmail = "test@example.com";

    when(companyRepository.existsByEmail(testEmail)).thenReturn(true);

    CustomException exception = assertThrows(CustomException.class, () -> {
      commonUserService.checkDuplicateEmail(testEmail);
    });

    assertEquals(ErrorCode.EMAIL_DUPLICATION.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("이메일 인증 코드 전송")
  public void testSendVerificationCode() {
    String testEmail = "test@example.com";
    long expirationTime = 5L;
    TimeUnit expirationUnit = TimeUnit.MINUTES;

    when(redisTemplate.opsForValue()).thenReturn(valueOperationsMock);

    ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

    assertDoesNotThrow(() -> {
      commonUserService.sendVerificationCode(testEmail);
    });

    verify(redisTemplate, times(1)).opsForValue();
    verify(valueOperationsMock, times(1)).set(eq(testEmail), codeCaptor.capture(),
        eq(expirationTime), eq(expirationUnit));
    verify(mailComponent, times(1)).sendVerificationCode(eq(testEmail), codeCaptor.capture());

    String capturedCode = codeCaptor.getValue();

    System.out.println(capturedCode);
    assertNotNull(capturedCode);
    assertEquals(6, capturedCode.length(), "인증 코드는 6자리 문자여야 합니다.");
    assertTrue(capturedCode.matches("[A-Za-z0-9!@#$%^&*()+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]+"),
        "인증 코드는 영문 대소문자, 숫자, 특수문자로 이루어져야 합니다.");
  }

  @Test
  @DisplayName("인증 코드 확인 : 성공 - 유효한 코드")
  public void testVerifyEmailVerificationCode_valid() {
    String testEmail = "test@example.com";
    String correctVerificationCode = "123456";

    ValueOperations<String, String> valueOpsMock = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

    when(valueOpsMock.get(testEmail)).thenReturn(correctVerificationCode);

    assertDoesNotThrow(
        () -> commonUserService.verifyEmailVerificationCode(testEmail, correctVerificationCode));
  }

  @Test
  @DisplayName("인증 코드 확인 : 실패 - 유효하지 않은 코드")
  public void testVerifyEmailVerificationCode_invalid() {
    String testEmail = "test@example.com";
    String correctVerificationCode = "123456";
    String incorrectVerificationCode = "654321";

    ValueOperations<String, String> valueOpsMock = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

    when(valueOpsMock.get(testEmail)).thenReturn(correctVerificationCode);

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> commonUserService.verifyEmailVerificationCode(testEmail, incorrectVerificationCode));

    assertEquals("유효하지 않은 인증 코드입니다.", exception.getMessage());
  }

  @Test
  @DisplayName("인증 코드 확인 : 실패 - 존재하지 않는 코드")
  public void testVerifyEmailVerificationCode_notFound() {
    String testEmail = "test@example.com";
    String correctVerificationCode = "123456";

    ValueOperations<String, String> valueOpsMock = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

    when(valueOpsMock.get(testEmail)).thenReturn(null);

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> commonUserService.verifyEmailVerificationCode(testEmail, correctVerificationCode));

    assertEquals("유효하지 않은 인증 코드입니다.", exception.getMessage());
  }

  @Test
  @DisplayName("임시 비밀번호 전송 - 회사")
  public void testSendTemporaryPassword_company() {
    CompanyEntity company = CompanyEntity.builder()
        .email("test@company.com")
        .companyName("TestCompany")
        .build();

    when(companyRepository.existsByEmail("test@company.com")).thenReturn(true);
    when(companyRepository.findByEmail("test@company.com")).thenReturn(Optional.of(company));

    String encodedPassword = passwordEncoder.encode("testPassword123");

    when(passwordEncoder.encode("testPassword123")).thenReturn(encodedPassword);

    doNothing().when(mailComponent).sendTemporaryPassword("test@company.com", encodedPassword);

    assertDoesNotThrow(
        () -> commonUserService.sendTemporaryPassword("test@company.com", "TestCompany"));

    verify(companyRepository, times(1)).save(any());
  }

  @Test
  @DisplayName("임시 비밀번호 전송 - 지원자")
  public void testSendTemporaryPassword_candidate() {
    CandidateEntity candidate = CandidateEntity.builder()
        .email("test@candidate.com")
        .name("TestCandidate")
        .build();

    when(candidateRepository.existsByEmail("test@candidate.com")).thenReturn(true);
    when(candidateRepository.findByEmail("test@candidate.com")).thenReturn(Optional.of(candidate));

    String encodedPassword = passwordEncoder.encode("testPassword456");

    when(passwordEncoder.encode("testPassword456")).thenReturn(encodedPassword);

    doNothing().when(mailComponent).sendTemporaryPassword("test@candidate.com", encodedPassword);

    assertDoesNotThrow(
        () -> commonUserService.sendTemporaryPassword("test@candidate.com", "TestCandidate"));

    verify(candidateRepository, times(1)).save(any(CandidateEntity.class));
  }

  @Test
  @DisplayName("COMPANY 로그인 성공 테스트")
  public void testLoginUser_company_success() {
    // given
    String email = "test@company.com";
    String companyName = "TestCompany";
    String password = "password123#";
    String encodedPassword = "encodedPassword";
    String generatedToken = "generatedToken";

    CompanyEntity company = CompanyEntity.builder()
        .email(email)
        .companyName(companyName)
        .password(encodedPassword)
        .role(UserRole.ROLE_COMPANY)
        .build();

    when(companyRepository.findByEmail(email)).thenReturn(Optional.of(company));
    when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
    when(jwtTokenProvider.generateToken(email, UserRole.ROLE_COMPANY)).thenReturn(generatedToken);

    // when
    SignInDto.Response response = commonUserService.loginUser(email, password);

    // then
    assertEquals(email, response.getEmail());
    assertEquals(company.getCompanyKey(), response.getKey());
    assertEquals(companyName, response.getName());
    assertEquals(generatedToken, response.getToken());
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
    String generatedToken = "generatedToken";

    CandidateEntity candidate = CandidateEntity.builder()
        .email(email)
        .name(name)
        .password(encodedPassword)
        .role(UserRole.ROLE_CANDIDATE)
        .build();

    when(candidateRepository.findByEmail(email)).thenReturn(Optional.of(candidate));
    when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
    when(jwtTokenProvider.generateToken(email, UserRole.ROLE_CANDIDATE)).thenReturn(generatedToken);

    // when
    SignInDto.Response response = commonUserService.loginUser(email, password);

    // then
    assertEquals(email, response.getEmail());
    assertEquals(candidate.getCandidateKey(), response.getKey());
    assertEquals(name, response.getName());
    assertEquals(generatedToken, response.getToken());
    assertEquals(UserRole.ROLE_CANDIDATE, response.getRole());
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
    RuntimeException exception = assertThrows(RuntimeException.class, () -> commonUserService.loginUser(email, wrongPassword));

    // then
    assertEquals("비밀번호가 일치하지 않습니다.", exception.getMessage());
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
    RuntimeException exception = assertThrows(RuntimeException.class, () -> commonUserService.loginUser(email, password));

    // then
    assertEquals("가입된 정보가 없습니다.", exception.getMessage());
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