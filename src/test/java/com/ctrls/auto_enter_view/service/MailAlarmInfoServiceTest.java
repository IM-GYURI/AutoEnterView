package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.Education.BACHELOR;
import static com.ctrls.auto_enter_view.enums.ErrorCode.FAILED_MAIL_SCHEDULING;
import static com.ctrls.auto_enter_view.enums.ErrorCode.INTERVIEW_SCHEDULE_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.MAIL_ALARM_INFO_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.JobCategory.BACKEND;
import static com.ctrls.auto_enter_view.enums.UserRole.ROLE_CANDIDATE;
import static com.ctrls.auto_enter_view.enums.UserRole.ROLE_COMPANY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.component.MailComponent;
import com.ctrls.auto_enter_view.dto.mailAlarmInfo.MailAlarmInfoDto;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleParticipantsEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.entity.MailAlarmInfoEntity;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.InterviewScheduleRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import com.ctrls.auto_enter_view.repository.MailAlarmInfoRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class MailAlarmInfoServiceTest {

  @Mock
  private MailAlarmInfoRepository mailAlarmInfoRepository;

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private JobPostingRepository jobPostingRepository;

  @Mock
  private JobPostingStepRepository jobPostingStepRepository;

  @Mock
  private InterviewScheduleRepository interviewScheduleRepository;

  @Mock
  private MailComponent mailComponent;

  @Mock
  private UserDetails userDetails;

  @Mock
  private Scheduler scheduler;

  @InjectMocks
  private MailAlarmInfoService mailAlarmInfoService;

  @Test
  @DisplayName("메일 예약 생성 : 성공")
  void createMailAlarmInfo_Success() throws SchedulerException {
    String companyKey = "companyKey";
    String jobPostingKey = "jobPostingKey";
    Long stepId = 1L;
    LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1);

    CompanyEntity company = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("company@example.com")
        .password("Password123!")
        .companyName("companyName")
        .companyNumber("02-0000-0000")
        .role(ROLE_COMPANY)
        .build();

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .interviewScheduleKey("interviewScheduleKey")
        .jobPostingKey(jobPostingKey)
        .jobPostingStepId(stepId)
        .firstInterviewDate(LocalDate.parse("2024-04-04"))
        .lastInterviewDate(LocalDate.parse("2024-04-06"))
        .build();

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("company@example.com");

    when(companyRepository.findByEmail("company@example.com")).thenReturn(Optional.of(company));
    when(interviewScheduleRepository.findByJobPostingKeyAndJobPostingStepId(jobPostingKey, stepId))
        .thenReturn(Optional.of(interviewScheduleEntity));

    MailAlarmInfoDto mailAlarmInfoDto = MailAlarmInfoDto.builder()
        .mailSendDateTime(futureDateTime)
        .mailContent("mailContent")
        .build();

    MailAlarmInfoEntity mailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .interviewScheduleKey("interviewScheduleKey")
        .jobPostingStepId(stepId)
        .jobPostingKey(jobPostingKey)
        .mailContent("mailContent")
        .mailSendDateTime(futureDateTime)
        .build();

    when(mailAlarmInfoRepository.save(any(MailAlarmInfoEntity.class))).thenReturn(
        mailAlarmInfoEntity);

    when(scheduler.scheduleJob(any(JobDetail.class), any(Trigger.class)))
        .thenReturn(new Date());

    mailAlarmInfoService.createMailAlarmInfo(companyKey, jobPostingKey, stepId, mailAlarmInfoDto,
        userDetails);

    verify(mailAlarmInfoRepository).save(any(MailAlarmInfoEntity.class));
    verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
  }

  @Test
  @DisplayName("메일 예약 생성 : 실패 - INTERVIEW_SCHEDULE_NOT_FOUND")
  void createMailAlarmInfo_Failure_InterviewScheduleNotFoundFailure() {
    String companyKey = "companyKey";
    String jobPostingKey = "jobPostingKey";
    Long stepId = 1L;
    LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1);

    CompanyEntity company = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("company@example.com")
        .password("Password123!")
        .companyName("companyName")
        .companyNumber("02-0000-0000")
        .role(ROLE_COMPANY)
        .build();

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("company@example.com");

    when(companyRepository.findByEmail("company@example.com")).thenReturn(Optional.of(company));
    when(interviewScheduleRepository.findByJobPostingKeyAndJobPostingStepId(jobPostingKey, stepId))
        .thenReturn(Optional.empty());

    MailAlarmInfoDto mailAlarmInfoDto = MailAlarmInfoDto.builder()
        .mailSendDateTime(futureDateTime)
        .mailContent("mailContent")
        .build();

    CustomException thrownException = assertThrows(CustomException.class, () -> {
      mailAlarmInfoService.createMailAlarmInfo(companyKey, jobPostingKey, stepId, mailAlarmInfoDto,
          userDetails);
    });

    assertEquals("해당 면접 일정을 찾을 수 없습니다.", thrownException.getMessage());
  }

  @Test
  @DisplayName("메일 예약 생성 : 실패 - MAIL_ALARM_TIME_BEFORE_NOW")
  void createMailAlarmInfo_MailAlarmTimeBeforeNowFailure() {
    String companyKey = "companyKey";
    String interviewScheduleKey = "interviewScheduleKey";
    Long stepId = 1L;

    MailAlarmInfoDto mailAlarmInfoDto = MailAlarmInfoDto.builder()
        .mailContent("메일 안내문")
        .mailSendDateTime(LocalDateTime.now().minusDays(1))
        .build();

    CustomException thrownException = assertThrows(CustomException.class, () ->
        mailAlarmInfoService.createMailAlarmInfo(companyKey, interviewScheduleKey, stepId,
            mailAlarmInfoDto, userDetails));

    assertEquals("사용자를 찾을 수 없습니다.", thrownException.getMessage());
  }

  @Test
  @DisplayName("메일 예약 생성 : 실패 - FAILED_MAIL_SCHEDULING")
  void createMailAlarmInfo_FailedMailSchedulingFailure() throws SchedulerException {
    String companyKey = "companyKey";
    String jobPostingKey = "jobPostingKey";
    Long stepId = 1L;
    LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1);

    CompanyEntity company = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("company@example.com")
        .password("Password123!")
        .companyName("companyName")
        .companyNumber("02-0000-0000")
        .role(ROLE_COMPANY)
        .build();

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .interviewScheduleKey("interviewScheduleKey")
        .jobPostingKey(jobPostingKey)
        .jobPostingStepId(stepId)
        .firstInterviewDate(LocalDate.parse("2024-04-04"))
        .lastInterviewDate(LocalDate.parse("2024-04-06"))
        .build();

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("company@example.com");

    when(companyRepository.findByEmail("company@example.com")).thenReturn(Optional.of(company));
    when(interviewScheduleRepository.findByJobPostingKeyAndJobPostingStepId(jobPostingKey, stepId))
        .thenReturn(Optional.of(interviewScheduleEntity));

    MailAlarmInfoDto mailAlarmInfoDto = MailAlarmInfoDto.builder()
        .mailSendDateTime(futureDateTime)
        .mailContent("mailContent")
        .build();

    MailAlarmInfoEntity mailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .interviewScheduleKey("interviewScheduleKey")
        .jobPostingStepId(stepId)
        .jobPostingKey(jobPostingKey)
        .mailContent("mailContent")
        .mailSendDateTime(futureDateTime)
        .build();

    when(mailAlarmInfoRepository.save(any(MailAlarmInfoEntity.class))).thenReturn(
        mailAlarmInfoEntity);

    doThrow(new SchedulerException("Scheduler exception"))
        .when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));

    CustomException thrownException = assertThrows(CustomException.class, () -> {
      mailAlarmInfoService.createMailAlarmInfo(companyKey, jobPostingKey, stepId, mailAlarmInfoDto,
          userDetails);
    });

    assertEquals(FAILED_MAIL_SCHEDULING, thrownException.getErrorCode());
  }

  @Test
  @DisplayName("메일 예약 조회 : 성공")
  void getMailAlarmInfo_Success() {
    String companyKey = "companyKey";
    String jobPostingKey = "jobPostingKey";
    Long stepId = 1L;
    String email = "test@example.com";

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn(email);

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email(email)
        .build();

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .interviewScheduleKey("interviewScheduleKey")
        .lastInterviewDate(LocalDate.of(2025, 5, 1))
        .build();

    MailAlarmInfoEntity mailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .mailContent("메일 내용")
        .mailSendDateTime(LocalDateTime.now().plusDays(1))
        .build();

    when(companyRepository.findByEmail(email))
        .thenReturn(Optional.of(companyEntity));
    when(interviewScheduleRepository.findByJobPostingKeyAndJobPostingStepId(jobPostingKey, stepId))
        .thenReturn(Optional.of(interviewScheduleEntity));
    when(mailAlarmInfoRepository.findByInterviewScheduleKey(
        interviewScheduleEntity.getInterviewScheduleKey()))
        .thenReturn(Optional.of(mailAlarmInfoEntity));

    MailAlarmInfoDto result = mailAlarmInfoService.getMailAlarmInfo(companyKey, jobPostingKey,
        stepId, userDetails);

    assertNotNull(result);
    assertEquals("메일 내용", result.getMailContent());
    assertNotNull(result.getMailSendDateTime());
  }

  @Test
  @DisplayName("메일 예약 조회 : 실패 - USER_NOT_FOUND")
  void getMailAlarmInfo_CompanyNotFoundFailure() {
    String companyKey = "companyKey";
    String jobPostingKey = "jobPostingKey";
    Long stepId = 1L;
    String email = "test@example.com";

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn(email);

    when(companyRepository.findByEmail(email))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () ->
        mailAlarmInfoService.getMailAlarmInfo(companyKey, jobPostingKey, stepId, userDetails)
    );

    assertEquals(USER_NOT_FOUND, thrownException.getErrorCode());
  }

  @Test
  @DisplayName("메일 예약 조회 : 실패 - INTERVIEW_SCHEDULE_NOT_FOUND")
  void getMailAlarmInfo_InterviewScheduleNotFoundFailure() {
    String companyKey = "companyKey";
    String jobPostingKey = "jobPostingKey";
    Long stepId = 1L;
    String email = "test@example.com";

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn(email);

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email(email)
        .build();

    when(companyRepository.findByEmail(email))
        .thenReturn(Optional.of(companyEntity));
    when(interviewScheduleRepository.findByJobPostingKeyAndJobPostingStepId(jobPostingKey, stepId))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () ->
        mailAlarmInfoService.getMailAlarmInfo(companyKey, jobPostingKey, stepId, userDetails)
    );

    assertEquals(INTERVIEW_SCHEDULE_NOT_FOUND, thrownException.getErrorCode());
  }

  @Test
  @DisplayName("메일 예약 조회 : 실패 - MAIL_ALARM_INFO_NOT_FOUND")
  void getMailAlarmInfo_MailAlarmInfoNotFoundFailure() {
    String companyKey = "companyKey";
    String jobPostingKey = "jobPostingKey";
    Long stepId = 1L;
    String email = "test@example.com";

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn(email);

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email(email)
        .build();

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .interviewScheduleKey("interviewScheduleKey")
        .lastInterviewDate(LocalDate.of(2025, 5, 1))
        .build();

    when(companyRepository.findByEmail(email))
        .thenReturn(Optional.of(companyEntity));
    when(interviewScheduleRepository.findByJobPostingKeyAndJobPostingStepId(jobPostingKey, stepId))
        .thenReturn(Optional.of(interviewScheduleEntity));
    when(mailAlarmInfoRepository.findByInterviewScheduleKey(
        interviewScheduleEntity.getInterviewScheduleKey()))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () ->
        mailAlarmInfoService.getMailAlarmInfo(companyKey, jobPostingKey, stepId, userDetails)
    );

    assertEquals(MAIL_ALARM_INFO_NOT_FOUND, thrownException.getErrorCode());
  }

  @Test
  @DisplayName("예약된 메일 수정 : 성공")
  void editMailAlarmInfo_Success() throws SchedulerException {
    String companyKey = "companyKey";
    String jobPostingKey = "jobPostingKey";
    Long stepId = 1L;
    LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1);

    CompanyEntity company = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("company@example.com")
        .password("Password123!")
        .companyName("companyName")
        .companyNumber("02-0000-0000")
        .role(ROLE_COMPANY)
        .build();

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .interviewScheduleKey("interviewScheduleKey")
        .jobPostingKey(jobPostingKey)
        .jobPostingStepId(stepId)
        .firstInterviewDate(LocalDate.parse("2024-04-04"))
        .lastInterviewDate(LocalDate.parse("2024-04-06"))
        .build();

    MailAlarmInfoEntity existingMailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .interviewScheduleKey("interviewScheduleKey")
        .jobPostingStepId(stepId)
        .jobPostingKey(jobPostingKey)
        .mailContent("oldContent")
        .mailSendDateTime(LocalDateTime.now().minusDays(1))
        .build();

    MailAlarmInfoDto mailAlarmInfoDto = MailAlarmInfoDto.builder()
        .mailSendDateTime(futureDateTime)
        .mailContent("newContent")
        .build();

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("company@example.com");

    when(companyRepository.findByEmail("company@example.com")).thenReturn(Optional.of(company));
    when(interviewScheduleRepository.findByJobPostingKeyAndJobPostingStepId(jobPostingKey, stepId))
        .thenReturn(Optional.of(interviewScheduleEntity));
    when(mailAlarmInfoRepository.findByInterviewScheduleKey("interviewScheduleKey"))
        .thenReturn(Optional.of(existingMailAlarmInfoEntity));

    doReturn(true).when(scheduler).unscheduleJob(any(TriggerKey.class));
    doReturn(new Date()).when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));

    mailAlarmInfoService.editMailAlarmInfo(companyKey, jobPostingKey, stepId, mailAlarmInfoDto,
        userDetails);

    MailAlarmInfoEntity updatedMailAlarmInfoEntity = mailAlarmInfoRepository.findByInterviewScheduleKey(
        "interviewScheduleKey").orElseThrow();

    assertEquals("newContent", updatedMailAlarmInfoEntity.getMailContent());
    assertEquals(futureDateTime, updatedMailAlarmInfoEntity.getMailSendDateTime());

    verify(scheduler).unscheduleJob(any(TriggerKey.class));
    verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
  }

  @Test
  @DisplayName("예약된 메일 수정 : 실패 - INTERVIEW_SCHEDULE_NOT_FOUND")
  void editMailAlarmInfo_InterviewScheduleNotFoundFailure() {
    String companyKey = "companyKey";
    String jobPostingKey = "jobPostingKey";
    Long stepId = 1L;

    MailAlarmInfoDto mailAlarmInfoDto = MailAlarmInfoDto.builder()
        .mailSendDateTime(LocalDateTime.now().plusDays(1))
        .mailContent("newContent")
        .build();

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("company@example.com");

    CompanyEntity company = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("company@example.com")
        .password("Password123!")
        .companyName("companyName")
        .companyNumber("02-0000-0000")
        .role(ROLE_COMPANY)
        .build();

    when(companyRepository.findByEmail("company@example.com")).thenReturn(Optional.of(company));
    when(interviewScheduleRepository.findByJobPostingKeyAndJobPostingStepId(jobPostingKey, stepId))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () -> {
      mailAlarmInfoService.editMailAlarmInfo(companyKey, jobPostingKey, stepId, mailAlarmInfoDto,
          userDetails);
    });

    assertEquals("해당 면접 일정을 찾을 수 없습니다.", thrownException.getMessage());
  }

  @Test
  @DisplayName("예약된 메일 수정 : 실패 - MAIL_ALARM_INFO_NOT_FOUND")
  void editMailAlarmInfo_MailAlarmInfoNotFoundFailure() {
    String companyKey = "companyKey";
    String jobPostingKey = "jobPostingKey";
    Long stepId = 1L;

    MailAlarmInfoDto mailAlarmInfoDto = MailAlarmInfoDto.builder()
        .mailSendDateTime(LocalDateTime.now().plusDays(1))
        .mailContent("newContent")
        .build();

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("company@example.com");

    CompanyEntity company = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("company@example.com")
        .password("Password123!")
        .companyName("companyName")
        .companyNumber("02-0000-0000")
        .role(ROLE_COMPANY)
        .build();

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .interviewScheduleKey("interviewScheduleKey")
        .jobPostingKey(jobPostingKey)
        .jobPostingStepId(stepId)
        .firstInterviewDate(LocalDate.parse("2024-04-04"))
        .lastInterviewDate(LocalDate.parse("2024-04-06"))
        .build();

    when(companyRepository.findByEmail("company@example.com")).thenReturn(Optional.of(company));
    when(interviewScheduleRepository.findByJobPostingKeyAndJobPostingStepId(jobPostingKey, stepId))
        .thenReturn(Optional.of(interviewScheduleEntity));
    when(mailAlarmInfoRepository.findByInterviewScheduleKey("interviewScheduleKey"))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () -> {
      mailAlarmInfoService.editMailAlarmInfo(companyKey, jobPostingKey, stepId, mailAlarmInfoDto,
          userDetails);
    });

    assertEquals("예약된 메일 내역을 찾을 수 없습니다.", thrownException.getMessage());
  }

  @Test
  @DisplayName("예약된 메일 수정 : 실패 - MAIL_ALARM_TIME_BEFORE_NOW")
  void editMailAlarmInfo_MailAlarmTimeBeforeNowFailure() {
    String companyKey = "companyKey";
    String jobPostingKey = "jobPostingKey";
    Long stepId = 1L;

    MailAlarmInfoDto mailAlarmInfoDto = MailAlarmInfoDto.builder()
        .mailSendDateTime(LocalDateTime.now().minusDays(1))
        .mailContent("newContent")
        .build();

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("company@example.com");

    CompanyEntity company = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("company@example.com")
        .password("Password123!")
        .companyName("companyName")
        .companyNumber("02-0000-0000")
        .role(ROLE_COMPANY)
        .build();

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .interviewScheduleKey("interviewScheduleKey")
        .jobPostingKey(jobPostingKey)
        .jobPostingStepId(stepId)
        .firstInterviewDate(LocalDate.parse("2024-04-04"))
        .lastInterviewDate(LocalDate.parse("2024-04-06"))
        .build();

    MailAlarmInfoEntity existingMailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .interviewScheduleKey("interviewScheduleKey")
        .jobPostingStepId(stepId)
        .jobPostingKey(jobPostingKey)
        .mailContent("oldContent")
        .mailSendDateTime(LocalDateTime.now().plusDays(1))
        .build();

    when(companyRepository.findByEmail("company@example.com")).thenReturn(Optional.of(company));
    when(interviewScheduleRepository.findByJobPostingKeyAndJobPostingStepId(jobPostingKey, stepId))
        .thenReturn(Optional.of(interviewScheduleEntity));
    when(mailAlarmInfoRepository.findByInterviewScheduleKey("interviewScheduleKey"))
        .thenReturn(Optional.of(existingMailAlarmInfoEntity));

    CustomException thrownException = assertThrows(CustomException.class, () -> {
      mailAlarmInfoService.editMailAlarmInfo(companyKey, jobPostingKey, stepId, mailAlarmInfoDto,
          userDetails);
    });

    assertEquals("메일 예약 발송 시간은 현재 이후여야 합니다.", thrownException.getMessage());
  }

  @Test
  @DisplayName("예약된 메일 수정 : 실패 - FAILED_MAIL_SCHEDULING")
  void editMailAlarmInfo_FailedMailSchedulingFailure() throws SchedulerException {
    String companyKey = "companyKey";
    String jobPostingKey = "jobPostingKey";
    Long stepId = 1L;
    LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1);

    CompanyEntity company = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("company@example.com")
        .password("Password123!")
        .companyName("companyName")
        .companyNumber("02-0000-0000")
        .role(ROLE_COMPANY)
        .build();

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .interviewScheduleKey("interviewScheduleKey")
        .jobPostingKey(jobPostingKey)
        .jobPostingStepId(stepId)
        .firstInterviewDate(LocalDate.parse("2024-04-04"))
        .lastInterviewDate(LocalDate.parse("2024-04-06"))
        .build();

    MailAlarmInfoEntity existingMailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .interviewScheduleKey("interviewScheduleKey")
        .jobPostingStepId(stepId)
        .jobPostingKey(jobPostingKey)
        .mailContent("oldContent")
        .mailSendDateTime(LocalDateTime.now().minusDays(1))
        .build();

    MailAlarmInfoDto mailAlarmInfoDto = MailAlarmInfoDto.builder()
        .mailSendDateTime(futureDateTime)
        .mailContent("newContent")
        .build();

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("company@example.com");

    when(companyRepository.findByEmail("company@example.com")).thenReturn(Optional.of(company));
    when(interviewScheduleRepository.findByJobPostingKeyAndJobPostingStepId(jobPostingKey, stepId))
        .thenReturn(Optional.of(interviewScheduleEntity));
    when(mailAlarmInfoRepository.findByInterviewScheduleKey("interviewScheduleKey"))
        .thenReturn(Optional.of(existingMailAlarmInfoEntity));

    doReturn(true).when(scheduler).unscheduleJob(any(TriggerKey.class));
    doThrow(new SchedulerException()).when(scheduler)
        .scheduleJob(any(JobDetail.class), any(Trigger.class));

    CustomException thrownException = assertThrows(CustomException.class, () -> {
      mailAlarmInfoService.editMailAlarmInfo(companyKey, jobPostingKey, stepId, mailAlarmInfoDto,
          userDetails);
    });

    assertEquals("메일 예약 등록을 실패했습니다.", thrownException.getMessage());
  }

  @Test
  @DisplayName("기존의 스케줄링된 작업 삭제 : 성공")
  void unscheduleMailJob_Success() throws SchedulerException {
    MailAlarmInfoEntity mailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .id(1L)
        .interviewScheduleKey("interviewScheduleKey")
        .jobPostingStepId(1L)
        .jobPostingKey("jobPostingKey")
        .mailContent("mailContent")
        .mailSendDateTime(LocalDateTime.now().minusDays(1))
        .build();

    TriggerKey triggerKey = TriggerKey.triggerKey("mailTrigger1", "mailGroup");

    when(scheduler.unscheduleJob(any(TriggerKey.class))).thenReturn(true);

    mailAlarmInfoService.unscheduleMailJob(mailAlarmInfoEntity);

    verify(scheduler, times(1)).unscheduleJob(triggerKey);
  }

  @Test
  @DisplayName("기존의 스케줄링된 작업 삭제 : 실패 - FAILED_MAIL_UNSCHEDULING")
  void unscheduleMailJob_FailedMailUnschedulingFailure() throws SchedulerException {
    MailAlarmInfoEntity mailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .id(1L)
        .interviewScheduleKey("interviewScheduleKey")
        .jobPostingStepId(1L)
        .jobPostingKey("jobPostingKey")
        .mailContent("mailContent")
        .mailSendDateTime(LocalDateTime.now().minusDays(1))
        .build();

    when(scheduler.unscheduleJob(any(TriggerKey.class))).thenThrow(new SchedulerException());

    CustomException thrownException = assertThrows(CustomException.class, () -> {
      mailAlarmInfoService.unscheduleMailJob(mailAlarmInfoEntity);
    });

    assertEquals("메일 예약 취소를 실패했습니다.", thrownException.getMessage());
  }

  @Test
  @DisplayName("면접 일정 이메일 발송 : 성공")
  void sendInterviewMailToCandidates_Success() {
    String jobPostingKey = "jobPostingKey";
    String companyKey = "companyKey";
    String candidateKey = "candidateKey";

    MailAlarmInfoEntity mailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .jobPostingKey(jobPostingKey)
        .jobPostingStepId(1L)
        .mailContent("메일 내용")
        .build();

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey(companyKey)
        .title("제목")
        .jobCategory(BACKEND)
        .education(BACHELOR)
        .startDate(LocalDate.parse("2025-04-02"))
        .endDate(LocalDate.parse(("2025-04-05")))
        .passingNumber(2)
        .career(3)
        .salary(30000000L)
        .workTime("무관")
        .workLocation("주소")
        .employmentType("인턴")
        .jobPostingContent("공고 내용")
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .jobPostingKey(jobPostingKey)
        .step("서류 단계")
        .build();

    LocalDateTime interviewStart = LocalDateTime.of(2024, 7, 25, 15, 48);
    LocalDateTime interviewEnd = interviewStart.plusMinutes(30);

    Duration duration = Duration.between(interviewStart, interviewEnd);
    long minutesDifference = duration.toMinutes();

    InterviewScheduleParticipantsEntity participant = InterviewScheduleParticipantsEntity.builder()
        .candidateKey(candidateKey)
        .candidateName("candidate")
        .id(1L)
        .interviewScheduleKey("interviewScheduleKey")
        .jobPostingKey(jobPostingKey)
        .interviewStartDatetime(interviewStart)
        .interviewEndDatetime(interviewEnd)
        .build();

    CandidateEntity candidate = CandidateEntity.builder()
        .candidateKey(candidateKey)
        .email("test@example.com")
        .name("candidate")
        .phoneNumber("010-0000-0000")
        .password("Password123!")
        .role(ROLE_CANDIDATE)
        .build();

    when(jobPostingRepository.findByJobPostingKey(mailAlarmInfoEntity.getJobPostingKey()))
        .thenReturn(Optional.of(jobPostingEntity));
    when(jobPostingStepRepository.findById(mailAlarmInfoEntity.getJobPostingStepId()))
        .thenReturn(Optional.of(jobPostingStepEntity));
    when(candidateRepository.findByCandidateKey(participant.getCandidateKey()))
        .thenReturn(Optional.of(candidate));

    doNothing().when(mailComponent)
        .sendHtmlMail(anyString(), anyString(), anyString(), anyBoolean());

    mailAlarmInfoService.sendInterviewMailToCandidates(
        Collections.singletonList(participant), mailAlarmInfoEntity);

    verify(mailComponent, times(1)).sendHtmlMail(
        eq("test@example.com"),
        eq("면접 일정 알림 : " + jobPostingEntity.getTitle() + " - " + jobPostingStepEntity.getStep()),
        contains(
            "지원해주신 " + jobPostingEntity.getTitle() + "의 " + jobPostingStepEntity.getStep()
                + " 면접 일정 안내드립니다.<br><br><strong>"
                + "면접 일시 : " + interviewStart.format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE HH:mm"))
                + "</strong><br><br>면접 시간 : " + minutesDifference + "분<br><br>"
                + mailAlarmInfoEntity.getMailContent()),
        eq(true)
    );
  }

  @Test
  @DisplayName("면접 일정 이메일 발송 : 실패 - JOB_POSTING_NOT_FOUND ")
  void sendInterviewMailToCandidates_JobPostingNotFoundFailure() {
    MailAlarmInfoEntity mailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .jobPostingKey("jobPostingKey")
        .jobPostingStepId(1L)
        .mailContent("메일 내용")
        .build();

    InterviewScheduleParticipantsEntity participant = InterviewScheduleParticipantsEntity.builder()
        .candidateKey("candidateKey")
        .interviewStartDatetime(LocalDateTime.now())
        .interviewEndDatetime(LocalDateTime.now().plusMinutes(30))
        .build();

    when(jobPostingRepository.findByJobPostingKey(mailAlarmInfoEntity.getJobPostingKey()))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () -> {
      mailAlarmInfoService.sendInterviewMailToCandidates(
          Collections.singletonList(participant), mailAlarmInfoEntity);
    });

    assertEquals("채용 공고를 찾을 수 없습니다.", thrownException.getMessage());
  }

  @Test
  @DisplayName("면접 일정 이메일 발송 : 실패 - JOB_POSTING_STEP_NOT_FOUND")
  void sendInterviewMailToCandidates_JobPostingStepNotFoundFailure() {
    MailAlarmInfoEntity mailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .jobPostingKey("jobPostingKey")
        .jobPostingStepId(1L)
        .mailContent("메일 내용")
        .build();

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey("jobPostingKey")
        .title("제목")
        .build();

    InterviewScheduleParticipantsEntity participant = InterviewScheduleParticipantsEntity.builder()
        .candidateKey("candidateKey")
        .interviewStartDatetime(LocalDateTime.now())
        .interviewEndDatetime(LocalDateTime.now().plusMinutes(30))
        .build();

    when(jobPostingRepository.findByJobPostingKey(mailAlarmInfoEntity.getJobPostingKey()))
        .thenReturn(Optional.of(jobPostingEntity));

    when(jobPostingStepRepository.findById(mailAlarmInfoEntity.getJobPostingStepId()))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () -> {
      mailAlarmInfoService.sendInterviewMailToCandidates(
          Collections.singletonList(participant), mailAlarmInfoEntity);
    });
    assertEquals("채용 공고의 해당 단계를 찾을 수 없습니다.", thrownException.getMessage());
  }

  @Test
  @DisplayName("면접 일정 이메일 발송 : 실패 - USER_NOT_FOUND ")
  void sendInterviewMailToCandidates_UserNotFoundFailure() {
    MailAlarmInfoEntity mailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .jobPostingKey("jobPostingKey")
        .jobPostingStepId(1L)
        .mailContent("메일 내용")
        .build();

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey("jobPostingKey")
        .title("제목")
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .jobPostingKey("jobPostingKey")
        .step("서류 단계")
        .build();

    InterviewScheduleParticipantsEntity participant = InterviewScheduleParticipantsEntity.builder()
        .candidateKey("candidateKey")
        .interviewStartDatetime(LocalDateTime.now())
        .interviewEndDatetime(LocalDateTime.now().plusMinutes(30))
        .build();

    when(jobPostingRepository.findByJobPostingKey(mailAlarmInfoEntity.getJobPostingKey()))
        .thenReturn(Optional.of(jobPostingEntity));
    when(jobPostingStepRepository.findById(mailAlarmInfoEntity.getJobPostingStepId()))
        .thenReturn(Optional.of(jobPostingStepEntity));

    when(candidateRepository.findByCandidateKey(participant.getCandidateKey()))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () -> {
      mailAlarmInfoService.sendInterviewMailToCandidates(
          Collections.singletonList(participant), mailAlarmInfoEntity);
    });
    assertEquals("사용자를 찾을 수 없습니다.", thrownException.getMessage());
  }

  @Test
  @DisplayName("과제 일정 이메일 발송 : 성공")
  void sendTaskMailToCandidates_Success() {
    String jobPostingKey = "jobPostingKey";
    String interviewScheduleKey = "interviewScheduleKey";
    String candidateKey = "candidateKey";
    String candidateEmail = "test@example.com";

    MailAlarmInfoEntity mailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .jobPostingKey(jobPostingKey)
        .jobPostingStepId(1L)
        .interviewScheduleKey(interviewScheduleKey)
        .mailContent("메일 내용")
        .build();

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .title("제목")
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .jobPostingKey(jobPostingKey)
        .step("서류 단계")
        .build();

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .interviewScheduleKey(interviewScheduleKey)
        .lastInterviewDate(LocalDate.of(2025, 5, 1))
        .build();

    LocalDateTime scheduledDateTime = LocalDateTime.of(
        interviewScheduleEntity.getLastInterviewDate(),
        LocalTime.of(23, 59, 0));

    CandidateListEntity participant = CandidateListEntity.builder()
        .candidateKey(candidateKey)
        .build();

    CandidateEntity candidate = CandidateEntity.builder()
        .candidateKey(candidateKey)
        .email(candidateEmail)
        .build();

    when(jobPostingRepository.findByJobPostingKey(mailAlarmInfoEntity.getJobPostingKey()))
        .thenReturn(Optional.of(jobPostingEntity));
    when(jobPostingStepRepository.findById(mailAlarmInfoEntity.getJobPostingStepId()))
        .thenReturn(Optional.of(jobPostingStepEntity));
    when(interviewScheduleRepository.findByInterviewScheduleKey(
        mailAlarmInfoEntity.getInterviewScheduleKey()))
        .thenReturn(Optional.of(interviewScheduleEntity));
    when(candidateRepository.findByCandidateKey(participant.getCandidateKey()))
        .thenReturn(Optional.of(candidate));

    doNothing().when(mailComponent)
        .sendHtmlMail(anyString(), anyString(), anyString(), anyBoolean());

    mailAlarmInfoService.sendTaskMailToCandidates(
        Collections.singletonList(participant), mailAlarmInfoEntity);

    verify(mailComponent, times(1)).sendHtmlMail(
        eq(candidateEmail),
        eq("과제 일정 알림 : " + jobPostingEntity.getTitle() + " - " + jobPostingStepEntity.getStep()),
        contains("지원해주신 " + jobPostingEntity.getTitle() + "의 " + jobPostingStepEntity.getStep()
            + " 과제 일정 안내드립니다.<br><br><strong>"
            + "과제 마감 일시 : " + scheduledDateTime.format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE HH:mm")) + "</strong><br><br>"
            + mailAlarmInfoEntity.getMailContent()),
        eq(true)
    );
  }

  @Test
  @DisplayName("과제 일정 이메일 발송 : 실패 - JOB_POSTING_NOT_FOUND")
  void sendTaskMailToCandidates_JobPostingNotFoundFailure() {
    MailAlarmInfoEntity mailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .jobPostingKey("jobPostingKey")
        .jobPostingStepId(1L)
        .interviewScheduleKey("interviewScheduleKey")
        .mailContent("메일 내용")
        .build();

    CandidateListEntity participant = CandidateListEntity.builder()
        .candidateKey("candidateKey")
        .build();

    when(jobPostingRepository.findByJobPostingKey(mailAlarmInfoEntity.getJobPostingKey()))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () -> {
      mailAlarmInfoService.sendTaskMailToCandidates(
          Collections.singletonList(participant), mailAlarmInfoEntity);
    });

    assertEquals("채용 공고를 찾을 수 없습니다.", thrownException.getMessage());
  }

  @Test
  @DisplayName("과제 일정 이메일 발송 : 실패 - JOB_POSTING_STEP_NOT_FOUND")
  void sendTaskMailToCandidates_JobPostingStepNotFoundFailure() {
    MailAlarmInfoEntity mailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .jobPostingKey("jobPostingKey")
        .jobPostingStepId(1L)
        .interviewScheduleKey("interviewScheduleKey")
        .mailContent("메일 내용")
        .build();

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey("jobPostingKey")
        .title("제목")
        .build();

    CandidateListEntity participant = CandidateListEntity.builder()
        .candidateKey("candidateKey")
        .build();

    when(jobPostingRepository.findByJobPostingKey(mailAlarmInfoEntity.getJobPostingKey()))
        .thenReturn(Optional.of(jobPostingEntity));
    when(jobPostingStepRepository.findById(mailAlarmInfoEntity.getJobPostingStepId()))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () -> {
      mailAlarmInfoService.sendTaskMailToCandidates(
          Collections.singletonList(participant), mailAlarmInfoEntity);
    });

    assertEquals("채용 공고의 해당 단계를 찾을 수 없습니다.", thrownException.getMessage());
  }

  @Test
  @DisplayName("과제 일정 이메일 발송 : 실패 - USER_NOT_FOUND")
  void sendTaskMailToCandidates_UserNotFoundFailure() {
    String jobPostingKey = "jobPostingKey";
    String interviewScheduleKey = "interviewScheduleKey";
    String candidateKey = "candidateKey";

    MailAlarmInfoEntity mailAlarmInfoEntity = MailAlarmInfoEntity.builder()
        .jobPostingKey(jobPostingKey)
        .jobPostingStepId(1L)
        .interviewScheduleKey(interviewScheduleKey)
        .mailContent("메일 내용")
        .build();

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .title("제목")
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .jobPostingKey(jobPostingKey)
        .step("서류 단계")
        .build();

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .interviewScheduleKey(interviewScheduleKey)
        .lastInterviewDate(LocalDate.of(2025, 5, 1))
        .build();

    CandidateListEntity participant = CandidateListEntity.builder()
        .candidateKey(candidateKey)
        .build();

    when(jobPostingRepository.findByJobPostingKey(mailAlarmInfoEntity.getJobPostingKey()))
        .thenReturn(Optional.of(jobPostingEntity));
    when(jobPostingStepRepository.findById(mailAlarmInfoEntity.getJobPostingStepId()))
        .thenReturn(Optional.of(jobPostingStepEntity));
    when(interviewScheduleRepository.findByInterviewScheduleKey(
        mailAlarmInfoEntity.getInterviewScheduleKey()))
        .thenReturn(Optional.of(interviewScheduleEntity));

    when(candidateRepository.findByCandidateKey(candidateKey))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () ->
        mailAlarmInfoService.sendTaskMailToCandidates(
            Collections.singletonList(participant), mailAlarmInfoEntity
        )
    );

    assertEquals("사용자를 찾을 수 없습니다.", thrownException.getMessage());
  }

  @Test
  @DisplayName("면접 일정 취소 이메일 발송 : 성공")
  void sendInterviewCancellationMailToParticipants_Success() {
    String jobPostingKey = "jobPostingKey";
    String candidateKey = "candidateKey";

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .jobPostingKey(jobPostingKey)
        .firstInterviewDate(LocalDate.of(2024, 7, 25))
        .build();

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .title("제목")
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .jobPostingKey(jobPostingKey)
        .step("서류 단계")
        .build();

    InterviewScheduleParticipantsEntity participant = InterviewScheduleParticipantsEntity.builder()
        .candidateKey(candidateKey)
        .interviewStartDatetime(LocalDateTime.of(2025, 5, 1, 14, 0))
        .jobPostingStepId(1L)
        .build();

    CandidateEntity candidate = CandidateEntity.builder()
        .candidateKey(candidateKey)
        .email("test@example.com")
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey))
        .thenReturn(Optional.of(jobPostingEntity));
    when(jobPostingStepRepository.findById(1L))
        .thenReturn(Optional.of(jobPostingStepEntity));
    when(candidateRepository.findByCandidateKey(candidateKey))
        .thenReturn(Optional.of(candidate));

    doNothing().when(mailComponent)
        .sendHtmlMail(anyString(), anyString(), anyString(), anyBoolean());

    mailAlarmInfoService.sendCancellationMailToParticipants(
        interviewScheduleEntity,
        Collections.singletonList(participant)
    );

    verify(mailComponent, times(1)).sendHtmlMail(
        eq("test@example.com"),
        eq("면접 일정 취소 안내 : " + jobPostingEntity.getTitle()),
        contains(
            "예정되었던 면접 일정이 <strong>취소</strong>되었음을 안내드립니다.<br><br>취소된 면접 정보<br>"
                + jobPostingEntity.getTitle() + " - " + jobPostingStepEntity.getStep()
                + "<br> 취소된 면접 일시 : "
                + participant.getInterviewStartDatetime().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE HH:mm"))),
        eq(true)
    );
  }

  @Test
  @DisplayName("과제 취소 이메일 발송 : 성공")
  void sendTaskCancellationMailToParticipants_Success() {
    String jobPostingKey = "jobPostingKey";
    String candidateKey = "candidateKey";

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .jobPostingKey(jobPostingKey)
        .firstInterviewDate(null)  // 과제일 경우 firstInterviewDate가 null
        .build();

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .title("제목")
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .jobPostingKey(jobPostingKey)
        .step("서류 단계")
        .build();

    InterviewScheduleParticipantsEntity participant = InterviewScheduleParticipantsEntity.builder()
        .candidateKey(candidateKey)
        .interviewEndDatetime(LocalDateTime.of(2025, 5, 1, 14, 0))
        .jobPostingStepId(1L)
        .build();

    CandidateEntity candidate = CandidateEntity.builder()
        .candidateKey(candidateKey)
        .email("test@example.com")
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey))
        .thenReturn(Optional.of(jobPostingEntity));
    when(jobPostingStepRepository.findById(1L))
        .thenReturn(Optional.of(jobPostingStepEntity));
    when(candidateRepository.findByCandidateKey(candidateKey))
        .thenReturn(Optional.of(candidate));

    doNothing().when(mailComponent)
        .sendHtmlMail(anyString(), anyString(), anyString(), anyBoolean());

    mailAlarmInfoService.sendCancellationMailToParticipants(
        interviewScheduleEntity,
        Collections.singletonList(participant)
    );

    verify(mailComponent, times(1)).sendHtmlMail(
        eq("test@example.com"),
        eq("과제 취소 안내 : " + jobPostingEntity.getTitle()),
        contains(
            "과제 일정이 <strong>취소</strong>되었음을 안내드립니다.<br><br>취소된 과제 정보<br>"
                + jobPostingEntity.getTitle() + " - " + jobPostingStepEntity.getStep()
                + "<br> 취소된 과제 마감 일시 : "
                + participant.getInterviewEndDatetime().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE HH:mm"))),
        eq(true)
    );
  }

  @Test
  @DisplayName("면접 일정 취소 이메일 발송 : 실패 - JOB_POSTING_NOT_FOUND")
  void sendCancellationMailToParticipants_JobPostingNotFoundFailure() {
    String jobPostingKey = "jobPostingKey";

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .jobPostingKey(jobPostingKey)
        .build();

    InterviewScheduleParticipantsEntity participant = InterviewScheduleParticipantsEntity.builder()
        .candidateKey("candidateKey")
        .jobPostingStepId(1L)
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () ->
        mailAlarmInfoService.sendCancellationMailToParticipants(
            interviewScheduleEntity,
            Collections.singletonList(participant)
        )
    );

    assertEquals("채용 공고를 찾을 수 없습니다.", thrownException.getMessage());
  }

  @Test
  @DisplayName("면접 일정 취소 이메일 발송 : 실패 - JOB_POSTING_STEP_NOT_FOUND")
  void sendCancellationMailToParticipants_JobPostingStepNotFoundFailure() {
    String jobPostingKey = "jobPostingKey";

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .jobPostingKey(jobPostingKey)
        .build();

    InterviewScheduleParticipantsEntity participant = InterviewScheduleParticipantsEntity.builder()
        .candidateKey("candidateKey")
        .jobPostingStepId(1L)
        .build();

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .title("제목")
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey))
        .thenReturn(Optional.of(jobPostingEntity));
    when(jobPostingStepRepository.findById(1L))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () ->
        mailAlarmInfoService.sendCancellationMailToParticipants(
            interviewScheduleEntity,
            Collections.singletonList(participant)
        )
    );

    assertEquals("채용 공고의 해당 단계를 찾을 수 없습니다.", thrownException.getMessage());
  }

  @Test
  @DisplayName("면접 일정 취소 이메일 발송 : 실패 - USER_NOT_FOUND")
  void sendCancellationMailToParticipants_UserNotFoundFailure() {
    String jobPostingKey = "jobPostingKey";
    String candidateKey = "candidateKey";

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .jobPostingKey(jobPostingKey)
        .build();

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .title("제목")
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .jobPostingKey(jobPostingKey)
        .step("서류 단계")
        .build();

    InterviewScheduleParticipantsEntity participant = InterviewScheduleParticipantsEntity.builder()
        .candidateKey(candidateKey)
        .jobPostingStepId(1L)
        .interviewStartDatetime(LocalDateTime.of(2025, 5, 1, 14, 0))
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey))
        .thenReturn(Optional.of(jobPostingEntity));
    when(jobPostingStepRepository.findById(1L))
        .thenReturn(Optional.of(jobPostingStepEntity));
    when(candidateRepository.findByCandidateKey(candidateKey))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () ->
        mailAlarmInfoService.sendCancellationMailToParticipants(
            interviewScheduleEntity,
            Collections.singletonList(participant)
        )
    );

    assertEquals("사용자를 찾을 수 없습니다.", thrownException.getMessage());
  }
}