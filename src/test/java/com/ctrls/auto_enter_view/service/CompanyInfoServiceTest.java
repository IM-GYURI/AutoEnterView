package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.UserRole.ROLE_COMPANY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.dto.company.ReadCompanyInfoDto.Response;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.CompanyInfoEntity;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CompanyInfoRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class CompanyInfoServiceTest {

  @Mock
  private CompanyInfoRepository companyInfoRepository;

  @Mock
  private CompanyRepository companyRepository;

  @InjectMocks
  private CompanyInfoService companyInfoService;

//  @BeforeAll
//  static void setup() {
//    User user = new User("company@naver.com", "test1234!",
//        List.of(new SimpleGrantedAuthority(UserRole.ROLE_COMPANY.name())));
//    SecurityContextHolder.getContext().setAuthentication(
//        new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
//  }

//  @Test
//  @DisplayName("회사 정보 생성_성공")
//  void createInfo_Success() {
//    // given
//    String companyKey = "companyKey";
//
//    Request request = Request.builder()
//        .employees(1)
//        .companyAge(LocalDate.now())
//        .companyUrl("testUrl")
//        .boss("testBoss")
//        .address("testAddress")
//        .build();
//
//    CompanyEntity companyEntity = CompanyEntity.builder()
//        .companyKey(companyKey)
//        .build();
//
//    // when
//    when(companyRepository.findByEmail("company@naver.com")).thenReturn(Optional.of(companyEntity));
//
//    // then
//    assertDoesNotThrow(
//        // execute
//        () -> companyInfoService.createInfo(companyKey, request));
//  }

//  @Test
//  @DisplayName("회사 정보 생성_실패_중복")
//  void createInfo_Failure_AlreadyExists() {
//    // given
//    String companyKey = "companyKey";
//
//    Request request = Request.builder()
//        .employees(1)
//        .companyAge(LocalDate.now())
//        .companyUrl("testUrl")
//        .boss("testBoss")
//        .address("testAddress")
//        .build();
//
//    CompanyEntity companyEntity = CompanyEntity.builder()
//        .companyKey(companyKey)
//        .build();
//
//    // when
//    when(companyRepository.findByEmail("company@naver.com")).thenReturn(Optional.of(companyEntity));
//    when(companyInfoRepository.existsByCompanyKey(companyKey)).thenReturn(true);
//
//    // then
//    CustomException exception = assertThrows(CustomException.class,
//        // execute
//        () -> companyInfoService.createInfo(companyKey, request));
//
//    assertEquals(ErrorCode.ALREADY_EXISTS, exception.getErrorCode());
//  }

  @Test
  @DisplayName("회사 정보 조회_성공")
  void readInfo_Success() {
    // given
    String companyKey = "companyKey";

    CompanyInfoEntity companyInfoEntity = CompanyInfoEntity.builder()
        .companyKey(companyKey)
        .companyAge(LocalDate.now())
        .companyUrl("testUrl")
        .boss("testBoss")
        .address("testAddress")
        .build();

    // when
    when(companyInfoRepository.findByCompanyKey(companyKey)).thenReturn(
        Optional.of(companyInfoEntity));

    // execute
    Response response = companyInfoService.readInfo(companyKey);

    // then
    assertEquals("testBoss", response.getBoss());
  }

  @Test
  @DisplayName("회사 정보 조회_성공_빈 값")
  void readInfo_Success_Empty() {
    // given
    String companyKey = "companyKey";

    // when
    when(companyInfoRepository.findByCompanyKey(companyKey)).thenReturn(Optional.empty());

    // execute
    Response response = companyInfoService.readInfo(companyKey);

    // then
    assertEquals(0, response.getEmployees());
  }

//  @Test
//  @DisplayName("회사 정보 수정_성공")
//  void updateInfo_Success() {
//    // given
//    String companyKey = "companyKey";
//
//    Request request = Request.builder()
//        .employees(5)
//        .build();
//
//    CompanyEntity companyEntity = CompanyEntity.builder()
//        .companyKey(companyKey)
//        .build();
//
//    CompanyInfoEntity companyInfoEntity = CompanyInfoEntity.builder()
//        .companyKey(companyKey)
//        .employees(3)
//        .build();
//
//    // when
//    when(companyRepository.findByEmail("company@naver.com")).thenReturn(Optional.of(companyEntity));
//    when(companyInfoRepository.findByCompanyKey(companyKey)).thenReturn(
//        Optional.of(companyInfoEntity));
//
//    // execute
//    companyInfoService.updateInfo(companyKey, request);
//
//    // then
//    verify(companyInfoRepository, times(1)).save(companyInfoEntity);
//    assertEquals(5, companyInfoEntity.getEmployees());
//  }

//  @Test
//  @DisplayName("회사 정보 수정_실패_없음")
//  void updateInfo_Failure_NotFound() {
//    // given
//    String companyKey = "companyKey";
//
//    Request request = Request.builder()
//        .employees(5)
//        .build();
//
//    CompanyEntity companyEntity = CompanyEntity.builder()
//        .companyKey(companyKey)
//        .build();
//
//    // when
//    when(companyRepository.findByEmail("company@naver.com")).thenReturn(Optional.of(companyEntity));
//    when(companyInfoRepository.findByCompanyKey(companyKey)).thenReturn(Optional.empty());
//
//    // then
//    CustomException exception = assertThrows(CustomException.class,
//        // execute
//        () -> companyInfoService.updateInfo(companyKey, request));
//
//    assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
//  }

  @Test
  @DisplayName("회사 정보 삭제 : 성공")
  void testDeleteInfo_Success() {
    String companyKey = "companyKey";
    String email = "test@example.com";

    CompanyEntity companyEntity = CompanyEntity.builder()
        .email(email)
        .companyKey(companyKey)
        .companyName("TestCompany")
        .build();

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn(email);
    when(companyRepository.findByEmail(email)).thenReturn(Optional.of(companyEntity));

    companyInfoService.deleteInfo(userDetails, companyKey);

    verify(companyInfoRepository, times(1)).deleteByCompanyKey(companyKey);
  }

  @Test
  @DisplayName("회사 정보 삭제 : 실패 - 회사 계정을 찾을 수 없음")
  void testDeleteInfo_CompanyNotFoundFailure() {
    String companyKey = "companyKey";
    String email = "test@example.com";

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn(email);
    when(companyRepository.findByEmail(email)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () ->
        companyInfoService.deleteInfo(userDetails, companyKey)
    );

    assertEquals("가입된 사용자 이메일이 없습니다.", exception.getMessage());
  }

  @Test
  @DisplayName("회사 정보 삭제 : 실패 - 권한 없음")
  void testDeleteInfo_NoAuthorityFailure() {
    String companyKey1 = "companyKey1";
    String email = "test@example.com";
    String companyKey2 = "companyKey2";

    CompanyEntity companyEntity = CompanyEntity.builder()
        .email(email)
        .companyKey(companyKey2)
        .companyName("TestCompany")
        .role(ROLE_COMPANY)
        .companyNumber("02-0000-0000")
        .password("Password123!")
        .build();

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn(email);
    when(companyRepository.findByEmail(email)).thenReturn(Optional.of(companyEntity));

    CustomException exception = assertThrows(CustomException.class, () ->
        companyInfoService.deleteInfo(userDetails, companyKey1)
    );

    assertEquals("권한이 없습니다.", exception.getMessage());
  }
}