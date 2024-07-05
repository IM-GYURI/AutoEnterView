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
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.exception.implement.NonUsableEmailException;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
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

    NonUsableEmailException exception = assertThrows(NonUsableEmailException.class, () -> {
      commonUserService.checkDuplicateEmail("test@example.com");
    });

    assertEquals("사용할 수 없는 이메일입니다.", exception.getMessage());
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

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      commonUserService.verifyEmailVerificationCode("test@example.com", "654321");
    });

    assertEquals("유효하지 않은 인증 코드입니다.", exception.getMessage());
  }

  @Test
  public void testVerifyEmailVerificationCode_notFound() {
    ValueOperations<String, String> valueOpsMock = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

    when(valueOpsMock.get(anyString())).thenReturn(null);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      commonUserService.verifyEmailVerificationCode("test@example.com", "123456");
    });

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

}