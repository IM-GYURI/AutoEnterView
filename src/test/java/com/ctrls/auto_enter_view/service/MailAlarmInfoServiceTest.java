package com.ctrls.auto_enter_view.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.dto.mailAlarmInfo.MailAlarmInfoDto;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleEntity;
import com.ctrls.auto_enter_view.entity.MailAlarmInfoEntity;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.InterviewScheduleRepository;
import com.ctrls.auto_enter_view.repository.MailAlarmInfoRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

@ExtendWith(MockitoExtension.class)
class MailAlarmInfoServiceTest {

  @Mock
  private MailAlarmInfoRepository mailAlarmInfoRepository;

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private InterviewScheduleRepository interviewScheduleRepository;

  @Mock
  private Scheduler scheduler;

  @Spy
  @InjectMocks
  private MailAlarmInfoService mailAlarmInfoService;

  private User principal;
  private SecurityContext securityContext;

  @BeforeEach
  void setUp() {
    principal = new User("test@example.com", "password", new ArrayList<>());
    securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(new TestingAuthenticationToken(principal, null));
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @DisplayName("메일 예약 성공")
  void createMailAlarmInfo_shouldCreateMailAlarmInfoSuccessfully() throws SchedulerException {
    // given
    String companyKey = "companyKey";
    String interviewScheduleKey = "interviewScheduleKey";
    Long stepId = 1L;
    MailAlarmInfoDto mailAlarmInfoDto = MailAlarmInfoDto.builder()
        .mailContent("메일 안내문")
        .mailSendDateTime(LocalDateTime.now().plusDays(1))
        .build();

    CompanyEntity company = CompanyEntity.builder()
        .companyKey(companyKey)
        .build();

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .jobPostingKey("jobPostingKey")
        .build();

    when(companyRepository.findByEmail(any())).thenReturn(Optional.of(company));
    when(interviewScheduleRepository.findByInterviewScheduleKey(any())).thenReturn(
        Optional.of(interviewScheduleEntity));
    when(mailAlarmInfoRepository.save(any(MailAlarmInfoEntity.class))).thenReturn(
        new MailAlarmInfoEntity());

    // Mock the scheduler
    when(scheduler.scheduleJob(any(JobDetail.class), any(Trigger.class))).thenReturn(null);

    // when
    mailAlarmInfoService.createMailAlarmInfo(companyKey, interviewScheduleKey, stepId,
        mailAlarmInfoDto);

    // then
    verify(mailAlarmInfoRepository, times(1)).save(any(MailAlarmInfoEntity.class));
    verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
  }

  @Test
  @DisplayName("메일 예약 실패 - 시간이 현재 시간 이전일 때 예외 발생")
  void createMailAlarmInfo_shouldThrowExceptionWhenMailSendDateTimeIsBeforeNow() {
    // given
    String companyKey = "companyKey";
    String interviewScheduleKey = "interviewScheduleKey";
    Long stepId = 1L;
    MailAlarmInfoDto mailAlarmInfoDto = MailAlarmInfoDto.builder()
        .mailContent("메일 안내문")
        .mailSendDateTime(LocalDateTime.now().minusDays(1))
        .build();

    // when & then
    assertThrows(CustomException.class, () ->
        mailAlarmInfoService.createMailAlarmInfo(companyKey, interviewScheduleKey, stepId,
            mailAlarmInfoDto));
  }

  @Test
  @DisplayName("예약 메일 수정 성공")
  void editMailAlarmInfo_shouldEditMailAlarmInfoSuccessfully() throws Exception {
    // given
    String companyKey = "companyKey";
    String interviewScheduleKey = "interviewScheduleKey";
    Long stepId = 1L;

    MailAlarmInfoDto mailAlarmInfoDto = MailAlarmInfoDto.builder()
        .mailContent("수정된 메일 내용")
        .mailSendDateTime(LocalDateTime.now().plusDays(1))
        .build();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .build();

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .interviewScheduleKey(interviewScheduleKey)
        .jobPostingKey("jobPostingKey")
        .build();

    MailAlarmInfoEntity existingMailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .id(1L)
        .interviewScheduleKey(interviewScheduleKey)
        .jobPostingStepId(stepId)
        .mailContent("기존 메일 내용")
        .mailSendDateTime(LocalDateTime.now().plusDays(2))
        .build();

    // Mock 설정
    when(companyRepository.findByEmail(principal.getUsername())).thenReturn(
        Optional.of(companyEntity));
    when(mailAlarmInfoRepository.findByInterviewScheduleKey(interviewScheduleKey)).thenReturn(
        Optional.of(existingMailAlarmInfoEntity));

    doNothing().when(mailAlarmInfoService).unscheduleMailJob(any(MailAlarmInfoEntity.class));
    doNothing().when(mailAlarmInfoService).scheduleMailJob(any(MailAlarmInfoEntity.class));

    // when
    mailAlarmInfoService.editMailAlarmInfo(companyKey, interviewScheduleKey, stepId,
        mailAlarmInfoDto);

    // then
    verify(mailAlarmInfoService, times(1)).unscheduleMailJob(any(MailAlarmInfoEntity.class));
    verify(mailAlarmInfoService, times(1)).scheduleMailJob(any(MailAlarmInfoEntity.class));
  }

  @Test
  @DisplayName("메일 수정 실패 - 시간이 현재 시간 이전일 때 예외 발생")
  void editMailAlarmInfo_shouldThrowExceptionWhenMailSendDateTimeIsBeforeNow() {
    // given
    String companyKey = "companyKey";
    String interviewScheduleKey = "interviewScheduleKey";
    Long stepId = 1L;
    MailAlarmInfoDto mailAlarmInfoDto = MailAlarmInfoDto.builder()
        .mailContent("수정된 메일 안내문")
        .mailSendDateTime(LocalDateTime.now().minusDays(1))
        .build();

    // when & then
    assertThrows(CustomException.class, () ->
        mailAlarmInfoService.editMailAlarmInfo(companyKey, interviewScheduleKey, stepId,
            mailAlarmInfoDto));
  }
}