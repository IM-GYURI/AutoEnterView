package com.ctrls.auto_enter_view.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

  @InjectMocks
  private CommonUserService commonUserService;

  @BeforeEach
  public void setUp() {

    MockitoAnnotations.openMocks(this);
    when(redisTemplate.opsForValue()).thenReturn(valueOperationsMock);
  }

  @Test
  public void testCheckDuplicateEmail_available() {

    when(companyRepository.existsByEmail(anyString())).thenReturn(false);
    when(candidateRepository.existsByEmail(anyString())).thenReturn(false);

    String result = commonUserService.checkDuplicateEmail("test@example.com");

    assertEquals("사용 가능한 이메일입니다.", result);
  }

  @Test
  public void testCheckDuplicateEmail_unavailable() {

    when(companyRepository.existsByEmail(anyString())).thenReturn(true);

    CustomException exception = assertThrows(CustomException.class, () -> {
      commonUserService.checkDuplicateEmail("test@example.com");
    });

    assertEquals(ErrorCode.EMAIL_DUPLICATION.getMessage(), exception.getMessage());
  }

  @Test
  public void testSendVerificationCode() {

    assertDoesNotThrow(() -> {
      commonUserService.sendVerificationCode("test@example.com");
    });

    verify(redisTemplate, times(1)).opsForValue();
    verify(valueOperationsMock, times(1)).set(eq("test@example.com"), anyString(), eq(5L),
        eq(TimeUnit.MINUTES));
    verify(mailComponent, times(1)).sendVerificationCode(eq("test@example.com"), anyString());
  }

  @Test
  public void testVerifyEmailVerificationCode_valid() {

    ValueOperations<String, String> valueOpsMock = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

    when(valueOpsMock.get(anyString())).thenReturn("123456");

    assertDoesNotThrow(
        () -> commonUserService.verifyEmailVerificationCode("test@example.com", "123456"));
  }

  @Test
  public void testVerifyEmailVerificationCode_invalid() {

    ValueOperations<String, String> valueOpsMock = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

    when(valueOpsMock.get(anyString())).thenReturn("123456");

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> commonUserService.verifyEmailVerificationCode("test@example.com", "654321"));

    assertEquals("유효하지 않은 인증 코드입니다.", exception.getMessage());
  }

  @Test
  public void testVerifyEmailVerificationCode_notFound() {

    ValueOperations<String, String> valueOpsMock = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

    when(valueOpsMock.get(anyString())).thenReturn(null);

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> commonUserService.verifyEmailVerificationCode("test@example.com", "123456"));

    assertEquals("유효하지 않은 인증 코드입니다.", exception.getMessage());
  }

  @Test
  public void testSendTemporaryPassword_company() {

    CompanyEntity company = CompanyEntity.builder()
        .email("test@company.com")
        .companyName("TestCompany")
        .build();

    when(companyRepository.existsByEmail(anyString())).thenReturn(true);
    when(companyRepository.findByEmail(anyString())).thenReturn(Optional.of(company));
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    doNothing().when(mailComponent).sendTemporaryPassword(anyString(), anyString());

    assertDoesNotThrow(
        () -> commonUserService.sendTemporaryPassword("test@company.com", "TestCompany"));

    verify(companyRepository, times(1)).save(any(CompanyEntity.class));
  }

  @Test
  public void testSendTemporaryPassword_candidate() {

    CandidateEntity candidate = CandidateEntity.builder()
        .email("test@candidate.com")
        .name("TestCandidate")
        .build();

    when(candidateRepository.existsByEmail(anyString())).thenReturn(true);
    when(candidateRepository.findByEmail(anyString())).thenReturn(Optional.of(candidate));
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    doNothing().when(mailComponent).sendTemporaryPassword(anyString(), anyString());

    assertDoesNotThrow(
        () -> commonUserService.sendTemporaryPassword("test@candidate.com", "TestCandidate"));

    verify(candidateRepository, times(1)).save(any(CandidateEntity.class));
  }

  @Test
  @DisplayName("COMPANY 로그인 성공 테스트")
  public void testLoginUser_company_success() {
    // given
    CompanyEntity company = CompanyEntity.builder()
        .email("test@company.com")
        .companyName("TestCompany")
        .password("encodedPassword")
        .role(UserRole.ROLE_COMPANY)
        .build();

    when(companyRepository.findByEmail(anyString())).thenReturn(Optional.of(company));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    when(jwtTokenProvider.generateToken(anyString(), any(UserRole.class))).thenReturn(
        "generatedToken");

    // when
    SignInDto.Response response = commonUserService.loginUser("test@company.com", "password123");

    // then
    assertEquals(company.getEmail(), response.getEmail());
    assertEquals(company.getCompanyKey(), response.getKey());
    assertEquals(company.getCompanyName(), response.getName());
    assertEquals("generatedToken", response.getToken());
    assertEquals(company.getRole(), response.getRole());
  }

  @Test
  @DisplayName("CANDIDATE 로그인 성공 테스트")
  public void testLoginUser_candidate_success() {
    // given
    CandidateEntity candidate = CandidateEntity.builder()
        .email("test@candidate.com")
        .name("TestCandidate")
        .password("encodedPassword") // Assuming already encoded
        .role(UserRole.ROLE_CANDIDATE)
        .build();

    when(candidateRepository.findByEmail(anyString())).thenReturn(Optional.of(candidate));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    when(jwtTokenProvider.generateToken(anyString(), any(UserRole.class))).thenReturn(
        "generatedToken");

    // when
    SignInDto.Response response = commonUserService.loginUser("test@candidate.com", "password123");

    // then
    assertEquals(candidate.getEmail(), response.getEmail());
    assertEquals(candidate.getCandidateKey(), response.getKey());
    assertEquals(candidate.getName(), response.getName());
    assertEquals("generatedToken", response.getToken());
    assertEquals(candidate.getRole(), response.getRole());
  }

  @Test
  @DisplayName("비밀번호 불일치 테스트")
  public void testLoginUser_passwordMismatch() {
    // given
    CompanyEntity company = CompanyEntity.builder()
        .email("test@company.com")
        .password("encodedPassword") // Assuming already encoded
        .role(UserRole.ROLE_COMPANY)
        .build();

    // Mocking behavior
    when(companyRepository.findByEmail(anyString())).thenReturn(Optional.of(company));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(
        false); // Password does not match

    // when
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      commonUserService.loginUser("test@company.com", "wrongPassword");
    });

    // then
    assertEquals("비밀번호가 일치하지 않습니다.", exception.getMessage());
  }

  @Test
  @DisplayName("가입되지 않은 이메일 테스트")
  public void testLoginUser_emailNotFound() {
    // given
    // Mocking behavior
    when(companyRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    when(candidateRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    // when
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      commonUserService.loginUser("nonexistent@example.com", "password123");
    });

    // then
    assertEquals("가입된 정보가 없습니다.", exception.getMessage());
  }


}