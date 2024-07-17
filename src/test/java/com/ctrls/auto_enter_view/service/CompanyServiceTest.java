package com.ctrls.auto_enter_view.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.dto.company.SignUpDto;
import com.ctrls.auto_enter_view.dto.company.SignUpDto.Request;
import com.ctrls.auto_enter_view.dto.company.SignUpDto.Response;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
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
  @DisplayName("회사 회원가입_성공")
  void signUp_Success() {
    // given
    String email = "company@naver.com";
    String password = "test1234!";
    String encodedPassword = "encodedPassword";
    String companyName = "testName";
    String companyNumber = "010-0000-0000";

    SignUpDto.Request request = SignUpDto.Request.builder()
        .email(email)
        .verificationCode("1234")
        .password(password)
        .companyName(companyName)
        .companyNumber(companyNumber)
        .build();

    CompanyEntity saved = CompanyEntity.builder()
        .email(email)
        .password(encodedPassword)
        .companyName(companyName)
        .companyNumber(companyNumber)
        .build();

    ArgumentCaptor<CompanyEntity> captor = ArgumentCaptor.forClass(CompanyEntity.class);

    // when
    when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
    when(
        companyRepository.save(argThat(e -> e.getPassword().equals(encodedPassword)))).thenReturn(
        saved);

    // execute
    Response response = companyService.signUp(request);

    // then
    verify(companyRepository, times(1)).save(captor.capture());
    CompanyEntity captured = captor.getValue();

    // assert captured
    assertEquals(request.getEmail(), captured.getEmail());
    assertEquals(encodedPassword, captured.getPassword());
    assertEquals(request.getCompanyName(), captured.getCompanyName());
    assertEquals(request.getCompanyNumber(), captured.getCompanyNumber());

    // assert response
    assertEquals(saved.getEmail(), response.getEmail());
    assertEquals(saved.getCompanyName(), response.getName());
  }

  @Test
  @DisplayName("회사 회원가입_실패_잘못된 포맷")
  void signUp_WrongFormat() {
    // given
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    String wrongForm = "wrongForm";

    SignUpDto.Request request = SignUpDto.Request.builder()
        .email(wrongForm)
        .verificationCode("1234")
        .password(wrongForm)
        .companyName("testName")
        .companyNumber(wrongForm)
        .build();

    List<String> expectedWrongFormats = new ArrayList<>(
        Arrays.asList("email", "password", "companyNumber"));

    List<String> actualWrongFormats = new ArrayList<>();

    // when
    Set<ConstraintViolation<Request>> validated = validator.validate(request);

    if (!validated.isEmpty()) {
      for (ConstraintViolation<Request> violation : validated) {
        String propertyPath = violation.getPropertyPath().toString();
        actualWrongFormats.add(propertyPath);
      }
    }

    // then
    assertTrue(expectedWrongFormats.size() == actualWrongFormats.size()
        && actualWrongFormats.containsAll(expectedWrongFormats));
    verify(companyRepository, times(0)).save(any());
  }

  @Test
  @DisplayName("회사 회원탈퇴_성공")
  void withdraw_Success() {
    // given
    String companyKey = "companyKey";
    String email = "company@naver.com";
    String password = "password";

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email(email)
        .password(password)
        .build();

    UserDetails userDetails = User.withUsername(email).password(password)
        .roles("COMPANY").build();
    SecurityContextHolder.setContext(securityContext);

    // when
    when(securityContext.getAuthentication()).thenReturn(
        new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
            userDetails.getAuthorities()));
    when(companyRepository.findByEmail(userDetails.getUsername())).thenReturn(
        Optional.of(companyEntity));

    // execute
    companyService.withdraw(companyKey);

    // then
    verify(companyRepository, times(1)).delete(companyEntity);
  }

  @Test
  @DisplayName("회사 회원탈퇴_실패_잘못된 비밀번호")
  void withdraw_Failure_WrongPassword() {
    // given
    String companyKey = "companyKey";
    String email = "company@naver.com";
    String password = "password";

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email(email)
        .password(password)
        .build();

    UserDetails userDetails = User.withUsername(email).password(password)
        .roles("COMPANY").build();
    SecurityContextHolder.setContext(securityContext);

    when(securityContext.getAuthentication()).thenReturn(
        new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
            userDetails.getAuthorities()));
    when(companyRepository.findByEmail(userDetails.getUsername())).thenReturn(
        Optional.of(companyEntity));

    // execute
    CustomException exception = assertThrows(CustomException.class,
        () -> companyService.withdraw(companyKey));

    // then
    assertEquals(ErrorCode.PASSWORD_NOT_MATCH, exception.getErrorCode());
    verify(companyRepository, times(0)).delete(any());
  }
}