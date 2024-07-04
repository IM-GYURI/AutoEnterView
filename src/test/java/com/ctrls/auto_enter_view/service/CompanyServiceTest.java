package com.ctrls.auto_enter_view.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.dto.company.ChangePasswordDto;
import com.ctrls.auto_enter_view.dto.company.SignUpDto;
import com.ctrls.auto_enter_view.dto.company.SignUpDto.Request;
import com.ctrls.auto_enter_view.dto.company.SignUpDto.Response;
import com.ctrls.auto_enter_view.dto.company.WithdrawDto;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private SecurityContext securityContext;

  @InjectMocks
  private CompanyService companyService;

  @Test
  void signUp_Success() {
    // given
    SignUpDto.Request request = SignUpDto.Request.builder()
        .email("company@naver.com")
        .verificationCode("1234")
        .password("test1234!")
        .companyName("testName")
        .companyNumber("010-0000-0000")
        .build();

    CompanyEntity saved = CompanyEntity.builder()
        .email("company@naver.com")
        .password("encodedPassword")
        .companyName("testName")
        .companyNumber("010-0000-0000")
        .build();

    ArgumentCaptor<CompanyEntity> captor = ArgumentCaptor.forClass(CompanyEntity.class);

    // when
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(companyRepository.save(any())).thenReturn(saved);

    Response response = companyService.signUp(request);

    // then
    verify(companyRepository, times(1)).save(captor.capture());
    assertEquals(request.getEmail(), captor.getValue().getEmail());
    assertEquals(saved.getEmail(), response.getEmail());
  }

  @Test
  void signUp_WrongFormat() {
    // given
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    SignUpDto.Request request = SignUpDto.Request.builder()
        .email("wrongform")
        .verificationCode("1234")
        .password("wrongform")
        .companyName("testName")
        .companyNumber("wrongform")
        .build();

    // when
    Set<ConstraintViolation<Request>> validated = validator.validate(request);

    // then
    assertThrows(ConstraintViolationException.class, () -> {
      if (!validated.isEmpty()) {
        throw new ConstraintViolationException(validated);
      }

      companyService.signUp(request);
    });
    verify(companyRepository, times(0)).save(any());
  }

  @Test
  void changePassword_Success() {
    // given
    String companyKey = "companyKey";

    ChangePasswordDto.Request request = ChangePasswordDto.Request.builder()
        .oldPassword("oldPassword")
        .newPassword("newPassword")
        .build();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("company@naver.com")
        .password("oldPassword")
        .build();

    UserDetails userDetails = User.withUsername("company@naver.com").password("oldPassword")
        .roles("COMPANY").build();

    SecurityContextHolder.setContext(securityContext);

    when(securityContext.getAuthentication()).thenReturn(
        new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
            userDetails.getAuthorities()));
    when(companyRepository.findByEmail(anyString())).thenReturn(Optional.of(companyEntity));
    when(passwordEncoder.matches(request.getOldPassword(), companyEntity.getPassword())).thenReturn(
        true);
    when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPassword");

    // when
    companyService.changePassword(companyKey, request);

    // then
    verify(companyRepository, times(1)).save(companyEntity);
    assertEquals("encodedNewPassword", companyEntity.getPassword());
  }

  @Test
  void changePassword_Failure_WrongOldPassword() {
    // given
    String companyKey = "companyKey";

    ChangePasswordDto.Request request = ChangePasswordDto.Request.builder()
        .oldPassword("wrongOldPassword")
        .newPassword("newPassword")
        .build();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("company@naver.com")
        .password("oldPassword")
        .build();

    UserDetails userDetails = User.withUsername("company@naver.com").password("oldPassword")
        .roles("COMPANY").build();

    SecurityContextHolder.setContext(securityContext);

    when(securityContext.getAuthentication()).thenReturn(
        new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
            userDetails.getAuthorities()));
    when(companyRepository.findByEmail(anyString())).thenReturn(Optional.of(companyEntity));
    when(passwordEncoder.matches(request.getOldPassword(), companyEntity.getPassword())).thenReturn(
        false);

    // when & then
    assertThrows(RuntimeException.class, () -> companyService.changePassword(companyKey, request));
    verify(companyRepository, never()).save(any());
  }

  @Test
  void withdraw_Success() {
    // given
    String companyKey = "companyKey";

    WithdrawDto.Request request = WithdrawDto.Request.builder()
        .password("password")
        .build();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("company@naver.com")
        .password("password")
        .build();

    UserDetails userDetails = User.withUsername("company@naver.com").password("password")
        .roles("COMPANY").build();

    SecurityContextHolder.setContext(securityContext);

    when(securityContext.getAuthentication()).thenReturn(
        new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
            userDetails.getAuthorities()));
    when(companyRepository.findByEmail(anyString())).thenReturn(Optional.of(companyEntity));
    when(passwordEncoder.matches(request.getPassword(), companyEntity.getPassword())).thenReturn(
        true);

    // when
    companyService.withdraw(companyKey, request);

    // then
    verify(companyRepository, times(1)).delete(companyEntity);
  }

  @Test
  void withdraw_Failure_WrongPassword() {
    // given
    String companyKey = "companyKey";

    WithdrawDto.Request request = WithdrawDto.Request.builder()
        .password("wrongPassword")
        .build();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("company@naver.com")
        .password("password")
        .build();

    UserDetails userDetails = User.withUsername("company@naver.com").password("password")
        .roles("COMPANY").build();

    SecurityContextHolder.setContext(securityContext);

    when(securityContext.getAuthentication()).thenReturn(
        new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
            userDetails.getAuthorities()));
    when(companyRepository.findByEmail(anyString())).thenReturn(Optional.of(companyEntity));
    when(passwordEncoder.matches(request.getPassword(), companyEntity.getPassword())).thenReturn(
        false);

    // when & then
    assertThrows(RuntimeException.class, () -> companyService.withdraw(companyKey, request));
    verify(companyRepository, never()).delete(any());
  }
}